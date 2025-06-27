package de.hysky.skyblocker.config.serialization;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import de.hysky.skyblocker.mixins.accessors.ConfigClassHandlerImplAccessor;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.ConfigField;
import dev.isxander.yacl3.config.v2.api.ConfigSerializer;
import dev.isxander.yacl3.config.v2.api.FieldAccess;

public class VanillaGsonConfigSerializer<T> extends ConfigSerializer<T> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Path path;
	private final Gson gson;

	public VanillaGsonConfigSerializer(ConfigClassHandler<T> config, Path path, UnaryOperator<GsonBuilder> builderUpdater) {
		super(config);
		this.path = path;
		this.gson = builderUpdater.apply(new GsonBuilder()).create();
	}

	@Override
	public void save() {
		T instance = this.config.instance();

		try {
			String json = this.gson.toJson(instance);

			Files.createDirectories(path.getParent());
			Files.writeString(this.path, json, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
			LOGGER.info("[Skyblocker Config] Successfully saved config file to {}.", this.path);
		} catch (Exception e) {
			LOGGER.error(LogUtils.FATAL_MARKER, "[Skyblocker Config] Failed to save the config file to: {}!!!", this.path, e);
		}
	}

	@Override
	public LoadResult loadSafely(Map<ConfigField<?>, FieldAccess<?>> bufferAccessMap) {
		try {
			if (!Files.exists(this.path)) {
				((ConfigClassHandlerImplAccessor) this.config).setInstance(createNewConfigInstance());
				save();
				return LoadResult.NO_CHANGE;
			}
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Config] Failed to create default config file!", e);
		}

		try {
			String config = Files.readString(this.path);
			T instance = this.gson.fromJson(JsonParser.parseString(config), this.config.configClass());

			//Set the instance
			((ConfigClassHandlerImplAccessor) this.config).setInstance(instance);
			LOGGER.info("[Skyblocker Config] Successfully loaded the config file from {}.", this.path);
		} catch (Exception e) {
			LOGGER.error(LogUtils.FATAL_MARKER, "[Skyblocker Config] Failed to load the config!", e);
		}

		//We use no change to avoid the instance being set by YACL to a fresh one since we just did that ourselves.
		return LoadResult.NO_CHANGE;
	}

	private T createNewConfigInstance() {
		try {
			return (T) this.config.configClass().getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Config] Failed to create new config instance!", e);
		}

		return null;
	}
}
