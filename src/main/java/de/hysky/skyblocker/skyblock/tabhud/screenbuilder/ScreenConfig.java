package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public record ScreenConfig(LayerConfig hud, LayerConfig tab, LayerConfig secondaryTab, Set<String> hiddenTabWidgets) {
	public static final ScreenConfig DUMMY = new ScreenConfig();
	public static final Codec<ScreenConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			LayerConfig.CODEC.fieldOf("hud").forGetter(ScreenConfig::hud),
			LayerConfig.CODEC.fieldOf("tab").forGetter(ScreenConfig::tab),
			LayerConfig.CODEC.fieldOf("secondary_tab").forGetter(ScreenConfig::secondaryTab),
			// Must be optional for backwards compatibility with configs that did not have this
			Codec.STRING.listOf().optionalFieldOf("hidden_tab_widgets", List.of()).<Set<String>>xmap(ObjectOpenHashSet::new, List::copyOf).forGetter(ScreenConfig::hiddenTabWidgets)
	).apply(instance, ScreenConfig::new));

	public ScreenConfig() {
		this(new LayerConfig(), new LayerConfig(), new LayerConfig(), new ObjectOpenHashSet<>());
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
