package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

public record LayerConfig(Map<String, WidgetConfig> widgets) {
	public static final LayerConfig DUMMY = new LayerConfig();
	public static final Codec<LayerConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, WidgetConfig.CODEC).fieldOf("widgets").forGetter(c -> c.widgets)
	).apply(instance, LayerConfig::new));

	public LayerConfig(Map<String, WidgetConfig> widgets) {
		this.widgets = new Object2ObjectOpenHashMap<>(widgets);
	}

	public LayerConfig() {
		this(new Object2ObjectOpenHashMap<>());
	}
}
