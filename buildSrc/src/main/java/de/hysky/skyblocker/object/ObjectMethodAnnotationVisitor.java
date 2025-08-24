package de.hysky.skyblocker.object;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

public class ObjectMethodAnnotationVisitor extends AnnotationVisitor {
	private boolean includeSuper = false;

	public ObjectMethodAnnotationVisitor() {
		super(Opcodes.ASM9);
	}

	@Override
	public void visit(String name, Object value) {
		if (name.equals("includeSuper")) {
			this.includeSuper = (boolean) value;
		}

		super.visit(name, value);
	}

	public boolean getIncludeSuper() {
		return this.includeSuper;
	}
}
