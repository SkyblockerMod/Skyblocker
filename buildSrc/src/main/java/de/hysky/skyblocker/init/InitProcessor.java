package de.hysky.skyblocker.init;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hysky.skyblocker.Processor;
import de.hysky.skyblocker.utils.MethodReference;

public class InitProcessor {
	public static void apply(ClassFile context, Map<Path, ClassModel> classes) {
		long start = System.currentTimeMillis();
		Map<MethodReference, Integer> methodSignatures = new HashMap<>();

		// Find all methods with the @Init annotation
		for (Map.Entry<Path, ClassModel> entry : classes.entrySet()) {
			InitReader.readClass(entry.getValue(), methodSignatures);
		}

		// Sort the methods by their priority. Its also converted to a list because the priority values are useless from here on
		List<MethodReference> sortedMethodSignatures = methodSignatures.entrySet()
				.stream()
				.sorted(Map.Entry.<MethodReference, Integer>comparingByValue().thenComparing(entry -> entry.getKey().classDesc().packageName()))
				.map(Map.Entry::getKey)
				.toList();

		// Inject calls to the @Init annotated methods in the SkyblockerMod class
		try {
			Path mainClassFile = Objects.requireNonNull(Processor.findClass("SkyblockerMod.class"), "SkyblockerMod class wasn't found :(").toPath();
			byte[] classBytes = InitInjector.transformClass(context, context.parse(mainClassFile), sortedMethodSignatures);

			Processor.writeClass(mainClassFile, classBytes);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		System.out.println("Injecting init methods took: " + (System.currentTimeMillis() - start) + "ms");
	}
}
