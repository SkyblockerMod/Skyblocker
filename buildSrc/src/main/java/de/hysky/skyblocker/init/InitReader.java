package de.hysky.skyblocker.init;

import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.AnnotationValue;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.attribute.RuntimeInvisibleAnnotationsAttribute;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.Map;
import java.util.Optional;

import de.hysky.skyblocker.utils.MethodReference;

public class InitReader {
	private static final ClassDesc INIT_ANNOTATION_DESC = ClassDesc.of("de.hysky.skyblocker.annotations.Init");

	public static void readClass(ClassModel classModel, Map<MethodReference, Integer> initMethodReferences) {
		for (MethodModel methodModel : classModel.methods()) {
			Optional<RuntimeInvisibleAnnotationsAttribute> runtimeInvisibleAnnotationsOpt = methodModel.findAttribute(Attributes.runtimeInvisibleAnnotations());

			if (runtimeInvisibleAnnotationsOpt.isPresent()) {
				RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotations = runtimeInvisibleAnnotationsOpt.get();

				for (Annotation annotation : runtimeInvisibleAnnotations.annotations()) {
					if (annotation.classSymbol().equals(INIT_ANNOTATION_DESC)) {
						// Ensure that this is a valid init method
						checkInitMethod(methodModel);

						AnnotationValue priorityValue = annotation.elements().stream()
								.filter(annotationElement -> annotationElement.name().equalsString("priority"))
								.map(AnnotationElement::value)
								.findAny()
								.orElse(null);
						int priority = priorityValue instanceof AnnotationValue.OfInt intValue ? intValue.intValue() : 0;
						MethodReference methodReference = MethodReference.fromModel(methodModel);

						initMethodReferences.put(methodReference, priority);
					}
				}
			}
		}
	}

	/// Requires that init methods are public, static, have no parameters, and have a void return type.
	private static void checkInitMethod(MethodModel methodModel) {
		if (!methodModel.flags().has(AccessFlag.PUBLIC)) {
			throw new IllegalStateException(methodModel.toDebugString() + ": Initializer methods must be public");
		}

		if (!methodModel.flags().has(AccessFlag.STATIC)) {
			throw new IllegalStateException(methodModel.toDebugString() + ": Initializer methods must be static");
		}

		MethodTypeDesc typeDesc = methodModel.methodTypeSymbol();
		if (typeDesc.parameterCount() > 0 || !typeDesc.returnType().equals(ConstantDescs.CD_void)) {
			throw new IllegalStateException(methodModel.toDebugString() + ": Initializer methods must have no arguments and a void return type");
		}
	}
}
