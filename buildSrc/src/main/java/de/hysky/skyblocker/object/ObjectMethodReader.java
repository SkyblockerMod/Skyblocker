package de.hysky.skyblocker.object;

import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.AnnotationValue;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.attribute.RuntimeInvisibleAnnotationsAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import de.hysky.skyblocker.utils.FieldReference;
import de.hysky.skyblocker.utils.MethodReference;
import de.hysky.skyblocker.utils.Pair;

public class ObjectMethodReader {
	private static final ClassDesc EQUALS_ANNOTATION_DESC = ClassDesc.of("de.hysky.skyblocker.annotations.GenEquals");
	private static final ClassDesc HASH_CODE_ANNOTATION_DESC = ClassDesc.of("de.hysky.skyblocker.annotations.GenHashCode");
	private static final ClassDesc TO_STRING_ANNOTATION_DESC = ClassDesc.of("de.hysky.skyblocker.annotations.GenToString");
	private static final MethodTypeDesc EQUALS_METHOD_DESC = MethodTypeDesc.of(ConstantDescs.CD_boolean, ConstantDescs.CD_Object);
	private static final MethodTypeDesc HASH_CODE_METHOD_DESC = MethodTypeDesc.of(ConstantDescs.CD_int);
	private static final MethodTypeDesc TO_STRING_METHOD_DESC = MethodTypeDesc.of(ConstantDescs.CD_String);

	public static void readClass(ClassModel classModel, Consumer<List<ObjectMethodGeneration>> mapAdder) {
		Map<MethodReference, Pair<ObjectMethodType, Boolean>> targetMethods = new HashMap<>();

		for (MethodModel methodModel : classModel.methods()) {
			Optional<RuntimeInvisibleAnnotationsAttribute> runtimeInvisibleAnnotationsOpt = methodModel.findAttribute(Attributes.runtimeInvisibleAnnotations());

			if (runtimeInvisibleAnnotationsOpt.isPresent()) {
				RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotations = runtimeInvisibleAnnotationsOpt.get();

				for (Annotation annotation : runtimeInvisibleAnnotations.annotations()) {
					ClassDesc annotationDesc = annotation.classSymbol();

					ObjectMethodType objectMethodType = switch (annotationDesc) {
						case ClassDesc _ when annotationDesc.equals(EQUALS_ANNOTATION_DESC) -> ObjectMethodType.EQUALS;
						case ClassDesc _ when annotationDesc.equals(HASH_CODE_ANNOTATION_DESC) -> ObjectMethodType.HASH_CODE;
						case ClassDesc _ when annotationDesc.equals(TO_STRING_ANNOTATION_DESC) -> ObjectMethodType.TO_STRING;
						default -> null;
					};

					if (objectMethodType != null) {
						checkMethodDesc(methodModel, objectMethodType);

						if (methodModel.flags().has(AccessFlag.ABSTRACT)) {
							throw new IllegalStateException("Methods that generate an Object method must not be abstract! Use the native modifier if you do not want a stub method body.");
						}

						AnnotationValue includeSuperValue = annotation.elements().stream()
								.filter(annotationElement -> annotationElement.name().equalsString("includeSuper"))
								.map(AnnotationElement::value)
								.findAny()
								.orElse(null);
						boolean includeSuper = includeSuperValue instanceof AnnotationValue.OfBoolean booleanValue && booleanValue.booleanValue();

						targetMethods.put(MethodReference.fromModel(methodModel), Pair.of(objectMethodType, includeSuper));
					}
				}
			}
		}

		if (targetMethods.isEmpty()) {
			return;
		}

		// Collect field references
		List<FieldReference> fieldReferences = classModel.fields().stream()
				.filter(fieldModel -> !fieldModel.flags().has(AccessFlag.STATIC) && !fieldModel.flags().has(AccessFlag.TRANSIENT))
				.map(FieldReference::fromModel)
				.toList();
		ClassDesc classDesc = classModel.thisClass().asSymbol();
		ClassDesc superClassDesc = classModel.superclass().map(ClassEntry::asSymbol).orElse(null);
		List<ObjectMethodGeneration> methodsToGenerate = targetMethods.entrySet().stream()
				.map(entry -> new ObjectMethodGeneration(classDesc, entry.getKey(), entry.getValue().left(), fieldReferences, entry.getValue().right() ? Optional.of(superClassDesc) : Optional.empty()))
				.toList();

		mapAdder.accept(methodsToGenerate);
	}

	/// Requires that methods have the correct signature for the object method they wish to generate.
	private static void checkMethodDesc(MethodModel methodModel, ObjectMethodType objectMethodType) {
		MethodTypeDesc requiredDescriptor = switch (objectMethodType) {
			case ObjectMethodType.EQUALS -> EQUALS_METHOD_DESC;
			case ObjectMethodType.HASH_CODE -> HASH_CODE_METHOD_DESC;
			case ObjectMethodType.TO_STRING -> TO_STRING_METHOD_DESC;
		};

		if (!methodModel.methodTypeSymbol().equals(requiredDescriptor)) {
			throw new RuntimeException(String.format("Method '%s' has a mismatched descriptor! Expected: '%s'.", methodModel.methodName().stringValue(), requiredDescriptor.descriptorString()));
		}
	}
}
