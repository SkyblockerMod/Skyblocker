package de.hysky.skyblocker.config.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;

import java.util.Map;
import java.util.function.Function;

public class ConfigFix8EventNotifications extends ConfigDataFix {
	public ConfigFix8EventNotifications(Schema outputSchema, boolean changesType) {
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

	private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
		return fixVersion(dynamic).update("eventNotifications", eventNotifications -> eventNotifications.renameAndFixField("eventsReminderTimes", "events", events -> events.updateMapValues(pair ->
				Pair.of(pair.getFirst(), dynamic.createMap(Map.of(
								dynamic.createString("enabled"), dynamic.createBoolean(!pair.getSecond().asList(Function.identity()).isEmpty()),
								dynamic.createString("reminderTimes"), pair.getSecond()
						))
				))));
	}
}
