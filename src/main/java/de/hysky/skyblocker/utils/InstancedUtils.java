package de.hysky.skyblocker.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

/**
 * @deprecated Use the new {@code GenEquals}, {@code GenHashCode}, and {@code GenToString} annotations which also have proper
 * superclass support.
 */
@Deprecated(forRemoval = true)
public class InstancedUtils {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<Class<?>, MethodHandle> EQUALS_CACHE = new ConcurrentHashMap<>();
	private static final Map<Class<?>, MethodHandle> HASH_CODE_CACHE = new ConcurrentHashMap<>();
	private static final Map<Class<?>, MethodHandle> TO_STRING_CACHE = new ConcurrentHashMap<>();

	public static MethodHandle equals(Class<?> type) {
		if (EQUALS_CACHE.containsKey(type)) return EQUALS_CACHE.get(type);

		try {
			Field[] fields = getClassFields(type);
			MethodHandle[] getters = getFieldGetters(fields);

			//The field names param can be anything as equals and hashCode don't care about it.
			MethodHandle equalsHandle = (MethodHandle) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodHandle.class, type, "", getters);

			EQUALS_CACHE.put(type, equalsHandle);

			return equalsHandle;
		} catch (Throwable t) {
			LOGGER.error("[Skyblocked Instanced Utils] Failed to create an equals method handle.", t);

			throw new RuntimeException();
		}
	}

	public static MethodHandle hashCode(Class<?> type) {
		if (HASH_CODE_CACHE.containsKey(type)) return HASH_CODE_CACHE.get(type);

		try {
			Field[] fields = getClassFields(type);
			MethodHandle[] getters = getFieldGetters(fields);

			//The field names param can be anything as equals and hashCode don't care about it.
			MethodHandle hashCodeHandle = (MethodHandle) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodHandle.class, type, "", getters);

			HASH_CODE_CACHE.put(type, hashCodeHandle);

			return hashCodeHandle;
		} catch (Throwable t) {
			LOGGER.error("[Skyblocked Instanced Utils] Failed to create a hashCode method handle.", t);

			throw new RuntimeException();
		}
	}

	public static MethodHandle toString(Class<?> type) {
		if (TO_STRING_CACHE.containsKey(type)) return TO_STRING_CACHE.get(type);

		try {
			Field[] fields = getClassFields(type);
			MethodHandle[] getters = getFieldGetters(fields);
			String fieldNames = String.join(";", Arrays.stream(fields).filter(InstancedUtils::nonStatic).map(Field::getName).toArray(String[]::new));

			MethodHandle toStringHandle = (MethodHandle) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodHandle.class, type, fieldNames, getters);

			TO_STRING_CACHE.put(type, toStringHandle);

			return toStringHandle;
		} catch (Throwable t) {
			LOGGER.error("[Skyblocked Instanced Utils] Failed to create a toString method handle.", t);

			throw new RuntimeException();
		}
	}

	private static Field[] getClassFields(Class<?> type) {
		return Stream.concat(Arrays.stream(type.getDeclaredFields()), Arrays.stream(type.getFields())).distinct().toArray(Field[]::new);
	}

	private static MethodHandle[] getFieldGetters(Field[] fields) throws Throwable {
		// Keep insertion order to make sure getters and field names match
		ObjectSet<MethodHandle> handles = new ObjectLinkedOpenHashSet<>();

		for (Field field : fields) {
			if (!nonStatic(field)) continue;

			field.setAccessible(true);

			MethodHandle getter = MethodHandles.lookup().unreflectGetter(field);

			handles.add(getter);
		}

		return handles.toArray(MethodHandle[]::new);
	}

	private static boolean nonStatic(Field field) {
		return (field.getModifiers() & Modifier.STATIC) == 0;
	}
}
