package de.hysky.skyblocker.init;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public abstract class InitProcessor implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		// https://docs.gradle.org/current/userguide/task_configuration_avoidance.html
		// This only configures the `compileJava` task and not other `JavaCompile` tasks such as `compileTestJava`. https://stackoverflow.com/a/77047012
        project.getTasks().withType(JavaCompile.class).named("compileJava").get().doLast(task -> {
			long start = System.currentTimeMillis();
			File classesDir = ((JavaCompile) task).getDestinationDirectory().get().getAsFile();
			Map<MethodReference, Integer> methodSignatures = new HashMap<>();

			//Find all methods with the @Init annotation
			findInitMethods(classesDir, methodSignatures);

			//Sort the methods by their priority. It's also converted to a list because the priority values are useless from here on
			List<MethodReference> sortedMethodSignatures = methodSignatures.entrySet()
					.stream()
					.sorted(Map.Entry.<MethodReference, Integer>comparingByValue().thenComparing(entry -> entry.getKey().className()))
					.map(Map.Entry::getKey)
					.toList();

			//Inject calls to the @Init annotated methods in the SkyblockerMod class
			injectInitCalls(classesDir, sortedMethodSignatures);

			System.out.println("Injecting init methods took: " + (System.currentTimeMillis() - start) + "ms");
		});
	}

	public void findInitMethods(@NotNull File directory, Map<MethodReference, Integer> methodSignatures) {
		try {
			Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                    if (!path.toString().endsWith(".class")) return FileVisitResult.CONTINUE;
					try (InputStream inputStream = Files.newInputStream(path)) {
						ClassReader classReader = new ClassReader(inputStream);
						classReader.accept(new InitReadingClassVisitor(classReader, methodSignatures), ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
					} catch (IOException e) {
						e.printStackTrace();
					}

					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void injectInitCalls(File directory, List<MethodReference> methodSignatures) {
		Path mainClassFile = Objects.requireNonNull(findMainClass(directory), "SkyblockerMod class wasn't found :(").toPath();

		try (InputStream inputStream = Files.newInputStream(mainClassFile)) {
			ClassReader classReader = new ClassReader(inputStream);
			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
			classReader.accept(new InitInjectingClassVisitor(classWriter, methodSignatures), 0);
			try (OutputStream outputStream = Files.newOutputStream(mainClassFile)) {
				outputStream.write(classWriter.toByteArray());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Find the main SkyblockerMod class
	@Nullable
	public File findMainClass(@NotNull File directory) {
		if (!directory.isDirectory()) throw new IllegalArgumentException("Not a directory");
		for (File file : Objects.requireNonNull(directory.listFiles())) {
			if (file.isDirectory()) {
				File foundFile = findMainClass(file);

				if (foundFile != null) return foundFile;
			} else if (file.getName().equals("SkyblockerMod.class")) {
				return file;
			}
		}

		return null;
	}

	/**
	 * @param className  the class name (e.g. de/hysky/skyblocker/skyblock/ChestValue)
	 * @param methodName the method's name (e.g. init)
	 * @param descriptor the method's descriptor (only ()V for now)
	 * @param itf        whether the target class is an {@code interface} or not
	 */
	public record MethodReference(String className, String methodName, String descriptor, boolean itf) {}
}
