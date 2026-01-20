package de.hysky.skyblocker;

/**
 * Record representing some reference to a field.
 *
 * @param className  The name of the class the field is in. See {@link ClassReference#className()}.
 * @param fieldName  The name of the field (e.g. 'value').
 * @param descriptor The field's descriptor (e.g. 'Z', 'Ljava/lang/Object;').
 */
public record FieldReference(String className, String fieldName, String descriptor) {
}
