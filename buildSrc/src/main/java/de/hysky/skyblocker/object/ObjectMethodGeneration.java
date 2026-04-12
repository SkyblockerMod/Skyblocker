package de.hysky.skyblocker.object;

import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.Optional;

import de.hysky.skyblocker.utils.FieldReference;
import de.hysky.skyblocker.utils.MethodReference;

/// Record representing information about an {@link Object} method to generate in a class.
///
/// @param classReference      The descriptor of the class that the method will be generated in.
/// @param target              The target method to replace in the class.
/// @param objectMethodType    The type method that we are generating.
/// @param fieldReferences     The fields to incorporate in the generated method (e.g. what fields will be used for equals, hashCode, and toString).
/// @param superClassDesc      The descriptor of the class' superclass. Only applicable to equals and hashCode, and is only present if the super class should be considered by the operation.
public record ObjectMethodGeneration(ClassDesc classDesc, MethodReference target, ObjectMethodType objectMethodType, List<FieldReference> fieldReferences, Optional<ClassDesc> superClassDesc) {
}
