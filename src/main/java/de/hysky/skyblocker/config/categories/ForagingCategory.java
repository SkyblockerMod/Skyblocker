package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.galatea.SeaLumiesHighlighter;
import de.hysky.skyblocker.skyblock.galatea.TreeBreakProgressHud;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.utils.Location;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

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
						.option(Option.<Float>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.galatea.lushlilacHighlighterOpacity"))
								.description(OptionDescription.of(Text.translatable("skyblocker.config.foraging.galatea.lushlilacHighlighterOpacity.@Tooltip")))
								.binding(defaults.foraging.galatea.lushlilacHighlighterOpacity,
										() -> config.foraging.galatea.lushlilacHighlighterOpacity,
										newValue -> config.foraging.galatea.lushlilacHighlighterOpacity = newValue)
								.controller(opt -> FloatFieldControllerBuilder.create(opt).range(0.0f, 1.0f))
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
						.option(Option.<Float>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.galatea.seaLumiesHighlighterOpacity"))
								.description(OptionDescription.of(Text.translatable("skyblocker.config.foraging.galatea.seaLumiesHighlighterOpacity.@Tooltip")))
								.binding(defaults.foraging.galatea.seaLumiesHighlighterOpacity,
										() -> config.foraging.galatea.seaLumiesHighlighterOpacity,
										newValue -> config.foraging.galatea.seaLumiesHighlighterOpacity = newValue)
								.controller(opt -> FloatFieldControllerBuilder.create(opt).range(0.0f, 1.0f))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.galatea.enableTreeBreakProgress"))
								.description(OptionDescription.of(Text.translatable("skyblocker.config.foraging.galatea.enableTreeBreakProgress.@Tooltip")))
								.binding(defaults.foraging.galatea.enableTreeBreakProgress,
										() -> config.foraging.galatea.enableTreeBreakProgress,
										newValue -> config.foraging.galatea.enableTreeBreakProgress = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.galatea.enableTreeBreakHud"))
								.text(Text.translatable("text.skyblocker.open"))
								.action((screen, opt) -> MinecraftClient.getInstance().setScreen(new WidgetsConfigurationScreen(Location.GALATEA, TreeBreakProgressHud.getInstance().getInternalID(), screen)))
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
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.galatea.disableFishingNetPlacement"))
								.description(OptionDescription.of(Text.translatable("skyblocker.config.foraging.galatea.disableFishingNetPlacement.@Tooltip")))
								.binding(defaults.foraging.galatea.disableFishingNetPlacement,
										() -> config.foraging.galatea.disableFishingNetPlacement,
										newValue -> config.foraging.galatea.disableFishingNetPlacement = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())
				.build();
	}
}
