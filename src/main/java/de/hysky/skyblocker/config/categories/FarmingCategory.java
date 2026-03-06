package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.configs.FarmingConfig;
import de.hysky.skyblocker.skyblock.garden.FarmingHudWidget;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.utils.Location;
import net.azureaaron.dandelion.api.ButtonOption;
import net.azureaaron.dandelion.api.ConfigCategory;
import net.azureaaron.dandelion.api.Option;
import net.azureaaron.dandelion.api.OptionGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class FarmingCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.id(SkyblockerMod.id("config/farming"))
				.name(Component.translatable("skyblocker.config.farming"))

				// Farming Hud
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.farming.farmingHud"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.farmingHud.enableHud"))
								.binding(defaults.farming.garden.farmingHud.enableHud,
										() -> config.farming.garden.farmingHud.enableHud,
										newValue -> config.farming.garden.farmingHud.enableHud = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(ButtonOption.createBuilder()
								.name(Component.translatable("skyblocker.config.farming.farmingHud.config"))
								.prompt(Component.translatable("text.skyblocker.open"))
								.action(screen -> Minecraft.getInstance().setScreen(new WidgetsConfigurationScreen(Location.GARDEN, FarmingHudWidget.getInstance().getInternalID(), screen)))
								.build())
						.option(Option.<FarmingConfig.Type>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.farmingHud.type"))
								.description(Component.translatable("skyblocker.config.farming.farmingHud.type.@Tooltip"))
								.binding(defaults.farming.garden.farmingHud.type,
										() -> config.farming.garden.farmingHud.type,
										newValue -> config.farming.garden.farmingHud.type = newValue)
								.controller(ConfigUtils.createEnumController())
								.build())
						.build())
				// Pest Highlighter
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.farming.pestHighlighter"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.pestHighlighter.enable"))
								.description(Component.translatable("skyblocker.config.farming.pestHighlighter.@Tooltip"))
								.binding(defaults.farming.garden.pestHighlighter,
										() -> config.farming.garden.pestHighlighter,
										newValue -> config.farming.garden.pestHighlighter = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.pestHighlighter.stereoHarmony"))
								.description(Component.translatable("skyblocker.config.farming.pestHighlighter.stereoHarmony.@Tooltip"))
								.binding(defaults.farming.garden.vinylHighlighter,
										() -> config.farming.garden.vinylHighlighter,
										newValue -> config.farming.garden.vinylHighlighter = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.pestHighlighter.stereoHarmony.contest"))
								.description(Component.translatable("skyblocker.config.farming.pestHighlighter.stereoHarmony.contest.@Tooltip"))
								.binding(defaults.farming.garden.enableStereoHarmonyHelperForContest,
										() -> config.farming.garden.enableStereoHarmonyHelperForContest,
										newValue -> config.farming.garden.enableStereoHarmonyHelperForContest = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())
				// Mouse Lock
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.farming.mouseLock"))
						.collapsed(true)
						.tags(Component.literal("camera lock"), Component.literal("sensitivity"), Component.literal("yaw pitch"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.mouseLock.lockMouseTool"))
								.binding(defaults.farming.garden.lockMouseTool,
										() -> config.farming.garden.lockMouseTool,
										newValue -> config.farming.garden.lockMouseTool = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.mouseLock.lockMouseGround"))
								.binding(defaults.farming.garden.lockMouseGroundOnly,
										() -> config.farming.garden.lockMouseGroundOnly,
										newValue -> config.farming.garden.lockMouseGroundOnly = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())
				// Garden Plots Widget
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.farming.plotsWidget"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.plotsWidget.enable"))
								.description(Component.translatable("skyblocker.config.farming.plotsWidget.enable.@Tooltip"))
								.binding(defaults.farming.garden.gardenPlotsWidget,
										() -> config.farming.garden.gardenPlotsWidget,
										newValue -> config.farming.garden.gardenPlotsWidget = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.plotsWidget.closeScreenOnPlotClick"))
								.description(Component.translatable("skyblocker.config.farming.plotsWidget.closeScreenOnPlotClick.@Tooltip"))
								.binding(defaults.farming.garden.closeScreenOnPlotClick,
										() -> config.farming.garden.closeScreenOnPlotClick,
										newValue -> config.farming.garden.closeScreenOnPlotClick = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())
				// Visitor Helper
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.farming.visitorHelper"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.visitorHelper.visitorHelper"))
								.description(Component.translatable("skyblocker.config.farming.visitorHelper.visitorHelper.@Tooltip"))
								.binding(defaults.farming.visitorHelper.visitorHelper,
										() -> config.farming.visitorHelper.visitorHelper,
										newValue -> config.farming.visitorHelper.visitorHelper = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.visitorHelper.visitorHelperGardenOnly"))
								.description(Component.translatable("skyblocker.config.farming.visitorHelper.visitorHelperGardenOnly.@Tooltip"))
								.binding(defaults.farming.visitorHelper.visitorHelperGardenOnly,
										() -> config.farming.visitorHelper.visitorHelperGardenOnly,
										newValue -> config.farming.visitorHelper.visitorHelperGardenOnly = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.visitorHelper.showStacksInVisitorHelper"))
								.description(Component.translatable("skyblocker.config.farming.visitorHelper.showStacksInVisitorHelper.@Tooltip"))
								.binding(defaults.farming.visitorHelper.showStacksInVisitorHelper,
										() -> config.farming.visitorHelper.showStacksInVisitorHelper,
										newValue -> config.farming.visitorHelper.showStacksInVisitorHelper = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())
				.build();

	}
}
