package de.hysky.skyblocker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassFile.ClassHierarchyResolverOption;
import java.lang.classfile.ClassHierarchyResolver;
import java.lang.classfile.ClassModel;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.reflect.AccessFlag;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jspecify.annotations.Nullable;

import de.hysky.skyblocker.hud.HudProcessor;
import de.hysky.skyblocker.init.InitProcessor;
import de.hysky.skyblocker.object.ObjectProcessor;

public class Processor implements Plugin<Project> {
	public static final Logger LOGGER = Logging.getLogger(Processor.class);
	public static @Nullable File classesDir;
	public static @Nullable URLClassLoader compileClasspathLoader;

	@Override
	public void apply(Project project) {
		// https://docs.gradle.org/current/userguide/task_configuration_avoidance.html
		// This only configures the `compileJava` task and not other `JavaCompile` tasks such as `compileTestJava`. https://stackoverflow.com/a/77047012
		project.getTasks().withType(JavaCompile.class).named("compileJava").get().doLast(task -> {
			JavaCompile javaCompile = (JavaCompile) task;
			classesDir = javaCompile.getDestinationDirectory().get().getAsFile();

			// Used for quickly extracting information from the mod's classes (so we don't reparse them a bunch of times)
			// NB: These class models MUST NOT be used when injecting into the class otherwise transformations will not stack!!!
			Map<Path, ClassModel> skyblockerClasses = readSkyblockerClasses();

			// The ClassFile API needs the complete hierarchy of classes in use to construct correct bytecode, so we
			// create the appropriate resolvers that provide such information.
			ClassHierarchyResolver skyblockerClassHierarchyResolver = skyblockerClassHierarchyResolver(skyblockerClasses);
			ClassHierarchyResolver compileClassPathHierarchyResolver = compileClasspathHierarchyResolver(javaCompile);
			ClassHierarchyResolver completeClassHierarchyResolver = skyblockerClassHierarchyResolver
					.orElse(compileClassPathHierarchyResolver)
					.orElse(ClassHierarchyResolver.defaultResolver());
			ClassFile context = ClassFile.of(ClassHierarchyResolverOption.of(completeClassHierarchyResolver));

			// Apply processors
			InitProcessor.apply(context, skyblockerClasses);
			ObjectProcessor.apply(context, skyblockerClasses);
			HudProcessor.apply(context, skyblockerClasses);

			// Close URL class loader
			if (compileClasspathLoader != null) {
				try {
					compileClasspathLoader.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	private static Map<Path, ClassModel> readSkyblockerClasses() {
		Map<Path, ClassModel> skyblockerClasses = new HashMap<>();
		ClassFile classFile = ClassFile.of();

		forEachClass((path, _) -> {
			try {
				ClassModel model = classFile.parse(path);

				skyblockerClasses.put(path, model);
			} catch (Exception e) {
				LOGGER.error("Failed to parse class {}", path, e);
			}
		});

		return skyblockerClasses;
	}

	/// Builds a {@code ClassHierarchyResolver} for Skyblocker's classes.
	private static ClassHierarchyResolver skyblockerClassHierarchyResolver(Map<Path, ClassModel> classes) {
		List<ClassDesc> interfaces = new ArrayList<>();
		Map<ClassDesc, ClassDesc> classToSuperClass = new HashMap<>();

		for (ClassModel model : classes.values()) {
			ClassDesc desc = model.thisClass().asSymbol();

			if (model.flags().has(AccessFlag.INTERFACE)) {
				interfaces.add(desc);
			}

			if (!model.isModuleInfo() && !desc.equals(ConstantDescs.CD_Object)) {
				classToSuperClass.put(desc, model.superclass().get().asSymbol());
			}
		}

		return ClassHierarchyResolver.of(interfaces, classToSuperClass);
	}

	/// Builds a {@code ClassHierarchyResolver} for all classes on the compile time classpath.
	private static ClassHierarchyResolver compileClasspathHierarchyResolver(JavaCompile javaCompile) {
		// Creates a URLClassLoader that will provide the compile classes to the ClassFile API on-demand.
		URL[] urls = javaCompile.getClasspath().getFiles().stream()
				.map(File::toURI)
				.map(uri -> {
					try {
						return uri.toURL();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
				.toArray(URL[]::new);
		compileClasspathLoader = new URLClassLoader(urls, null);

		// We only want the classes to be parsed, not completely loaded so we want the resource parsing resolver.
		return ClassHierarchyResolver.ofResourceParsing(compileClasspathLoader);
	}

	public static void forEachClass(File directory, BiConsumer<Path, InputStream> consumer) {
		try {
			Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
					if (!path.toString().endsWith(".class")) return FileVisitResult.CONTINUE;

					try (InputStream inputStream = Files.newInputStream(path)) {
						consumer.accept(path, inputStream);
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

	public static void forEachClass(BiConsumer<Path, InputStream> consumer) {
		forEachClass(classesDir, consumer);
	}

	public static void forEachClass(Consumer<InputStream> consumer) {
		forEachClass((_, inputStream) -> consumer.accept(inputStream));
	}

	private static @Nullable File findClass(File directory, String className) {
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

	public static void writeClass(Path classFilePath, byte[] classBytes) {
		try (OutputStream outputStream = Files.newOutputStream(classFilePath)) {
			outputStream.write(classBytes);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/// Pretty much [child instanceof superClass]
	///
	/// @param child the class to test
	/// @param superClass the super class
	///
	/// @return whether the child is an instance of {@code superClass}
	public static boolean instanceOf(ClassDesc child, ClassDesc superClass) {
		ClassFile context = ClassFile.of();
		Path start = classesDir.toPath();
		ClassDesc sup = child;

		while (sup != null) {
			if (sup.equals(superClass)) return true;
			Path resolve = start.resolve(sup.descriptorString().replace(";", "").replaceFirst("L", "") + ".class");

			try {
				ClassModel classModel = context.parse(resolve);
				sup = classModel.superclass().get().asSymbol();
			} catch (Exception e) {
				LOGGER.error("Failed to read class {}", resolve, e);
				return false;
			}
		}

		return false;
	}

	/// Prints out any {@code VerifyErrors} that arise from class transformations.
	///
	/// All class transformations should call this at the end to ensure everything is correct.
	public static void verifyClass(ClassFile context, ClassDesc desc, byte[] newClassBytes) {
		List<VerifyError> verifyErrors = context.verify(newClassBytes);

		for (VerifyError error : verifyErrors) {
			System.out.println("Verify error for '%s': %s".formatted(desc.packageName(), error));
		}
	}
}
