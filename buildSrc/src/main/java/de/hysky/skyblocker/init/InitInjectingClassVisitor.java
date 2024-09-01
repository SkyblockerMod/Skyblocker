package de.hysky.skyblocker.init;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class InitInjectingClassVisitor extends ClassVisitor {
	private final List<InitProcessor.MethodReference> methodSignatures;

	public InitInjectingClassVisitor(ClassVisitor classVisitor, List<InitProcessor.MethodReference> methodSignatures) {
		super(Opcodes.ASM9, classVisitor);
		this.methodSignatures = methodSignatures;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);

		// Limit replacing to the init method which is private, static, named init, has no args, and has a void return type
		if ((access & Opcodes.ACC_PRIVATE) != 0 && (access & Opcodes.ACC_STATIC) != 0 && name.equals("init") && descriptor.equals("()V")) {
			// Method node that we will overwrite the init method with
			MethodNode methodNode = new MethodNode(Opcodes.ASM9, access, name, descriptor, signature, exceptions);

			// Inject calls to each found @Init annotated method
			for (InitProcessor.MethodReference methodCall : methodSignatures) {
				methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, methodCall.className(), methodCall.methodName(), methodCall.descriptor(), methodCall.itf());
			}

			// Return from the method
			methodNode.visitInsn(Opcodes.RETURN);

			// Apply our new method node to the visitor to replace the original one
			methodNode.accept(methodVisitor);
		}

		return methodVisitor;
	}
}
