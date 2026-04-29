package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;

public class ConfigScreenBuilder extends ScreenBuilder {
	public ConfigScreenBuilder() {
		super(new ConfigLayerBuilder(), new ConfigLayerBuilder(), new ConfigLayerBuilder());
	}

	@Override
	public ConfigLayerBuilder hud() {
		return (ConfigLayerBuilder) super.hud();
	}

	@Override
	public ConfigLayerBuilder tab() {
		return (ConfigLayerBuilder) super.tab();
	}

	@Override
	public ConfigLayerBuilder secondaryTab() {
		return (ConfigLayerBuilder) super.secondaryTab();
	}

	@Override
	public ConfigLayerBuilder get(WidgetManager.ScreenLayer layer) {
		return (ConfigLayerBuilder) super.get(layer);
	}
}
