package de.hysky.skyblocker.object;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import de.hysky.skyblocker.ClassReference;
import de.hysky.skyblocker.FieldReference;
import de.hysky.skyblocker.MethodReference;
import de.hysky.skyblocker.Pair;
import de.hysky.skyblocker.Processor;

public class ObjectProcessor {
	private static final String EQUALS = "Lde/hysky/skyblocker/annotations/GenEquals;";
	private static final String HASH_CODE = "Lde/hysky/skyblocker/annotations/GenHashCode;";
	private static final String TO_STRING = "Lde/hysky/skyblocker/annotations/GenToString;";

	public static void apply() {
		long start = System.currentTimeMillis();
		Map<Path, List<ObjectMethodGeneration>> methodsToGenerate = new HashMap<>();

		//Find all methods with the @GenEquals, @GenHashCode, or @GenToString annotations
		findObjectGenerationMethods(methodsToGenerate);

		//Ensure that the method descriptors match, but not the names since it isn't strictly necessary that they match
		validateObjectGenerationMethods(methodsToGenerate);

		//Inject the bytecode for the generated equals, hashCode, and toString implementations
		injectObjectGenerationMethods(methodsToGenerate);
		System.out.println("Injecting object methods took: " + (System.currentTimeMillis() - start) + "ms");
	}

	private static void findObjectGenerationMethods(Map<Path, List<ObjectMethodGeneration>> methodsToGenerate) {
		Processor.forEachClass((path, inputStream) -> {
			ClassNode classNode = new ClassNode(Opcodes.ASM9);
			Processor.readClass(inputStream, classReader -> classNode);

			processClass(classNode, methods -> methodsToGenerate.put(path, methods));
		});
	}

	private static void processClass(ClassNode classNode, Consumer<List<ObjectMethodGeneration>> mapAdder) {
		Map<MethodReference, Pair<ObjectMethodType, Boolean>> targetMethods = new HashMap<>();

		//Check each method of the class for the annotations
		for (MethodNode method : classNode.methods) {
			MethodReference methodReference = new MethodReference(classNode.name, method.name, method.desc, false);
			List<AnnotationNode> annotations = Stream.of(method.invisibleAnnotations, method.visibleAnnotations)
					.filter(Objects::nonNull)
					.flatMap(List::stream)
					.toList();

			for (AnnotationNode annotation : annotations) {
				ObjectMethodAnnotationVisitor annotationVisitor = new ObjectMethodAnnotationVisitor();
				annotation.accept(annotationVisitor);

				//The type of method to generate
				ObjectMethodType objectMethodType = null;

				switch (annotation.desc) {
					case EQUALS -> objectMethodType = ObjectMethodType.EQUALS;
					case HASH_CODE -> objectMethodType = ObjectMethodType.HASH_CODE;
					case TO_STRING -> objectMethodType = ObjectMethodType.TO_STRING;
				}

				if (objectMethodType != null) {
					//Ensure method is not abstract since this should not be used in interfaces
					//It can however be native if you do not want a stub method body.
					if ((method.access & Opcodes.ACC_ABSTRACT) != 0) throw new IllegalStateException("Methods that generate an Object method must not be abstract! Use the native modifier if you do not want a stub method body.");

					targetMethods.put(methodReference, new Pair<>(objectMethodType, annotationVisitor.getIncludeSuper()));
				}
			}
		}

		//Return early if there was no methods in the class that had our annotations
		if (targetMethods.isEmpty()) return;

		//Collect field references
		List<FieldReference> fieldReferences = classNode.fields.stream()
				.filter(field -> (field.access & Opcodes.ACC_STATIC) == 0 && (field.access & Opcodes.ACC_TRANSIENT) == 0)
				.map(field -> new FieldReference(classNode.name, field.name, field.desc))
				.toList();

		//Create the ObjectMethodGeneration instances
		ClassReference classReference = new ClassReference(classNode.name);
		ClassReference superClassReference = classNode.superName != null ? new ClassReference(classNode.superName) : null;
		List<ObjectMethodGeneration> methodsToGenerate = targetMethods.entrySet().stream()
				.map(entry -> new ObjectMethodGeneration(entry.getKey(), entry.getValue().left(), classReference, fieldReferences, entry.getValue().right() ? superClassReference : null))
				.toList();

		//Add the method generations to the map
		mapAdder.accept(methodsToGenerate);
	}

	private static void validateObjectGenerationMethods(Map<Path, List<ObjectMethodGeneration>> methodsToGenerate) {
		List<ObjectMethodGeneration> allGenerations = methodsToGenerate.values().stream()
				.flatMap(List::stream)
				.toList();

		for (ObjectMethodGeneration generation : allGenerations) {
			String requiredDescriptor = switch (generation.objectMethodType()) {
				case ObjectMethodType.EQUALS -> "(Ljava/lang/Object;)Z";
				case ObjectMethodType.HASH_CODE -> "()I";
				case ObjectMethodType.TO_STRING -> "()Ljava/lang/String;";
			};

			if (!generation.target().descriptor().equals(requiredDescriptor)) {
				throw new RuntimeException(String.format("Method '%s' has a mismatched descriptor! Expected: '%s'.", generation.target().toString(), requiredDescriptor));
			}
		}
	}

	private static void injectObjectGenerationMethods(Map<Path, List<ObjectMethodGeneration>> methodsToGenerate) {
		for (Map.Entry<Path, List<ObjectMethodGeneration>> entry : methodsToGenerate.entrySet()) {
			Processor.writeClass(entry.getKey(), classWriter -> new ObjectInjectingClassVisitor(classWriter, entry.getValue()));
		}
	}
}
