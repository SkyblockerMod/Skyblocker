package de.hysky.skyblocker.config.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

/**
 * Transform the chat rules file from a json list to a json object containing a list.
 */
public class ConfigFix4ChatRulesObject extends ConfigDataFix {
	public ConfigFix4ChatRulesObject(Schema outputSchema, boolean changesType) {
		super(outputSchema, changesType);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return fixTypeEverywhereTyped(
				getClass().getSimpleName(),
				getInputSchema().getType(ConfigDataFixer.CHAT_RULES_TYPE),
				typed -> typed.update(DSL.remainderFinder(), this::fix)
		);
	}

	private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
		if (dynamic.asStreamOpt().result().isPresent()) {
			dynamic = dynamic.emptyMap().set("rules", dynamic);
		}
		return fixVersion(dynamic);
	}
}
