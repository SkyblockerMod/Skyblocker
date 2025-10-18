package de.hysky.skyblocker.hud;

import de.hysky.skyblocker.Processor;
import org.gradle.api.tasks.compile.JavaCompile;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

public class HudProcessor {

	private final Map<ClassNode, Integer> annotatedClassesConstructors = new HashMap<>();

	public void apply(JavaCompile task) {
		long start = System.currentTimeMillis();

		Processor.forEachClass(this::visitClass);

		List<ClassNode> constructors = new ArrayList<>(annotatedClassesConstructors.keySet());
		constructors.sort(Comparator.comparingInt(annotatedClassesConstructors::get));

		inject(constructors);

		System.out.println("Injecting widget instancing took: " + (System.currentTimeMillis() - start) + "ms");
	}

	private void visitClass(InputStream inputStream) {
		try {
			ClassNode classNode = new ClassNode(Opcodes.ASM9);
			Processor.readClass(inputStream, classReader -> classNode);

			// Look for the annotation
			boolean annotationFound = false;
			int priority = 0;
			List<AnnotationNode> annotationNodes = new ArrayList<>(classNode.visibleAnnotations == null ? List.of() : classNode.visibleAnnotations);
			annotationNodes.addAll(classNode.invisibleAnnotations == null ? List.of() : classNode.invisibleAnnotations);
			for (AnnotationNode annotationNode : annotationNodes) {
				String desc = annotationNode.desc;
				if (!desc.equals("Lde/hysky/skyblocker/annotations/RegisterWidget;")) continue;

				annotationFound = true;
				// null if no parameters are given, defaults don't show up :shrug:
				if (annotationNode.values != null) {
					for (int i = 0; i < annotationNode.values.size(); i++) {
						if ("priority".equals(annotationNode.values.get(i))) {
							priority = (int) annotationNode.values.get(i + 1);
						}
					}
				}
				break;
			}
			if (!annotationFound) return;
			if (!Processor.instanceOf(classNode.name, "de/hysky/skyblocker/skyblock/tabhud/widget/HudWidget")) {
				throw new IllegalArgumentException("Class " + classNode.name + " has @RegisterWidget annotation but does not extend HudWidget");
			}

			// Look for constructor
			MethodNode constructor = null;
			for (MethodNode method : classNode.methods) {
				if (!method.name.equals("<init>")) continue;
				if (!method.desc.equals("()V")) continue;
				constructor = method;
				break;
			}
			if (constructor == null) throw new IllegalStateException("No parameterless constructor found for " + classNode.name);

			annotatedClassesConstructors.put(classNode, priority);



		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void inject(List<ClassNode> constructors) {
		Path mainClassFile = Objects.requireNonNull(Processor.findClass("WidgetManager.class"), "WidgetManager class wasn't found :(").toPath();

		Processor.writeClass(mainClassFile, classWriter -> new HudInjectClassVisitor(classWriter, constructors));
	}
}
