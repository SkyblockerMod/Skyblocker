package de.hysky.skyblocker.config.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

import java.util.Optional;

/**
 * Separates all outputs and renames a few things.
 */
public class ConfigFix5ChatRulesSeparateOutputs extends ConfigDataFix {
	public ConfigFix5ChatRulesSeparateOutputs(Schema outputSchema, boolean changesType) {
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
		return fixVersion(dynamic).update("rules", rules -> rules.createList(rules.asStream().map(rule -> rule
				.renameField("isPartialMatch", "partialMatch")
				.renameField("isRegex", "regex")
				.renameField("isIgnoreCase", "ignoreCase")
				.renameField("validLocations", "locations")
				.renameField("hideMessage", "hideOriginalMessage")
				.renameField("replaceMessage", "chatMessage")
				.setFieldIfPresent("actionBarMessage", rule.get("showActionBar").result().flatMap(dynamic1 ->
					dynamic1.asBoolean(false) ? rule.get("replaceMessage").result() : Optional.empty()
				))
				.remove("showActionBar")
				.setFieldIfPresent("announcementMessage", rule.get("showAnnouncement").result().flatMap(dynamic1 ->
					dynamic1.asBoolean(false) ? rule.get("replaceMessage").result() : Optional.empty()
				))
				.remove("showAnnouncement")
		)));
	}
}
