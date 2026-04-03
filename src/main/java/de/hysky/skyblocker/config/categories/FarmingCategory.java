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

				// Farming HUD
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.farming.farmingHud"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.farmingHud.enabled"))
								.binding(defaults.farming.farmingHud.enabled,
										() -> config.farming.farmingHud.enabled,
										newValue -> config.farming.farmingHud.enabled = newValue)
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
								.binding(defaults.farming.farmingHud.type,
										() -> config.farming.farmingHud.type,
										newValue -> config.farming.farmingHud.type = newValue)
								.controller(ConfigUtils.createEnumController())
								.build())
						.build())
				// Pest Highlighter
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.farming.pestHighlighter"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.pestHighlighter.enabled"))
								.description(Component.translatable("skyblocker.config.farming.pestHighlighter.enabled.@Tooltip"))
								.binding(defaults.farming.pestHighlighter.enabled,
										() -> config.farming.pestHighlighter.enabled,
										newValue -> config.farming.pestHighlighter.enabled = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.pestHighlighter.vinylHighlighter"))
								.description(Component.translatable("skyblocker.config.farming.pestHighlighter.vinylHighlighter.@Tooltip"))
								.binding(defaults.farming.pestHighlighter.vinylHighlighter,
										() -> config.farming.pestHighlighter.vinylHighlighter,
										newValue -> config.farming.pestHighlighter.vinylHighlighter = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.pestHighlighter.stereoHarmony"))
								.description(Component.translatable("skyblocker.config.farming.pestHighlighter.stereoHarmony.@Tooltip"))
								.binding(defaults.farming.pestHighlighter.enableStereoHarmonyHelperForContest,
										() -> config.farming.pestHighlighter.enableStereoHarmonyHelperForContest,
										newValue -> config.farming.pestHighlighter.enableStereoHarmonyHelperForContest = newValue)
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
								.binding(defaults.farming.mouseLock.lockMouseTool,
										() -> config.farming.mouseLock.lockMouseTool,
										newValue -> config.farming.mouseLock.lockMouseTool = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.mouseLock.lockMouseGround"))
								.binding(defaults.farming.mouseLock.lockMouseGroundOnly,
										() -> config.farming.mouseLock.lockMouseGroundOnly,
										newValue -> config.farming.mouseLock.lockMouseGroundOnly = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())
				// Garden Plots Widget
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.farming.plotsWidget"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.plotsWidget.enabled"))
								.description(Component.translatable("skyblocker.config.farming.plotsWidget.enabled.@Tooltip"))
								.binding(defaults.farming.plotsWidget.enabled,
										() -> config.farming.plotsWidget.enabled,
										newValue -> config.farming.plotsWidget.enabled = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.plotsWidget.closeScreenOnPlotClick"))
								.description(Component.translatable("skyblocker.config.farming.plotsWidget.closeScreenOnPlotClick.@Tooltip"))
								.binding(defaults.farming.plotsWidget.closeScreenOnPlotClick,
										() -> config.farming.plotsWidget.closeScreenOnPlotClick,
										newValue -> config.farming.plotsWidget.closeScreenOnPlotClick = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())
				// Visitor Helper
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.farming.visitorHelper"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.visitorHelper.enabled"))
								.description(Component.translatable("skyblocker.config.farming.visitorHelper.enabled.@Tooltip"))
								.binding(defaults.farming.visitorHelper.enabled,
										() -> config.farming.visitorHelper.enabled,
										newValue -> config.farming.visitorHelper.enabled = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.visitorHelper.showInGardenOnly"))
								.description(Component.translatable("skyblocker.config.farming.visitorHelper.showInGardenOnly.@Tooltip"))
								.binding(defaults.farming.visitorHelper.showInGardenOnly,
										() -> config.farming.visitorHelper.showInGardenOnly,
										newValue -> config.farming.visitorHelper.showInGardenOnly = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.farming.visitorHelper.showInStacks"))
								.description(Component.translatable("skyblocker.config.farming.visitorHelper.showInStacks.@Tooltip"))
								.binding(defaults.farming.visitorHelper.showInStacks,
										() -> config.farming.visitorHelper.showInStacks,
										newValue -> config.farming.visitorHelper.showInStacks = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())
				.build();

	}
}
