package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import de.hysky.skyblocker.skyblock.foraging.SweepOverlay;
import de.hysky.skyblocker.skyblock.galatea.SeaLumiesHighlighter;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.text.Text;

import java.awt.*;

public class ForagingCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.name(Text.translatable("skyblocker.config.foraging"))

				//Galatea
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.foraging.galatea"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.galatea.enableForestNodeHelper"))
								.binding(defaults.foraging.galatea.enableForestNodeHelper,
										() -> config.foraging.galatea.enableForestNodeHelper,
										newValue -> config.foraging.galatea.enableForestNodeHelper = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.galatea.solveForestTemplePuzzle"))
								.description(OptionDescription.of(Text.translatable("skyblocker.config.foraging.galatea.solveForestTemplePuzzle.@Tooltip")))
								.binding(defaults.foraging.galatea.solveForestTemplePuzzle,
										() -> config.foraging.galatea.solveForestTemplePuzzle,
										newValue -> config.foraging.galatea.solveForestTemplePuzzle = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.galatea.enableLushlilacHighlighter"))
								.description(OptionDescription.of(Text.translatable("skyblocker.config.foraging.galatea.enableLushlilacHighlighter.@Tooltip")))
								.binding(defaults.foraging.galatea.enableLushlilacHighlighter,
										() -> config.foraging.galatea.enableLushlilacHighlighter,
										newValue -> config.foraging.galatea.enableLushlilacHighlighter = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.galatea.enableSeaLumiesHighlighter"))
								.description(OptionDescription.of(Text.translatable("skyblocker.config.foraging.galatea.enableSeaLumiesHighlighter.@Tooltip")))
								.binding(defaults.foraging.galatea.enableSeaLumiesHighlighter,
										() -> config.foraging.galatea.enableSeaLumiesHighlighter,
										newValue -> {
											config.foraging.galatea.enableSeaLumiesHighlighter = newValue;
											SeaLumiesHighlighter.INSTANCE.configCallback();
										})
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.galatea.seaLumieMinCount"))
								.description(OptionDescription.of(Text.translatable("skyblocker.config.foraging.galatea.seaLumieMinCount.@Tooltip")))
								.binding(defaults.foraging.galatea.seaLumiesMinimumCount,
										() -> config.foraging.galatea.seaLumiesMinimumCount,
										newValue -> {
											config.foraging.galatea.seaLumiesMinimumCount = newValue;
											SeaLumiesHighlighter.INSTANCE.configCallback();
										})
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 4).step(1))
								.build())
						.build())
				//Sweep Overlays
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.foraging.sweepOverlay"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.sweepOverlay.enableSweepOverlay"))
								.description(OptionDescription.of(Text.translatable("skyblocker.config.foraging.sweepOverlay.enableSweepOverlay.@Tooltip")))
								.binding(defaults.foraging.sweepOverlay.enableSweepOverlay,
										() -> config.foraging.sweepOverlay.enableSweepOverlay,
										newValue -> config.foraging.sweepOverlay.enableSweepOverlay = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.sweepOverlay.enableThrownAbilityOverlay"))
								.binding(defaults.foraging.sweepOverlay.enableThrownAbilityOverlay,
										() -> config.foraging.sweepOverlay.enableThrownAbilityOverlay,
										newValue -> config.foraging.sweepOverlay.enableThrownAbilityOverlay = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.sweepOverlay.sweepOverlayColor"))
								.binding(defaults.foraging.sweepOverlay.sweepOverlayColor,
										() -> config.foraging.sweepOverlay.sweepOverlayColor,
										newValue -> {
											config.foraging.sweepOverlay.sweepOverlayColor = newValue;
											SweepOverlay.configCallback(newValue);
										})
								.controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
								.build())
						.build())
				.build();
	}
}
