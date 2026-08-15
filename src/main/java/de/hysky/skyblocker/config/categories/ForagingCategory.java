package de.hysky.skyblocker.config.categories;

import java.awt.Color;

import net.azureaaron.dandelion.api.ButtonOption;
import net.azureaaron.dandelion.api.ConfigCategory;
import net.azureaaron.dandelion.api.Option;
import net.azureaaron.dandelion.api.OptionGroup;
import net.azureaaron.dandelion.api.controllers.ColourController;
import net.azureaaron.dandelion.api.controllers.IntegerController;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.CommonTags;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.foraging.SweepOverlay;
import de.hysky.skyblocker.skyblock.galatea.SeaLumiesHighlighter;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.utils.Location;

public class ForagingCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.id(SkyblockerMod.id("config/foraging"))
				.name(Component.translatable("skyblocker.config.foraging"))

				// Ungrouped Options
				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("skyblocker.config.foraging.enableTreeFelledNotification"))
						.description(Component.translatable("skyblocker.config.foraging.enableTreeFelledNotification.@Tooltip"))
						.tags(CommonTags.ADDED_IN_6_9_1)
						.binding(defaults.foraging.enableTreeFelledNotification,
								() -> config.foraging.enableTreeFelledNotification,
								newValue -> config.foraging.enableTreeFelledNotification = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())

				// Moonglade Marsh
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.foraging.moongladeMarsh"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.foraging.moongladeMarsh.solveForestTemplePuzzle"))
								.description(Component.translatable("skyblocker.config.foraging.moongladeMarsh.solveForestTemplePuzzle.@Tooltip"))
								.binding(defaults.foraging.moongladeMarsh.solveForestTemplePuzzle,
										() -> config.foraging.moongladeMarsh.solveForestTemplePuzzle,
										newValue -> config.foraging.moongladeMarsh.solveForestTemplePuzzle = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.foraging.moongladeMarsh.enableLushlilacHighlighter"))
								.description(Component.translatable("skyblocker.config.foraging.moongladeMarsh.enableLushlilacHighlighter.@Tooltip"))
								.binding(defaults.foraging.moongladeMarsh.enableLushlilacHighlighter,
										() -> config.foraging.moongladeMarsh.enableLushlilacHighlighter,
										newValue -> config.foraging.moongladeMarsh.enableLushlilacHighlighter = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.foraging.moongladeMarsh.enableSeaLumiesHighlighter"))
								.description(Component.translatable("skyblocker.config.foraging.moongladeMarsh.enableSeaLumiesHighlighter.@Tooltip"))
								.binding(defaults.foraging.moongladeMarsh.enableSeaLumiesHighlighter,
										() -> config.foraging.moongladeMarsh.enableSeaLumiesHighlighter,
										newValue -> {
											config.foraging.moongladeMarsh.enableSeaLumiesHighlighter = newValue;
											SeaLumiesHighlighter.INSTANCE.configCallback();
										})
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.translatable("skyblocker.config.foraging.moongladeMarsh.seaLumieMinCount"))
								.description(Component.translatable("skyblocker.config.foraging.moongladeMarsh.seaLumieMinCount.@Tooltip"))
								.binding(defaults.foraging.moongladeMarsh.seaLumiesMinimumCount,
										() -> config.foraging.moongladeMarsh.seaLumiesMinimumCount,
										newValue -> {
											config.foraging.moongladeMarsh.seaLumiesMinimumCount = newValue;
											SeaLumiesHighlighter.INSTANCE.configCallback();
										})
								.controller(IntegerController.createBuilder().range(1, 4).slider(1).build())
								.build())
						.option(ButtonOption.createBuilder()
								.name(Component.translatable("skyblocker.config.foraging.moongladeMarsh.enableTreeBreakHud"))
								.description(Component.translatable("skyblocker.config.hud.movedMessage"))
								.prompt(Component.translatable("text.skyblocker.open"))
								.action(screen -> Minecraft.getInstance().gui.setScreen(new WidgetsConfigurationScreen(Location.GALATEA, screen)))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.foraging.moongladeMarsh.enableTunerSolver"))
								.description(Component.translatable("skyblocker.config.foraging.moongladeMarsh.enableTunerSolver.@Tooltip"))
								.binding(defaults.foraging.moongladeMarsh.enableTunerSolver,
										() -> config.foraging.moongladeMarsh.enableTunerSolver,
										newValue -> config.foraging.moongladeMarsh.enableTunerSolver = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.foraging.moongladeMarsh.enableSweepDetailsWidget"))
								.description(Component.translatable("skyblocker.config.foraging.moongladeMarsh.enableSweepDetailsWidget.@Tooltip"))
								.binding(defaults.foraging.moongladeMarsh.enableSweepDetailsWidget,
										() -> config.foraging.moongladeMarsh.enableSweepDetailsWidget,
										newValue -> config.foraging.moongladeMarsh.enableSweepDetailsWidget = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				// Torrhus Canyon
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.foraging.torrhusCanyon"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.foraging.torrhusCanyon.solveDesertTemplePuzzles"))
								.description(Component.translatable("skyblocker.config.foraging.torrhusCanyon.solveDesertTemplePuzzles.@Tooltip"))
								.tags(CommonTags.ADDED_IN_6_9_0)
								.binding(defaults.foraging.torrhusCanyon.solveDesertTemplePuzzles,
										() -> config.foraging.torrhusCanyon.solveDesertTemplePuzzles,
										newValue -> config.foraging.torrhusCanyon.solveDesertTemplePuzzles = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.foraging.torrhusCanyon.enableRubyVeilshroomHighlighter"))
								.description(Component.translatable("skyblocker.config.foraging.torrhusCanyon.enableRubyVeilshroomHighlighter.@Tooltip"))
								.tags(CommonTags.ADDED_IN_6_9_0)
								.binding(defaults.foraging.torrhusCanyon.enableRubyVeilshroomHighlighter,
										() -> config.foraging.torrhusCanyon.enableRubyVeilshroomHighlighter,
										newValue -> config.foraging.torrhusCanyon.enableRubyVeilshroomHighlighter = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.foraging.torrhusCanyon.enableHoneyhiveHighlighter"))
								.description(Component.translatable("skyblocker.config.foraging.torrhusCanyon.enableHoneyhiveHighlighter.@Tooltip"))
								.tags(CommonTags.ADDED_IN_6_9_0)
								.binding(defaults.foraging.torrhusCanyon.enableHoneyhiveHighlighter,
										() -> config.foraging.torrhusCanyon.enableHoneyhiveHighlighter,
										newValue -> config.foraging.torrhusCanyon.enableHoneyhiveHighlighter = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				// Sweep Overlay
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.foraging.sweepOverlay"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.foraging.sweepOverlay.enableSweepOverlay"))
								.description(Component.translatable("skyblocker.config.foraging.sweepOverlay.enableSweepOverlay.@Tooltip"))
								.binding(defaults.foraging.sweepOverlay.enableSweepOverlay,
										() -> config.foraging.sweepOverlay.enableSweepOverlay,
										newValue -> config.foraging.sweepOverlay.enableSweepOverlay = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.foraging.sweepOverlay.enableThrownAbilityOverlay"))
								.description(Component.translatable("skyblocker.config.foraging.sweepOverlay.enableThrownAbilityOverlay.@Tooltip"))
								.binding(defaults.foraging.sweepOverlay.enableThrownAbilityOverlay,
										() -> config.foraging.sweepOverlay.enableThrownAbilityOverlay,
										newValue -> config.foraging.sweepOverlay.enableThrownAbilityOverlay = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.translatable("skyblocker.config.foraging.sweepOverlay.sweepOverlayColor"))
								.binding(defaults.foraging.sweepOverlay.sweepOverlayColor,
										() -> config.foraging.sweepOverlay.sweepOverlayColor,
										newValue -> {
											config.foraging.sweepOverlay.sweepOverlayColor = newValue;
											SweepOverlay.configCallback(newValue);
										})
								.controller(ColourController.createBuilder().hasAlpha(true).build())
								.build())
						.build())
				.build();
	}
}
