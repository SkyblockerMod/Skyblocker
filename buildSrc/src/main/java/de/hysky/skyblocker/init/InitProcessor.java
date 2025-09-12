package de.hysky.skyblocker.init;

import de.hysky.skyblocker.MethodReference;
import de.hysky.skyblocker.Processor;
import org.gradle.api.tasks.compile.JavaCompile;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public class InitProcessor {
	public void apply(JavaCompile task) {
		long start = System.currentTimeMillis();
		Map<MethodReference, Integer> methodSignatures = new HashMap<>();

		//Find all methods with the @Init annotation
		findInitMethods(methodSignatures);

		//Sort the methods by their priority. It's also converted to a list because the priority values are useless from here on
		List<MethodReference> sortedMethodSignatures = methodSignatures.entrySet()
				.stream()
				.sorted(Map.Entry.<MethodReference, Integer>comparingByValue().thenComparing(entry -> entry.getKey().className()))
				.map(Map.Entry::getKey)
				.toList();

		//Inject calls to the @Init annotated methods in the SkyblockerMod class
		injectInitCalls(sortedMethodSignatures);

		System.out.println("Injecting init methods took: " + (System.currentTimeMillis() - start) + "ms");
	}

	public void findInitMethods(Map<MethodReference, Integer> methodSignatures) {
		Processor.forEachClass(inputStream -> Processor.readClass(inputStream, classReader -> new InitReadingClassVisitor(classReader, methodSignatures)));
	}

	public void injectInitCalls(List<MethodReference> methodSignatures) {
		Path mainClassFile = Objects.requireNonNull(Processor.findClass("SkyblockerMod.class"), "SkyblockerMod class wasn't found :(").toPath();

		Processor.writeClass(mainClassFile, classWriter -> new InitInjectingClassVisitor(classWriter, methodSignatures));
	}
}
