package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;

public class ScreenBuilder {
	private final LayerBuilder hud;
	private final TabLayerBuilder tab;
	private final LayerBuilder secondaryTab;
	private ScreenConfig config = ScreenConfig.DUMMY;

	protected ScreenBuilder(LayerBuilder hud, TabLayerBuilder tab, LayerBuilder secondaryTab) {
		this.hud = hud;
		this.tab = tab;
		this.secondaryTab = secondaryTab;
	}

	public ScreenBuilder() {
		this(new LayerBuilder(), new TabLayerBuilder(), new LayerBuilder());
	}

	public LayerBuilder get(WidgetManager.ScreenLayer layer) {
		return switch (layer) {
			case HUD -> hud;
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

	public void updateFancyTab() {
		tab.updateTab(config.hiddenTabWidgets());
	}

	public void clearFancyTab() {
		tab.clearTab();
	}

	public void setConfig(ScreenConfig config) {
		this.config = config;
		hud.setConfig(config.hud());
		tab.setConfig(config.tab());
		secondaryTab.setConfig(config.secondaryTab());
	}

	public boolean contains(HudWidget widget) {
		return hud.contains(widget) || tab.contains(widget) || secondaryTab.contains(widget);
	}
}
