package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.foraging.SweepOverlay;
import de.hysky.skyblocker.skyblock.galatea.SeaLumiesHighlighter;
import de.hysky.skyblocker.skyblock.galatea.TreeBreakProgressHud;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.utils.Location;
import net.azureaaron.dandelion.systems.ConfigCategory;
import net.azureaaron.dandelion.systems.Option;
import net.azureaaron.dandelion.systems.OptionGroup;
import net.azureaaron.dandelion.systems.ButtonOption;
import net.azureaaron.dandelion.systems.controllers.ColourController;
import net.azureaaron.dandelion.systems.controllers.IntegerController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;

public class ForagingCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.id(Identifier.of(SkyblockerMod.NAMESPACE, "config/foraging"))
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
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.galatea.solveForestTemplePuzzle"))
								.description(Text.translatable("skyblocker.config.foraging.galatea.solveForestTemplePuzzle.@Tooltip"))
								.binding(defaults.foraging.galatea.solveForestTemplePuzzle,
										() -> config.foraging.galatea.solveForestTemplePuzzle,
										newValue -> config.foraging.galatea.solveForestTemplePuzzle = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.galatea.enableLushlilacHighlighter"))
								.description(Text.translatable("skyblocker.config.foraging.galatea.enableLushlilacHighlighter.@Tooltip"))
								.binding(defaults.foraging.galatea.enableLushlilacHighlighter,
										() -> config.foraging.galatea.enableLushlilacHighlighter,
										newValue -> config.foraging.galatea.enableLushlilacHighlighter = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.galatea.enableSeaLumiesHighlighter"))
								.description(Text.translatable("skyblocker.config.foraging.galatea.enableSeaLumiesHighlighter.@Tooltip"))
								.binding(defaults.foraging.galatea.enableSeaLumiesHighlighter,
										() -> config.foraging.galatea.enableSeaLumiesHighlighter,
										newValue -> {
											config.foraging.galatea.enableSeaLumiesHighlighter = newValue;
											SeaLumiesHighlighter.INSTANCE.configCallback();
										})
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.galatea.enableTreeBreakProgress"))
								.description(Text.translatable("skyblocker.config.foraging.galatea.enableTreeBreakProgress.@Tooltip"))
								.binding(defaults.foraging.galatea.enableTreeBreakProgress,
										() -> config.foraging.galatea.enableTreeBreakProgress,
										newValue -> config.foraging.galatea.enableTreeBreakProgress = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.galatea.enableTreeBreakHud"))
								.prompt(Text.translatable("text.skyblocker.open"))
								.action((screen) -> MinecraftClient.getInstance().setScreen(new WidgetsConfigurationScreen(Location.GALATEA, TreeBreakProgressHud.getInstance().getInternalID(), screen)))
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.galatea.seaLumieMinCount"))
								.description(Text.translatable("skyblocker.config.foraging.galatea.seaLumieMinCount.@Tooltip"))
								.binding(defaults.foraging.galatea.seaLumiesMinimumCount,
										() -> config.foraging.galatea.seaLumiesMinimumCount,
										newValue -> {
											config.foraging.galatea.seaLumiesMinimumCount = newValue;
											SeaLumiesHighlighter.INSTANCE.configCallback();
										})
								.controller(IntegerController.createBuilder().range(1, 4).slider(1).build())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.galatea.enableTunerSolver"))
								.description(Text.translatable("skyblocker.config.foraging.galatea.enableTunerSolver.@Tooltip"))
								.binding(defaults.foraging.galatea.enableTunerSolver,
										() -> config.foraging.galatea.enableTunerSolver,
										newValue -> config.foraging.galatea.enableTunerSolver = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())
				//Sweep Overlays
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.foraging.sweepOverlay"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.sweepOverlay.enableSweepOverlay"))
								.description(Text.translatable("skyblocker.config.foraging.sweepOverlay.enableSweepOverlay.@Tooltip"))
								.binding(defaults.foraging.sweepOverlay.enableSweepOverlay,
										() -> config.foraging.sweepOverlay.enableSweepOverlay,
										newValue -> config.foraging.sweepOverlay.enableSweepOverlay = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.sweepOverlay.enableThrownAbilityOverlay"))
								.binding(defaults.foraging.sweepOverlay.enableThrownAbilityOverlay,
										() -> config.foraging.sweepOverlay.enableThrownAbilityOverlay,
										newValue -> config.foraging.sweepOverlay.enableThrownAbilityOverlay = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.sweepOverlay.sweepOverlayColor"))
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
