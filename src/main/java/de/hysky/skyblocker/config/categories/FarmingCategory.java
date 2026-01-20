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

				//Garden
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.farming.garden"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.garden.enableHud"))
								.binding(defaults.farming.garden.farmingHud.enableHud,
										() -> config.farming.garden.farmingHud.enableHud,
										newValue -> config.farming.garden.farmingHud.enableHud = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(ButtonOption.createBuilder()
								.name(Component.translatable("skyblocker.config.farming.garden.farmingHud"))
								.prompt(Component.translatable("text.skyblocker.open"))
								.action(screen -> Minecraft.getInstance().setScreen(new WidgetsConfigurationScreen(Location.GARDEN, FarmingHudWidget.getInstance().getInternalID(), screen)))
								.build())
						.option(Option.<FarmingConfig.Type>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.garden.farmingHud.type"))
								.description(Component.translatable("skyblocker.config.farming.garden.farmingHud.type.@Tooltip"))
								.binding(defaults.farming.garden.farmingHud.type,
										() -> config.farming.garden.farmingHud.type,
										newValue -> config.farming.garden.farmingHud.type = newValue)
								.controller(ConfigUtils.createEnumController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.garden.dicerTitlePrevent"))
								.binding(defaults.farming.garden.dicerTitlePrevent,
										() -> config.farming.garden.dicerTitlePrevent,
										newValue -> config.farming.garden.dicerTitlePrevent = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.garden.pestHighlighter"))
								.description(Component.translatable("skyblocker.config.farming.garden.pestHighlighter.@Tooltip"))
								.binding(defaults.farming.garden.pestHighlighter,
										() -> config.farming.garden.pestHighlighter,
										newValue -> config.farming.garden.pestHighlighter = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.garden.lockMouseTool"))
								.binding(defaults.farming.garden.lockMouseTool,
										() -> config.farming.garden.lockMouseTool,
										newValue -> config.farming.garden.lockMouseTool = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.garden.lockMouseGround"))
								.binding(defaults.farming.garden.lockMouseGroundOnly,
										() -> config.farming.garden.lockMouseGroundOnly,
										newValue -> config.farming.garden.lockMouseGroundOnly = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.garden.gardenPlotsWidget"))
								.description(Component.translatable("skyblocker.config.farming.garden.gardenPlotsWidget.@Tooltip"))
								.binding(defaults.farming.garden.gardenPlotsWidget,
										() -> config.farming.garden.gardenPlotsWidget,
										newValue -> config.farming.garden.gardenPlotsWidget = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.garden.closeScreenOnPlotClick"))
								.description(Component.translatable("skyblocker.config.farming.garden.closeScreenOnPlotClick.@Tooltip"))
								.binding(defaults.farming.garden.closeScreenOnPlotClick,
										() -> config.farming.garden.closeScreenOnPlotClick,
										newValue -> config.farming.garden.closeScreenOnPlotClick = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.garden.enableStereoHarmonyHelperForContest"))
								.description(Component.translatable("skyblocker.config.farming.garden.enableStereoHarmonyHelperForContest.@Tooltip"))
								.binding(defaults.farming.garden.enableStereoHarmonyHelperForContest,
										() -> config.farming.garden.enableStereoHarmonyHelperForContest,
										newValue -> config.farming.garden.enableStereoHarmonyHelperForContest = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.farming.visitorHelper"))
						.collapsed(false)
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
