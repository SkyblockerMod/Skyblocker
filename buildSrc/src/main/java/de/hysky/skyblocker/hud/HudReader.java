package de.hysky.skyblocker.hud;

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

import de.hysky.skyblocker.Processor;

public class HudReader {
	private static final ClassDesc REGISTER_WIDGET_ANNOTATION_DESC = ClassDesc.of("de.hysky.skyblocker.annotations.RegisterWidget");
	private static final ClassDesc HUD_WIDGET_DESC = ClassDesc.of("de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget");
	private static final MethodTypeDesc DEFAULT_CONSTRUCTOR_DESC = MethodTypeDesc.of(ConstantDescs.CD_void);

	public static void readClass(ClassModel classModel, Map<ClassDesc, Integer> registeredHudWidgets) {
		Optional<RuntimeInvisibleAnnotationsAttribute> runtimeInvisibleAnnotationsOpt = classModel.findAttribute(Attributes.runtimeInvisibleAnnotations());

		if (runtimeInvisibleAnnotationsOpt.isPresent()) {
			RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotations = runtimeInvisibleAnnotationsOpt.get();

			for (Annotation annotation : runtimeInvisibleAnnotations.annotations()) {
				if (annotation.classSymbol().equals(REGISTER_WIDGET_ANNOTATION_DESC)) {
					checkSuperClass(classModel);
					checkForDefaultConstructor(classModel);

					AnnotationValue priorityValue = annotation.elements().stream()
							.filter(annotationElement -> annotationElement.name().equalsString("priority"))
							.map(AnnotationElement::value)
							.findAny()
							.orElse(null);
					int priority = priorityValue instanceof AnnotationValue.OfInt intValue ? intValue.intValue() : 0;

					registeredHudWidgets.put(classModel.thisClass().asSymbol(), priority);
				}
			}
		}
	}

	/// Requires that widgets extend {@code HudWidget}.
	private static void checkSuperClass(ClassModel classModel) {
		if (classModel.superclass().isPresent() && Processor.instanceOf(classModel.thisClass().asSymbol(), HUD_WIDGET_DESC)) {
			return;
		}

		throw new IllegalStateException("Class " + classModel.thisClass().asSymbol().packageName() + "is annotated with @RegisterWidget but does not extend HudWidget!");
	}

	/// Requires that widgets have a no-args constructor that is public.
	private static void checkForDefaultConstructor(ClassModel classModel) {
		for (MethodModel methodModel : classModel.methods()) {
			if (methodModel.flags().has(AccessFlag.PUBLIC) && methodModel.methodName().equalsString("<init>") && methodModel.methodTypeSymbol().equals(DEFAULT_CONSTRUCTOR_DESC)) {
				return;
			}
		}

		throw new IllegalStateException("Did not find a public, no-args constructor found for " + classModel.thisClass().asSymbol().displayName() + "!");
	}
}
