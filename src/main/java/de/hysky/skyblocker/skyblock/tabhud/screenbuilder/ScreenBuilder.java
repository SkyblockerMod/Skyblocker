package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

public final class ScreenBuilder {
	private final LayerBuilder hud;
	private final LayerBuilder tab;
	private final LayerBuilder secondaryTab;
	private ScreenConfig config = ScreenConfig.DUMMY;

	public ScreenBuilder(LayerBuilder hud, LayerBuilder tab, LayerBuilder secondaryTab) {
		this.hud = hud;
		this.tab = tab;
		this.secondaryTab = secondaryTab;
	}

	public ScreenBuilder() {
		this(new LayerBuilder(), new LayerBuilder(), new LayerBuilder());
	}

	public LayerBuilder get(WidgetManager.ScreenLayer layer) {
		return switch (layer) {
			case HUD, DEFAULT -> hud;
			case MAIN_TAB -> tab;
			case SECONDARY_TAB -> secondaryTab;
		};
	}

	public LayerBuilder hud() {
		return hud;
	}

	public LayerBuilder tab() {
		return tab;
	}

	public LayerBuilder secondaryTab() {
		return secondaryTab;
	}

	public void setConfig(ScreenConfig config) {
		this.config = config;
	}
}
