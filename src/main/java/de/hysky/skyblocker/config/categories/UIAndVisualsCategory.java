package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.CommonTags;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.skyblock.GyroOverlay;
import de.hysky.skyblocker.skyblock.ItemPickupWidget;
import de.hysky.skyblocker.skyblock.teleport.TeleportOverlay;
import de.hysky.skyblocker.skyblock.fancybars.StatusBarsConfigScreen;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextManager;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextMode;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.waypoint.WaypointsScreen;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.container.SlotTextAdder;
import de.hysky.skyblocker.utils.render.title.TitleContainerConfigScreen;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.azureaaron.dandelion.systems.ButtonOption;
import net.azureaaron.dandelion.systems.ConfigCategory;
import net.azureaaron.dandelion.systems.LabelOption;
import net.azureaaron.dandelion.systems.Option;
import net.azureaaron.dandelion.systems.OptionFlag;
import net.azureaaron.dandelion.systems.OptionGroup;
import net.azureaaron.dandelion.systems.controllers.ColourController;
import net.azureaaron.dandelion.systems.controllers.FloatController;
import net.azureaaron.dandelion.systems.controllers.IntegerController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.SystemKeycodes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class UIAndVisualsCategory {
    public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
        return ConfigCategory.createBuilder()
        		.id(SkyblockerMod.id("config/uiandvisuals"))
				.name(Text.translatable("skyblocker.config.uiAndVisuals"))

                //Ungrouped Options
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.uiAndVisuals.swingOnAbilities"))
						.description(Text.translatable("skyblocker.config.uiAndVisuals.swingOnAbilities.@Tooltip"))
						.binding(defaults.uiAndVisuals.swingOnAbilities,
								() -> config.uiAndVisuals.swingOnAbilities,
								newValue -> config.uiAndVisuals.swingOnAbilities = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Integer>createBuilder()
						.name(Text.translatable("skyblocker.config.uiAndVisuals.nightVisionStrength"))
						.description(Text.translatable("skyblocker.config.uiAndVisuals.nightVisionStrength.@Tooltip"))
						.binding(defaults.uiAndVisuals.nightVisionStrength,
								() -> config.uiAndVisuals.nightVisionStrength,
								newValue -> config.uiAndVisuals.nightVisionStrength = newValue)
						.controller(IntegerController.createBuilder().range(0, 100).slider(1).build())
						.build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.compactorDeletorPreview"))
                        .binding(defaults.uiAndVisuals.compactorDeletorPreview,
                                () -> config.uiAndVisuals.compactorDeletorPreview,
                                newValue -> config.uiAndVisuals.compactorDeletorPreview = newValue)
                        .controller(ConfigUtils.createBooleanController())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.dontStripSkinAlphaValues"))
                        .description(Text.translatable("skyblocker.config.uiAndVisuals.dontStripSkinAlphaValues.@Tooltip"))
                        .binding(defaults.uiAndVisuals.dontStripSkinAlphaValues,
                                () -> config.uiAndVisuals.dontStripSkinAlphaValues,
                                newValue -> config.uiAndVisuals.dontStripSkinAlphaValues = newValue)
                        .controller(ConfigUtils.createBooleanController())
                        .flags(OptionFlag.ASSET_RELOAD)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.backpackPreviewWithoutShift"))
                        .binding(defaults.uiAndVisuals.backpackPreviewWithoutShift,
                                () -> config.uiAndVisuals.backpackPreviewWithoutShift,
                                newValue -> config.uiAndVisuals.backpackPreviewWithoutShift = newValue)
                        .controller(ConfigUtils.createBooleanController())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.hideEmptyTooltips"))
                        .description(Text.translatable("skyblocker.config.uiAndVisuals.hideEmptyTooltips.@Tooltip"))
                        .binding(defaults.uiAndVisuals.hideEmptyTooltips,
                                () -> config.uiAndVisuals.hideEmptyTooltips,
                                newValue -> config.uiAndVisuals.hideEmptyTooltips = newValue)
                        .controller(ConfigUtils.createBooleanController())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.fancyCraftingTable"))
                        .binding(defaults.uiAndVisuals.fancyCraftingTable,
                                () -> config.uiAndVisuals.fancyCraftingTable,
                                newValue -> config.uiAndVisuals.fancyCraftingTable = newValue)
                        .controller(ConfigUtils.createBooleanController())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.hideStatusEffectOverlay"))
                        .binding(defaults.uiAndVisuals.hideStatusEffectOverlay,
                                () -> config.uiAndVisuals.hideStatusEffectOverlay,
                                newValue -> config.uiAndVisuals.hideStatusEffectOverlay = newValue)
                        .controller(ConfigUtils.createBooleanController())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.showEquipmentInInventory"))
                        .binding(defaults.uiAndVisuals.showEquipmentInInventory,
                                () -> config.uiAndVisuals.showEquipmentInInventory,
                                newValue -> config.uiAndVisuals.showEquipmentInInventory = newValue)
                        .controller(ConfigUtils.createBooleanController())
                        .build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.uiAndVisuals.museumOverlay"))
						.description(Text.translatable("skyblocker.config.uiAndVisuals.museumOverlay.@Tooltip"))
						.binding(defaults.uiAndVisuals.museumOverlay,
								() -> config.uiAndVisuals.museumOverlay,
								newValue -> config.uiAndVisuals.museumOverlay = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.cancelComponentUpdateAnimation"))
                        .description(Text.translatable("skyblocker.config.uiAndVisuals.cancelComponentUpdateAnimation.@Tooltip"))
                        .binding(defaults.uiAndVisuals.cancelComponentUpdateAnimation,
                                () -> config.uiAndVisuals.cancelComponentUpdateAnimation,
                                newValue -> config.uiAndVisuals.cancelComponentUpdateAnimation = newValue)
                        .controller(ConfigUtils.createBooleanController())
                        .build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.uiAndVisuals.showCustomizeButton"))
						.description(Text.translatable("skyblocker.config.uiAndVisuals.showCustomizeButton.@Tooltip"))
						.binding(defaults.uiAndVisuals.showCustomizeButton,
								() -> config.uiAndVisuals.showCustomizeButton,
								newValue -> config.uiAndVisuals.showCustomizeButton = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.uiAndVisuals.showConfigButton"))
						.description(Text.translatable("skyblocker.config.uiAndVisuals.showConfigButton.@Tooltip"))
						.binding(defaults.uiAndVisuals.showConfigButton,
								() -> config.uiAndVisuals.showConfigButton,
								newValue -> config.uiAndVisuals.showConfigButton = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.uiAndVisuals.trueQuiverCount"))
						.description(Text.translatable("skyblocker.config.uiAndVisuals.trueQuiverCount.@Tooltip"))
						.binding(defaults.uiAndVisuals.trueQuiverCount,
								() -> config.uiAndVisuals.trueQuiverCount,
								newValue -> config.uiAndVisuals.trueQuiverCount = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())

                //Chest Value FIXME change dropdown to color controller
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.chestValue"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.chestValue.enableChestValue"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.chestValue.enableChestValue.@Tooltip"))
                                .binding(defaults.uiAndVisuals.chestValue.enableChestValue,
                                        () -> config.uiAndVisuals.chestValue.enableChestValue,
                                        newValue -> config.uiAndVisuals.chestValue.enableChestValue = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Formatting>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.chestValue.color"))
                                .binding(defaults.uiAndVisuals.chestValue.color,
                                        () -> config.uiAndVisuals.chestValue.color,
                                        newValue -> config.uiAndVisuals.chestValue.color = newValue)
                                .controller(ConfigUtils.createEnumDropdownController(ConfigUtils.FORMATTING_FORMATTER))
                                .build())
                        .option(Option.<Formatting>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.chestValue.incompleteColor"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.chestValue.incompleteColor.@Tooltip"))
                                .binding(defaults.uiAndVisuals.chestValue.incompleteColor,
                                        () -> config.uiAndVisuals.chestValue.incompleteColor,
                                        newValue -> config.uiAndVisuals.chestValue.incompleteColor = newValue)
                                .controller(ConfigUtils.createEnumDropdownController(ConfigUtils.FORMATTING_FORMATTER))
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
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .build())

				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.uiAndVisuals.slotText"))
						.collapsed(true)
						.option(Option.<SlotTextMode>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.slotText"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.slotText.@Tooltip"))
								.binding(defaults.uiAndVisuals.slotText.slotTextMode,
										() -> config.uiAndVisuals.slotText.slotTextMode,
										newValue -> config.uiAndVisuals.slotText.slotTextMode = newValue)
								.controller(ConfigUtils.createEnumController())
								.build())
						.option(ConfigUtils.createShortcutToKeybindsScreen())
						.option(LabelOption.createBuilder()
								.label(Text.translatable("skyblocker.config.uiAndVisuals.slotText.separator"))
								.build())
						.options(createSlotTextToggles(config))
						.build()
				)

                // Inventory Search
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.inventorySearch"))
                        .collapsed(true)
                        .option(Option.<UIAndVisualsConfig.InventorySearchConfig.EnableState>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.inventorySearch.enabled"))
                                .binding(defaults.uiAndVisuals.inventorySearch.enabled,
                                        () -> config.uiAndVisuals.inventorySearch.enabled,
                                        newValue -> config.uiAndVisuals.inventorySearch.enabled = newValue)
                                .controller(ConfigUtils.createEnumController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(SystemKeycodes.IS_MAC_OS ? Text.translatable("skyblocker.config.uiAndVisuals.inventorySearch.cmdK") : Text.translatable("skyblocker.config.uiAndVisuals.inventorySearch.ctrlK"))
                                .binding(defaults.uiAndVisuals.inventorySearch.ctrlK,
                                        () -> config.uiAndVisuals.inventorySearch.ctrlK,
                                        newValue -> config.uiAndVisuals.inventorySearch.ctrlK = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.inventorySearch.showClickableText"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.inventorySearch.showClickableText.@Tooltip"))
                                .binding(defaults.uiAndVisuals.inventorySearch.clickableText,
                                        () -> config.uiAndVisuals.inventorySearch.clickableText,
                                        newValue -> config.uiAndVisuals.inventorySearch.clickableText = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .build())

                //Title Container
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.titleContainer"))
                        .description(Text.translatable("skyblocker.config.uiAndVisuals.titleContainer.@Tooltip"))
                        .collapsed(true)
                        .option(Option.<Float>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.titleContainer.titleContainerScale"))
                                .binding(defaults.uiAndVisuals.titleContainer.titleContainerScale,
                                        () -> config.uiAndVisuals.titleContainer.titleContainerScale,
                                        newValue -> config.uiAndVisuals.titleContainer.titleContainerScale = newValue)
                                .controller(FloatController.createBuilder().range(TitleContainerConfigScreen.MIN_TITLE_SCALE, TitleContainerConfigScreen.MAX_TITLE_SCALE).build())
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.titleContainer.config"))
                                .prompt(Text.translatable("text.skyblocker.open"))
                                .action(screen -> MinecraftClient.getInstance().setScreen(new TitleContainerConfigScreen(screen)))
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
                                .controller(ConfigUtils.createBooleanController())
                                .build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.configScreen"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.configScreen.@Tooltip"))
								.tags(Text.literal("gui"))
								.prompt(Text.translatable("text.skyblocker.open"))
								.action(screen -> {
									if (Utils.isOnSkyblock() && config.uiAndVisuals.tabHud.tabHudEnabled) {
										MessageScheduler.INSTANCE.sendMessageAfterCooldown("/widgets", true);
									} else {
										MinecraftClient.getInstance().setScreen(new WidgetsConfigurationScreen(Location.HUB, WidgetManager.ScreenLayer.MAIN_TAB, screen));
									}
								})
								.build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.tabHudScale"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.tabHudScale.@Tooltip"))
                                .binding(defaults.uiAndVisuals.tabHud.tabHudScale,
                                        () -> config.uiAndVisuals.tabHud.tabHudScale,
                                        newValue -> config.uiAndVisuals.tabHud.tabHudScale = newValue)
                                .controller(IntegerController.createBuilder().range(10, 200).slider(1).build())
                                .build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.showVanillaTabByDefault"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.showVanillaTabByDefault.@Tooltip"))
								.binding(defaults.uiAndVisuals.tabHud.showVanillaTabByDefault,
										() -> config.uiAndVisuals.tabHud.showVanillaTabByDefault,
										newValue -> config.uiAndVisuals.tabHud.showVanillaTabByDefault = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<UIAndVisualsConfig.TabHudStyle>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.style"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.style.@Tooltip[0]"),
										Text.translatable("skyblocker.config.uiAndVisuals.tabHud.style.@Tooltip[1]"),
										Text.translatable("skyblocker.config.uiAndVisuals.tabHud.style.@Tooltip[2]"),
										Text.translatable("skyblocker.config.uiAndVisuals.tabHud.style.@Tooltip[3]"))
								.binding(defaults.uiAndVisuals.tabHud.style,
										() -> config.uiAndVisuals.tabHud.style,
										newValue -> config.uiAndVisuals.tabHud.style = newValue)
								.controller(ConfigUtils.createEnumController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.displayIcons"))
								.binding(defaults.uiAndVisuals.tabHud.displayIcons,
										() -> config.uiAndVisuals.tabHud.displayIcons,
										newValue -> config.uiAndVisuals.tabHud.displayIcons = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.compactWidgets"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.compactWidgets.@Tooltip"))
								.binding(defaults.uiAndVisuals.tabHud.compactWidgets,
										() -> config.uiAndVisuals.tabHud.compactWidgets,
										newValue -> config.uiAndVisuals.tabHud.compactWidgets = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.enableHudBackground"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.enableHudBackground.@Tooltip"))
                                .binding(defaults.uiAndVisuals.tabHud.enableHudBackground,
                                        () -> config.uiAndVisuals.tabHud.enableHudBackground,
                                        newValue -> config.uiAndVisuals.tabHud.enableHudBackground = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.effectsFooter"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.effectsFooter.@Tooltip"))
                                .controller(ConfigUtils.createBooleanController())
                                .binding(defaults.uiAndVisuals.tabHud.effectsFromFooter,
                                        () -> config.uiAndVisuals.tabHud.effectsFromFooter,
                                        newValue -> config.uiAndVisuals.tabHud.effectsFromFooter = newValue)
                                .build())
                        .option(Option.<ScreenBuilder.DefaultPositioner>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.defaultPositioning"))
                                .binding(defaults.uiAndVisuals.tabHud.defaultPositioning,
                                        () -> config.uiAndVisuals.tabHud.defaultPositioning,
                                        newValue -> config.uiAndVisuals.tabHud.defaultPositioning = newValue)
                                .controller(ConfigUtils.createEnumController())
                                .build()
                        )
						.option(Option.<UIAndVisualsConfig.NameSorting>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.nameSorting"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.nameSorting.@Tooltip"))
								.binding(defaults.uiAndVisuals.tabHud.nameSorting,
										() -> config.uiAndVisuals.tabHud.nameSorting,
										newValue -> config.uiAndVisuals.tabHud.nameSorting = newValue)
								.controller(ConfigUtils.createEnumController())
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
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.fancyAuctionHouse.highlightUnderAvgPrice"))
                                .binding(defaults.uiAndVisuals.fancyAuctionHouse.highlightCheapBIN,
                                        () -> config.uiAndVisuals.fancyAuctionHouse.highlightCheapBIN,
                                        newValue -> config.uiAndVisuals.fancyAuctionHouse.highlightCheapBIN = newValue)
                                .controller(ConfigUtils.createBooleanController())
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
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.bars.openScreen"))
                                .prompt(Text.translatable("text.skyblocker.open"))
                                .action(screen -> MinecraftClient.getInstance().setScreen(new StatusBarsConfigScreen()))
                                .build())
						.option(Option.<UIAndVisualsConfig.IntelligenceDisplay>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.bars.intelligenceDisplay"))
								.binding(defaults.uiAndVisuals.bars.intelligenceDisplay,
										() -> config.uiAndVisuals.bars.intelligenceDisplay,
										newValue -> config.uiAndVisuals.bars.intelligenceDisplay = newValue)
								.controller(ConfigUtils.createEnumController(intelligenceDisplay -> Text.translatable("skyblocker.config.uiAndVisuals.bars.intelligenceDisplay." + intelligenceDisplay.name())))
								.build()
						)
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
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Waypoint.Type>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.waypointType"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.waypointType.@Tooltip"),
                                        Text.translatable("skyblocker.config.uiAndVisuals.waypoints.waypointType.generalNote"))
                                .binding(defaults.uiAndVisuals.waypoints.waypointType,
                                        () -> config.uiAndVisuals.waypoints.waypointType,
                                        newValue -> config.uiAndVisuals.waypoints.waypointType = newValue)
                                .controller(ConfigUtils.createEnumController())
                                .build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.renderLine"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.renderLine.@Tooltip"))
								.tags(CommonTags.ADDED_IN_5_9_0)
								.binding(defaults.uiAndVisuals.waypoints.renderLine,
										() -> config.uiAndVisuals.waypoints.renderLine,
										newValue -> config.uiAndVisuals.waypoints.renderLine = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.lineColor"))
								.tags(CommonTags.ADDED_IN_5_9_0)
								.binding(defaults.uiAndVisuals.waypoints.lineColor,
										() -> config.uiAndVisuals.waypoints.lineColor,
										newValue -> config.uiAndVisuals.waypoints.lineColor = newValue)
								.controller(ConfigUtils.createColourController(true))
								.build())
						.option(Option.<Float>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.lineWidth"))
								.tags(CommonTags.ADDED_IN_5_9_0)
								.binding(defaults.uiAndVisuals.waypoints.lineWidth,
										() -> config.uiAndVisuals.waypoints.lineWidth,
										newValue -> config.uiAndVisuals.waypoints.lineWidth = newValue)
								.controller(FloatController.createBuilder().range(1f, 15f).slider(0.5f).build())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.allowSkippingWaypoints"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.allowSkippingWaypoints.@Tooltip"))
								.tags(CommonTags.ADDED_IN_5_9_0)
								.binding(defaults.uiAndVisuals.waypoints.allowSkippingWaypoints,
										() -> config.uiAndVisuals.waypoints.allowSkippingWaypoints,
										newValue -> config.uiAndVisuals.waypoints.allowSkippingWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.allowGoingBackwards"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.allowGoingBackwards.@Tooltip"))
								.tags(CommonTags.ADDED_IN_5_9_0)
								.binding(defaults.uiAndVisuals.waypoints.allowGoingBackwards,
										() -> config.uiAndVisuals.waypoints.allowGoingBackwards,
										newValue -> config.uiAndVisuals.waypoints.allowGoingBackwards = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("skyblocker.waypoints.config"))
                                .prompt(Text.translatable("text.skyblocker.open"))
                                .action(screen -> MinecraftClient.getInstance().setScreen(new WaypointsScreen(screen)))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                        		.name(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.enableChatWaypoints"))
                        		.description(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.enableChatWaypoints.@Tooltip"))
                        		.binding(defaults.uiAndVisuals.waypoints.enableChatWaypoints,
                        				() -> config.uiAndVisuals.waypoints.enableChatWaypoints,
                        				newValue -> config.uiAndVisuals.waypoints.enableChatWaypoints = newValue)
                        		.controller(ConfigUtils.createBooleanController())
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
                                .controller(ConfigUtils.createBooleanController())
                                .build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.teleportOverlay.showWhenInAir"))
								.binding(defaults.uiAndVisuals.teleportOverlay.showWhenInAir,
										() -> config.uiAndVisuals.teleportOverlay.showWhenInAir,
										newValue -> config.uiAndVisuals.teleportOverlay.showWhenInAir = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.teleportOverlay.teleportOverlayColor"))
                                .binding(defaults.uiAndVisuals.teleportOverlay.teleportOverlayColor,
                                        () -> config.uiAndVisuals.teleportOverlay.teleportOverlayColor,
                                        newValue -> {
                                            config.uiAndVisuals.teleportOverlay.teleportOverlayColor = newValue;
                                            TeleportOverlay.configCallback(newValue);
                                       })
                                .controller(ColourController.createBuilder().hasAlpha(true).build())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.teleportOverlay.enableWeirdTransmission"))
                                .binding(defaults.uiAndVisuals.teleportOverlay.enableWeirdTransmission,
                                        () -> config.uiAndVisuals.teleportOverlay.enableWeirdTransmission,
                                        newValue -> config.uiAndVisuals.teleportOverlay.enableWeirdTransmission = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.teleportOverlay.enableInstantTransmission"))
                                .binding(defaults.uiAndVisuals.teleportOverlay.enableInstantTransmission,
                                        () -> config.uiAndVisuals.teleportOverlay.enableInstantTransmission,
                                        newValue -> config.uiAndVisuals.teleportOverlay.enableInstantTransmission = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.teleportOverlay.enableEtherTransmission"))
                                .binding(defaults.uiAndVisuals.teleportOverlay.enableEtherTransmission,
                                        () -> config.uiAndVisuals.teleportOverlay.enableEtherTransmission,
                                        newValue -> config.uiAndVisuals.teleportOverlay.enableEtherTransmission = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.teleportOverlay.enableSinrecallTransmission"))
                                .binding(defaults.uiAndVisuals.teleportOverlay.enableSinrecallTransmission,
                                        () -> config.uiAndVisuals.teleportOverlay.enableSinrecallTransmission,
                                        newValue -> config.uiAndVisuals.teleportOverlay.enableSinrecallTransmission = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.teleportOverlay.enableWitherImpact"))
                                .binding(defaults.uiAndVisuals.teleportOverlay.enableWitherImpact,
                                        () -> config.uiAndVisuals.teleportOverlay.enableWitherImpact,
                                        newValue -> config.uiAndVisuals.teleportOverlay.enableWitherImpact = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .build())

                //Smooth AOTE
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.smoothAOTE"))
                        .description(Text.translatable("skyblocker.config.uiAndVisuals.smoothAOTE.@Tooltip"))
                        .collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.smoothAOTE.predictive"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.smoothAOTE.predictive.@Tooltip"))
								.binding(defaults.uiAndVisuals.smoothAOTE.predictive,
										() -> config.uiAndVisuals.smoothAOTE.predictive,
										newValue -> config.uiAndVisuals.smoothAOTE.predictive = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.smoothAOTE.enableWeirdTransmission"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.smoothAOTE.enableWeirdTransmission.@Tooltip"))
                                .binding(defaults.uiAndVisuals.smoothAOTE.enableWeirdTransmission,
                                        () -> config.uiAndVisuals.smoothAOTE.enableWeirdTransmission,
                                        newValue -> config.uiAndVisuals.smoothAOTE.enableWeirdTransmission = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.smoothAOTE.enableInstantTransmission"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.smoothAOTE.enableInstantTransmission.@Tooltip"))
                                .binding(defaults.uiAndVisuals.smoothAOTE.enableInstantTransmission,
                                        () -> config.uiAndVisuals.smoothAOTE.enableInstantTransmission,
                                        newValue -> config.uiAndVisuals.smoothAOTE.enableInstantTransmission = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.smoothAOTE.enableEtherTransmission"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.smoothAOTE.enableEtherTransmission.@Tooltip"))
                                .binding(defaults.uiAndVisuals.smoothAOTE.enableEtherTransmission,
                                        () -> config.uiAndVisuals.smoothAOTE.enableEtherTransmission,
                                        newValue -> config.uiAndVisuals.smoothAOTE.enableEtherTransmission = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.smoothAOTE.enableSinrecallTransmission"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.smoothAOTE.enableSinrecallTransmission.@Tooltip"))
                                .binding(defaults.uiAndVisuals.smoothAOTE.enableSinrecallTransmission,
                                        () -> config.uiAndVisuals.smoothAOTE.enableSinrecallTransmission,
                                        newValue -> config.uiAndVisuals.smoothAOTE.enableSinrecallTransmission = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.smoothAOTE.enableWitherImpact"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.smoothAOTE.enableWitherImpact.@Tooltip"))
                                .binding(defaults.uiAndVisuals.smoothAOTE.enableWitherImpact,
                                        () -> config.uiAndVisuals.smoothAOTE.enableWitherImpact,
                                        newValue -> config.uiAndVisuals.smoothAOTE.enableWitherImpact = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.smoothAOTE.maximumAddedLag"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.smoothAOTE.maximumAddedLag.@Tooltip"))
								.binding(defaults.uiAndVisuals.smoothAOTE.maximumAddedLag,
										() -> config.uiAndVisuals.smoothAOTE.maximumAddedLag,
										newValue -> config.uiAndVisuals.smoothAOTE.maximumAddedLag = newValue)
								.controller(IntegerController.createBuilder().range(0, 500).slider(1).build())
								.build())
                        .build())

                //Search overlay
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.enableBazaar"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.enableBazaar.@Tooltip"))
                                .binding(defaults.uiAndVisuals.searchOverlay.enableBazaar,
                                        () -> config.uiAndVisuals.searchOverlay.enableBazaar,
                                        newValue -> config.uiAndVisuals.searchOverlay.enableBazaar = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.enableAuctionHouse"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.enableAuctionHouse.@Tooltip"))
                                .binding(defaults.uiAndVisuals.searchOverlay.enableAuctionHouse,
                                        () -> config.uiAndVisuals.searchOverlay.enableAuctionHouse,
                                        newValue -> config.uiAndVisuals.searchOverlay.enableAuctionHouse = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.enableMuseum"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.enableMuseum.@Tooltip"))
								.binding(defaults.uiAndVisuals.searchOverlay.enableMuseum,
										() -> config.uiAndVisuals.searchOverlay.enableMuseum,
										newValue -> config.uiAndVisuals.searchOverlay.enableMuseum = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.keepPreviousSearches"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.keepPreviousSearches.@Tooltip"))
                                .binding(defaults.uiAndVisuals.searchOverlay.keepPreviousSearches,
                                        () -> config.uiAndVisuals.searchOverlay.keepPreviousSearches,
                                        newValue -> config.uiAndVisuals.searchOverlay.keepPreviousSearches = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.maxSuggestions"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.maxSuggestions.@Tooltip"))
                                .binding(defaults.uiAndVisuals.searchOverlay.maxSuggestions,
                                        () -> config.uiAndVisuals.searchOverlay.maxSuggestions,
                                        newValue -> config.uiAndVisuals.searchOverlay.maxSuggestions = newValue)
                                .controller(IntegerController.createBuilder().range(0, 5).slider(1).build())
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.historyLength"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.historyLength.@Tooltip"))
                                .binding(defaults.uiAndVisuals.searchOverlay.historyLength,
                                        () -> config.uiAndVisuals.searchOverlay.historyLength,
                                        newValue -> config.uiAndVisuals.searchOverlay.historyLength = newValue)
                                .controller(IntegerController.createBuilder().range(0, 5).slider(1).build())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.enableCommands"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.searchOverlay.enableCommands.@Tooltip"))
                                .binding(defaults.uiAndVisuals.searchOverlay.enableCommands,
                                        () -> config.uiAndVisuals.searchOverlay.enableCommands,
                                        newValue -> config.uiAndVisuals.searchOverlay.enableCommands = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .build())

				// Bazaar Quick Quantities
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.uiAndVisuals.bazaarQuickQuantities"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.bazaarQuickQuantities.enabled"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.bazaarQuickQuantities.enabled.@Tooltip"))
								.binding(defaults.uiAndVisuals.bazaarQuickQuantities.enabled,
										() -> config.uiAndVisuals.bazaarQuickQuantities.enabled,
										newValue -> config.uiAndVisuals.bazaarQuickQuantities.enabled = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.bazaarQuickQuantities.closeSignOnUse"))
								.binding(defaults.uiAndVisuals.bazaarQuickQuantities.closeSignOnUse,
										() -> config.uiAndVisuals.bazaarQuickQuantities.closeSignOnUse,
										newValue -> config.uiAndVisuals.bazaarQuickQuantities.closeSignOnUse = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.bazaarQuickQuantities.slotQuantity", 1))
								.binding(defaults.uiAndVisuals.bazaarQuickQuantities.slot1Quantity,
										() -> config.uiAndVisuals.bazaarQuickQuantities.slot1Quantity,
										newValue -> config.uiAndVisuals.bazaarQuickQuantities.slot1Quantity = newValue)
								.controller(IntegerController.createBuilder().range(1, 71680).build())
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.bazaarQuickQuantities.slotQuantity", 2))
								.binding(defaults.uiAndVisuals.bazaarQuickQuantities.slot2Quantity,
										() -> config.uiAndVisuals.bazaarQuickQuantities.slot2Quantity,
										newValue -> config.uiAndVisuals.bazaarQuickQuantities.slot2Quantity = newValue)
								.controller(IntegerController.createBuilder().range(1, 71680).build())
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.bazaarQuickQuantities.slotQuantity", 3))
								.binding(defaults.uiAndVisuals.bazaarQuickQuantities.slot3Quantity,
										() -> config.uiAndVisuals.bazaarQuickQuantities.slot3Quantity,
										newValue -> config.uiAndVisuals.bazaarQuickQuantities.slot3Quantity = newValue)
								.controller(IntegerController.createBuilder().range(1, 71680).build())
								.build())
						.build())

                //Input Calculator
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.inputCalculator"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.inputCalculator.enabled"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.inputCalculator.enabled.@Tooltip"))
                                .binding(defaults.uiAndVisuals.inputCalculator.enabled,
                                        () -> config.uiAndVisuals.inputCalculator.enabled,
                                        newValue -> config.uiAndVisuals.inputCalculator.enabled = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.inputCalculator.requiresEquals"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.inputCalculator.requiresEquals.@Tooltip"))
                                .binding(defaults.uiAndVisuals.inputCalculator.requiresEquals,
                                        () -> config.uiAndVisuals.inputCalculator.requiresEquals,
                                        newValue -> config.uiAndVisuals.inputCalculator.requiresEquals = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.inputCalculator.closeSignsWithEnter"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.inputCalculator.closeSignsWithEnter.@Tooltip"))
								.binding(defaults.uiAndVisuals.inputCalculator.closeSignsWithEnter,
										() -> config.uiAndVisuals.inputCalculator.closeSignsWithEnter,
										newValue -> config.uiAndVisuals.inputCalculator.closeSignsWithEnter = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
                        .build())

                //Flame Overlay
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.uiAndVisuals.flameOverlay"))
                        .collapsed(true)
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.flameOverlay.flameHeight"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.flameOverlay.flameHeight.@Tooltip"))
                                .binding(defaults.uiAndVisuals.flameOverlay.flameHeight,
                                        () -> config.uiAndVisuals.flameOverlay.flameHeight,
                                        newValue -> config.uiAndVisuals.flameOverlay.flameHeight = newValue)
                                .controller(IntegerController.createBuilder().range(0, 100).slider(1).build())
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.flameOverlay.flameOpacity"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.flameOverlay.flameOpacity.@Tooltip"))
                                .binding(defaults.uiAndVisuals.flameOverlay.flameOpacity,
                                        () -> config.uiAndVisuals.flameOverlay.flameOpacity,
                                        newValue -> config.uiAndVisuals.flameOverlay.flameOpacity = newValue)
                                .controller(IntegerController.createBuilder().range(0, 100).slider(1).build())
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
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.compactDamage.precision"))
                                .description(Text.translatable("skyblocker.config.uiAndVisuals.compactDamage.precision.@Tooltip"))
                                .binding(defaults.uiAndVisuals.compactDamage.precision,
                                        () -> config.uiAndVisuals.compactDamage.precision,
                                        newValue -> config.uiAndVisuals.compactDamage.precision = newValue)
                                .controller(IntegerController.createBuilder().range(1, 3).slider(1).build())
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.compactDamage.normalDamageColor"))
                                .binding(defaults.uiAndVisuals.compactDamage.normalDamageColor,
                                        () -> config.uiAndVisuals.compactDamage.normalDamageColor,
                                        newValue -> config.uiAndVisuals.compactDamage.normalDamageColor = newValue)
                                .controller(ColourController.createBuilder().build())
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.compactDamage.critDamageGradientStart"))
                                .binding(defaults.uiAndVisuals.compactDamage.critDamageGradientStart,
                                        () -> config.uiAndVisuals.compactDamage.critDamageGradientStart,
                                        newValue -> config.uiAndVisuals.compactDamage.critDamageGradientStart = newValue)
                                .controller(ColourController.createBuilder().build())
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("skyblocker.config.uiAndVisuals.compactDamage.critDamageGradientEnd"))
                                .binding(defaults.uiAndVisuals.compactDamage.critDamageGradientEnd,
                                        () -> config.uiAndVisuals.compactDamage.critDamageGradientEnd,
                                        newValue -> config.uiAndVisuals.compactDamage.critDamageGradientEnd = newValue)
                                .controller(ColourController.createBuilder().build())
                                .build())
                        .build()
                )

				//Custom Health bars
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.uiAndVisuals.healthBars"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.healthBars.enabled"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.healthBars.enabled.@Tooltip"))
								.binding(defaults.uiAndVisuals.healthBars.enabled,
										() -> config.uiAndVisuals.healthBars.enabled,
										newValue -> config.uiAndVisuals.healthBars.enabled = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Float>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.healthBars.scale"))
								.binding(defaults.uiAndVisuals.healthBars.scale,
										() -> config.uiAndVisuals.healthBars.scale,
										newValue -> config.uiAndVisuals.healthBars.scale = newValue)
								.controller(FloatController.createBuilder().build())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.healthBars.removeHealthFromName"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.healthBars.removeHealthFromName.@Tooltip"))
								.binding(defaults.uiAndVisuals.healthBars.removeHealthFromName,
										() -> config.uiAndVisuals.healthBars.removeHealthFromName,
										newValue -> config.uiAndVisuals.healthBars.removeHealthFromName = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.healthBars.removeMaxHealthFromName"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.healthBars.removeMaxHealthFromName.@Tooltip"))
								.binding(defaults.uiAndVisuals.healthBars.removeMaxHealthFromName,
										() -> config.uiAndVisuals.healthBars.removeMaxHealthFromName,
										newValue -> config.uiAndVisuals.healthBars.removeMaxHealthFromName = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.healthBars.applyToHealthOnlyMobs"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.healthBars.applyToHealthOnlyMobs.@Tooltip"))
								.binding(defaults.uiAndVisuals.healthBars.applyToHealthOnlyMobs,
										() -> config.uiAndVisuals.healthBars.applyToHealthOnlyMobs,
										newValue -> config.uiAndVisuals.healthBars.applyToHealthOnlyMobs = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.healthBars.hideFullHealth"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.healthBars.hideFullHealth.@Tooltip"))
								.binding(defaults.uiAndVisuals.healthBars.hideFullHealth,
										() -> config.uiAndVisuals.healthBars.hideFullHealth,
										newValue -> config.uiAndVisuals.healthBars.hideFullHealth = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.healthBars.fullBarColor"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.healthBars.fullBarColor.@Tooltip"))
								.binding(defaults.uiAndVisuals.healthBars.fullBarColor,
										() -> config.uiAndVisuals.healthBars.fullBarColor,
										newValue -> config.uiAndVisuals.healthBars.fullBarColor = newValue)
								.controller(ColourController.createBuilder().build())
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.healthBars.halfBarColor"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.healthBars.halfBarColor.@Tooltip"))
								.binding(defaults.uiAndVisuals.healthBars.halfBarColor,
										() -> config.uiAndVisuals.healthBars.halfBarColor,
										newValue -> config.uiAndVisuals.healthBars.halfBarColor = newValue)
								.controller(ColourController.createBuilder().build())
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.healthBars.emptyBarColor"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.healthBars.emptyBarColor.@Tooltip"))
								.binding(defaults.uiAndVisuals.healthBars.emptyBarColor,
										() -> config.uiAndVisuals.healthBars.emptyBarColor,
										newValue -> config.uiAndVisuals.healthBars.emptyBarColor = newValue)
								.controller(ColourController.createBuilder().build())
								.build())
						.build()
				)
				//Gyro Overlay
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.uiAndVisuals.gyroOverlay"))
						.collapsed(true)
						.option(Option.<GyroOverlay.Mode>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.gyroOverlay.modeSelect"))
								.binding(defaults.uiAndVisuals.gyroOverlay.gyroOverlayMode,
										() -> config.uiAndVisuals.gyroOverlay.gyroOverlayMode,
										newValue -> config.uiAndVisuals.gyroOverlay.gyroOverlayMode = newValue)
								.controller(ConfigUtils.createEnumController())
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.gyroOverlay.Color"))
								.binding(defaults.uiAndVisuals.gyroOverlay.gyroOverlayColor,
										() -> config.uiAndVisuals.gyroOverlay.gyroOverlayColor,
										newValue -> {
											config.uiAndVisuals.gyroOverlay.gyroOverlayColor = newValue;
											GyroOverlay.configCallback(newValue);
										})
								.controller(ColourController.createBuilder().hasAlpha(true).build())
								.build())
						.build()
				)

				//item pickup widget
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.uiAndVisuals.itemPickup"))
						.collapsed(true)
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.itemPickup.hud.screen"))
								.prompt(Text.translatable("text.skyblocker.open"))
								.action(screen -> MinecraftClient.getInstance().setScreen(new WidgetsConfigurationScreen(Location.HUB, ItemPickupWidget.getInstance().getInternalID(), screen)))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.itemPickup.sackNotifications"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.itemPickup.sackNotifications.@Tooltip"))
								.binding(defaults.uiAndVisuals.itemPickup.sackNotifications,
										() -> config.uiAndVisuals.itemPickup.sackNotifications,
										newValue -> config.uiAndVisuals.itemPickup.sackNotifications = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.itemPickup.showItemName"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.itemPickup.showItemName.@Tooltip"))
								.binding(defaults.uiAndVisuals.itemPickup.showItemName,
										() -> config.uiAndVisuals.itemPickup.showItemName,
										newValue -> config.uiAndVisuals.itemPickup.showItemName = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.itemPickup.lifeTime"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.itemPickup.lifeTime.@Tooltip"))
								.binding(defaults.uiAndVisuals.itemPickup.lifeTime,
										() -> config.uiAndVisuals.itemPickup.lifeTime,
										newValue -> config.uiAndVisuals.itemPickup.lifeTime = newValue)
								.controller(IntegerController.createBuilder().range(1, 10).slider(1).build())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.uiAndVisuals.itemPickup.splitSack"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.itemPickup.splitSack.@Tooltip"))
								.binding(defaults.uiAndVisuals.itemPickup.splitNotifications,
										() -> config.uiAndVisuals.itemPickup.splitNotifications,
										newValue -> config.uiAndVisuals.itemPickup.splitNotifications = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build()
				)
                .build();
    }

	private static List<Option<Boolean>> createSlotTextToggles(SkyblockerConfig config) {
		return SlotTextManager.getAdderStream().map(SlotTextAdder::getConfigInformation).filter(Objects::nonNull).distinct()
				.map(configInfo -> configInfo.getOption(config))
				.sorted(Comparator.comparing(option -> option.name().getString())).toList();
	}
}
