package de.hysky.skyblocker.object;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.TypeDescriptor;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import de.hysky.skyblocker.FieldReference;

public class ObjectInjectingClassVisitor extends ClassVisitor {
	/**
	 * The descriptor of the {@link java.lang.runtime.ObjectMethods#bootstrap} method.
	 */
	private static final String BOOTSTRAP_DESCRIPTOR = MethodType.methodType(
			Object.class,
			MethodHandles.Lookup.class,
			String.class,
			TypeDescriptor.class,
			Class.class,
			String.class,
			MethodHandle[].class
			).toMethodDescriptorString();
	private final List<ObjectMethodGeneration> objectMethodsToGenerate;

	public ObjectInjectingClassVisitor(ClassVisitor classVisitor, List<ObjectMethodGeneration> objectMethodsToGenerate) {
		super(Opcodes.ASM9, classVisitor);
		this.objectMethodsToGenerate = objectMethodsToGenerate;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		ObjectMethodGeneration objectMethod = this.objectMethodsToGenerate.stream()
				.filter(om -> om.target().matches(name, descriptor))
				.findAny()
				.orElse(null);

		//If we aren't meant to inject into this method then return
		if (objectMethod == null) {
			return super.visitMethod(access, name, descriptor, signature, exceptions);
		}

		//Remove native modifier
		access &= ~Opcodes.ACC_NATIVE;
		MethodNode methodNode = new MethodNode(Opcodes.ASM9, access, name, descriptor, signature, exceptions);

		//Load the "this" variable since the resulting method handle takes it in as a parameter
		methodNode.visitVarInsn(Opcodes.ALOAD, 0);

		//Load the other object parameter from equals method (2nd local variable) if applicable
		if (objectMethod.objectMethodType() == ObjectMethodType.EQUALS) {
			methodNode.visitVarInsn(Opcodes.ALOAD, 1);
		}

		//Emit the INDY instruction
		emitIndyInstruction(objectMethod, methodNode);

		//Emit instructions to consider superclass equals and hash codes when applicable
		//Note that the result from the INDY is on the stack already!
		if (objectMethod.superClassReference() != null) {
			//These methods also emit the necessary return instructions
			switch (objectMethod.objectMethodType()) {
				case ObjectMethodType.EQUALS -> emitEqualsSuperInstructions(objectMethod, methodNode);
				case ObjectMethodType.HASH_CODE -> emitHashCodeSuperInstructions(objectMethod, methodNode);
				case ObjectMethodType.TO_STRING -> throw new UnsupportedOperationException("Cannot compute toString values for superclasses!");
			}
		} else {
			//Emit return instruction if we only care about the current class
			methodNode.visitInsn(switch (objectMethod.objectMethodType()) {
				case ObjectMethodType.EQUALS, ObjectMethodType.HASH_CODE -> Opcodes.IRETURN;
				case ObjectMethodType.TO_STRING -> Opcodes.ARETURN;
			});
		}

		//Replace the target method with our new generated one
		methodNode.accept(this.getDelegate());

		return null;
	}

	private void emitIndyInstruction(ObjectMethodGeneration objectMethod, MethodNode methodNode) {
		//This is the name of our Call Site which ObjectMethods#bootstrap infers as the name of the method to generate 
		String callSiteName = switch (objectMethod.objectMethodType()) {
			case ObjectMethodType.EQUALS -> "equals";
			case ObjectMethodType.HASH_CODE -> "hashCode";
			case ObjectMethodType.TO_STRING -> "toString";
		};

		//The descriptor of the Call Site - the first parameter is the current class/instance (since the MH the INDY produces needs the instance
		//to operate on), followed by the method's parameters (optional), and then the return type.
		String callSiteDescriptor = "(" + objectMethod.classReference().descriptor() + switch (objectMethod.objectMethodType()) {
			case ObjectMethodType.EQUALS -> "Ljava/lang/Object;)Z";
			case ObjectMethodType.HASH_CODE -> ")I";
			case ObjectMethodType.TO_STRING -> ")Ljava/lang/String;";
		};
		//The handle that links to ObjectMethods#bootstrap
		Handle bootstrapHandle = new Handle(Opcodes.H_INVOKESTATIC, "java/lang/runtime/ObjectMethods", "bootstrap", BOOTSTRAP_DESCRIPTOR, false);

		//INDY bootstrap arguments

		//The type of the current class
		Type type = Type.getType(objectMethod.classReference().descriptor());
		//Concat of all the field names separated by ';' as required by ObjectMethods#bootstrap
		String fieldNamesConcat = String.join(";", objectMethod.fieldReferences().stream()
				.map(FieldReference::fieldName)
				.toArray(String[]::new));
		//Handles to each field involved in the operation
		Handle[] fieldHandles = objectMethod.fieldReferences().stream()
				.map(ref -> new Handle(Opcodes.H_GETFIELD, ref.className(), ref.fieldName(), ref.descriptor(), false))
				.toArray(Handle[]::new);

		//Compile arguments into array
		//type + name concat + field handles (must be in this order!)
		Object bsmArgs[] = new Object[2 + fieldHandles.length];
		bsmArgs[0] = type;
		bsmArgs[1] = fieldNamesConcat;
		System.arraycopy(fieldHandles, 0, bsmArgs, 2, fieldHandles.length);

		methodNode.visitInvokeDynamicInsn(callSiteName, callSiteDescriptor, bootstrapHandle, bsmArgs);
	}

	/**
	 * Effectively performs a Logical AND with the result of the generated equals and the superclass' equals method.
	 *
	 * Looks like: {@code return instanceEquals(o) ? superEquals(o) : false} in Java code.
	 */
	private void emitEqualsSuperInstructions(ObjectMethodGeneration objectMethod, MethodNode methodNode) {
		//Label representing the branch for instanceEquals(o) returning false
		//Note that IFEQ succeeds only if the preceding value on the stack (instanceEquals(o) result) was 0 (false)
		//so we will either continue onto the next instructions (if true) or jump to the false branch.
		Label notEquals = new Label();
		methodNode.visitJumpInsn(Opcodes.IFEQ, notEquals);

		methodNode.visitVarInsn(Opcodes.ALOAD, 0);
		methodNode.visitVarInsn(Opcodes.ALOAD, 1);
		methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, objectMethod.superClassReference().className(), "equals", "(Ljava/lang/Object;)Z", false);
		methodNode.visitInsn(Opcodes.IRETURN);

		//False branch
		//Return false since instanceEquals(o) was false
		methodNode.visitLabel(notEquals);
		methodNode.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		methodNode.visitInsn(Opcodes.ICONST_0); //0 = false
		methodNode.visitInsn(Opcodes.IRETURN);
	}

	/**
	 * Combines the hash code produced by the INDY and the one from the superclass.
	 *
	 * Looks like: {@code instanceHash() * 31 + superHash()} in Java code.
	 */
	private void emitHashCodeSuperInstructions(ObjectMethodGeneration objectMethod, MethodNode methodNode) {
		//Load 31 onto the stack & multiply it with the instanceHash, then get superHash and add it to the result.
		methodNode.visitIntInsn(Opcodes.BIPUSH, 31);
		methodNode.visitInsn(Opcodes.IMUL);
		methodNode.visitVarInsn(Opcodes.ALOAD, 0);
		methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, objectMethod.superClassReference().className(), "hashCode", "()I", false);
		methodNode.visitInsn(Opcodes.IADD);
		methodNode.visitInsn(Opcodes.IRETURN);
	}
}
