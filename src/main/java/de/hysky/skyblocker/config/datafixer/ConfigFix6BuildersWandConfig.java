package de.hysky.skyblocker.config.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class ConfigFix6BuildersWandConfig extends ConfigDataFix {
	public ConfigFix6BuildersWandConfig(Schema outputSchema, boolean changesType) {
		super(outputSchema, changesType);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return fixTypeEverywhereTyped(
				"ConfigFix6BuildersWandConfig",
				getInputSchema().getType(ConfigDataFixer.CONFIG_TYPE),
				typed -> typed.update(DSL.remainderFinder(), this::fix)
		);
	}

	private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
		return fixVersion(dynamic).update("helpers", helpers -> helpers.set("buildersWand", dynamic.emptyMap().setFieldIfPresent("enableBuildersWandPreview", helpers.get("enableBuildersWandPreview").result())));
	}
}
