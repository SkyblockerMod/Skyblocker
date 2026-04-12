package de.hysky.skyblocker.config.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class ConfigFix7Farming extends ConfigDataFix {
	public ConfigFix7Farming(Schema outputSchema, boolean changesType) {
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
		return fixVersion(dynamic).update("farming", farming -> farming
				.set("farmingHud", dynamic.emptyMap()
						.setFieldIfPresent("enabled", farming.get("garden").get("farmingHud").get("enableHud").result())
						.setFieldIfPresent("type", farming.get("garden").get("farmingHud").get("type").result())
				).set("pestHighlighter", dynamic.emptyMap()
						.setFieldIfPresent("enabled", farming.get("garden").get("pestHighlighter").result())
						.setFieldIfPresent("vinylHighlighter", farming.get("garden").get("vinylHighlighter").result())
						.setFieldIfPresent("enableStereoHarmonyHelperForContest", farming.get("garden").get("enableStereoHarmonyHelperForContest").result())
				).set("mouseLock", dynamic.emptyMap()
						.setFieldIfPresent("lockMouseTool", farming.get("garden").get("lockMouseTool").result())
						.setFieldIfPresent("lockMouseGroundOnly", farming.get("garden").get("lockMouseGroundOnly").result())
				).set("plotsWidget", dynamic.emptyMap()
						.setFieldIfPresent("enabled", farming.get("garden").get("gardenPlotsWidget").result())
						.setFieldIfPresent("closeScreenOnPlotClick", farming.get("garden").get("closeScreenOnPlotClick").result())
				).update("visitorHelper", visitorHelper -> visitorHelper
						.renameField("visitorHelper", "enabled")
						.renameField("visitorHelperGardenOnly", "showInGardenOnly")
						.renameField("showStacksInVisitorHelper", "showInStacks")
				).remove("garden")
		);
	}
}
