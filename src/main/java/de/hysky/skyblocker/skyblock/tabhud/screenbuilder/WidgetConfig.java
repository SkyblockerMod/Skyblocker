package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.utils.CodecUtils;

import java.util.Optional;

public record WidgetConfig(Optional<JsonObject> config, Optional<PositionRule> position) {
	public static final Codec<WidgetConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			CodecUtils.JSON_OBJECT_CODEC.optionalFieldOf("config").forGetter(WidgetConfig::config),
			PositionRule.CODEC.optionalFieldOf("position").forGetter(WidgetConfig::position)
	).apply(instance, WidgetConfig::new));

	public WidgetConfig(JsonObject config, PositionRule position) {
		this(Optional.of(config), Optional.of(position));
	}

	public static WidgetConfig disabled() {
		return new WidgetConfig(Optional.empty(), Optional.empty());
	}
}
