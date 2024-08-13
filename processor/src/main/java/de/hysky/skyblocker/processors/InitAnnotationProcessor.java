package de.hysky.skyblocker.processors;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes(InitAnnotationProcessor.INIT)
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class InitAnnotationProcessor extends AbstractProcessor {
	public static final String INIT = "de.hysky.skyblocker.annotations.Init";
	private static final String GENERATED_CLASS_NAME = "de.hysky.skyblocker.Initializer";
	private static final String GENERATED_METHOD_NAME = "init";

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		List<String> methodCalls = new ArrayList<>();

		for (TypeElement annotation : annotations) {
			for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
				if (!annotation.getQualifiedName().contentEquals(INIT)) continue;
				Set<Modifier> modifiers = element.getModifiers();
				if (element.getKind() == ElementKind.METHOD) {
					if (!modifiers.contains(Modifier.PUBLIC) || !modifiers.contains(Modifier.STATIC)) {
						processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, ((TypeElement) element.getEnclosingElement()).getQualifiedName() + "#" + element.getSimpleName() +": Methods annotated with @Init must be public and static");
						continue;
					}
					ExecutableElement method = (ExecutableElement) element;
					String className = ((TypeElement) method.getEnclosingElement()).getQualifiedName().toString();
					String methodName = method.getSimpleName().toString();
					methodCalls.add(className + "." + methodName + "();");
				}
			}
		}

		if (!methodCalls.isEmpty()) {
			try {
				Filer filer = processingEnv.getFiler();
				JavaFileObject sourceFile = filer.createSourceFile(GENERATED_CLASS_NAME);
				try (Writer writer = sourceFile.openWriter()) {
					writer.write(generateClassContent(methodCalls));
				}
			} catch (IOException e) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate new class: " + e.getMessage());
			}
		}

		return true;
	}

	private String generateClassContent(List<String> methodCalls) {
		StringBuilder content = new StringBuilder();
		content.append("package de.hysky.skyblocker;\n\n");
		content.append("/** Auto-generated class for initializing features. Do not edit, as changes will be overwritten. */\n");
		content.append("public class Initializer {\n");
		content.append("    public static void ").append(GENERATED_METHOD_NAME).append("() {\n");
		for (String methodCall : methodCalls) {
			content.append("        ").append(methodCall).append("\n");
		}
		content.append("    }\n");
		content.append("}\n");
		return content.toString();
	}
}