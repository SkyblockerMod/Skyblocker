package de.hysky.skyblocker.config.datafixer;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import org.jspecify.annotations.Nullable;

// I doubt this is how it's meant to be done, but I really don't see another way.
public class ConfigFix12MoveWidgetOptions extends ConfigDataFix {
	private final Map<String, SubFixer> widgetToSubfixer = Map.of(
			"hud_end", new SubFixer(
					dynamic -> dynamic.get("otherLocations").result().flatMap(otherLocations -> otherLocations.get("end").result()),
					this::fixEnd
			),
			"hud_farming", new SubFixer(
					dynamic -> dynamic.get("farming").result().flatMap(otherLocations -> otherLocations.get("farmingHud").result()),
					this::fixFarming
			),
			"hud_fishing", new SubFixer(
					dynamic -> dynamic.get("helpers").result().flatMap(otherLocations -> otherLocations.get("fishing").result()),
					this::fixFishing
			),
			"item_pickup", new SubFixer(
					dynamic -> dynamic.get("uiAndVisuals").result().flatMap(otherLocations -> otherLocations.get("itemPickup").result()),
					this::fixItemPickup
			),
			"players", new SubFixer(
					dynamic -> dynamic.get("uiAndVisuals").result().flatMap(uiAndVisuals -> uiAndVisuals.get("tabHud").result()),
					this::fixPlayerList
			)
	);

	public ConfigFix12MoveWidgetOptions(Schema outputSchema, boolean changesType) {
		super(outputSchema, changesType);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return TypeRewriteRule.seq(
				fixTypeEverywhereTyped(
						getClass().getSimpleName(),
						getInputSchema().getType(ConfigDataFixer.CONFIG_TYPE),
						typed -> typed.update(DSL.remainderFinder(), dynamic -> {
							collect(dynamic);
							return dynamic;
						})
				),
				fixTypeEverywhereTyped(
						getClass().getSimpleName(),
						getInputSchema().getType(ConfigDataFixer.HUD_WIDGETS_TYPE),
						typed -> typed.update(DSL.remainderFinder(), this::fix)
				)
		);
	}

	private void collect(Dynamic<?> dynamic) {
		System.out.println("Fetching DATA");
		for (SubFixer fixer : widgetToSubfixer.values()) {
			fixer.fetchData(dynamic);
		}
	}

	private Dynamic<?> fix(Dynamic<?> dynamic) {
		Dynamic<?> updated = fixVersion(dynamic).update(
				"configs",
				configs -> configs.updateMapValues(location -> Pair.of(
						location.getFirst(),
						location.getSecond().updateMapValues(layer -> Pair.of(
								layer.getFirst(),
								layer.getSecond().update("widgets", widgets -> widgets.updateMapValues(this::fixWidget)))))));
		for (SubFixer fixer : widgetToSubfixer.values()) fixer.data = null;
		return updated;
	}

	private static Dynamic<?> lowercase(Dynamic<?> dynamic) {
		Optional<Dynamic<?>> lowercase = dynamic.asString().result().map(s -> s.toLowerCase(Locale.ENGLISH)).map(dynamic::createString);
		return lowercase.orElse(dynamic);
	}

	private Pair<Dynamic<?>, Dynamic<?>> fixWidget(Pair<Dynamic<?>, Dynamic<?>> widget) {
		String widgetId = widget.getFirst().asString("");
		SubFixer fixer = widgetToSubfixer.get(widgetId);
		if (fixer == null) return widget;
		return Pair.of(widget.getFirst(), widget.getSecond().update("config", fixer::tryFix));
	}

	private Dynamic<?> fixEnd(Dynamic<?> widgetData, Dynamic<?> previous) {
		return widgetData
				.setFieldIfPresent("zealot_kills", previous.get("zealotKillsEnabled").result())
				.setFieldIfPresent("protector_location", previous.get("protectorLocationEnabled").result());
	}

	private Dynamic<?> fixFarming(Dynamic<?> widgetData, Dynamic<?> previous) {
		return widgetData
				.setFieldIfPresent("counter", previous.get("counter").result())
				.setFieldIfPresent("coins", previous.get("coins").result())
				.setFieldIfPresent("price_type", previous.get("type").map(ConfigFix12MoveWidgetOptions::lowercase).result())
				.setFieldIfPresent("experience", previous.get("experience").result())
				.setFieldIfPresent("include_seeds_price", previous.get("includeSeedsPrice").result());
	}

	private Dynamic<?> fixFishing(Dynamic<?> widgetData, Dynamic<?> previous) {
		return widgetData
				.setFieldIfPresent("creature_counter", previous.get("enableSeaCreatureCounter").result())
				.setFieldIfPresent("fishing_timer", previous.get("enableFishingTimer").result())
				.setFieldIfPresent("only_barn", previous.get("onlyShowHudInBarn").result());
	}

	private Dynamic<?> fixItemPickup(Dynamic<?> widgetData, Dynamic<?> previous) {
		return widgetData
				.setFieldIfPresent("sack_notifications", previous.get("sackNotifications").result())
				.setFieldIfPresent("split_sack", previous.get("splitNotifications").result())
				.setFieldIfPresent("show_item_name", previous.get("showItemName").result())
				.setFieldIfPresent("lifetime", previous.get("lifetime").result());
	}

	private Dynamic<?> fixPlayerList(Dynamic<?> widgetData, Dynamic<?> previous) {
		return widgetData.setFieldIfPresent("name_sorting", previous.get("nameSorting").map(ConfigFix12MoveWidgetOptions::lowercase).result());
	}

	private static class SubFixer {
		private final Function<Dynamic<?>, Optional<Dynamic<?>>> previousDataSupplier;
		private final BiFunction<Dynamic<?>, Dynamic<?>, Dynamic<?>> fixer;
		private @Nullable Dynamic<?> data;

		private SubFixer(Function<Dynamic<?>, Optional<Dynamic<?>>> previousDataSupplier, BiFunction<Dynamic<?>, Dynamic<?>, Dynamic<?>> fixer) {
			this.previousDataSupplier = previousDataSupplier;
			this.fixer = fixer;
		}

		private Dynamic<?> tryFix(Dynamic<?> widget) {
			if (data == null) return widget;
			return fixer.apply(widget, data);
		}

		private void fetchData(Dynamic<?> dynamic) {
			data = previousDataSupplier.apply(dynamic).orElse(null);
		}
	}
}
