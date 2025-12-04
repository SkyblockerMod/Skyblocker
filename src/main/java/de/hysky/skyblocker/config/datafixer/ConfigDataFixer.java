package de.hysky.skyblocker.config.datafixer;

import com.google.gson.JsonObject;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.*;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import org.slf4j.Logger;

public class ConfigDataFixer {
	static final String VERSION_KEY = "version";
	protected static final Logger LOGGER = LogUtils.getLogger();
	public static final DSL.TypeReference CONFIG_TYPE = () -> "config";
	public static final DSL.TypeReference CHAT_RULES_TYPE = () -> "chat_rules";
	private static DataFixer dataFixer;

	public static JsonObject apply(DSL.TypeReference type, JsonObject oldConfig) {
		return apply(type, oldConfig, SkyblockerConfigManager.CONFIG_VERSION);
	}

	public static <T> Dynamic<T> apply(DSL.TypeReference type, Dynamic<T> oldConfig) {
		return apply(type, oldConfig, SkyblockerConfigManager.CONFIG_VERSION);
	}

	public static <T> Dynamic<T> apply(DSL.TypeReference type, Dynamic<T> oldConfig, int newVersion) {
		long start = System.currentTimeMillis();

		Dynamic<T> newConfig = build().update(type, oldConfig, oldConfig.get(VERSION_KEY).asInt(1), newVersion);

		long end = System.currentTimeMillis();
		LOGGER.info("[Skyblocker Config Data Fixer] Applied {} datafixers in {} ms!", type.typeName(), end - start);
		return newConfig;
	}

	public static JsonObject apply(DSL.TypeReference type, JsonObject oldConfig, int newVersion) {
		return apply(type, new Dynamic<>(JsonOps.INSTANCE, oldConfig), newVersion).getValue().getAsJsonObject();
	}

	private static DataFixer build() {
		if (dataFixer != null) return dataFixer;
		DataFixerBuilder builder = new DataFixerBuilder(SkyblockerConfigManager.CONFIG_VERSION);

		builder.addSchema(1, ConfigSchema::new);
		Schema schema2 = builder.addSchema(2, Schema::new);
		builder.addFixer(new ConfigFix1(schema2, true));
		Schema schema3 = builder.addSchema(3, Schema::new);
		builder.addFixer(new ConfigFix2QuickNav(schema3, true));
		Schema schema4 = builder.addSchema(4, Schema::new);
		builder.addFixer(new ConfigFix3AnimatedDyeAndItemBackground(schema4, true));
		Schema schema5 = builder.addSchema(5, Schema::new);
		builder.addFixer(new ConfigFix4ChatRulesObject(schema5, true));
		Schema schema6 = builder.addSchema(6, Schema::new);
		builder.addFixer(new ConfigFix5ChatRulesSeparateOutputs(schema6, true));

		return dataFixer = builder.build().fixer();
	}

	public static <A> Codec<A> createDataFixingCodec(DSL.TypeReference type, Codec<A> baseCodec) {
		return new Codec<>() {
			@Override
			public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
				return baseCodec.encode(input, ops, prefix);
			}

			@Override
			public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
				Dynamic<T> dynamic2 = apply(type, new Dynamic<>(ops, input));
				return baseCodec.decode(dynamic2);
			}
		};
	}
}
