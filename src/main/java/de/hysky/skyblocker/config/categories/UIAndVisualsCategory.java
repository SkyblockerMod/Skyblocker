package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.fancybars.StatusBarsConfigScreen;
import de.hysky.skyblocker.utils.render.title.TitleContainerConfigScreen;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class UIAndVisualsCategory {
    public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals"))

                //Ungrouped Options
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.compactorDeletorPreview"))
                        .binding(defaults.uiAndVisuals.compactorDeletorPreview,
                                () -> config.uiAndVisuals.compactorDeletorPreview,
                                newValue -> config.uiAndVisuals.compactorDeletorPreview = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.dontStripSkinAlphaValues"))
                        .description(OptionDescription.of(Text.translatable("skyblocker.config.userInterfaceAndVisuals.dontStripSkinAlphaValues.@Tooltip")))
                        .binding(defaults.uiAndVisuals.dontStripSkinAlphaValues,
                                () -> config.uiAndVisuals.dontStripSkinAlphaValues,
                                newValue -> config.uiAndVisuals.dontStripSkinAlphaValues = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .flag(OptionFlag.ASSET_RELOAD)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.backpackPreviewWithoutShift"))
                        .binding(defaults.uiAndVisuals.backpackPreviewWithoutShift,
                                () -> config.uiAndVisuals.backpackPreviewWithoutShift,
                                newValue -> config.uiAndVisuals.backpackPreviewWithoutShift = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.hideEmptyTooltips"))
                        .binding(defaults.uiAndVisuals.hideEmptyTooltips,
                                () -> config.uiAndVisuals.hideEmptyTooltips,
                                newValue -> config.uiAndVisuals.hideEmptyTooltips = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.fancyCraftingTable"))
                        .binding(defaults.uiAndVisuals.fancyCraftingTable,
                                () -> config.uiAndVisuals.fancyCraftingTable,
                                newValue -> config.uiAndVisuals.fancyCraftingTable = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .build())

                //Chest Value FIXME change dropdown to color controller
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.chestValue"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.chestValue.enableChestValue"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.userInterfaceAndVisuals.chestValue.enableChestValue.@Tooltip")))
                                .binding(defaults.uiAndVisuals.chestValue.enableChestValue,
                                        () -> config.uiAndVisuals.chestValue.enableChestValue,
                                        newValue -> config.uiAndVisuals.chestValue.enableChestValue = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Formatting>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.chestValue.color"))
                                .binding(defaults.uiAndVisuals.chestValue.color,
                                        () -> config.uiAndVisuals.chestValue.color,
                                        newValue -> config.uiAndVisuals.chestValue.color = newValue)
                                .controller(ConfigUtils.getEnumDropdownControllerFactory(ConfigUtils.FORMATTING_FORMATTER))
                                .build())
                        .option(Option.<Formatting>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.chestValue.incompleteColor"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.userInterfaceAndVisuals.chestValue.incompleteColor.@Tooltip")))
                                .binding(defaults.uiAndVisuals.chestValue.incompleteColor,
                                        () -> config.uiAndVisuals.chestValue.incompleteColor,
                                        newValue -> config.uiAndVisuals.chestValue.incompleteColor = newValue)
                                .controller(ConfigUtils.getEnumDropdownControllerFactory(ConfigUtils.FORMATTING_FORMATTER))
                                .build())
                        .build())

                //Item Cooldown
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.itemCooldown"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.itemCooldown.enableItemCooldowns"))
                                .binding(defaults.uiAndVisuals.itemCooldown.enableItemCooldowns,
                                        () -> config.uiAndVisuals.itemCooldown.enableItemCooldowns,
                                        newValue -> config.uiAndVisuals.itemCooldown.enableItemCooldowns = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Title Container
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.titleContainer"))
                        .description(OptionDescription.of(Text.translatable("skyblocker.config.userInterfaceAndVisuals.titleContainer.@Tooltip")))
                        .collapsed(true)
                        .option(Option.<Float>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.titleContainer.titleContainerScale"))
                                .binding(defaults.uiAndVisuals.titleContainer.titleContainerScale,
                                        () -> config.uiAndVisuals.titleContainer.titleContainerScale,
                                        newValue -> config.uiAndVisuals.titleContainer.titleContainerScale = newValue)
                                .controller(opt -> FloatFieldControllerBuilder.create(opt).range(30f, 140f))
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.titleContainer.config"))
                                .text(Text.translatable("text.skyblocker.open"))
                                .action((screen, opt) -> MinecraftClient.getInstance().setScreen(new TitleContainerConfigScreen(screen)))
                                .build())
                        .build())

                //Tab Hud
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.tabHud"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.tabHud.tabHudEnabled"))
                                .binding(defaults.uiAndVisuals.tabHud.tabHudEnabled,
                                        () -> config.uiAndVisuals.tabHud.tabHudEnabled,
                                        newValue -> config.uiAndVisuals.tabHud.tabHudEnabled = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.tabHud.tabHudScale"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.userInterfaceAndVisuals.tabHud.tabHudScale.@Tooltip")))
                                .binding(defaults.uiAndVisuals.tabHud.tabHudScale,
                                        () -> config.uiAndVisuals.tabHud.tabHudScale,
                                        newValue -> config.uiAndVisuals.tabHud.tabHudScale = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(10, 200).step(1))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.tabHud.enableHudBackground"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.userInterfaceAndVisuals.tabHud.enableHudBackground.@Tooltip")))
                                .binding(defaults.uiAndVisuals.tabHud.enableHudBackground,
                                        () -> config.uiAndVisuals.tabHud.enableHudBackground,
                                        newValue -> config.uiAndVisuals.tabHud.enableHudBackground = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.tabHud.plainPlayerNames"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.userInterfaceAndVisuals.tabHud.plainPlayerNames.@Tooltip")))
                                .binding(defaults.uiAndVisuals.tabHud.plainPlayerNames,
                                        () -> config.uiAndVisuals.tabHud.plainPlayerNames,
                                        newValue -> config.uiAndVisuals.tabHud.plainPlayerNames = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<UIAndVisualsConfig.NameSorting>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.tabHud.nameSorting"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.userInterfaceAndVisuals.tabHud.nameSorting.@Tooltip")))
                                .binding(defaults.uiAndVisuals.tabHud.nameSorting,
                                        () -> config.uiAndVisuals.tabHud.nameSorting,
                                        newValue -> config.uiAndVisuals.tabHud.nameSorting = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .build())

                // Fancy Auction House
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.fancyAuctionHouse"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.fancyAuctionHouse.enabled"))
                                .binding(defaults.uiAndVisuals.fancyAuctionHouse.enabled,
                                        () -> config.uiAndVisuals.fancyAuctionHouse.enabled,
                                        newValue -> config.uiAndVisuals.fancyAuctionHouse.enabled = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.fancyAuctionHouse.highlightUnderAvgPrice"))
                                .binding(defaults.uiAndVisuals.fancyAuctionHouse.highlightCheapBIN,
                                        () -> config.uiAndVisuals.fancyAuctionHouse.highlightCheapBIN,
                                        newValue -> config.uiAndVisuals.fancyAuctionHouse.highlightCheapBIN = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Fancy Bars
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.bars"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.bars.enableBars"))
                                .binding(defaults.uiAndVisuals.bars.enableBars,
                                        () -> config.uiAndVisuals.bars.enableBars,
                                        newValue -> config.uiAndVisuals.bars.enableBars = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.bars.openScreen"))
                                .text(Text.translatable("text.skyblocker.open"))
                                .action((screen, opt) -> MinecraftClient.getInstance().setScreen(new StatusBarsConfigScreen()))
                                .build())
                        .build())

                //Waypoints
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.waypoints"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.waypoints.enableWaypoints"))
                                .binding(defaults.uiAndVisuals.waypoints.enableWaypoints,
                                        () -> config.uiAndVisuals.waypoints.enableWaypoints,
                                        newValue -> config.uiAndVisuals.waypoints.enableWaypoints = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Waypoint.Type>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.waypoints.waypointType"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.userInterfaceAndVisuals.waypoints.waypointType.@Tooltip"),
                                        Text.translatable("skyblocker.config.userInterfaceAndVisuals.waypoints.waypointType")))
                                .binding(defaults.uiAndVisuals.waypoints.waypointType,
                                        () -> config.uiAndVisuals.waypoints.waypointType,
                                        newValue -> config.uiAndVisuals.waypoints.waypointType = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .build())

                //Teleport Overlays
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.teleportOverlay"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.teleportOverlay.enableTeleportOverlays"))
                                .binding(defaults.uiAndVisuals.teleportOverlay.enableTeleportOverlays,
                                        () -> config.uiAndVisuals.teleportOverlay.enableTeleportOverlays,
                                        newValue -> config.uiAndVisuals.teleportOverlay.enableTeleportOverlays = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.teleportOverlay.enableWeirdTransmission"))
                                .binding(defaults.uiAndVisuals.teleportOverlay.enableWeirdTransmission,
                                        () -> config.uiAndVisuals.teleportOverlay.enableWeirdTransmission,
                                        newValue -> config.uiAndVisuals.teleportOverlay.enableWeirdTransmission = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.teleportOverlay.enableInstantTransmission"))
                                .binding(defaults.uiAndVisuals.teleportOverlay.enableInstantTransmission,
                                        () -> config.uiAndVisuals.teleportOverlay.enableInstantTransmission,
                                        newValue -> config.uiAndVisuals.teleportOverlay.enableInstantTransmission = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.teleportOverlay.enableEtherTransmission"))
                                .binding(defaults.uiAndVisuals.teleportOverlay.enableEtherTransmission,
                                        () -> config.uiAndVisuals.teleportOverlay.enableEtherTransmission,
                                        newValue -> config.uiAndVisuals.teleportOverlay.enableEtherTransmission = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.teleportOverlay.enableSinrecallTransmission"))
                                .binding(defaults.uiAndVisuals.teleportOverlay.enableSinrecallTransmission,
                                        () -> config.uiAndVisuals.teleportOverlay.enableSinrecallTransmission,
                                        newValue -> config.uiAndVisuals.teleportOverlay.enableSinrecallTransmission = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.teleportOverlay.enableWitherImpact"))
                                .binding(defaults.uiAndVisuals.teleportOverlay.enableWitherImpact,
                                        () -> config.uiAndVisuals.teleportOverlay.enableWitherImpact,
                                        newValue -> config.uiAndVisuals.teleportOverlay.enableWitherImpact = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Search overlay
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.searchOverlay"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.searchOverlay.enableBazaar"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.userInterfaceAndVisuals.searchOverlay.enableBazaar.@Tooltip")))
                                .binding(defaults.uiAndVisuals.searchOverlay.enableBazaar,
                                        () -> config.uiAndVisuals.searchOverlay.enableBazaar,
                                        newValue -> config.uiAndVisuals.searchOverlay.enableBazaar = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.searchOverlay.enableAuctionHouse"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.userInterfaceAndVisuals.searchOverlay.enableAuctionHouse.@Tooltip")))
                                .binding(defaults.uiAndVisuals.searchOverlay.enableAuctionHouse,
                                        () -> config.uiAndVisuals.searchOverlay.enableAuctionHouse,
                                        newValue -> config.uiAndVisuals.searchOverlay.enableAuctionHouse = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.searchOverlay.keepPreviousSearches"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.userInterfaceAndVisuals.searchOverlay.keepPreviousSearches.@Tooltip")))
                                .binding(defaults.uiAndVisuals.searchOverlay.keepPreviousSearches,
                                        () -> config.uiAndVisuals.searchOverlay.keepPreviousSearches,
                                        newValue -> config.uiAndVisuals.searchOverlay.keepPreviousSearches = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.searchOverlay.maxSuggestions"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.userInterfaceAndVisuals.searchOverlay.maxSuggestions.@Tooltip")))
                                .binding(defaults.uiAndVisuals.searchOverlay.maxSuggestions,
                                        () -> config.uiAndVisuals.searchOverlay.maxSuggestions,
                                        newValue -> config.uiAndVisuals.searchOverlay.maxSuggestions = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 5).step(1))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.searchOverlay.historyLength"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.userInterfaceAndVisuals.searchOverlay.historyLength.@Tooltip")))
                                .binding(defaults.uiAndVisuals.searchOverlay.historyLength,
                                        () -> config.uiAndVisuals.searchOverlay.historyLength,
                                        newValue -> config.uiAndVisuals.searchOverlay.historyLength = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 5).step(1))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.searchOverlay.enableCommands"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.userInterfaceAndVisuals.searchOverlay.enableCommands.@Tooltip")))
                                .binding(defaults.uiAndVisuals.searchOverlay.enableCommands,
                                        () -> config.uiAndVisuals.searchOverlay.enableCommands,
                                        newValue -> config.uiAndVisuals.searchOverlay.enableCommands = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Flame Overlay
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.flameOverlay"))
                        .collapsed(true)
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.flameOverlay.flameHeight"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.userInterfaceAndVisuals.flameOverlay.flameHeight.@Tooltip")))
                                .binding(defaults.uiAndVisuals.flameOverlay.flameHeight,
                                        () -> config.uiAndVisuals.flameOverlay.flameHeight,
                                        newValue -> config.uiAndVisuals.flameOverlay.flameHeight = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 100).step(1))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.userInterfaceAndVisuals.flameOverlay.flameOpacity"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.userInterfaceAndVisuals.flameOverlay.flameOpacity.@Tooltip")))
                                .binding(defaults.uiAndVisuals.flameOverlay.flameOpacity,
                                        () -> config.uiAndVisuals.flameOverlay.flameOpacity,
                                        newValue -> config.uiAndVisuals.flameOverlay.flameOpacity = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 100).step(1))
                                .build())
                        .build())

                .build();
    }
}
