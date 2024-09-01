package de.hysky.skyblocker.init;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;

import java.util.Map;

public class InitReadingClassVisitor extends ClassVisitor {
	private final Map<InitProcessor.MethodReference, Integer> methodSignatures;
	private final ClassReader classReader;

	public InitReadingClassVisitor(ClassReader classReader, Map<InitProcessor.MethodReference, Integer> methodSignatures) {
		super(Opcodes.ASM9);
		this.classReader = classReader;
		this.methodSignatures = methodSignatures;
	}

	@Override
	public MethodVisitor visitMethod(int access, String methodName, String descriptor, String signature, String[] exceptions) {
		return new MethodVisitor(Opcodes.ASM9) {
			@Override
			public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
				//This method visitor checks all methods and only acts upon those with the Init annotation.
				//This lets us warn the user about invalid init methods and misuse of the annotation
				if (!desc.equals("Lde/hysky/skyblocker/annotations/Init;")) return super.visitAnnotation(desc, visible);

				//Delegates adding the method call to the map to the InitAnnotationVisitor since we don't have a value to put in the map here
				return new InitAnnotationVisitor(methodSignatures, getMethodCall());
			}

			private @NotNull InitProcessor.MethodReference getMethodCall() {
				String className = classReader.getClassName();
				String methodCallString = className + "." + methodName;
				if ((access & Opcodes.ACC_PUBLIC) == 0) throw new IllegalStateException(methodCallString + ": Initializer methods must be public");
				if ((access & Opcodes.ACC_STATIC) == 0) throw new IllegalStateException(methodCallString + ": Initializer methods must be static");
				if (!descriptor.equals("()V")) throw new IllegalStateException(methodCallString + ": Initializer methods must have no args and a void return type");

				//Interface static methods need special handling, so we add a special marker for that
				boolean itf = (classReader.getAccess() & Opcodes.ACC_INTERFACE) != 0;

				return new InitProcessor.MethodReference(className, methodName, descriptor, itf);
			}
		};
	}

	static class InitAnnotationVisitor extends AnnotationVisitor {
		private final Map<InitProcessor.MethodReference, Integer> methodSignatures;
		private final InitProcessor.MethodReference methodCall;

		protected InitAnnotationVisitor(Map<InitProcessor.MethodReference, Integer> methodSignatures, InitProcessor.MethodReference methodCall) {
			super(Opcodes.ASM9);
			this.methodSignatures = methodSignatures;
			this.methodCall = methodCall;
		}

		@Override
		public void visitEnd() {
			//Annotations that use the default value for the priority field will not be called by the visit method, so we have to handle them here.
			methodSignatures.putIfAbsent(methodCall, 0);
			super.visitEnd();
		}

		@Override
		public void visit(String name, Object value) {
			if (name.equals("priority")) {
				methodSignatures.put(methodCall, (int) value);
			}
			super.visit(name, value);
		}
	}
}
