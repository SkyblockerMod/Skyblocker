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
		return fixVersion(dynamic).renameAndFixField("positions", "configs", positions -> positions.updateMapValues(pair -> {
			Map<Dynamic<?>, Dynamic<?>> layers = new HashMap<>(Map.of(
					dynamic.createString("hud"), dynamic.emptyMap(),
					dynamic.createString("tab"), dynamic.emptyMap(),
					dynamic.createString("secondary_tab"), dynamic.emptyMap()
			));
			Map<String, Dynamic<?>> oldLayerToNewLayer = Map.of(
					"HUD", dynamic.createString("hud"),
					"MAIN_TAB", dynamic.createString("tab"),
					"SECONDARY_TAB", dynamic.createString("secondary_tab"),
					"DEFAULT", dynamic.createString("tab")
			);
			pair.getSecond().getMapValues().getOrThrow().forEach((key, widget) -> layers.computeIfPresent(
					oldLayerToNewLayer.get(widget.get("layer").asString("DEFAULT")),
					(_, widgets) -> widgets.set(key.asString(""), dynamic.emptyMap().set("config", dynamic.emptyMap()).set("position", widget
							.remove("layer")
							.remove("parent")
							.setFieldIfPresent("parent", widget.get("parent").asString("screen").equals("screen") ? Optional.empty() : widget.get("parent").result())))));
			layers.replaceAll((_, widgets) -> dynamic.emptyMap().set("widgets", widgets));
			return Pair.of(pair.getFirst(), dynamic.createMap(layers));
		})).set("copies", dynamic.createMap(Map.of(
				dynamic.createString("hud"), dynamic.emptyMap(),
				dynamic.createString("tab"), dynamic.emptyMap(),
				dynamic.createString("secondary_tab"), dynamic.emptyMap()
		)));
	}
}
