package de.hysky.skyblocker.init;

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

public class InitInjector {
	private static final MethodReference INIT_METHOD_REFERENCE = new MethodReference(ClassDesc.of("de.hysky.skyblocker.SkyblockerMod"), "init", MethodTypeDesc.of(ConstantDescs.CD_void), false);

	public static byte[] transformClass(ClassFile context, ClassModel classModel, List<MethodReference> initMethodReferences) {
		byte[] newBytes = context.transformClass(classModel, ClassTransform.transformingMethods(INIT_METHOD_REFERENCE::matches, (methodBuilder, methodElement) -> {
			if (methodElement instanceof CodeModel) {
				methodBuilder.withCode(codeBuilder -> {
					for (MethodReference methodReference : initMethodReferences) {
						codeBuilder.invokestatic(methodReference.classDesc(), methodReference.methodName(), methodReference.typeDesc(), methodReference.itf());
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
