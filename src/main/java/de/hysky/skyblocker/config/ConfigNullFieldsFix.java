package de.hysky.skyblocker.config;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

/**
 * While this sounds like a data fixer it isn't. - It's the only reasonable solution to deal with the mine field
 * that is YACL's null handling.
 */
public class ConfigNullFieldsFix {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String CONFIGS_PACKAGE = "de.hysky.skyblocker.config.configs";

	@SuppressWarnings("removal")
	public static void init() {
		SkyblockerConfig current = SkyblockerConfigManager.getUnpatched();
		SkyblockerConfig clean = new SkyblockerConfig();

		try {
			fixNullFields(current, clean);
			SkyblockerConfigManager.save();
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Config Null Fields Fixer] Failed to ensure that the config has no null fields! You may encounter crashes :(", e);
		}
	}

	/**
	 * Traverse through every config field to ensure that is isn't null, if it is then reset the value.
	 */
	private static void fixNullFields(Object target, Object source) throws Exception {
		for (Field field : target.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(SerialEntry.class)) {
				field.setAccessible(true);

				Object targetValue = field.get(target);
				Object sourceValue = field.get(source);

				if (targetValue == null && sourceValue != null) {
					field.set(target, sourceValue);
				} else if (targetValue != null && sourceValue != null && isFixable(field.getType())) {
					fixNullFields(targetValue, sourceValue);
				}
			}
		}
	}

	private static boolean isFixable(Class<?> clazz) {
		return !clazz.isPrimitive()
				&& !clazz.isEnum()
				&& !clazz.isRecord()
				&& !clazz.equals(String.class)
				&& !Number.class.isAssignableFrom(clazz)
				&& !Map.class.isAssignableFrom(clazz)
				&& !Collection.class.isAssignableFrom(clazz)
				&& clazz.getPackageName().startsWith(CONFIGS_PACKAGE);
	}
}
