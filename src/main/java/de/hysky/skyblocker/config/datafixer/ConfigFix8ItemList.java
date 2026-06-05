package de.hysky.skyblocker.config.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class ConfigFix8ItemList extends ConfigDataFix {
	public ConfigFix8ItemList(Schema outputSchema, boolean changesType) {
		super(outputSchema, changesType);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return fixTypeEverywhereTyped(
				getClass().getSimpleName(),
				getInputSchema().getType(ConfigDataFixer.CONFIG_TYPE),
				typed -> typed.update(DSL.remainderFinder(), this::fix)
		);
	}

	private Dynamic<?> fix(Dynamic<?> dynamic) {
		return fixVersion(dynamic).update("general", general -> general
				.update("itemList", itemList ->
						itemList.setFieldIfPresent("enableRecipeBook", itemList.get("enableItemList").result())
				)
		);
	}
}
