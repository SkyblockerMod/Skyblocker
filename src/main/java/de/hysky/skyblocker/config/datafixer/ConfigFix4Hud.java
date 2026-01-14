package de.hysky.skyblocker.config.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class ConfigFix4Hud extends ConfigDataFix {
	public ConfigFix4Hud(Schema outputSchema, boolean changesType) {
		super(outputSchema, changesType);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return fixTypeEverywhereTyped(
				"ConfigFix4Hud",
				getInputSchema().getType(ConfigDataFixer.CONFIG_TYPE),
				typed -> typed.update(DSL.remainderFinder(), this::fix)
		);
	}

	private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
		return fixVersion(dynamic).update("uiAndVisuals", uiAndVisuals -> uiAndVisuals
				.renameAndFixField("tabHud", "hud", tabHud -> tabHud
						.renameField("tabHudScale", "hudScale"))
		);
	}
}
