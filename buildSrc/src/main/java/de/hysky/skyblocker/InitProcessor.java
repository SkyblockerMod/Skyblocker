package de.hysky.skyblocker;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public abstract class InitProcessor implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getTasks().withType(JavaCompile.class).configureEach(task -> task.doLast(t -> {
			if (!task.getName().equals("compileJava")) return;

			long start = System.currentTimeMillis();
			File classesDir = task.getDestinationDirectory().get().getAsFile();
			Map<MethodReference, Integer> methodSignatures = new HashMap<>();

			//Find all methods with the @Init annotation
			findInitMethods(classesDir, methodSignatures);

			//Sort the methods by their priority. It's also converted to a list because the priority values are useless from here on
			List<MethodReference> sortedMethodSignatures = methodSignatures.entrySet()
			                                                      .stream()
			                                                      .sorted(Map.Entry.comparingByValue())
			                                                      .map(Map.Entry::getKey)
			                                                      .toList();

			//Inject calls to the @Init annotated methods in the SkyblockerMod class
			injectInitCalls(classesDir, sortedMethodSignatures);

			System.out.println("Injecting init methods took: " + (System.currentTimeMillis() - start) + "ms");
		}));
	}

	public void findInitMethods(File directory, Map<MethodReference, Integer> methodSignatures) {
		try {
			Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
					File file = path.toFile();
					if (!file.getName().endsWith(".class")) return FileVisitResult.CONTINUE;
					try (InputStream inputStream = new FileInputStream(file)) {
						ClassReader classReader = new ClassReader(inputStream);
						classReader.accept(new ReadingClassVisitor(classReader, methodSignatures), ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
					} catch (IOException e) {
						e.printStackTrace();
					}

					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void injectInitCalls(File directory, List<MethodReference> methodSignatures) {
		File mainClassFile = findMainClass(directory);

		if (mainClassFile == null) {
			throw new RuntimeException("SkyblockerMod class wasn't found :(");
		}

		byte[] classBytes;
		try {
			classBytes = Files.readAllBytes(mainClassFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		ClassReader classReader = new ClassReader(classBytes);
		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

		classReader.accept(new InjectingClassVisitor(classWriter, methodSignatures), 0);

		try {
			Files.write(mainClassFile.toPath(), classWriter.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Find the main SkyblockerMod class
	public File findMainClass(File directory) {
		if (!directory.isDirectory()) throw new IllegalArgumentException("Not a directory");
		for (File file : Objects.requireNonNull(directory.listFiles())) {
			if (file.isDirectory()) {
				File foundFile = findMainClass(file);

				if (foundFile != null) return foundFile;
			} else if (file.getName().equals("SkyblockerMod.class")) {
				return file;
			}
		}

		return null;
	}

	static class InjectingClassVisitor extends ClassVisitor {
		private final List<MethodReference> methodSignatures;

		public InjectingClassVisitor(ClassVisitor classVisitor, List<MethodReference> methodSignatures) {
			super(Opcodes.ASM9, classVisitor);
			this.methodSignatures = methodSignatures;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
			MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);

			// Limit replacing to the init method which is private, static, named init, has no args, and has a void return type
			if ((access & Opcodes.ACC_PRIVATE) != 0 && (access & Opcodes.ACC_STATIC) != 0 && name.equals("init") && descriptor.equals("()V")) {
				// Method node that we will overwrite the init method with
				MethodNode methodNode = new MethodNode(Opcodes.ASM9, access, name, descriptor, signature, exceptions);

				// The instructions that will replace the content of the init method
				InsnList insnList = new InsnList();

				// Inject calls to each found @Init annotated method
				for (MethodReference methodCall : methodSignatures) {
					MethodInsnNode methodInsnNode = new MethodInsnNode(Opcodes.INVOKESTATIC, methodCall.className(), methodCall.methodName(), methodCall.descriptor(), methodCall.itf());

					insnList.add(methodInsnNode);
				}

				// Return from the method
				insnList.add(new InsnNode(Opcodes.RETURN));

				// Put our instructions in the method node
				methodNode.instructions = insnList;

				// Apply our new method node to the visitor to replace the original one
				methodNode.accept(methodVisitor);
			}

			return methodVisitor;
		}
	}

	static class ReadingClassVisitor extends ClassVisitor {
		private final Map<MethodReference, Integer> methodSignatures;
		private final ClassReader classReader;

		public ReadingClassVisitor(ClassReader classReader, Map<MethodReference, Integer> methodSignatures) {
			super(Opcodes.ASM9);
			this.classReader = classReader;
			this.methodSignatures = methodSignatures;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
			return new MethodVisitor(Opcodes.ASM9) {
				@Override
				public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
					//This method visitor checks all methods and only acts upon those with the Init annotation.
					//This lets us warn the user about invalid init methods and misuse of the annotation
					if (!desc.equals("Lde/hysky/skyblocker/annotations/Init;")) return super.visitAnnotation(desc, visible);

					//Delegates adding the method call to the map to the InitAnnotationVisitor since we don't have a value to put in the map here
					return new InitAnnotationVisitor(methodSignatures, getMethodCall());
				}

				private @NotNull MethodReference getMethodCall() {
					String className = classReader.getClassName();
					String methodName = name;
					String methodCallString = className + "." + methodName;
					if ((access & Opcodes.ACC_PUBLIC) == 0) throw new IllegalStateException(methodCallString + ": Initializer methods must be public");
					if ((access & Opcodes.ACC_STATIC) == 0) throw new IllegalStateException(methodCallString + ": Initializer methods must be static");
					if (!descriptor.equals("()V")) throw new IllegalStateException(methodCallString + ": Initializer methods must have no args and a void return type");

					//Interface static methods need special handling, so we add a special marker for that
					boolean itf = (classReader.getAccess() & Opcodes.ACC_INTERFACE) != 0;

					return new MethodReference(className, methodName, descriptor, itf);
				}
			};
		}
	}

	static class InitAnnotationVisitor extends AnnotationVisitor {
		private final Map<MethodReference, Integer> methodSignatures;
		private final MethodReference methodCall;

		protected InitAnnotationVisitor(Map<MethodReference, Integer> methodSignatures, MethodReference methodCall) {
			super(Opcodes.ASM9);
			this.methodSignatures = methodSignatures;
			this.methodCall = methodCall;
		}

		@Override
		public void visitEnd() {
			//Annotations that use the default value for the priority field will not be called by the visit method, so we have to handle them here.
			methodSignatures.putIfAbsent(methodCall, 0);
			super.visitEnd();
		}

		@Override
		public void visit(String name, Object value) {
			if (name.equals("priority")) {
				methodSignatures.put(methodCall, (int) value);
			}
			super.visit(name, value);
		}
	}

	/**
	 * @param className  the class name (e.g. de/hysky/skyblocker/skyblock/ChestValue)
	 * @param methodName the method's name (e.g. init)
	 * @param descriptor the method's descriptor (only ()V for now)
	 * @param itf        whether the target class is an {@code interface} or not
	 */
	private record MethodReference(String className, String methodName, String descriptor, boolean itf) {}
}
