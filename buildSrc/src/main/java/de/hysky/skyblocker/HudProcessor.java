package de.hysky.skyblocker;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.function.Function;

public class HudProcessor implements BasicProcessor {

	private final Map<ClassNode, Integer> annotatedClassesConstructors = new HashMap<>();

	public void parseClass(ClassNode classNode) {

		// Look for the annotation
		Optional<Integer> priorityOptional = BasicProcessor.getAnnotations(classNode)
				.stream()
				.filter(annotation -> "Lde/hysky/skyblocker/annotations/RegisterWidget;".equals(annotation.desc))
				.findFirst()
				.map(annotation -> BasicProcessor.getIntOrDefault(annotation, "priority", 0));

		if (priorityOptional.isEmpty()) return;
		if (!SkyblockerPlugin.instanceOf(classNode.name, "de/hysky/skyblocker/skyblock/tabhud/widget/HudWidget")) {
			throw new IllegalArgumentException("Class " + classNode.name + " has @RegisterWidget annotation but does not extend HudWidget");
		}

		boolean hasConstructor = classNode.methods.stream()
				.noneMatch(method -> "<init>".equals(method.name) && "()V".equals(method.desc) && (method.access & Opcodes.ACC_PUBLIC) != 0);
		if (hasConstructor) throw new IllegalStateException("No public parameterless constructor found for " + classNode.name);

		annotatedClassesConstructors.put(classNode, priorityOptional.get());
	}

	@Override
	public void writeToClasses(Function<String, ClassNode> classProvider) {
		ClassNode classNode = classProvider.apply("de/hysky/skyblocker/skyblock/tabhud/screenbuilder/WidgetManager");
		Optional<MethodNode> instantiateMethodOptional = classNode.methods.stream()
				.filter(methodNode -> (methodNode.access & Opcodes.ACC_PRIVATE) != 0
						&& (methodNode.access & Opcodes.ACC_STATIC) != 0
						&& "instantiateWidgets".equals(methodNode.name)
						&& "()V".equals(methodNode.desc))
				.findFirst();
		if (instantiateMethodOptional.isEmpty()) throw new IllegalStateException("Couldn't find instantiateWidgets method in WidgetManager");
		MethodNode instantiateMethod = instantiateMethodOptional.get();
		instantiateMethod.access = instantiateMethod.access & ~Opcodes.ACC_NATIVE;
		instantiateMethod.instructions.clear();

		List<ClassNode> constructors = new ArrayList<>(annotatedClassesConstructors.keySet());
		constructors.sort(Comparator.comparingInt(annotatedClassesConstructors::get));
		for (ClassNode constructor : constructors) {
			instantiateMethod.visitTypeInsn(Opcodes.NEW, constructor.name);
			instantiateMethod.visitInsn(Opcodes.DUP);
			instantiateMethod.visitMethodInsn(Opcodes.INVOKESPECIAL, constructor.name, "<init>", "()V", false);
			instantiateMethod.visitMethodInsn(Opcodes.INVOKESTATIC, "de/hysky/skyblocker/skyblock/tabhud/screenbuilder/WidgetManager", "addWidgetInstance", "(Lde/hysky/skyblocker/skyblock/tabhud/widget/HudWidget;)V", false);
		}
		instantiateMethod.visitInsn(Opcodes.RETURN);
	}
}
