package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

public record ScreenBuilder(LayerBuilder hud, LayerBuilder tab, LayerBuilder secondaryTab) {
	public LayerBuilder get(WidgetManager.ScreenLayer layer) {
		return switch (layer) {
			case HUD, DEFAULT -> hud;
			case MAIN_TAB ->  tab;
			case SECONDARY_TAB -> secondaryTab;
		};
	}
}
