package de.hysky.skyblocker.utils;

import java.lang.classfile.ClassModel;
import java.lang.classfile.MethodModel;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;

/// Record representing some reference to a method.
///
/// @param classDesc  The descriptor of the class in which this method is declared.
/// @param methodName The name of the method (e.g. 'main').
/// @param typeDesc   The type descriptor of the method (e.g. '(IJ)Z', '(Lnet/java/lang/Long;IZ)Ljava/lang/String;').
/// @param itf        Whether this method is declared in an interface.
public record MethodReference(ClassDesc classDesc, String methodName, MethodTypeDesc typeDesc, boolean itf) {

	public static MethodReference fromModel(MethodModel methodModel) {
		// Expect that that the parent class is known
		ClassModel parentClassModel = methodModel.parent().get();
		ClassDesc classDesc = parentClassModel.thisClass().asSymbol();
		String methodName = methodModel.methodName().stringValue();
		MethodTypeDesc typeDesc = methodModel.methodTypeSymbol();
		boolean itf = parentClassModel.flags().has(AccessFlag.INTERFACE);

		return new MethodReference(classDesc, methodName, typeDesc, itf);
	}

	public boolean matches(MethodModel methodModel) {
		return methodModel.parent()
				.map(classModel -> classModel.thisClass().asSymbol())
				.map(this.classDesc::equals).orElse(true) &&
				this.methodName.equals(methodModel.methodName().stringValue()) &&
				this.typeDesc.equals(methodModel.methodTypeSymbol()) &&
				this.itf == methodModel.parent().map(classModel -> classModel.flags().has(AccessFlag.INTERFACE)).orElse(true);
	}
}
