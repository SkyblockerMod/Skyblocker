package de.hysky.skyblocker.hud;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeModel;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

import de.hysky.skyblocker.Processor;
import de.hysky.skyblocker.utils.MethodReference;

public class HudInjector {
	private static final ClassDesc WIDGETS_MANAGER_DESC = ClassDesc.of("de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager");
	private static final MethodReference INIT_WIDGETS_METHOD_REFERENCE = new MethodReference(WIDGETS_MANAGER_DESC, "instantiateWidgets", MethodTypeDesc.of(ConstantDescs.CD_void), false);

	public static byte[] transformClass(ClassFile context, ClassModel classModel, List<ClassDesc> widgetClasses) {
		byte[] newBytes = context.transformClass(classModel, ClassTransform.transformingMethods(INIT_WIDGETS_METHOD_REFERENCE::matches, (methodBuilder, methodElement) -> {
			if (methodElement instanceof CodeModel) {
				methodBuilder.withCode(codeBuilder -> {
					for (ClassDesc widgetClass : widgetClasses) {
						codeBuilder.new_(widgetClass);
						codeBuilder.dup();
						codeBuilder.invokespecial(widgetClass, "<init>", MethodTypeDesc.of(ConstantDescs.CD_void));
						codeBuilder.invokestatic(WIDGETS_MANAGER_DESC, "addWidgetInstance", MethodTypeDesc.of(ConstantDescs.CD_void, ClassDesc.of("de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget")));
					}

					codeBuilder.return_();
				});
			} else {
				methodBuilder.with(methodElement);
			}
		}));
		Processor.verifyClass(context, classModel.thisClass().asSymbol(), newBytes);

		return newBytes;
	}
}
