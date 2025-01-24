package de.hysky.skyblocker.init;

import de.hysky.skyblocker.Processor;
import org.gradle.api.tasks.compile.JavaCompile;
import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public class InitProcessor {
	public void apply(JavaCompile task) {
		long start = System.currentTimeMillis();
		Map<MethodReference, Integer> methodSignatures = new HashMap<>();

		//Find all methods with the @Init annotation
		findInitMethods(methodSignatures);

		//Sort the methods by their priority. It's also converted to a list because the priority values are useless from here on
		List<MethodReference> sortedMethodSignatures = methodSignatures.entrySet()
				.stream()
				.sorted(Map.Entry.<MethodReference, Integer>comparingByValue().thenComparing(entry -> entry.getKey().className()))
				.map(Map.Entry::getKey)
				.toList();

		//Inject calls to the @Init annotated methods in the SkyblockerMod class
		injectInitCalls(sortedMethodSignatures);

		System.out.println("Injecting init methods took: " + (System.currentTimeMillis() - start) + "ms");
	}

	public void findInitMethods(Map<MethodReference, Integer> methodSignatures) {
		Processor.forEachClass(inputStream -> {
			try {
				ClassReader classReader = new ClassReader(inputStream);
				classReader.accept(new InitReadingClassVisitor(classReader, methodSignatures), ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public void injectInitCalls(List<MethodReference> methodSignatures) {
		Path mainClassFile = Objects.requireNonNull(Processor.findClass("SkyblockerMod.class"), "SkyblockerMod class wasn't found :(").toPath();

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

	/**
	 * @param className  the class name (e.g. de/hysky/skyblocker/skyblock/ChestValue)
	 * @param methodName the method's name (e.g. init)
	 * @param descriptor the method's descriptor (only ()V for now)
	 * @param itf        whether the target class is an {@code interface} or not
	 */
	public record MethodReference(String className, String methodName, String descriptor, boolean itf) {}
}
