package de.hysky.skyblocker.register;

import de.hysky.skyblocker.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RegisterAnnotationProcessor {

	private static final String INSTANCE_FIELD = "INSTANCE";

	public enum Registry {
		SLOT_TEXT("RegisterSlotTextAdder", "SlotTextManager", "ADDERS"),
		TOOLTIP("RegisterTooltipAdder", "TooltipManager", "ADDERS"),
		CONTAINER_SOLVER("RegisterContainerSolver", "ContainerSolverManager", "SOLVERS");
		Registry(String annotation, String targetClass, String targetField) {
			this.annotation = annotation;
			this.targetClass = targetClass;
			this.targetField = targetField;
		}
		public final String annotation;
		public final String targetClass;
		public final String targetField;

		private static final Map<String, Registry> REGISTRY_MAP = Arrays.stream(Registry.values())
				.collect(Collectors.toMap(
						registry -> "Lde/hysky/skyblocker/annotations/" + registry.annotation + ";",
						Function.identity()));

		public static @Nullable Registry of(String annotation) {
			return REGISTRY_MAP.get(annotation);
		}
	}



	private final Map<Registry, Map<String, Integer>> targetClasses = new EnumMap<>(Registry.class);
	private final Set<String> classesToAddInstanceField = new HashSet<>();

	public RegisterAnnotationProcessor(){
		for (Registry value : Registry.values()) {
			targetClasses.put(value, new HashMap<>());
		}
	}

	public void apply() {
		Processor.forEachClass(this::readClass);

		Map<String, String> classToDescriptor = new HashMap<>();
		// Inject INSTANCE fields
		for (String clazz : classesToAddInstanceField) {
			Path classPath = Processor.getClassPath(clazz);
			ClassWriter writer = null;
			try (InputStream stream = Files.newInputStream(classPath)) {
				ClassReader reader = new ClassReader(stream);
				ClassNode classNode = new ClassNode(Opcodes.ASM9);
				reader.accept(classNode, 0);

				// Add the field if it's not here
				FieldNode instanceField = null;
				for (FieldNode field : classNode.fields) {
					if (INSTANCE_FIELD.equals(field.name)) {
						instanceField = field;
						break;
					}
				}
				if (instanceField != null) {
					classToDescriptor.put(classNode.name, instanceField.desc);
					continue;
				}

				boolean foundParameterlessConstructor = false;
				for (MethodNode method : classNode.methods) {
					if ("<init>".equals(method.name) && "()V".equals(method.desc)) {
						foundParameterlessConstructor = true;
						break;
					}
				}
				if (!foundParameterlessConstructor) {
					Processor.LOGGER.error("Could not find parameterless constructor for {}", clazz);
				}

				String descriptor = "L" + clazz + ";";
				classNode.fields.add(new FieldNode(
						Opcodes.ASM9,
						Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
						INSTANCE_FIELD,
						descriptor,
						null,
						null
				));
				classToDescriptor.put(classNode.name, descriptor);

				// Allocate in clinit
				MethodNode clinit = getClinitMethod(classNode);
				AbstractInsnNode returnNode = null;
				for (AbstractInsnNode insn : clinit.instructions) {
					if (insn instanceof InsnNode && insn.getOpcode() == Opcodes.RETURN) {
						returnNode = insn;
					}
				}
				if (returnNode == null) {
					Processor.LOGGER.warn("Somehow couldn't find return node in clinit of class: {}", clazz);
					continue;
				}
				InsnList list = new InsnList();
				list.add(new TypeInsnNode(Opcodes.NEW, clazz));
				list.add(new InsnNode(Opcodes.DUP));
				list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, clazz, "<init>", "()V"));
				list.add(new FieldInsnNode(Opcodes.PUTSTATIC, clazz, INSTANCE_FIELD, descriptor));
				clinit.instructions.insertBefore(returnNode, list);

				writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
				classNode.accept(writer);

			} catch (IOException e) {
				Processor.LOGGER.error("Error reading class {}", clazz, e);
			}

			if (writer == null) continue;
			try (OutputStream outputStream = Files.newOutputStream(classPath)) {
				outputStream.write(writer.toByteArray());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		// Inject into manager classes
		for (Registry registry : targetClasses.keySet()) {
			ClassWriter classWriter = null;
			File file = Processor.findClass(registry.targetClass);
			if (file == null) {
				Processor.LOGGER.error("Could not find class {}", registry.targetClass);
				continue;
			}
			List<String> classesToInstantiate = targetClasses.get(registry).entrySet()
					.stream()
					.sorted(Comparator.comparingInt(Map.Entry::getValue))
					.map(Map.Entry::getKey)
					.toList();
			try (InputStream stream = Files.newInputStream(file.toPath())) {
				ClassReader reader = new ClassReader(stream);
				ClassNode classNode = new ClassNode(Opcodes.ASM9);
				reader.accept(classNode, 0);

				MethodNode clinit = getClinitMethod(classNode);
				AbstractInsnNode putStaticNode = null;
				for (AbstractInsnNode instruction : clinit.instructions) {
					if (instruction instanceof FieldInsnNode fieldNode && instruction.getOpcode() == Opcodes.PUTSTATIC && registry.targetField.equals(fieldNode.name)) {
						putStaticNode = instruction;
						break;
					}
				}
				if (putStaticNode == null) {
					Processor.LOGGER.error("Couldn't find put static node in clinit of class: {}, field: {}", registry.targetClass, registry.targetField);
					continue;
				}
				AbstractInsnNode newArrayNode = putStaticNode.getPrevious();
				AbstractInsnNode newArraySizeNode = newArrayNode.getPrevious();

				IntInsnNode arraySizeNode = new IntInsnNode(Opcodes.BIPUSH, classesToInstantiate.size());
				clinit.instructions.set(newArraySizeNode, arraySizeNode);

				InsnList list = new InsnList();
				for (int i = 0; i < classesToInstantiate.size(); i++) {
					String className = classesToInstantiate.get(i);
					list.add(new InsnNode(Opcodes.DUP));
					list.add(new IntInsnNode(Opcodes.BIPUSH, i));
					list.add(new FieldInsnNode(Opcodes.GETSTATIC, className, INSTANCE_FIELD, classToDescriptor.get(className)));
					list.add(new InsnNode(Opcodes.AASTORE));
				}
				clinit.instructions.insert(newArrayNode, list);
				classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
				classNode.accept(classWriter);

			} catch (IOException e) {
				Processor.LOGGER.error("Error reading file {}", file, e);
			}

			if (classWriter == null) continue;
			try (OutputStream outputStream = Files.newOutputStream(file.toPath())) {
				outputStream.write(classWriter.toByteArray());
			} catch (IOException e) {
				Processor.LOGGER.error("Error write file {}", file, e);
			}


		}
	}

	private @NotNull MethodNode getClinitMethod(ClassNode classNode) {
		MethodNode clinit = null;
		for (MethodNode method : classNode.methods) {
			if ("<clinit>".equals(method.name)) {
				clinit = method;
				break;
			}
		}
		if (clinit == null) {
			clinit = new MethodNode(Opcodes.ASM9, Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
			clinit.instructions.add(new InsnNode(Opcodes.RETURN));
			classNode.methods.add(clinit);
		}
		return clinit;
	}

	private void readClass(InputStream inputStream) {
		try {
			ClassReader classReader = new ClassReader(inputStream);
			ClassNode classNode = new ClassNode(Opcodes.ASM9);
			classReader.accept(classNode, 0);

			List<AnnotationNode> annotationNodes = new ArrayList<>(classNode.visibleAnnotations == null ? List.of() : classNode.visibleAnnotations);
			annotationNodes.addAll(classNode.invisibleAnnotations == null ? List.of() : classNode.invisibleAnnotations);


			for (AnnotationNode annotationNode : annotationNodes) {
				Registry registry = Registry.of(annotationNode.desc);
				if (registry == null) continue;
				OptionalInt priority = OptionalInt.empty();
				if (annotationNode.values != null) {
					for (int i = 0; i < annotationNode.values.size(); i++) {
						if ("priority".equals(annotationNode.values.get(i))) {
							priority = OptionalInt.of((int) annotationNode.values.get(i + 1));
							break;
						}
					}
				}
				targetClasses.getOrDefault(registry, new HashMap<>()).put(classNode.name, priority.orElse(0));
				classesToAddInstanceField.add(classNode.name);


			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
