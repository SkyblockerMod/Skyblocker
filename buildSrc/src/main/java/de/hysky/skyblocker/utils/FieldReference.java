package de.hysky.skyblocker.utils;

import java.lang.classfile.FieldModel;
import java.lang.constant.ClassDesc;

/// Record representing some reference to a field.
///
/// @param classDesc  The descriptor of the class the field is declared in.
/// @param fieldName  The name of the field (e.g. 'value').
/// @param typeDesc   The field's descriptor (e.g. 'Z', 'Ljava/lang/Object;').
public record FieldReference(ClassDesc classDesc, String fieldName, ClassDesc typeDesc) {

	public static FieldReference fromModel(FieldModel fieldModel) {
		// Expect that that the parent class is known
		ClassDesc classDesc = fieldModel.parent().get().thisClass().asSymbol();
		String fieldName = fieldModel.fieldName().stringValue();
		ClassDesc typeDesc = fieldModel.fieldTypeSymbol();

		return new FieldReference(classDesc, fieldName, typeDesc);
	}

	public boolean matches(FieldModel fieldModel) {
		return fieldModel.parent()
				.map(classModel -> classModel.thisClass().asSymbol())
				.map(this.classDesc::equals).orElse(true) &&
				this.fieldName.equals(fieldModel.fieldName().stringValue()) &&
				this.typeDesc.equals(fieldModel.fieldTypeSymbol());
	}
}
