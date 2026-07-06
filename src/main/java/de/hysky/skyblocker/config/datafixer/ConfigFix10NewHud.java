package de.hysky.skyblocker.config.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

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
		return fixVersion(dynamic).renameAndFixField(
				"positions",
				"configs",
				fixWidgets()
		).set("copies", dynamic.createMap(Map.of(
				dynamic.createString("hud"), dynamic.emptyMap(),
				dynamic.createString("tab"), dynamic.emptyMap(),
				dynamic.createString("secondary_tab"), dynamic.emptyMap()
		)));
	}

	/**
	 * Fixes the map of skyblock locations to widgets.
	 */
	private static UnaryOperator<Dynamic<?>> fixWidgets() {
		return locations -> locations.updateMapValues(ConfigFix10NewHud::fixWidgetsForLocation);
	}

	/**
	 * Fixes widgets in this skyblock location and returns a map of layers to widgets.
	 */
	private static Pair<Dynamic<?>, Dynamic<?>> fixWidgetsForLocation(Pair<Dynamic<?>, Dynamic<?>> location) {
		Dynamic<?> locationId = location.getFirst();
		Map<Dynamic<?>, Dynamic<?>> layers = new HashMap<>(Map.of(
				locationId.createString("hud"), locationId.emptyMap(),
				locationId.createString("tab"), locationId.emptyMap(),
				locationId.createString("secondary_tab"), locationId.emptyMap()
		));
		location.getSecond().getMapValues().getOrThrow().forEach((widgetId, widget) -> fixWidgetAndLayer(fixWidgetId(widgetId), widget, layers));
		layers.replaceAll((_, widgets) -> locationId.emptyMap().set("widgets", widgets));
		return Pair.of(locationId, locationId.createMap(layers));
	}

	private static void fixWidgetAndLayer(Dynamic<?> widgetIdNew, Dynamic<?> widget, Map<Dynamic<?>, Dynamic<?>> layers) {
		layers.computeIfPresent(
				fixWidgetLayer(widget, widgetIdNew.asString("")),
				(_, widgets) -> widgets.set(widgetIdNew.asString(""), fixWidget(widget))
		);
	}

	private static Dynamic<?> fixWidgetId(Dynamic<?> widgetId) {
		return switch (widgetId.asString("")) {
			case "sweepDetails" -> widgetId.createString("sweep_details");
			case "Lasso HUD" -> widgetId.createString("hud_lasso");
			case "Dungeon Splits" -> widgetId.createString("dungeon_splits");
			case "Item Pickup" -> widgetId.createString("item_pickup");
			default -> widgetId;
		};
	}

	/**
	 * Returns the layer the widget should be on.
	 */
	private static Dynamic<?> fixWidgetLayer(Dynamic<?> widget, String widgetId) {
		String layer = switch (widget.get("layer").asString("DEFAULT")) {
			case "HUD" -> "hud";
			case "MAIN_TAB" -> "tab";
			case "SECONDARY_TAB" -> "secondary_tab";
			default -> widgetId.contains("hud") || widgetId.equals("sweep_details") || widgetId.equals("powder_mining_tracker") || widgetId.equals("dungeon_splits") || widgetId.equals("item_pickup") ? "hud" : "tab";
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
