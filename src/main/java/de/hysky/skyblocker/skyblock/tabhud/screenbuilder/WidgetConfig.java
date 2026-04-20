package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;

import java.util.Optional;

public record WidgetConfig(Optional<JsonObject> config, Optional<PositionRule> position) {

	public WidgetConfig(JsonObject config, PositionRule position) {
		this(Optional.of(config), Optional.of(position));
	}

	public record Meta(Optional<ScreenId> overrides, Optional<ScreenId> inheritedFrom, WidgetConfig widgetConfig) {
		public Meta getWithInheritedFrom(ScreenId screenId) {
			return new Meta(overrides, Optional.of(inheritedFrom.orElse(screenId)), widgetConfig);
		}
	}
}
