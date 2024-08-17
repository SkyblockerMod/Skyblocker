package de.hysky.skyblocker;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.compile.JavaCompile;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public abstract class InitProcessor implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getTasks().withType(JavaCompile.class).configureEach(task -> task.doLast(t -> {
			if (!task.getName().equals("compileJava")) return;

			long start = System.currentTimeMillis();
			File classesDir = task.getDestinationDirectory().get().getAsFile();
			List<String> methodSignatures = new ArrayList<>();

			//Find all methods with the @Init annotation
			findInitMethods(classesDir, methodSignatures);

			//Inject calls to the @Init annotated methods in the SkyblockerMod class
			injectInitCalls(classesDir, methodSignatures);

			project.getLogger().info("Injecting init methods took: {}ms", System.currentTimeMillis() - start);
		}));
	}

	public void findInitMethods(File directory, List<String> methodSignatures) {
		try {
			Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
					File file = path.toFile();
					if (!file.getName().endsWith(".class")) return FileVisitResult.CONTINUE;
					try (InputStream inputStream = new FileInputStream(file)) {
						ClassReader classReader = new ClassReader(inputStream);
						classReader.accept(new ReadingClassVisitor(Opcodes.ASM9, classReader, methodSignatures), ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
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

	public void injectInitCalls(File directory, List<String> methodSignatures) {
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

		classReader.accept(new InjectingClassVisitor(Opcodes.ASM9, classWriter, methodSignatures), 0);

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
		private final List<String> methodSignatures;

		public InjectingClassVisitor(int api, ClassVisitor classVisitor, List<String> methodSignatures) {
			super(api, classVisitor);
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
				for (String sig : methodSignatures) {
					String className = sig.substring(0, sig.indexOf('.'));
					String methodName = sig.substring(sig.indexOf('.') + 1).replace("-ITF", "");

					MethodInsnNode methodInsnNode = new MethodInsnNode(Opcodes.INVOKESTATIC, className, methodName, "()V", sig.endsWith("-ITF"));

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
		private final List<String> methodSignatures;
		private final ClassReader classReader;

		public ReadingClassVisitor(int api, ClassReader classReader, List<String> methodSignatures) {
			super(api);
			this.classReader = classReader;
			this.methodSignatures = methodSignatures;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
			// Only check methods that are public, static, have no args, and have a void return type
			if ((access & Opcodes.ACC_PUBLIC) != 0 && (access & Opcodes.ACC_STATIC) != 0 && descriptor.equals("()V")) {
				return new MethodVisitor(Opcodes.ASM9) {
					@Override
					public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
						if (desc.equals("Lde/hysky/skyblocker/annotations/Init;")) {
							String methodCall = classReader.getClassName() + "." + name;

							//Interface static methods need special handling, so we add a special marker for that
							if ((classReader.getAccess() & Opcodes.ACC_INTERFACE) != 0) methodCall += "-ITF";

							methodSignatures.add(methodCall);
						}

						return super.visitAnnotation(desc, visible);
					}
				};
			}

			return super.visitMethod(access, name, descriptor, signature, exceptions);
		}
	}
}
