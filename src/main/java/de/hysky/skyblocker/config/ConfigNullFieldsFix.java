package de.hysky.skyblocker.config;

import java.lang.reflect.Field;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

/**
 * While this sounds like a data fixer it isn't. - It's the only reasonable solution to deal with the mine field
 * that is YACL's null handling.
 */
public class ConfigNullFieldsFix {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static void init() {
		SkyblockerConfigManager.update(config -> {
			try {
				fixNullFields(config, new SkyblockerConfig());
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Config Null Fields Fixer] Failed to ensure that the config has no null fields! You may encounter crashes :(", e);
			}
		});
	}

	/**
	 * Traverse through every config field to ensure that is isn't null, if it is then reset the value.
	 */
	private static void fixNullFields(Object config, Object defaultConfig) throws Exception {
		for (Field field : config.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(SerialEntry.class)) {
				field.setAccessible(true);

				Object configValue = field.get(config);
				Object defaultValue = field.get(defaultConfig);

				if (configValue == null && defaultValue != null) {
					field.set(config, defaultValue);
				} else if (configValue != null && defaultValue != null && SkyblockerConfigManager.isConfigClass(field.getType())) {
					fixNullFields(configValue, defaultValue);
				}
			}
		}
	}
}
