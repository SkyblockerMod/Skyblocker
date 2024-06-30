package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.fancybars.StatusBarsConfigScreen;
import de.hysky.skyblocker.skyblock.waypoint.WaypointsScreen;
import de.hysky.skyblocker.utils.render.title.TitleContainerConfigScreen;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.*;

public class UIAndVisualsCategory {
    public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("skyblocker.config.uiAndVisuals"))

                //Ungrouped Options
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.compactorDeletorPreview"))
                        .binding(defaults.uiAndVisuals.compactorDeletorPreview,
                                () -> config.uiAndVisuals.compactorDeletorPreview,
                                newValue -> config.uiAndVisuals.compactorDeletorPreview = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.dontStripSkinAlphaValues"))
                        .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.dontStripSkinAlphaValues.@Tooltip")))
                        .binding(defaults.uiAndVisuals.dontStripSkinAlphaValues,
                                () -> config.uiAndVisuals.dontStripSkinAlphaValues,
                                newValue -> config.uiAndVisuals.dontStripSkinAlphaValues = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .flag(OptionFlag.ASSET_RELOAD)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.backpackPreviewWithoutShift"))
                        .binding(defaults.uiAndVisuals.backpackPreviewWithoutShift,
                                () -> config.uiAndVisuals.backpackPreviewWithoutShift,
                                newValue -> config.uiAndVisuals.backpackPreviewWithoutShift = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.hideEmptyTooltips"))
                        .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.hideEmptyTooltips.@Tooltip")))
                        .binding(defaults.uiAndVisuals.hideEmptyTooltips,
                                () -> config.uiAndVisuals.hideEmptyTooltips,
                                newValue -> config.uiAndVisuals.hideEmptyTooltips = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.fancyCraftingTable"))
                        .binding(defaults.uiAndVisuals.fancyCraftingTable,
                                () -> config.uiAndVisuals.fancyCraftingTable,
                                newValue -> config.uiAndVisuals.fancyCraftingTable = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.hideStatusEffectOverlay"))
                        .binding(defaults.uiAndVisuals.hideStatusEffectOverlay,
                                () -> config.uiAndVisuals.hideStatusEffectOverlay,
                                newValue -> config.uiAndVisuals.hideStatusEffectOverlay = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.showEquipmentInInventory"))
                        .binding(defaults.uiAndVisuals.showEquipmentInInventory,
                                () -> config.uiAndVisuals.showEquipmentInInventory,
                                newValue -> config.uiAndVisuals.showEquipmentInInventory = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .build())

                //STP
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.skyblockerTexturePredicates"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.skyblockerTexturePredicates.skyblockItemTextures"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.skyblockerTexturePredicates.note")))
                                .binding(defaults.uiAndVisuals.skyblockerTexturePredicates.skyblockItemTextures,
                                        () -> config.uiAndVisuals.skyblockerTexturePredicates.skyblockItemTextures,
                                        newValue -> config.uiAndVisuals.skyblockerTexturePredicates.skyblockItemTextures = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.skyblockerTexturePredicates.universalItemTextures"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.skyblockerTexturePredicates.note")))
                                .binding(defaults.uiAndVisuals.skyblockerTexturePredicates.universalItemTextures,
                                        () -> config.uiAndVisuals.skyblockerTexturePredicates.universalItemTextures,
                                        newValue -> config.uiAndVisuals.skyblockerTexturePredicates.universalItemTextures = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.skyblockerTexturePredicates.armorTextures"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.skyblockerTexturePredicates.note")))
                                .binding(defaults.uiAndVisuals.skyblockerTexturePredicates.armorTextures,
                                        () -> config.uiAndVisuals.skyblockerTexturePredicates.armorTextures,
                                        newValue -> config.uiAndVisuals.skyblockerTexturePredicates.armorTextures = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.skyblockerTexturePredicates.blockTextures"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.skyblockerTexturePredicates.note")))
                                .binding(defaults.uiAndVisuals.skyblockerTexturePredicates.blockTextures,
                                        () -> config.uiAndVisuals.skyblockerTexturePredicates.blockTextures,
                                        newValue -> config.uiAndVisuals.skyblockerTexturePredicates.blockTextures = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Chest Value FIXME change dropdown to color controller
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.chestValue"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.chestValue.enableChestValue"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.chestValue.enableChestValue.@Tooltip")))
                                .binding(defaults.uiAndVisuals.chestValue.enableChestValue,
                                        () -> config.uiAndVisuals.chestValue.enableChestValue,
                                        newValue -> config.uiAndVisuals.chestValue.enableChestValue = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Formatting>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.chestValue.color"))
                                .binding(defaults.uiAndVisuals.chestValue.color,
                                        () -> config.uiAndVisuals.chestValue.color,
                                        newValue -> config.uiAndVisuals.chestValue.color = newValue)
                                .controller(ConfigUtils.getEnumDropdownControllerFactory(ConfigUtils.FORMATTING_FORMATTER))
                                .build())
                        .option(Option.<Formatting>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.chestValue.incompleteColor"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.chestValue.incompleteColor.@Tooltip")))
                                .binding(defaults.uiAndVisuals.chestValue.incompleteColor,
                                        () -> config.uiAndVisuals.chestValue.incompleteColor,
                                        newValue -> config.uiAndVisuals.chestValue.incompleteColor = newValue)
                                .controller(ConfigUtils.getEnumDropdownControllerFactory(ConfigUtils.FORMATTING_FORMATTER))
                                .build())
                        .build())

                //Item Cooldown
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.itemCooldown"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.itemCooldown.enableItemCooldowns"))
                                .binding(defaults.uiAndVisuals.itemCooldown.enableItemCooldowns,
                                        () -> config.uiAndVisuals.itemCooldown.enableItemCooldowns,
                                        newValue -> config.uiAndVisuals.itemCooldown.enableItemCooldowns = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Title Container
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.titleContainer"))
                        .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.titleContainer.@Tooltip")))
                        .collapsed(true)
                        .option(Option.<Float>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.titleContainer.titleContainerScale"))
                                .binding(defaults.uiAndVisuals.titleContainer.titleContainerScale,
                                        () -> config.uiAndVisuals.titleContainer.titleContainerScale,
                                        newValue -> config.uiAndVisuals.titleContainer.titleContainerScale = newValue)
                                .controller(opt -> FloatFieldControllerBuilder.create(opt).range(30f, 140f))
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.titleContainer.config"))
                                .text(Text.translatable("text.skyblocker.open"))
                                .action((screen, opt) -> MinecraftClient.getInstance().setScreen(new TitleContainerConfigScreen(screen)))
                                .build())
                        .build())

                //Tab Hud
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.tabHud"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.tabHudEnabled"))
                                .binding(defaults.uiAndVisuals.tabHud.tabHudEnabled,
                                        () -> config.uiAndVisuals.tabHud.tabHudEnabled,
                                        newValue -> config.uiAndVisuals.tabHud.tabHudEnabled = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.tabHudScale"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.tabHudScale.@Tooltip")))
                                .binding(defaults.uiAndVisuals.tabHud.tabHudScale,
                                        () -> config.uiAndVisuals.tabHud.tabHudScale,
                                        newValue -> config.uiAndVisuals.tabHud.tabHudScale = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(10, 200).step(1))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.enableHudBackground"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.enableHudBackground.@Tooltip")))
                                .binding(defaults.uiAndVisuals.tabHud.enableHudBackground,
                                        () -> config.uiAndVisuals.tabHud.enableHudBackground,
                                        newValue -> config.uiAndVisuals.tabHud.enableHudBackground = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.plainPlayerNames"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.plainPlayerNames.@Tooltip")))
                                .binding(defaults.uiAndVisuals.tabHud.plainPlayerNames,
                                        () -> config.uiAndVisuals.tabHud.plainPlayerNames,
                                        newValue -> config.uiAndVisuals.tabHud.plainPlayerNames = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<UIAndVisualsConfig.NameSorting>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.nameSorting"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.nameSorting.@Tooltip")))
                                .binding(defaults.uiAndVisuals.tabHud.nameSorting,
                                        () -> config.uiAndVisuals.tabHud.nameSorting,
                                        newValue -> config.uiAndVisuals.tabHud.nameSorting = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .build())

                // Fancy Auction House
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.fancyAuctionHouse"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.fancyAuctionHouse.enabled"))
                                .binding(defaults.uiAndVisuals.fancyAuctionHouse.enabled,
                                        () -> config.uiAndVisuals.fancyAuctionHouse.enabled,
                                        newValue -> config.uiAndVisuals.fancyAuctionHouse.enabled = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.fancyAuctionHouse.highlightUnderAvgPrice"))
                                .binding(defaults.uiAndVisuals.fancyAuctionHouse.highlightCheapBIN,
                                        () -> config.uiAndVisuals.fancyAuctionHouse.highlightCheapBIN,
                                        newValue -> config.uiAndVisuals.fancyAuctionHouse.highlightCheapBIN = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Fancy Bars
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.bars"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.bars.enableBars"))
                                .binding(defaults.uiAndVisuals.bars.enableBars,
                                        () -> config.uiAndVisuals.bars.enableBars,
                                        newValue -> config.uiAndVisuals.bars.enableBars = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.bars.openScreen"))
                                .text(Text.translatable("text.skyblocker.open"))
                                .action((screen, opt) -> MinecraftClient.getInstance().setScreen(new StatusBarsConfigScreen()))
                                .build())
                        .build())

                //Waypoints
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.waypoints"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.enableWaypoints"))
                                .binding(defaults.uiAndVisuals.waypoints.enableWaypoints,
                                        () -> config.uiAndVisuals.waypoints.enableWaypoints,
                                        newValue -> config.uiAndVisuals.waypoints.enableWaypoints = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Waypoint.Type>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.waypointType"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.waypointType.@Tooltip"),
                                        Text.translatable("skyblocker.config.uiAndVisuals.waypoints.waypointType.generalNote")))
                                .binding(defaults.uiAndVisuals.waypoints.waypointType,
                                        () -> config.uiAndVisuals.waypoints.waypointType,
                                        newValue -> config.uiAndVisuals.waypoints.waypointType = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("skyblocker.waypoints.config"))
                                .text(Text.translatable("text.skyblocker.open"))
                                .action((screen, opt) -> MinecraftClient.getInstance().setScreen(new WaypointsScreen(screen)))
                                .build())
                        .build())

                //Teleport Overlays
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.teleportOverlay"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.teleportOverlay.enableTeleportOverlays"))
                                .binding(defaults.uiAndVisuals.teleportOverlay.enableTeleportOverlays,
                                        () -> config.uiAndVisuals.teleportOverlay.enableTeleportOverlays,
                                        newValue -> config.uiAndVisuals.teleportOverlay.enableTeleportOverlays = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.teleportOverlay.enableWeirdTransmission"))
                                .binding(defaults.uiAndVisuals.teleportOverlay.enableWeirdTransmission,
                                        () -> config.uiAndVisuals.teleportOverlay.enableWeirdTransmission,
                                        newValue -> config.uiAndVisuals.teleportOverlay.enableWeirdTransmission = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.teleportOverlay.enableInstantTransmission"))
                                .binding(defaults.uiAndVisuals.teleportOverlay.enableInstantTransmission,
                                        () -> config.uiAndVisuals.teleportOverlay.enableInstantTransmission,
                                        newValue -> config.uiAndVisuals.teleportOverlay.enableInstantTransmission = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.teleportOverlay.enableEtherTransmission"))
                                .binding(defaults.uiAndVisuals.teleportOverlay.enableEtherTransmission,
                                        () -> config.uiAndVisuals.teleportOverlay.enableEtherTransmission,
                                        newValue -> config.uiAndVisuals.teleportOverlay.enableEtherTransmission = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.teleportOverlay.enableSinrecallTransmission"))
                                .binding(defaults.uiAndVisuals.teleportOverlay.enableSinrecallTransmission,
                                        () -> config.uiAndVisuals.teleportOverlay.enableSinrecallTransmission,
                                        newValue -> config.uiAndVisuals.teleportOverlay.enableSinrecallTransmission = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.teleportOverlay.enableWitherImpact"))
                                .binding(defaults.uiAndVisuals.teleportOverlay.enableWitherImpact,
                                        () -> config.uiAndVisuals.teleportOverlay.enableWitherImpact,
                                        newValue -> config.uiAndVisuals.teleportOverlay.enableWitherImpact = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Search overlay
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.enableBazaar"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.enableBazaar.@Tooltip")))
                                .binding(defaults.uiAndVisuals.searchOverlay.enableBazaar,
                                        () -> config.uiAndVisuals.searchOverlay.enableBazaar,
                                        newValue -> config.uiAndVisuals.searchOverlay.enableBazaar = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.enableAuctionHouse"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.enableAuctionHouse.@Tooltip")))
                                .binding(defaults.uiAndVisuals.searchOverlay.enableAuctionHouse,
                                        () -> config.uiAndVisuals.searchOverlay.enableAuctionHouse,
                                        newValue -> config.uiAndVisuals.searchOverlay.enableAuctionHouse = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.keepPreviousSearches"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.keepPreviousSearches.@Tooltip")))
                                .binding(defaults.uiAndVisuals.searchOverlay.keepPreviousSearches,
                                        () -> config.uiAndVisuals.searchOverlay.keepPreviousSearches,
                                        newValue -> config.uiAndVisuals.searchOverlay.keepPreviousSearches = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.maxSuggestions"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.maxSuggestions.@Tooltip")))
                                .binding(defaults.uiAndVisuals.searchOverlay.maxSuggestions,
                                        () -> config.uiAndVisuals.searchOverlay.maxSuggestions,
                                        newValue -> config.uiAndVisuals.searchOverlay.maxSuggestions = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 5).step(1))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.historyLength"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.historyLength.@Tooltip")))
                                .binding(defaults.uiAndVisuals.searchOverlay.historyLength,
                                        () -> config.uiAndVisuals.searchOverlay.historyLength,
                                        newValue -> config.uiAndVisuals.searchOverlay.historyLength = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 5).step(1))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.enableCommands"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.enableCommands.@Tooltip")))
                                .binding(defaults.uiAndVisuals.searchOverlay.enableCommands,
                                        () -> config.uiAndVisuals.searchOverlay.enableCommands,
                                        newValue -> config.uiAndVisuals.searchOverlay.enableCommands = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Input Calculator
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.inputCalculator"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.inputCalculator.enabled"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.inputCalculator.enabled.@Tooltip")))
                                .binding(defaults.uiAndVisuals.inputCalculator.enabled,
                                        () -> config.uiAndVisuals.inputCalculator.enabled,
                                        newValue -> config.uiAndVisuals.inputCalculator.enabled = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.inputCalculator.requiresEquals"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.inputCalculator.requiresEquals.@Tooltip")))
                                .binding(defaults.uiAndVisuals.inputCalculator.requiresEquals,
                                        () -> config.uiAndVisuals.inputCalculator.requiresEquals,
                                        newValue -> config.uiAndVisuals.inputCalculator.requiresEquals = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Flame Overlay
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.flameOverlay"))
                        .collapsed(true)
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.flameOverlay.flameHeight"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.flameOverlay.flameHeight.@Tooltip")))
                                .binding(defaults.uiAndVisuals.flameOverlay.flameHeight,
                                        () -> config.uiAndVisuals.flameOverlay.flameHeight,
                                        newValue -> config.uiAndVisuals.flameOverlay.flameHeight = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 100).step(1))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.flameOverlay.flameOpacity"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.flameOverlay.flameOpacity.@Tooltip")))
                                .binding(defaults.uiAndVisuals.flameOverlay.flameOpacity,
                                        () -> config.uiAndVisuals.flameOverlay.flameOpacity,
                                        newValue -> config.uiAndVisuals.flameOverlay.flameOpacity = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 100).step(1))
                                .build())
                        .build())

                //Compact Damage Numbers
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.compactDamage"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.compactDamage.enabled"))
                                .binding(defaults.uiAndVisuals.compactDamage.enabled,
                                        () -> config.uiAndVisuals.compactDamage.enabled,
                                        newValue -> config.uiAndVisuals.compactDamage.enabled = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.compactDamage.precision"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.uiAndVisuals.compactDamage.precision.@Tooltip")))
                                .binding(defaults.uiAndVisuals.compactDamage.precision,
                                        () -> config.uiAndVisuals.compactDamage.precision,
                                        newValue -> config.uiAndVisuals.compactDamage.precision = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1,3).step(1))
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.compactDamage.normalDamageColor"))
                                .binding(defaults.uiAndVisuals.compactDamage.normalDamageColor,
                                        () -> config.uiAndVisuals.compactDamage.normalDamageColor,
                                        newValue -> config.uiAndVisuals.compactDamage.normalDamageColor = newValue)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.compactDamage.critDamageGradientStart"))
                                .binding(defaults.uiAndVisuals.compactDamage.critDamageGradientStart,
                                        () -> config.uiAndVisuals.compactDamage.critDamageGradientStart,
                                        newValue -> config.uiAndVisuals.compactDamage.critDamageGradientStart = newValue)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.compactDamage.critDamageGradientEnd"))
                                .binding(defaults.uiAndVisuals.compactDamage.critDamageGradientEnd,
                                        () -> config.uiAndVisuals.compactDamage.critDamageGradientEnd,
                                        newValue -> config.uiAndVisuals.compactDamage.critDamageGradientEnd = newValue)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .build()
                )

                .build();
    }
}
