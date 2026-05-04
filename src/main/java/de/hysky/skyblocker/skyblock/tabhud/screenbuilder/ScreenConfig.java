package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Set;
import java.util.stream.Stream;

public class ScreenConfig {
	public static final ScreenConfig DUMMY = new ScreenConfig(new LayerConfig(), new LayerConfig(), new LayerConfig());
	public static final Codec<ScreenConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			LayerConfig.CODEC.fieldOf("hud").forGetter(ScreenConfig::hud),
			LayerConfig.CODEC.fieldOf("tab").forGetter(ScreenConfig::tab),
			LayerConfig.CODEC.fieldOf("secondary_tab").forGetter(ScreenConfig::secondaryTab)
	).apply(instance, ScreenConfig::new));

	private final LayerConfig hud;
	private final LayerConfig tab;
	private final LayerConfig secondaryTab;
	public final Set<String> hiddenTabWidgets = new ObjectOpenHashSet<>();

	public ScreenConfig(LayerConfig hud, LayerConfig tab, LayerConfig secondaryTab) {
		this.hud = hud;
		this.tab = tab;
		this.secondaryTab = secondaryTab;
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

	public LayerConfig hud() {
		return hud;
	}

	public LayerConfig tab() {
		return tab;
	}

	public LayerConfig secondaryTab() {
		return secondaryTab;
	}

	public Stream<LayerConfig> allLayers() {
		return Stream.of(hud, tab, secondaryTab);
	}
}
