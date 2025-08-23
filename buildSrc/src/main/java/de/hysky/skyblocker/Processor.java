package de.hysky.skyblocker;

import de.hysky.skyblocker.hud.HudProcessor;
import de.hysky.skyblocker.init.InitProcessor;
import de.hysky.skyblocker.object.ObjectProcessor;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class Processor implements Plugin<Project> {

	public static final Logger logger = Logging.getLogger(Processor.class);
	public static File classesDir;

	@Override
	public void apply(@NotNull Project project) {
		// https://docs.gradle.org/current/userguide/task_configuration_avoidance.html
		// This only configures the `compileJava` task and not other `JavaCompile` tasks such as `compileTestJava`. https://stackoverflow.com/a/77047012
		project.getTasks().withType(JavaCompile.class).named("compileJava").get().doLast(task -> {
			JavaCompile javaCompile = (JavaCompile) task;
			classesDir = javaCompile.getDestinationDirectory().get().getAsFile();

			new InitProcessor().apply(javaCompile);
			new HudProcessor().apply(javaCompile);
			ObjectProcessor.apply();
		});
	}

	public static void forEachClass(@NotNull File directory, BiConsumer<Path, InputStream> consumer) {
		try {
			Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
					if (!path.toString().endsWith(".class")) return FileVisitResult.CONTINUE;
					try (InputStream inputStream = Files.newInputStream(path)) {
						consumer.accept(path, inputStream);
					} catch (IOException e) {
						logger.error("Failed to run consumer on class {}", path, e);
					}

					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			logger.error("Failed to walk classes", e);
		}
	}

	public static void forEachClass(BiConsumer<Path, InputStream> consumer) {
		forEachClass(classesDir, consumer);
	}

	public static void forEachClass(Consumer<InputStream> consumer) {
		forEachClass((_path, inputStream) -> consumer.accept(inputStream));
	}

	public static @Nullable File findClass(File directory, String className) {
		if (!className.endsWith(".class")) className += ".class";

		if (!directory.isDirectory()) throw new IllegalArgumentException("Not a directory");

		for (File file : Objects.requireNonNull(directory.listFiles())) {
			if (file.isDirectory()) {
				File foundFile = findClass(file, className);

				if (foundFile != null) return foundFile;
			} else if (file.getName().equals(className)) {
				return file;
			}
		}
		return null;
	}

	public static @Nullable File findClass(String className) {
		return findClass(classesDir, className);
	}

	public static void readClass(InputStream classData, Function<ClassReader, ClassVisitor> visitorFactory) {
		try {
			ClassReader classReader = new ClassReader(classData);
			classReader.accept(visitorFactory.apply(classReader), ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void writeClass(Path classFilePath, Function<ClassWriter, ClassVisitor> visitorFactory) {
		try (InputStream inputStream = Files.newInputStream(classFilePath)) {
			ClassReader classReader = new ClassReader(inputStream);
			//ASM's frame calculation reads classes reflectively which will cause a TypeNotPresentException
			//since the classes we are transforming aren't on the class path. So we need to manually calculate
			//the stack frames.
			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			classReader.accept(visitorFactory.apply(classWriter), 0);

			try (OutputStream outputStream = Files.newOutputStream(classFilePath)) {
				outputStream.write(classWriter.toByteArray());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Pretty much [child instanceof superClass]
	 * <p>
	 * Classes are full name. Example: de/hysky/skyblocker/SkyblockerMod
	 * @param child the class to test
	 * @param superClass super
	 * @return if child is an instance of superclass
	 */
	public static boolean instanceOf(String child, String superClass) {
		Path start = classesDir.toPath();
		String sup = child;
		while (sup != null) {
			if (sup.equals(superClass)) return true;
			Path resolve = start.resolve(sup + ".class");
			try (InputStream stream = Files.newInputStream(resolve)) {
				ClassReader classReader = new ClassReader(stream);
				sup = classReader.getSuperName();
			} catch (IOException e) {
				logger.error("Failed to read class {}", resolve, e);
				return false;
			}
		}
		return false;
	}
}
