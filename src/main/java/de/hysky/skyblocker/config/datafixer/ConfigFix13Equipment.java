package de.hysky.skyblocker.config.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class ConfigFix13Equipment extends ConfigDataFix {
	public ConfigFix13Equipment(Schema outputSchema, boolean changesType) {
		super(outputSchema, changesType);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return fixTypeEverywhereTyped(
				"ConfigFix13Equipment",
				getInputSchema().getType(ConfigDataFixer.CONFIG_TYPE),
				typed -> typed.update(DSL.remainderFinder(), this::fix)
		);
	}

	private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
		return fixVersion(dynamic).
				update("uiAndVisuals", uiAndVisuals -> uiAndVisuals
						.update("skyblockInventoryScreen", inventoryScreen -> inventoryScreen
										.setFieldIfPresent("showEquipmentInInventory", uiAndVisuals.get("showEquipmentInInventory").result())
										.renameAndFixField("openEquipmentToStatsPage", "equipmentCommand", openToStats -> openToStats.createString(openToStats.asBoolean(false) ? "STATS" : "EQUIPMENT")))
						.remove("showEquipmentInInventory")
				);
	}
}
