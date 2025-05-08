package de.hysky.skyblocker;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Consumer;

public class SkyblockerPlugin implements Plugin<Project> {

	public static final Logger LOGGER = Logging.getLogger(SkyblockerPlugin.class);
	public static File classesDir;

	private final BasicProcessor[] basicProcessors = new BasicProcessor[]{
			new RegisterAnnotationProcessor(),
			new InitProcessor(),
			new HudProcessor()
	};

	private final Map<String, ClassNode> classes = new HashMap<>();

	@Override
	public void apply(@NotNull Project project) {
		// https://docs.gradle.org/current/userguide/task_configuration_avoidance.html
		// This only configures the `compileJava` task and not other `JavaCompile` tasks such as `compileTestJava`. https://stackoverflow.com/a/77047012
		project.getTasks().withType(JavaCompile.class).named("compileJava").get().doLast(task -> {
			JavaCompile javaCompile = (JavaCompile) task;
			classesDir = javaCompile.getDestinationDirectory().get().getAsFile();


			long millis = System.currentTimeMillis();
			forEachClass(this::runParseClassOnProcessors);
			LOGGER.lifecycle("Parsed classes in {} ms", System.currentTimeMillis() - millis);

			millis = System.currentTimeMillis();
			for (BasicProcessor basicProcessor : basicProcessors) {
				basicProcessor.writeToClasses(this::getClassNode);
			}
			LOGGER.lifecycle("Edited classes in {} ms", System.currentTimeMillis() - millis);
			millis = System.currentTimeMillis();
			for (Map.Entry<String, ClassNode> entry : classes.entrySet()) {
				try (OutputStream outputStream = Files.newOutputStream(getClassPath(entry.getKey()))) {
					ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
					entry.getValue().accept(writer);
					outputStream.write(writer.toByteArray());
				} catch (IOException e) {
					throw new RuntimeException("Failed to write class file for " + entry.getKey(), e);
				}
			}
			LOGGER.lifecycle("Wrote edited classes in {} ms", System.currentTimeMillis() - millis);
		});
	}

	private void runParseClassOnProcessors(InputStream stream) {
		try {
			ClassReader reader = new ClassReader(stream);
			ClassNode classNode = new ClassNode(Opcodes.ASM9);
			reader.accept(classNode, 0);
			for (BasicProcessor basicProcessor : basicProcessors) {
				basicProcessor.parseClass(classNode);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private ClassNode getClassNode(String className) {
		ClassNode node = classes.get(className);
		if (node != null) return node;
		try (InputStream stream = Files.newInputStream(getClassPath(className))) {
			ClassReader reader = new ClassReader(stream);
			ClassNode classNode = new ClassNode(Opcodes.ASM9);
			reader.accept(classNode, 0);
			classes.put(className, classNode);
			return classNode;
		} catch (IOException e) {
			throw new RuntimeException("Couldn't find class: " + className, e);
		}
	}

	public static void forEachClass(@NotNull File directory, final Consumer<InputStream> consumer) {
		try {
			Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<>() {
				@Override
				public @NotNull FileVisitResult visitFile(@NotNull Path path, @NotNull BasicFileAttributes attrs) {
					if (!path.toString().endsWith(".class")) return FileVisitResult.CONTINUE;
					try (InputStream inputStream = Files.newInputStream(path)) {
						consumer.accept(inputStream);
					} catch (IOException e) {
						LOGGER.error("Failed to run consumer on class {}", path, e);

					}

					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			LOGGER.error("Failed to walk classes", e);
		}
	}

	public static void forEachClass(final Consumer<InputStream> consumer) {
		forEachClass(classesDir, consumer);
	}

	/**
	 * Returns a path to the class so you can open it
	 * @param fullClassName the full class name and path, with / instead of .
	 * @return the file
	 * @see org.objectweb.asm.tree.ClassNode#name
	 */
	public static Path getClassPath(String fullClassName) {
		if (!fullClassName.endsWith(".class")) fullClassName += ".class";
		return classesDir.toPath().resolve(fullClassName);
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
				LOGGER.error("Failed to read class {}", resolve, e);
				return false;
			}
		}
		return false;
	}
}
