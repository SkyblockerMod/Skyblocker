package de.hysky.skyblocker.config.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.math.ColorHelper;

import java.util.stream.Stream;

public class ConfigFix3AnimatedDyeAndItemBackground extends ConfigDataFix {
	public ConfigFix3AnimatedDyeAndItemBackground(Schema outputSchema, boolean changesType) {
		super(outputSchema, changesType);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return fixTypeEverywhereTyped(
				"ConfigFix3AnimatedDyeAndItemBackground",
				getInputSchema().getType(ConfigDataFixer.CONFIG_TYPE),
				typed -> typed.update(DSL.remainderFinder(), this::fix)
		);
	}

	private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
		return fixVersion(dynamic).update("general", general -> general
				.update("customAnimatedDyes", customAnimatedDyes -> customAnimatedDyes
						.updateMapValues(customAnimatedDye -> customAnimatedDye.mapSecond(this::fixCustomAnimatedDye))
				)
				.update("itemInfoDisplay", itemInfoDisplay -> itemInfoDisplay
						.renameField("itemRarityBackgroundStyle", "itemBackgroundStyle")
						.renameField("itemRarityBackgroundsOpacity", "itemBackgroundOpacity")
				)
		);
	}

	private <T> Dynamic<T> fixCustomAnimatedDye(Dynamic<T> customAnimatedDye) {
		return customAnimatedDye
				.set("keyframes", customAnimatedDye.createList(Stream.of(
						customAnimatedDye.emptyMap()
								.set("color", customAnimatedDye.createInt(ColorHelper.fullAlpha(customAnimatedDye.get("color1").asInt(0))))
								.set("time", customAnimatedDye.createFloat(0)),
						customAnimatedDye.emptyMap()
								.set("color", customAnimatedDye.createInt(ColorHelper.fullAlpha(customAnimatedDye.get("color2").asInt(0))))
								.set("time", customAnimatedDye.createFloat(1))
				)))
				.remove("color1")
				.remove("color2")
				// Samples is how many steps the animation has, and tickDelay is how long each step takes in ticks.
				.set("duration", customAnimatedDye.createFloat(customAnimatedDye.get("samples").asInt(20) * customAnimatedDye.get("tickDelay").asInt(1) / 20f))
				.remove("samples")
				.remove("tickDelay");
	}
}
