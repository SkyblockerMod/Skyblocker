package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Set;
import java.util.stream.Stream;

public record ScreenConfig(LayerConfig hud, LayerConfig tab, LayerConfig secondaryTab, Set<String> hiddenTabWidgets) {
	public static final ScreenConfig DUMMY = new ScreenConfig(new LayerConfig(), new LayerConfig(), new LayerConfig());
	public static final Codec<ScreenConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			LayerConfig.CODEC.fieldOf("hud").forGetter(ScreenConfig::hud),
			LayerConfig.CODEC.fieldOf("tab").forGetter(ScreenConfig::tab),
			LayerConfig.CODEC.fieldOf("secondary_tab").forGetter(ScreenConfig::secondaryTab)
	).apply(instance, ScreenConfig::new));

	public ScreenConfig(LayerConfig hud, LayerConfig tab, LayerConfig secondaryTab) {
		this(hud, tab, secondaryTab, new ObjectOpenHashSet<>());
	}

	public ScreenConfig() {
		this(new LayerConfig(), new LayerConfig(), new LayerConfig());
	}

	public LayerConfig get(WidgetManager.ScreenLayer layer) {
		return switch (layer) {
			case HUD -> hud();
			case MAIN_TAB ->  tab();
			case SECONDARY_TAB -> secondaryTab();
		};
	}

	public Stream<LayerConfig> allLayers() {
		return Stream.of(hud, tab, secondaryTab);
	}
}
