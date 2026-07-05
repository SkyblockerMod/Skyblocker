package de.hysky.skyblocker.config.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConfigFix10NewHud extends ConfigDataFix {
	public ConfigFix10NewHud(Schema outputSchema, boolean changesType) {
		super(outputSchema, changesType);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return fixTypeEverywhereTyped(
				getClass().getSimpleName(),
				getInputSchema().getType(ConfigDataFixer.HUD_WIDGETS_TYPE),
				typed -> typed.update(DSL.remainderFinder(), this::fix)
		);
	}

	private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
		return fixVersion(dynamic).renameAndFixField("positions", "configs", locations -> locations.updateMapValues(location -> {
			Map<Dynamic<?>, Dynamic<?>> layers = new HashMap<>(Map.of(
					dynamic.createString("hud"), dynamic.emptyMap(),
					dynamic.createString("tab"), dynamic.emptyMap(),
					dynamic.createString("secondary_tab"), dynamic.emptyMap()
			));
			location.getSecond().getMapValues().getOrThrow().forEach((widgetId, widget) -> layers.computeIfPresent(
					fixWidgetLayer(widget, widgetId.asString("")),
					(_, widgets) -> widgets.set(widgetId.asString(""), fixWidget(widget))
			));
			layers.replaceAll((_, widgets) -> dynamic.emptyMap().set("widgets", widgets));
			return Pair.of(location.getFirst(), dynamic.createMap(layers));
		})).set("copies", dynamic.createMap(Map.of(
				dynamic.createString("hud"), dynamic.emptyMap(),
				dynamic.createString("tab"), dynamic.emptyMap(),
				dynamic.createString("secondary_tab"), dynamic.emptyMap()
		)));
	}

	/**
	 * Returns the layer the widget should be on.
	 */
	private static Dynamic<?> fixWidgetLayer(Dynamic<?> widget, String widgetId) {
		String layer = switch (widget.get("layer").asString("DEFAULT")) {
			case "HUD" -> "hud";
			case "MAIN_TAB" -> "tab";
			case "SECONDARY_TAB" -> "secondary_tab";
			default -> widgetId.contains("hud") ? "hud" : "tab";
		};
		return widget.createString(layer);
	}

	private static Dynamic<?> fixWidget(Dynamic<?> widget) {
		return widget.emptyMap().set("config", widget.emptyMap()).set("position", widget
				.remove("layer")
				.remove("parent")
				.setFieldIfPresent("parent", fixWidgetParent(widget))
		);
	}

	/**
	 * Returns the parent of the widget or empty if the parent is the screen.
	 */
	private static Optional<? extends Dynamic<?>> fixWidgetParent(Dynamic<?> widget) {
		return widget.get("parent").asString("screen").equals("screen") ? Optional.empty() : widget.get("parent").result();
	}
}
