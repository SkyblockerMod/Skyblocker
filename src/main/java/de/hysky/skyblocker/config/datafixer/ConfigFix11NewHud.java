package de.hysky.skyblocker.config.datafixer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import org.jspecify.annotations.Nullable;

public class ConfigFix11NewHud extends ConfigDataFix {
	public ConfigFix11NewHud(Schema outputSchema, boolean changesType) {
		super(outputSchema, changesType);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return TypeRewriteRule.seq(fixTypeEverywhereTyped(
						getClass().getSimpleName(),
						getInputSchema().getType(ConfigDataFixer.CONFIG_TYPE),
						typed -> typed.update(DSL.remainderFinder(), this::collect)),
				fixTypeEverywhereTyped(
						getClass().getSimpleName(),
						getInputSchema().getType(ConfigDataFixer.HUD_WIDGETS_TYPE),
						typed -> typed.update(DSL.remainderFinder(), this::fix))
		);
	}

	private @Nullable Dynamic<?> mainConfig;

	private <T> Dynamic<T> collect(Dynamic<T> dynamic) {
		mainConfig = dynamic;
		return dynamic;
	}

	private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
		Dynamic<T> updated = fixVersion(dynamic).renameAndFixField(
				"positions",
				"configs",
				fixWidgets()
		).set("copies", dynamic.createMap(Map.of(
				dynamic.createString("hud"), dynamic.emptyMap(),
				dynamic.createString("tab"), dynamic.emptyMap(),
				dynamic.createString("secondary_tab"), dynamic.emptyMap()
		)));
		mainConfig = null;
		return updated;
	}

	/**
	 * Fixes the map of skyblock locations to widgets.
	 */
	private <T> UnaryOperator<Dynamic<? extends T>> fixWidgets() {
		return locations -> locations.updateMapValues(this::fixWidgetsForLocation);
	}

	/**
	 * Fixes widgets in this skyblock location and returns a map of layers to widgets.
	 */
	private Pair<Dynamic<?>, Dynamic<?>> fixWidgetsForLocation(Pair<Dynamic<?>, Dynamic<?>> location) {
		Dynamic<?> locationId = location.getFirst();
		Map<Dynamic<?>, Dynamic<?>> layers = new HashMap<>(Map.of(
				locationId.createString("hud"), locationId.emptyMap(),
				locationId.createString("tab"), locationId.emptyMap(),
				locationId.createString("secondary_tab"), locationId.emptyMap()
		));
		location.getSecond().getMapValues().getOrThrow().forEach((widgetId, widget) -> fixWidgetAndLayer(fixWidgetId(widgetId).asString(""), widget, layers));
		layers.replaceAll((_, widgets) -> locationId.emptyMap().set("widgets", widgets));
		return Pair.of(locationId, locationId.createMap(layers));
	}

	private <T> void fixWidgetAndLayer(String widgetIdNew, Dynamic<T> widget, Map<Dynamic<?>, Dynamic<?>> layers) {
		if (!isEnabled(widgetIdNew)) return;
		layers.computeIfPresent(
				fixWidgetLayer(widget, widgetIdNew),
				(_, widgets) -> widgets.set(widgetIdNew, fixWidget(widget))
		);
	}

	private static <T> Dynamic<T> fixWidgetId(Dynamic<T> widgetId) {
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
	private static <T> Dynamic<T> fixWidgetLayer(Dynamic<T> widget, String widgetId) {
		String layer = switch (widget.get("layer").asString("DEFAULT")) {
			case "HUD" -> "hud";
			case "MAIN_TAB" -> "tab";
			case "SECONDARY_TAB" -> "secondary_tab";
			default -> widgetId.contains("hud") || widgetId.equals("sweep_details") || widgetId.equals("powder_mining_tracker") || widgetId.equals("dungeon_splits") || widgetId.equals("item_pickup") ? "hud" : "tab";
		};
		return widget.createString(layer);
	}

	private static <T> Dynamic<T> fixWidget(Dynamic<T> widget) {
		return widget.emptyMap().set("config", widget.emptyMap()).set("position", widget
				.remove("layer")
				.replaceField("parent", "parent", fixWidgetParent(widget))
		);
	}

	/**
	 * Returns the parent of the widget or empty if the parent is the screen.
	 */
	private static <T> Optional<? extends Dynamic<T>> fixWidgetParent(Dynamic<T> widget) {
		return widget.get("parent").asString("screen").equals("screen") ? Optional.empty() : widget.get("parent").map(ConfigFix11NewHud::fixWidgetId).result();
	}

	private boolean isEnabled(String widgetId) {
		if (mainConfig == null) return true;
		return switch (widgetId) {
			case "hud_farming" -> mainConfig.get("farming").get("farmingHud").get("enabled").asBoolean(true);
			case "hud_treeprogress" -> mainConfig.get("foraging").get("moongladeMarsh").get("enableTreeBreakProgress").asBoolean(true);
			case "sweep_details" -> mainConfig.get("foraging").get("moongladeMarsh").get("enableSweepDetailsWidget").asBoolean(true);
			case "hud_fishing" -> mainConfig.get("helpers").get("fishing").get("enableFishingHud").asBoolean(true);
			case "hud_lasso" -> mainConfig.get("hunting").get("lassoHud").get("enabled").asBoolean(true);
			case "hud_crystals" -> mainConfig.get("mining").get("crystalsHud").get("enabled").asBoolean(true);
			case "hud_end" -> mainConfig.get("otherLocations").get("end").get("hudEnabled").asBoolean(true);
			case "hud_slayer" -> mainConfig.get("slayers").get("enableHud").asBoolean(true);
			case "dungeon_splits" -> mainConfig.get("dungeons").get("dungeonSplits").asBoolean(true);
			default -> true;
		};
	}
}
