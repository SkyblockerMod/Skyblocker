package de.hysky.skyblocker;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.function.Function;

public class InitProcessor implements BasicProcessor {

	private final List<InitMethod> initMethods = new ArrayList<>();

	@Override
	public void parseClass(ClassNode clazz) {
		clazz.methods.stream()
				.map(method ->
						BasicProcessor.getAnnotations(method)
								.stream()
								.filter(annotationNode -> "Lde/hysky/skyblocker/annotations/Init;".equals(annotationNode.desc))
								.limit(1) // Shouldn't possibly get more but you never know
								.map(annotation -> {
									if ((method.access & Opcodes.ACC_PUBLIC) == 0) throw new IllegalStateException(method.name + ": Initializer methods must be public");
									if ((method.access & Opcodes.ACC_STATIC) == 0) throw new IllegalStateException(method.name + ": Initializer methods must be static");
									if (!("()V".equals(method.desc))) throw new IllegalStateException(method.name + ": Initializer methods must have no args and return void");
									return new InitMethod(new MethodReference(clazz, method), BasicProcessor.getIntOrDefault(annotation, "priority", 0));
								})
								.findFirst())
				.filter(Optional::isPresent).map(Optional::get).forEach(initMethods::add);
	}

	@Override
	public void writeToClasses(Function<String, ClassNode> classProvider) {
		ClassNode classNode = classProvider.apply("de/hysky/skyblocker/SkyblockerMod");
		MethodNode initMethod = classNode.methods.stream()
				.filter(methodNode -> (methodNode.access & Opcodes.ACC_PRIVATE) != 0
						&& (methodNode.access & Opcodes.ACC_STATIC) != 0
						&& "init".equals(methodNode.name)
						&& "()V".equals(methodNode.desc))
				.findFirst().orElseThrow(() -> new IllegalStateException("Couldn't find init method in SkyblockerMod"));

		initMethods.sort(Comparator.comparingInt(InitMethod::priority));

		initMethod.access = initMethod.access & ~Opcodes.ACC_NATIVE;
		initMethod.instructions.clear();
		for (InitMethod method : initMethods) {
			MethodReference methodReference = method.method();
			initMethod.visitMethodInsn(Opcodes.INVOKESTATIC, methodReference.className(), methodReference.methodName(), methodReference.descriptor(), methodReference.itf());
		}
		initMethod.visitInsn(Opcodes.RETURN);
	}

	record InitMethod(MethodReference method, int priority) {}
}
