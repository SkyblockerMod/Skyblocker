package de.hysky.skyblocker.object;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hysky.skyblocker.Processor;

public class ObjectProcessor {

	public static void apply(ClassFile context, Map<Path, ClassModel> classes) {
		long start = System.currentTimeMillis();
		Map<Path, List<ObjectMethodGeneration>> methodsToGenerate = new HashMap<>();

		// Find all methods with the @GenEquals, @GenHashCode, or @GenToString annotations
		for (Map.Entry<Path, ClassModel> entry : classes.entrySet()) {
			ObjectMethodReader.readClass(entry.getValue(), list -> methodsToGenerate.put(entry.getKey(), list));
		}

		// Inject the bytecode for the generated equals, hashCode, and toString implementations
		for (Map.Entry<Path, List<ObjectMethodGeneration>> entry : methodsToGenerate.entrySet()) {
			try {
				byte[] classBytes = ObjectMethodInjector.transformClass(context, context.parse(entry.getKey()), entry.getValue());

				Processor.writeClass(entry.getKey(), classBytes);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		System.out.println("Injecting object methods took: " + (System.currentTimeMillis() - start) + "ms");
	}
}
