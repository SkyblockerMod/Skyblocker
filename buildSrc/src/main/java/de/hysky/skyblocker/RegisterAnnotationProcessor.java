package de.hysky.skyblocker;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RegisterAnnotationProcessor implements BasicProcessor {

	public enum Registry {
		SLOT_TEXT("RegisterSlotTextAdder", "de/hysky/skyblocker/skyblock/item/slottext/SlotTextManager", "getAdders"),
		TOOLTIP("RegisterTooltipAdder", "de/hysky/skyblocker/skyblock/item/tooltip/TooltipManager", "getAdders"),
		CONTAINER_SOLVER("RegisterContainerSolver", "de/hysky/skyblocker/utils/container/ContainerSolverManager", "getSolvers"),
		;

		Registry(String annotation, String targetClass, String targetMethod) {
			this.annotation = annotation;
			this.targetClass = targetClass;
			this.targetMethod = targetMethod;
		}

		public final String annotation;
		public final String targetClass;
		public final String targetMethod;

		private static final Map<String, Registry> REGISTRY_MAP = Arrays.stream(Registry.values())
				.collect(Collectors.toMap(
						registry -> "Lde/hysky/skyblocker/annotations/" + registry.annotation + ";",
						Function.identity()));

		public static @Nullable Registry of(String annotation) {
			return REGISTRY_MAP.get(annotation);
		}
	}


	private final Map<Registry, List<Target>> registryTargets = new EnumMap<>(Registry.class);

	public RegisterAnnotationProcessor() {
		for (Registry registry : Registry.values()) {
			registryTargets.put(registry, new ArrayList<>());
		}
	}

	public void writeToClasses(Function<String, ClassNode> classProvider) {
		// Inject into manager classes
		for (Registry registry : Registry.values()) {
			ClassNode classNode = classProvider.apply(registry.targetClass);

			List<Target> targets = registryTargets.get(registry);
			targets.sort(Comparator.comparingInt(t -> t.priority));

			MethodNode targetMethod = classNode.methods.stream()
					.filter(method -> method.name.equals(registry.targetMethod))
					.findFirst().orElseThrow(() -> new IllegalStateException("Could not find method " + registry.targetMethod));

			targetMethod.access = targetMethod.access & ~Opcodes.ACC_NATIVE;
			Type returnType = Type.getReturnType(targetMethod.desc);
			if (returnType.getSort() != Type.ARRAY) throw new IllegalStateException("Method " + registry.targetMethod + " in " + registry.targetClass + " does not return an array");

			Type arrayElementType = returnType.getElementType(); // the type inside the array
			String internalName = arrayElementType.getInternalName();

			// Create the method
			targetMethod.instructions.clear();
			targetMethod.visitIntInsn(Opcodes.BIPUSH, targets.size());
			targetMethod.visitTypeInsn(Opcodes.ANEWARRAY, internalName);
			for (int i = 0; i < targets.size(); i++) {
				targetMethod.visitInsn(Opcodes.DUP);
				targetMethod.visitIntInsn(Opcodes.BIPUSH, i);
				Target target = targets.get(i);
				target.map(targetClassName -> {
					// Create new instance of class
					targetMethod.visitTypeInsn(Opcodes.NEW, targetClassName);
					targetMethod.visitInsn(Opcodes.DUP);
					targetMethod.visitMethodInsn(Opcodes.INVOKESPECIAL, targetClassName, "<init>", "()V", false);
				}, (field, ownerClassName) ->
						// get static field
						targetMethod.visitFieldInsn(Opcodes.GETSTATIC, ownerClassName, field.name, field.desc));
				targetMethod.visitInsn(Opcodes.AASTORE);
			}
			targetMethod.visitInsn(Opcodes.ARETURN);


		}
	}

	public void parseClass(ClassNode classNode) {
		BasicProcessor.getAnnotations(classNode)
				.stream()
				.map(RegisterAnnotationProcessor::getAnnotation)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.forEach(annotation -> {
					boolean hasParameterlessConstructor = classNode.methods.stream()
							.anyMatch(method -> "<init>".equals(method.name) && "()V".equals(method.desc) && (method.access & Opcodes.ACC_PUBLIC) != 0);

					if (!hasParameterlessConstructor) {
						throw new IllegalStateException("Class " + classNode.name + " has " + annotation.registry.annotation + " but has no public parameterless constructor");
					}
					registryTargets.get(annotation.registry).add(new Target(classNode.name, annotation.priority));
				});

		for (FieldNode field : classNode.fields) {
			BasicProcessor.getAnnotations(field)
					.stream()
					.map(RegisterAnnotationProcessor::getAnnotation)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.forEach(annotation -> registryTargets.get(annotation.registry).add(new Target(classNode.name, field, annotation.priority)));
		}
	}

	private static Optional<Annotation> getAnnotation(AnnotationNode annotationNode) {
		Registry registry = Registry.of(annotationNode.desc);
		if (registry == null) return Optional.empty();
		return Optional.of(new Annotation(registry, BasicProcessor.getIntOrDefault(annotationNode, "priority", 0)));
	}

	private record Annotation(Registry registry, int priority) {}

	private record Target(String className, Optional<FieldNode> targetField, int priority) {
		private Target(String className, int priority) {
			this(className, Optional.empty(), priority);
		}

		private Target(String className, FieldNode targetField, int priority) {
			this(className, Optional.of(targetField), priority);
		}

		private void map(Consumer<String> classNode, BiConsumer<FieldNode, String> fieldNodeAndOwner) {
			targetField.ifPresentOrElse(fieldNode -> fieldNodeAndOwner.accept(fieldNode, className), () -> classNode.accept(className));
		}
	}
}
