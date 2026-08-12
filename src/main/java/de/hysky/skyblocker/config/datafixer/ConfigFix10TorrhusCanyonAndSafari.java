package de.hysky.skyblocker.config.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class ConfigFix10TorrhusCanyonAndSafari extends ConfigDataFix {
	public ConfigFix10TorrhusCanyonAndSafari(Schema outputSchema, boolean changesType) {
		super(outputSchema, changesType);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
				this.getClass().getSimpleName(),
				this.getInputSchema().getType(ConfigDataFixer.CONFIG_TYPE),
				this.getOutputSchema().getType(ConfigDataFixer.CONFIG_TYPE),
				typed -> typed.update(DSL.remainderFinder(), this::fix)
				);
	}

	private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
		dynamic = this.fixVersion(dynamic);
		dynamic = dynamic.update("foraging", foragingDynamic -> foragingDynamic.renameField("galatea", "moongladeMarsh"));
		dynamic = dynamic.update("hunting", huntingDynamic -> huntingDynamic.renameField("huntingMobs", "moongladeMobs"));

		return dynamic;
	}
}
