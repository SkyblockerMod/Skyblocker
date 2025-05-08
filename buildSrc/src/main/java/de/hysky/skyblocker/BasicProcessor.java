package de.hysky.skyblocker;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface BasicProcessor {


	/**
	 * This method will be called on all classes in the skyblocker codebase, use this to gather information.
	 * Modifications will not be saved and will just bother processors implementing this interface.
	 */
	void parseClass(ClassNode clazz);

	/**
	 * Write modifications to classes
	 * @param classProvider returns a ClassNode based on a class' full name that will be saved after all procesors implementing this interface have run
	 *                      example: de/hysky/skyblocker/SkyblockerMod
	 */
	void writeToClasses(Function<String, ClassNode> classProvider);

	static List<AnnotationNode> mergeAnnotationLists(@Nullable List<AnnotationNode> visible, @Nullable List<AnnotationNode> invisible) {
		List<AnnotationNode> merged = new ArrayList<>(visible == null ? List.of() : visible);
		merged.addAll(invisible == null ? List.of() : invisible);
		return merged;
	}

	static List<AnnotationNode> getAnnotations(ClassNode classNode) {
		return mergeAnnotationLists(classNode.visibleAnnotations, classNode.invisibleAnnotations);
	}

	static List<AnnotationNode> getAnnotations(MethodNode methodNode) {
		return mergeAnnotationLists(methodNode.visibleAnnotations, methodNode.invisibleAnnotations);
	}

	static List<AnnotationNode> getAnnotations(FieldNode fieldNode) {
		return mergeAnnotationLists(fieldNode.visibleAnnotations, fieldNode.invisibleAnnotations);
	}

	/**
	 * @param className  the class name (e.g. de/hysky/skyblocker/skyblock/ChestValue)
	 * @param methodName the method's name (e.g. init)
	 * @param descriptor the method's descriptor (only ()V for now)
	 * @param itf        whether the target class is an {@code interface} or not
	 */
	record MethodReference(String className, String methodName, String descriptor, boolean itf) {
		public MethodReference(ClassNode classNode, MethodNode node) {
			this(classNode.name, node.name, node.desc, (classNode.access & Opcodes.ACC_INTERFACE) != 0);
		}
	}

	static int getIntOrDefault(AnnotationNode annotationNode, String valueName, int defaultValue) {
		if (annotationNode.values == null) return defaultValue;
		for (int i = 0; i < annotationNode.values.size(); i++) {
			if (valueName.equals(annotationNode.values.get(i))) {
				return (int) annotationNode.values.get(i+1);
			}
		}
		return defaultValue;
	}
}
