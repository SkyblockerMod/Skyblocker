package de.hysky.skyblocker.hud;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class HudInjectClassVisitor extends ClassVisitor {

	private final List<ClassNode> widgetClasses;

	protected HudInjectClassVisitor(ClassVisitor delegate, List<ClassNode> widgetClasses) {
		super(Opcodes.ASM9, delegate);
		this.widgetClasses = widgetClasses;
	}


	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		if ((access & Opcodes.ACC_PRIVATE) != 0 && (access & Opcodes.ACC_STATIC) != 0 && name.equals("instantiateWidgets") && descriptor.equals("()V")) {
			MethodNode methodNode = new MethodNode(Opcodes.ASM9, access, name, descriptor, signature, exceptions);

			for (ClassNode widget : widgetClasses) {
				methodNode.visitTypeInsn(Opcodes.NEW, widget.name);
				methodNode.visitInsn(Opcodes.DUP);
				methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, widget.name, "<init>", "()V", false);
				methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, "de/hysky/skyblocker/skyblock/tabhud/screenbuilder/WidgetManager", "addWidgetInstance", "(Lde/hysky/skyblocker/skyblock/tabhud/widget/HudWidget;)V", false);
			}

			// Return from the method
			methodNode.visitInsn(Opcodes.RETURN);

			// Apply our new method node to the visitor to replace the original one
			methodNode.accept(this.getDelegate());

			return null;
		}

		return super.visitMethod(access, name, descriptor, signature, exceptions);
	}
}
