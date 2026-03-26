package de.hysky.skyblocker.object;

import java.lang.classfile.AccessFlags;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeModel;
import java.lang.classfile.MethodModel;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.hysky.skyblocker.Processor;
import de.hysky.skyblocker.utils.FieldReference;

public class ObjectMethodInjector {

	public static byte[] transformClass(ClassFile context, ClassModel classModel, List<ObjectMethodGeneration> objectMethodsToGenerate) {
		Predicate<MethodModel> methodsToTransform = methodModel -> objectMethodsToGenerate.stream()
				.map(ObjectMethodGeneration::target)
				.anyMatch(methodReference -> methodReference.matches(methodModel));

		byte[] newBytes = context.transformClass(classModel, (classBuilder, classElement) -> {
			if (classElement instanceof MethodModel methodModel && methodsToTransform.test(methodModel)) {
				// Find what method we are meant to generate
				ObjectMethodGeneration objectMethodToGenerate = objectMethodsToGenerate.stream()
						.filter(generation -> generation.target().matches(methodModel))
						.findFirst()
						.orElseThrow(() -> new IllegalStateException("Could not find generation for: " + methodModel.toDebugString()));

				// Transform the method
				classBuilder.transformMethod(methodModel, (methodBuilder, methodElement) -> {
					switch (methodElement) {
						case AccessFlags accessFlags -> {
							// Remove the native flag if present
							if (accessFlags.has(AccessFlag.NATIVE)) {
								// Copy the access flags set
								Set<AccessFlag> newAccessFlags = new HashSet<>(accessFlags.flags());
								newAccessFlags.remove(AccessFlag.NATIVE);
								methodBuilder.withFlags(newAccessFlags.toArray(AccessFlag[]::new));

								// If the method was declared native, it won't have a code attribute so we now need to add one
								// since our block below that looks for a CodeModel will not run
								methodBuilder.withCode(codeBuilder -> {
									loadLocalVariables(objectMethodToGenerate, codeBuilder);
									buildInvokeDynamic(objectMethodToGenerate, codeBuilder);
									addSuperClassComparisons(objectMethodToGenerate, codeBuilder);
								});
							}
						}

						// This will only run if the object method was not declared as native
						case CodeModel _ -> {
							methodBuilder.withCode(codeBuilder -> {
								loadLocalVariables(objectMethodToGenerate, codeBuilder);
								buildInvokeDynamic(objectMethodToGenerate, codeBuilder);
								addSuperClassComparisons(objectMethodToGenerate, codeBuilder);
							});
						}

						default -> {
							methodBuilder.with(methodElement);
						}
					}
				});
			} else {
				classBuilder.with(classElement);
			}
		});
		Processor.verifyClass(context, classModel.thisClass().asSymbol(), newBytes);

		return newBytes;
	}

	private static void loadLocalVariables(ObjectMethodGeneration objectMethodToGenerate, CodeBuilder codeBuilder) {
		// Load the "this" variable since the resulting method handle takes it in as a parameter
		codeBuilder.aload(0);

		// Load the other object parameter from equals method (2nd local variable)
		if (objectMethodToGenerate.objectMethodType() == ObjectMethodType.EQUALS) {
			codeBuilder.aload(1);
		}
	}

	private static void buildInvokeDynamic(ObjectMethodGeneration objectMethodToGenerate, CodeBuilder codeBuilder) {
		List<ConstantDesc> bootstrapArgsBuilder = new ArrayList<>();
		// Add the target class
		bootstrapArgsBuilder.add(objectMethodToGenerate.classDesc());
		// Add all the field names - they must be separated by a semicolon
		bootstrapArgsBuilder.add(objectMethodToGenerate.fieldReferences().stream()
				.map(FieldReference::fieldName)
				.collect(Collectors.joining(";")));
		// Add the getter handles to all the fields
		bootstrapArgsBuilder.addAll(objectMethodToGenerate.fieldReferences().stream()
				.map(fieldReference -> MethodHandleDesc.of(
						DirectMethodHandleDesc.Kind.GETTER,
						objectMethodToGenerate.classDesc(),
						fieldReference.fieldName(),
						fieldReference.typeDesc().descriptorString()))
				.toList());

		DirectMethodHandleDesc bootstrapMethod = MethodHandleDesc.of(
				DirectMethodHandleDesc.Kind.STATIC,
				ClassDesc.of("java.lang.runtime.ObjectMethods"),
				"bootstrap",
				MethodTypeDesc.of(
						ConstantDescs.CD_Object,
						ConstantDescs.CD_MethodHandles_Lookup,
						ConstantDescs.CD_String,
						ClassDesc.of("java.lang.invoke.TypeDescriptor"),
						ConstantDescs.CD_Class,
						ConstantDescs.CD_String,
						ConstantDescs.CD_MethodHandle.arrayType()).descriptorString());
		// We must also pass the current class (the one we are injecting into) as the first parameter of the invocation type since that is
		// the instance that the resulting MethodHandle will need to consume, then followed by the respective parameters of the object method we are generating
		MethodTypeDesc invocationType = switch (objectMethodToGenerate.objectMethodType()) {
			case ObjectMethodType.EQUALS -> MethodTypeDesc.of(ConstantDescs.CD_boolean, objectMethodToGenerate.classDesc(), ConstantDescs.CD_Object);
			case ObjectMethodType.HASH_CODE -> MethodTypeDesc.of(ConstantDescs.CD_int, objectMethodToGenerate.classDesc());
			case ObjectMethodType.TO_STRING -> MethodTypeDesc.of(ConstantDescs.CD_String, objectMethodToGenerate.classDesc());
		};
		ConstantDesc[] bootstrapArgs = bootstrapArgsBuilder.toArray(ConstantDesc[]::new);
		DynamicCallSiteDesc callSite = DynamicCallSiteDesc.of(bootstrapMethod, objectMethodToGenerate.objectMethodType().methodName, invocationType, bootstrapArgs);

		codeBuilder.invokedynamic(callSite);
	}

	private static void addSuperClassComparisons(ObjectMethodGeneration objectMethodToGenerate, CodeBuilder codeBuilder) {
		if (objectMethodToGenerate.superClassDesc().isPresent()) {
			switch (objectMethodToGenerate.objectMethodType()) {
				case ObjectMethodType.EQUALS -> {
					// The preceding boolean value on the stack is the result of the equals method on this instance:
					// If the current instance is equal to the other instance then do the super class equals and return that
					// Or, if the current instance and the other instance are not equal then simply return false
					codeBuilder.ifThenElse(trueCodeBuilder -> {
						trueCodeBuilder.aload(0);
						trueCodeBuilder.aload(1);
						trueCodeBuilder.invokespecial(objectMethodToGenerate.superClassDesc().get(), "equals", MethodTypeDesc.of(ConstantDescs.CD_boolean, ConstantDescs.CD_Object), false);
						trueCodeBuilder.ireturn();
					}, falseCodeBuilder -> {
						falseCodeBuilder.iconst_0();
						falseCodeBuilder.ireturn();
					});
				}

				case ObjectMethodType.HASH_CODE -> {
					// Load 31 onto the stack and multiply it with the instance's, then get the super class' hash and add it to the result.
					codeBuilder.bipush(31);
					codeBuilder.imul();
					codeBuilder.aload(0);
					codeBuilder.invokespecial(objectMethodToGenerate.superClassDesc().get(), "hashCode", MethodTypeDesc.of(ConstantDescs.CD_int), false);
					codeBuilder.iadd();
					codeBuilder.ireturn();
				}

				case ObjectMethodType.TO_STRING -> throw new UnsupportedOperationException("Cannot compute toString values for superclasses!");
			}
		} else {
			switch (objectMethodToGenerate.objectMethodType()) {
				case ObjectMethodType.EQUALS, ObjectMethodType.HASH_CODE -> codeBuilder.ireturn();
				case ObjectMethodType.TO_STRING -> codeBuilder.areturn();
			}
		}
	}
}
