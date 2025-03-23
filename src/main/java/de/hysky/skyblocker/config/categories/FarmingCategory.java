package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.garden.FarmingHudWidget;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.utils.Location;
import dev.isxander.yacl3.api.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class FarmingCategory {

    public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("skyblocker.config.farming"))

                //Garden
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.farming.garden"))
                        .collapsed(false)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.garden.dicerTitlePrevent"))
                                .binding(defaults.farming.garden.dicerTitlePrevent,
                                        () -> config.farming.garden.dicerTitlePrevent,
                                        newValue -> config.farming.garden.dicerTitlePrevent = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.garden.pestHighlighter"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.farming.garden.pestHighlighter.@Tooltip")))
                                .binding(defaults.farming.garden.pestHighlighter,
                                        () -> config.farming.garden.pestHighlighter,
                                        newValue -> config.farming.garden.pestHighlighter = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.garden.lockMouseTool"))
                                .binding(defaults.farming.garden.lockMouseTool,
                                        () -> config.farming.garden.lockMouseTool,
                                        newValue -> config.farming.garden.lockMouseTool = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.garden.lockMouseGround"))
                                .binding(defaults.farming.garden.lockMouseGroundOnly,
                                        () -> config.farming.garden.lockMouseGroundOnly,
                                        newValue -> config.farming.garden.lockMouseGroundOnly = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.garden.gardenPlotsWidget"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.farming.garden.gardenPlotsWidget.@Tooltip")))
                                .binding(defaults.farming.garden.gardenPlotsWidget,
                                        () -> config.farming.garden.gardenPlotsWidget,
                                        newValue -> config.farming.garden.gardenPlotsWidget = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.garden.closeScreenOnPlotClick"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.farming.garden.closeScreenOnPlotClick.@Tooltip")))
                                .binding(defaults.farming.garden.closeScreenOnPlotClick,
                                        () -> config.farming.garden.closeScreenOnPlotClick,
                                        newValue -> config.farming.garden.closeScreenOnPlotClick = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.farming.visitorHelper"))
                        .collapsed(false)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.visitorHelper.visitorHelper"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.farming.visitorHelper.visitorHelper.@Tooltip")))
                                .binding(defaults.farming.visitorHelper.visitorHelper,
                                        () -> config.farming.visitorHelper.visitorHelper,
                                        newValue -> config.farming.visitorHelper.visitorHelper = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.visitorHelper.visitorHelperGardenOnly"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.farming.visitorHelper.visitorHelperGardenOnly.@Tooltip")))
                                .binding(defaults.farming.visitorHelper.visitorHelperGardenOnly,
                                        () -> config.farming.visitorHelper.visitorHelperGardenOnly,
                                        newValue -> config.farming.visitorHelper.visitorHelperGardenOnly = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.visitorHelper.showStacksInVisitorHelper"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.farming.visitorHelper.showStacksInVisitorHelper.@Tooltip")))
                                .binding(defaults.farming.visitorHelper.showStacksInVisitorHelper,
                                        () -> config.farming.visitorHelper.showStacksInVisitorHelper,
                                        newValue -> config.farming.visitorHelper.showStacksInVisitorHelper = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.visitorHelper.showCopyAmountButton"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.farming.visitorHelper.showCopyAmountButton.@Tooltip")))
                                .binding(defaults.farming.visitorHelper.showCopyAmountButton,
                                        () -> config.farming.visitorHelper.showCopyAmountButton,
                                        newValue -> config.farming.visitorHelper.showCopyAmountButton = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.farming.farmingHud"))
                        .collapsed(false)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.farmingHud.enableHud"))
                                .binding(defaults.farming.farmingHud.enableHud,
                                        () -> config.farming.farmingHud.enableHud,
                                        newValue -> config.farming.farmingHud.enableHud = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.farmingHud.openConfig"))
                                .text(Text.translatable("text.skyblocker.open"))
                                .action((screen, opt) -> MinecraftClient.getInstance().setScreen(new WidgetsConfigurationScreen(Location.GARDEN, FarmingHudWidget.getInstance().getInternalID(), screen)))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.farmingHud.showCropCounter"))
                                .binding(defaults.farming.farmingHud.showCropCounter,
                                        () -> config.farming.farmingHud.showCropCounter,
                                        newValue -> config.farming.farmingHud.showCropCounter = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.farmingHud.showCropsPerMinute"))
                                .binding(defaults.farming.farmingHud.showCropsPerMinute,
                                        () -> config.farming.farmingHud.showCropsPerMinute,
                                        newValue -> config.farming.farmingHud.showCropsPerMinute = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.farmingHud.showCoinsPerHour"))
                                .binding(defaults.farming.farmingHud.showCoinsPerHour,
                                        () -> config.farming.farmingHud.showCoinsPerHour,
                                        newValue -> config.farming.farmingHud.showCoinsPerHour = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.farmingHud.showBlocksPerSecond"))
                                .binding(defaults.farming.farmingHud.showBlocksPerSecond,
                                        () -> config.farming.farmingHud.showBlocksPerSecond,
                                        newValue -> config.farming.farmingHud.showBlocksPerSecond = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.farmingHud.showFarmingLevelProgress"))
                                .binding(defaults.farming.farmingHud.showFarmingLevelProgress,
                                        () -> config.farming.farmingHud.showFarmingLevelProgress,
                                        newValue -> config.farming.farmingHud.showFarmingLevelProgress = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.farming.farmingHud.showXpPerHour"))
                                .binding(defaults.farming.farmingHud.showXpPerHour,
                                        () -> config.farming.farmingHud.showXpPerHour,
                                        newValue -> config.farming.farmingHud.showXpPerHour = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())
                .build();
    }
}
