package de.hysky.skyblocker.config.datafixer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.datafixer.JsonHelper;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigDataFixer {
	protected static final Logger LOGGER = LogUtils.getLogger();
	private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
	public static final DSL.TypeReference CONFIG_TYPE = () -> "config";

	public static void apply() {
		apply(CONFIG_DIR.resolve("skyblocker.json"), CONFIG_DIR.resolve("skyblocker.json.old"));
	}

	public static void apply(Path configDir, Path backupDir) {
		//User is new - has no config file (or maybe config folder)
		if (!Files.exists(CONFIG_DIR) || !Files.exists(configDir)) return;

		//Should never be null if the file exists unless its malformed JSON or something in which case well it gets reset
		JsonObject oldConfig = loadConfig(configDir);
		if (oldConfig == null || JsonHelper.getInt(oldConfig, "version").orElse(1) == SkyblockerConfigManager.CONFIG_VERSION) return;

		JsonObject newConfig = apply(oldConfig);

		//Write the updated file
        if (!writeConfig(configDir, newConfig)) {
			LOGGER.error(LogUtils.FATAL_MARKER, "[Skyblocker Config Data Fixer] Failed to fix up config file!");
			writeConfig(backupDir, oldConfig);
		}
	}

	public static JsonObject apply(JsonObject oldConfig) {
		return apply(oldConfig, SkyblockerConfigManager.CONFIG_VERSION);
	}

	public static JsonObject apply(JsonObject oldConfig, int newVersion) {
        long start = System.currentTimeMillis();

		JsonObject newConfig = build().update(CONFIG_TYPE, new Dynamic<>(JsonOps.INSTANCE, oldConfig), JsonHelper.getInt(oldConfig, "version").orElse(1), newVersion).getValue().getAsJsonObject();

		long end = System.currentTimeMillis();
		LOGGER.info("[Skyblocker Config Data Fixer] Applied datafixers in {} ms!", end - start);
		return newConfig;
	}

	private static DataFixer build() {
		DataFixerBuilder builder = new DataFixerBuilder(SkyblockerConfigManager.CONFIG_VERSION);

		builder.addSchema(1, ConfigSchema::new);
		Schema schema2 = builder.addSchema(2, Schema::new);
		builder.addFixer(new ConfigFix1(schema2, true));
		Schema schema3 = builder.addSchema(3, Schema::new);
		builder.addFixer(new ConfigFix2QuickNav(schema3, true));

		return builder.build().fixer();
	}

	private static JsonObject loadConfig(Path path) {
		try (BufferedReader reader = Files.newBufferedReader(path)) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		} catch (Throwable t) {
			LOGGER.error("[Skyblocker Config Data Fixer] Failed to load config file!", t);
		}

		return null;
	}

	private static boolean writeConfig(Path path, JsonObject config) {
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			SkyblockerMod.GSON.toJson(config, writer);

			return true;
		} catch (Throwable t) {
			LOGGER.error("[Skyblocker Config Data Fixer] Failed to save config file at {}!", path, t);
		}

		return false;
	}
}
