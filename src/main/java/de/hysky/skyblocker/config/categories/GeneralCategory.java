package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.configs.GeneralConfig;
import de.hysky.skyblocker.config.configs.UiAndVisualsConfig;
import de.hysky.skyblocker.skyblock.fancybars.StatusBarsConfigScreen;
import de.hysky.skyblocker.skyblock.shortcut.ShortcutsConfigScreen;
import de.hysky.skyblocker.utils.render.title.TitleContainerConfigScreen;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GeneralCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.name(Text.translatable("skyblocker.category.general"))

				//Ungrouped Options
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.option.general.enableTips"))
						.binding(defaults.general.enableTips,
								() -> config.general.enableTips,
								newValue -> config.general.enableTips = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.option.general.acceptReparty"))
						.binding(defaults.dungeons.acceptReparty,
								() -> config.dungeons.acceptReparty,
								newValue -> config.dungeons.acceptReparty = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.option.general.betterPartyFinder"))
						.binding(defaults.dungeons.fancyPartyFinder,
								() -> config.dungeons.fancyPartyFinder,
								newValue -> config.dungeons.fancyPartyFinder = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.option.general.fancyCraftingTable"))
						.binding(defaults.uiAndVisuals.fancyCraftingTable,
								() -> config.uiAndVisuals.fancyCraftingTable,
								newValue -> config.uiAndVisuals.fancyCraftingTable = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.option.general.backpackPreviewWithoutShift"))
						.binding(defaults.uiAndVisuals.backpackPreviewWithoutShift,
								() -> config.uiAndVisuals.backpackPreviewWithoutShift,
								newValue -> config.uiAndVisuals.backpackPreviewWithoutShift = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.option.general.compactorDeletorPreview"))
						.binding(defaults.uiAndVisuals.compactorDeletorPreview,
								() -> config.uiAndVisuals.compactorDeletorPreview,
								newValue -> config.uiAndVisuals.compactorDeletorPreview = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.option.general.hideEmptyTooltips"))
						.binding(defaults.general.hideEmptyTooltips,
								() -> config.general.hideEmptyTooltips,
								newValue -> config.general.hideEmptyTooltips = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.option.general.hideStatusEffectOverlay"))
						.binding(defaults.misc.hideStatusEffectOverlay,
								() -> config.misc.hideStatusEffectOverlay,
								newValue -> config.misc.hideStatusEffectOverlay = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.option.general.dontStripSkinAlphaValues"))
						.description(OptionDescription.of(Text.translatable("skyblocker.option.general.dontStripSkinAlphaValues.@Tooltip")))
						.binding(defaults.uiAndVisuals.dontStripSkinAlphaValues,
								() -> config.uiAndVisuals.dontStripSkinAlphaValues,
								newValue -> config.uiAndVisuals.dontStripSkinAlphaValues = newValue)
						.controller(ConfigUtils::createBooleanController)
						.flag(OptionFlag.ASSET_RELOAD)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.option.general.dungeonQuality"))
						.description(OptionDescription.of(Text.translatable("skyblocker.option.general.dungeonQuality.@Tooltip")))
						.binding(defaults.general.itemTooltip.dungeonQuality,
								() -> config.general.itemTooltip.dungeonQuality,
								newValue -> config.general.itemTooltip.dungeonQuality = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.option.general.enableNewYearCakesHelper"))
						.description(OptionDescription.of(Text.translatable("skyblocker.option.general.enableNewYearCakesHelper.@Tooltip")))
						.binding(defaults.helper.enableNewYearCakesHelper,
								() -> config.helper.enableNewYearCakesHelper,
								newValue -> config.helper.enableNewYearCakesHelper = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())

				//Tab Hud
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.tabHud"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.tabHud.tabHudEnabled"))
								.binding(defaults.uiAndVisuals.tabHud.tabHudEnabled,
										() -> config.uiAndVisuals.tabHud.tabHudEnabled,
										newValue -> config.uiAndVisuals.tabHud.tabHudEnabled = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("skyblocker.option.general.tabHud.tabHudScale"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.tabHud.tabHudScale.@Tooltip")))
								.binding(defaults.uiAndVisuals.tabHud.tabHudScale,
										() -> config.uiAndVisuals.tabHud.tabHudScale,
										newValue -> config.uiAndVisuals.tabHud.tabHudScale = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(10, 200).step(1))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.tabHud.enableHudBackground"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.tabHud.enableHudBackground.@Tooltip")))
								.binding(defaults.uiAndVisuals.tabHud.enableHudBackground,
										() -> config.uiAndVisuals.tabHud.enableHudBackground,
										newValue -> config.uiAndVisuals.tabHud.enableHudBackground = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.tabHud.plainPlayerNames"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.tabHud.plainPlayerNames.@Tooltip")))
								.binding(defaults.uiAndVisuals.tabHud.plainPlayerNames,
										() -> config.uiAndVisuals.tabHud.plainPlayerNames,
										newValue -> config.uiAndVisuals.tabHud.plainPlayerNames = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<UiAndVisualsConfig.NameSorting>createBuilder()
								.name(Text.translatable("skyblocker.option.general.tabHud.nameSorting"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.tabHud.nameSorting.@Tooltip")))
								.binding(defaults.uiAndVisuals.tabHud.nameSorting,
										() -> config.uiAndVisuals.tabHud.nameSorting,
										newValue -> config.uiAndVisuals.tabHud.nameSorting = newValue)
								.controller(ConfigUtils::createEnumCyclingListController)
								.build())
						.build())

				//Fancy Bars
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.bars"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.bars.enableBars"))
								.binding(defaults.uiAndVisuals.bars.enableBars,
										() -> config.uiAndVisuals.bars.enableBars,
										newValue -> config.uiAndVisuals.bars.enableBars = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("skyblocker.bars.config.openScreen"))
								.text(Text.translatable("text.skyblocker.open"))
								.action((screen, opt) -> MinecraftClient.getInstance().setScreen(new StatusBarsConfigScreen()))
								.build())
						.build())

				//Experiments Solver
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.experiments"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.experiments.enableChronomatronSolver"))
								.binding(defaults.helper.experiments.enableChronomatronSolver,
										() -> config.helper.experiments.enableChronomatronSolver,
										newValue -> config.helper.experiments.enableChronomatronSolver = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.experiments.enableSuperpairsSolver"))
								.binding(defaults.helper.experiments.enableSuperpairsSolver,
										() -> config.helper.experiments.enableSuperpairsSolver,
										newValue -> config.helper.experiments.enableSuperpairsSolver = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.experiments.enableUltrasequencerSolver"))
								.binding(defaults.helper.experiments.enableUltrasequencerSolver,
										() -> config.helper.experiments.enableUltrasequencerSolver,
										newValue -> config.helper.experiments.enableUltrasequencerSolver = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Fishing Helper
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.fishing"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.fishing.enableFishingHelper"))
								.binding(defaults.helper.fishing.enableFishingHelper,
										() -> config.helper.fishing.enableFishingHelper,
										newValue -> config.helper.fishing.enableFishingHelper = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.fishing.enableFishingTimer"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.fishing.enableFishingTimer.@Tooltip")))
								.binding(defaults.helper.fishing.enableFishingTimer,
										() -> config.helper.fishing.enableFishingTimer,
										newValue -> config.helper.fishing.enableFishingTimer = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.fishing.changeTimerColor"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.fishing.changeTimerColor.@Tooltip")))
								.binding(defaults.helper.fishing.changeTimerColor,
										() -> config.helper.fishing.changeTimerColor,
										newValue -> config.helper.fishing.changeTimerColor = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Float>createBuilder()
								.name(Text.translatable("skyblocker.option.general.fishing.fishingTimerScale"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.fishing.fishingTimerScale.@Tooltip")))
								.binding(defaults.helper.fishing.fishingTimerScale,
										() -> config.helper.fishing.fishingTimerScale,
										newValue -> config.helper.fishing.fishingTimerScale = newValue)
								.controller(FloatFieldControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.fishing.hideOtherPlayers"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.fishing.hideOtherPlayers.@Tooltip")))
								.binding(defaults.helper.fishing.hideOtherPlayersRods,
										() -> config.helper.fishing.hideOtherPlayersRods,
										newValue -> config.helper.fishing.hideOtherPlayersRods = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Fairy Souls Helper
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.fairySouls"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.fairySouls.enableFairySoulsHelper"))
								.binding(defaults.helper.fairySouls.enableFairySoulsHelper,
										() -> config.helper.fairySouls.enableFairySoulsHelper,
										newValue -> config.helper.fairySouls.enableFairySoulsHelper = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.fairySouls.highlightFoundSouls"))
								.binding(defaults.helper.fairySouls.highlightFoundSouls,
										() -> config.helper.fairySouls.highlightFoundSouls,
										newValue -> config.helper.fairySouls.highlightFoundSouls = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.fairySouls.highlightOnlyNearbySouls"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.fairySouls.highlightOnlyNearbySouls.@Tooltip")))
								.binding(defaults.helper.fairySouls.highlightOnlyNearbySouls,
										() -> config.helper.fairySouls.highlightOnlyNearbySouls,
										newValue -> config.helper.fairySouls.highlightOnlyNearbySouls = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Mythological Ritual
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.mythologicalRitual"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.mythologicalRitual.enableMythologicalRitualHelper"))
								.binding(defaults.helper.mythologicalRitual.enableMythologicalRitualHelper,
										() -> config.helper.mythologicalRitual.enableMythologicalRitualHelper,
										newValue -> config.helper.mythologicalRitual.enableMythologicalRitualHelper = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Item Cooldown
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.itemCooldown"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.itemCooldown.enableItemCooldowns"))
								.binding(defaults.uiAndVisuals.itemCooldown.enableItemCooldowns,
										() -> config.uiAndVisuals.itemCooldown.enableItemCooldowns,
										newValue -> config.uiAndVisuals.itemCooldown.enableItemCooldowns = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Shortcuts
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.shortcuts"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.shortcuts.enableShortcuts"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.shortcuts.enableShortcuts.@Tooltip")))
								.binding(defaults.general.shortcuts.enableShortcuts,
										() -> config.general.shortcuts.enableShortcuts,
										newValue -> config.general.shortcuts.enableShortcuts = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.shortcuts.enableCommandShortcuts"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.shortcuts.enableCommandShortcuts.@Tooltip")))
								.binding(defaults.general.shortcuts.enableCommandShortcuts,
										() -> config.general.shortcuts.enableCommandShortcuts,
										newValue -> config.general.shortcuts.enableCommandShortcuts = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.shortcuts.enableCommandArgShortcuts"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.shortcuts.enableCommandArgShortcuts.@Tooltip")))
								.binding(defaults.general.shortcuts.enableCommandArgShortcuts,
										() -> config.general.shortcuts.enableCommandArgShortcuts,
										newValue -> config.general.shortcuts.enableCommandArgShortcuts = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("skyblocker.option.general.shortcuts.config"))
								.text(Text.translatable("text.skyblocker.open"))
								.action((screen, opt) -> MinecraftClient.getInstance().setScreen(new ShortcutsConfigScreen(screen)))
								.build())
						.build())

				//Waypoints
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.waypoints"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.waypoints.enableWaypoints"))
								.binding(defaults.uiAndVisuals.waypoints.enableWaypoints,
										() -> config.uiAndVisuals.waypoints.enableWaypoints,
										newValue -> config.uiAndVisuals.waypoints.enableWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Waypoint.Type>createBuilder()
								.name(Text.translatable("skyblocker.option.general.waypoints.waypointType"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.waypoints.waypointType.@Tooltip"), Text.translatable("skyblocker.option.general.waypoints.waypointType.generalNote")))
								.binding(defaults.uiAndVisuals.waypoints.waypointType,
										() -> config.uiAndVisuals.waypoints.waypointType,
										newValue -> config.uiAndVisuals.waypoints.waypointType = newValue)
								.controller(ConfigUtils::createEnumCyclingListController)
								.build())
						.build())

				//Quiver Warning
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.quiverWarning"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.quiverWarning.enableQuiverWarning"))
								.binding(defaults.general.quiverWarning.enableQuiverWarning,
										() -> config.general.quiverWarning.enableQuiverWarning,
										newValue -> config.general.quiverWarning.enableQuiverWarning = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.quiverWarning.enableQuiverWarningInDungeons"))
								.binding(defaults.general.quiverWarning.enableQuiverWarningInDungeons,
										() -> config.general.quiverWarning.enableQuiverWarningInDungeons,
										newValue -> config.general.quiverWarning.enableQuiverWarningInDungeons = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.quiverWarning.enableQuiverWarningAfterDungeon"))
								.binding(defaults.general.quiverWarning.enableQuiverWarningAfterDungeon,
										() -> config.general.quiverWarning.enableQuiverWarningAfterDungeon,
										newValue -> config.general.quiverWarning.enableQuiverWarningAfterDungeon = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Item List
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.itemList"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.itemList.enableItemList"))
								.binding(defaults.general.itemList.enableItemList,
										() -> config.general.itemList.enableItemList,
										newValue -> config.general.itemList.enableItemList = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Item Tooltip
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.itemTooltip"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.itemTooltip.enableNPCPrice"))
								.binding(defaults.general.itemTooltip.enableNPCPrice,
										() -> config.general.itemTooltip.enableNPCPrice,
										newValue -> config.general.itemTooltip.enableNPCPrice = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.itemTooltip.enableMotesPrice"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.itemTooltip.enableMotesPrice.@Tooltip")))
								.binding(defaults.general.itemTooltip.enableMotesPrice,
										() -> config.general.itemTooltip.enableMotesPrice,
										newValue -> config.general.itemTooltip.enableMotesPrice = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.itemTooltip.enableAvgBIN"))
								.binding(defaults.general.itemTooltip.enableAvgBIN,
										() -> config.general.itemTooltip.enableAvgBIN,
										newValue -> config.general.itemTooltip.enableAvgBIN = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<GeneralConfig.Average>createBuilder()
								.name(Text.translatable("skyblocker.option.general.itemTooltip.avg"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.itemTooltip.avg.@Tooltip")))
								.binding(defaults.general.itemTooltip.avg,
										() -> config.general.itemTooltip.avg,
										newValue -> config.general.itemTooltip.avg = newValue)
								.controller(ConfigUtils::createEnumCyclingListController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.itemTooltip.enableLowestBIN"))
								.binding(defaults.general.itemTooltip.enableLowestBIN,
										() -> config.general.itemTooltip.enableLowestBIN,
										newValue -> config.general.itemTooltip.enableLowestBIN = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.itemTooltip.enableBazaarPrice"))
								.binding(defaults.general.itemTooltip.enableBazaarPrice,
										() -> config.general.itemTooltip.enableBazaarPrice,
										newValue -> config.general.itemTooltip.enableBazaarPrice = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.itemTooltip.enableObtainedDate"))
								.binding(defaults.general.itemTooltip.enableObtainedDate,
										() -> config.general.itemTooltip.enableObtainedDate,
										newValue -> config.general.itemTooltip.enableObtainedDate = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.itemTooltip.enableMuseumInfo"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.itemTooltip.enableMuseumInfo.@Tooltip")))
								.binding(defaults.general.itemTooltip.enableMuseumInfo,
										() -> config.general.itemTooltip.enableMuseumInfo,
										newValue -> config.general.itemTooltip.enableMuseumInfo = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.itemTooltip.enableExoticTooltip"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.itemTooltip.enableExoticTooltip.@Tooltip")))
								.binding(defaults.general.itemTooltip.enableExoticTooltip,
										() -> config.general.itemTooltip.enableExoticTooltip,
										newValue -> config.general.itemTooltip.enableExoticTooltip = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.itemTooltip.enableAccessoriesHelper"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.itemTooltip.enableAccessoriesHelper.@Tooltip[0]"), Text.literal("\n\n✔ Collected").formatted(Formatting.GREEN), Text.translatable("skyblocker.option.general.itemTooltip.enableAccessoriesHelper.@Tooltip[1]"),
										Text.literal("\n✦ Upgrade").withColor(0x218bff), Text.translatable("skyblocker.option.general.itemTooltip.enableAccessoriesHelper.@Tooltip[2]"), Text.literal("\n↑ Upgradable").withColor(0xf8d048), Text.translatable("skyblocker.option.general.itemTooltip.enableAccessoriesHelper.@Tooltip[3]"),
												Text.literal("\n↓ Downgrade").formatted(Formatting.GRAY), Text.translatable("skyblocker.option.general.itemTooltip.enableAccessoriesHelper.@Tooltip[4]"), Text.literal("\n✖ Missing").formatted(Formatting.RED), Text.translatable("skyblocker.option.general.itemTooltip.enableAccessoriesHelper.@Tooltip[5]")))
								.binding(defaults.general.itemTooltip.enableAccessoriesHelper,
										() -> config.general.itemTooltip.enableAccessoriesHelper,
										newValue -> config.general.itemTooltip.enableAccessoriesHelper = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Item Info Display
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.itemInfoDisplay"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.itemInfoDisplay.attributeShardInfo"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.itemInfoDisplay.attributeShardInfo.@Tooltip")))
								.binding(defaults.general.itemInfoDisplay.attributeShardInfo,
										() -> config.general.itemInfoDisplay.attributeShardInfo,
										newValue -> config.general.itemInfoDisplay.attributeShardInfo = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.itemInfoDisplay.itemRarityBackgrounds"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.itemInfoDisplay.itemRarityBackgrounds.@Tooltip")))
								.binding(defaults.general.itemInfoDisplay.itemRarityBackgrounds,
										() -> config.general.itemInfoDisplay.itemRarityBackgrounds,
										newValue -> config.general.itemInfoDisplay.itemRarityBackgrounds = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<GeneralConfig.RarityBackgroundStyle>createBuilder()
								.name(Text.translatable("skyblocker.option.general.itemInfoDisplay.itemRarityBackgroundStyle"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.itemInfoDisplay.itemRarityBackgroundStyle.@Tooltip")))
								.binding(defaults.general.itemInfoDisplay.itemRarityBackgroundStyle,
										() -> config.general.itemInfoDisplay.itemRarityBackgroundStyle,
										newValue -> config.general.itemInfoDisplay.itemRarityBackgroundStyle = newValue)
								.controller(ConfigUtils::createEnumCyclingListController)
								.build())
						.option(Option.<Float>createBuilder()
								.name(Text.translatable("skyblocker.option.general.itemInfoDisplay.itemRarityBackgroundsOpacity"))
								.binding(defaults.general.itemInfoDisplay.itemRarityBackgroundsOpacity,
										() -> config.general.itemInfoDisplay.itemRarityBackgroundsOpacity,
										newValue -> config.general.itemInfoDisplay.itemRarityBackgroundsOpacity = newValue)
								.controller(opt -> FloatSliderControllerBuilder.create(opt).range(0f, 1f).step(0.05f).formatValue(ConfigUtils.FLOAT_TWO_FORMATTER))
								.build())
						.build())

				//Item Protection
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.itemProtection"))
						.collapsed(true)
						.option(Option.<GeneralConfig.SlotLockStyle>createBuilder()
								.name(Text.translatable("skyblocker.option.general.itemProtection.slotLockStyle"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.itemProtection.slotLockStyle.@Tooltip")))
								.binding(defaults.general.itemProtection.slotLockStyle,
										() -> config.general.itemProtection.slotLockStyle,
										newValue -> config.general.itemProtection.slotLockStyle = newValue)
								.controller(ConfigUtils::createEnumCyclingListController)
								.build())
						.build())

				//Wiki Lookup
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.wikiLookup"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.wikiLookup.enableWikiLookup"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.wikiLookup.enableWikiLookup.@Tooltip")))
								.binding(defaults.general.wikiLookup.enableWikiLookup,
										() -> config.general.wikiLookup.enableWikiLookup,
										newValue -> config.general.wikiLookup.enableWikiLookup = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.wikiLookup.officialWiki"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.wikiLookup.officialWiki.@Tooltip")))
								.binding(defaults.general.wikiLookup.officialWiki,
										() -> config.general.wikiLookup.officialWiki,
										newValue -> config.general.wikiLookup.officialWiki = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Chest Value
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.chestValue"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.chestValue.enableChestValue"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.chestValue.enableChestValue.@Tooltip")))
								.binding(defaults.uiAndVisuals.chestValue.enableChestValue,
										() -> config.uiAndVisuals.chestValue.enableChestValue,
										newValue -> config.uiAndVisuals.chestValue.enableChestValue = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Formatting>createBuilder()
								.name(Text.translatable("skyblocker.option.general.chestValue.color"))
								.binding(defaults.uiAndVisuals.chestValue.color,
										() -> config.uiAndVisuals.chestValue.color,
										newValue -> config.uiAndVisuals.chestValue.color = newValue)
								.controller(ConfigUtils.getEnumDropdownControllerFactory(ConfigUtils.FORMATTING_FORMATTER))
								.build())
						.option(Option.<Formatting>createBuilder()
								.name(Text.translatable("skyblocker.option.general.chestValue.incompleteColor"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.chestValue.incompleteColor.@Tooltip")))
								.binding(defaults.uiAndVisuals.chestValue.incompleteColor,
										() -> config.uiAndVisuals.chestValue.incompleteColor,
										newValue -> config.uiAndVisuals.chestValue.incompleteColor = newValue)
								.controller(ConfigUtils.getEnumDropdownControllerFactory(ConfigUtils.FORMATTING_FORMATTER))
								.build())
						.build())

				//Special Effects
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.specialEffects"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.specialEffects.rareDungeonDropEffects"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.specialEffects.rareDungeonDropEffects.@Tooltip")))
								.binding(defaults.general.specialEffects.rareDungeonDropEffects,
										() -> config.general.specialEffects.rareDungeonDropEffects,
										newValue -> config.general.specialEffects.rareDungeonDropEffects = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Hitboxes
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.hitbox"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.hitbox.oldFarmlandHitbox"))
								.binding(defaults.general.hitbox.oldFarmlandHitbox,
										() -> config.general.hitbox.oldFarmlandHitbox,
										newValue -> config.general.hitbox.oldFarmlandHitbox = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.hitbox.oldLeverHitbox"))
								.binding(defaults.general.hitbox.oldLeverHitbox,
										() -> config.general.hitbox.oldLeverHitbox,
										newValue -> config.general.hitbox.oldLeverHitbox = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Title Container
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.titleContainer"))
						.description(OptionDescription.of(Text.translatable("skyblocker.option.general.titleContainer.@Tooltip")))
						.collapsed(true)
						.option(Option.<Float>createBuilder()
								.name(Text.translatable("skyblocker.option.general.titleContainer.titleContainerScale"))
								.binding(defaults.uiAndVisuals.titleContainer.titleContainerScale,
										() -> config.uiAndVisuals.titleContainer.titleContainerScale,
										newValue -> config.uiAndVisuals.titleContainer.titleContainerScale = newValue)
								.controller(opt -> FloatFieldControllerBuilder.create(opt).range(30f, 140f))
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("skyblocker.option.general.titleContainer.config"))
								.text(Text.translatable("text.skyblocker.open"))
								.action((screen, opt) -> MinecraftClient.getInstance().setScreen(new TitleContainerConfigScreen(screen)))
								.build())
						.build())

				//Teleport Overlays
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.teleportOverlay"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.teleportOverlay.enableTeleportOverlays"))
								.binding(defaults.uiAndVisuals.teleportOverlay.enableTeleportOverlays,
										() -> config.uiAndVisuals.teleportOverlay.enableTeleportOverlays,
										newValue -> config.uiAndVisuals.teleportOverlay.enableTeleportOverlays = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.teleportOverlay.enableWeirdTransmission"))
								.binding(defaults.uiAndVisuals.teleportOverlay.enableWeirdTransmission,
										() -> config.uiAndVisuals.teleportOverlay.enableWeirdTransmission,
										newValue -> config.uiAndVisuals.teleportOverlay.enableWeirdTransmission = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.teleportOverlay.enableInstantTransmission"))
								.binding(defaults.uiAndVisuals.teleportOverlay.enableInstantTransmission,
										() -> config.uiAndVisuals.teleportOverlay.enableInstantTransmission,
										newValue -> config.uiAndVisuals.teleportOverlay.enableInstantTransmission = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.teleportOverlay.enableEtherTransmission"))
								.binding(defaults.uiAndVisuals.teleportOverlay.enableEtherTransmission,
										() -> config.uiAndVisuals.teleportOverlay.enableEtherTransmission,
										newValue -> config.uiAndVisuals.teleportOverlay.enableEtherTransmission = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.teleportOverlay.enableSinrecallTransmission"))
								.binding(defaults.uiAndVisuals.teleportOverlay.enableSinrecallTransmission,
										() -> config.uiAndVisuals.teleportOverlay.enableSinrecallTransmission,
										newValue -> config.uiAndVisuals.teleportOverlay.enableSinrecallTransmission = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.teleportOverlay.enableWitherImpact"))
								.binding(defaults.uiAndVisuals.teleportOverlay.enableWitherImpact,
										() -> config.uiAndVisuals.teleportOverlay.enableWitherImpact,
										newValue -> config.uiAndVisuals.teleportOverlay.enableWitherImpact = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Flame Overlay
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.flameOverlay"))
						.collapsed(true)
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("skyblocker.option.general.flameOverlay.flameHeight"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.flameOverlay.flameHeight.@Tooltip")))
								.binding(defaults.uiAndVisuals.flameOverlay.flameHeight,
										() -> config.uiAndVisuals.flameOverlay.flameHeight,
										newValue -> config.uiAndVisuals.flameOverlay.flameHeight = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 100).step(1))
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("skyblocker.option.general.flameOverlay.flameOpacity"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.flameOverlay.flameOpacity.@Tooltip")))
								.binding(defaults.uiAndVisuals.flameOverlay.flameOpacity,
										() -> config.uiAndVisuals.flameOverlay.flameOpacity,
										newValue -> config.uiAndVisuals.flameOverlay.flameOpacity = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 100).step(1))
								.build())
						.build())

				//Search overlay
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.searchOverlay"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.searchOverlay.enableBazaar"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.searchOverlay.enableBazaar.@Tooltip")))
								.binding(defaults.uiAndVisuals.searchOverlay.enableBazaar,
										() -> config.uiAndVisuals.searchOverlay.enableBazaar,
										newValue -> config.uiAndVisuals.searchOverlay.enableBazaar = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.searchOverlay.enableAuctionHouse"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.searchOverlay.enableAuctionHouse.@Tooltip")))
								.binding(defaults.uiAndVisuals.searchOverlay.enableAuctionHouse,
										() -> config.uiAndVisuals.searchOverlay.enableAuctionHouse,
										newValue -> config.uiAndVisuals.searchOverlay.enableAuctionHouse = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.searchOverlay.keepPreviousSearches"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.searchOverlay.keepPreviousSearches.@Tooltip")))
								.binding(defaults.uiAndVisuals.searchOverlay.keepPreviousSearches,
										() -> config.uiAndVisuals.searchOverlay.keepPreviousSearches,
										newValue -> config.uiAndVisuals.searchOverlay.keepPreviousSearches = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("skyblocker.option.general.searchOverlay.maxSuggestions"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.searchOverlay.maxSuggestions.@Tooltip")))
								.binding(defaults.uiAndVisuals.searchOverlay.maxSuggestions,
										() -> config.uiAndVisuals.searchOverlay.maxSuggestions,
										newValue -> config.uiAndVisuals.searchOverlay.maxSuggestions = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 5).step(1))
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("skyblocker.option.general.searchOverlay.historyLength"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.searchOverlay.historyLength.@Tooltip")))
								.binding(defaults.uiAndVisuals.searchOverlay.historyLength,
										() -> config.uiAndVisuals.searchOverlay.historyLength,
										newValue -> config.uiAndVisuals.searchOverlay.historyLength = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 5).step(1))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.searchOverlay.enableCommands"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.general.searchOverlay.enableCommands.@Tooltip")))
								.binding(defaults.uiAndVisuals.searchOverlay.enableCommands,
										() -> config.uiAndVisuals.searchOverlay.enableCommands,
										newValue -> config.uiAndVisuals.searchOverlay.enableCommands = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				// Fancy Auction House
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.general.betterAuctionHouse"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.betterAuctionHouse.enabled"))
								.binding(defaults.uiAndVisuals.fancyAuctionHouse.enabled,
										() -> config.uiAndVisuals.fancyAuctionHouse.enabled,
										newValue -> config.uiAndVisuals.fancyAuctionHouse.enabled = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.general.betterAuctionHouse.highlightUnderAvgPrice"))
								.binding(defaults.uiAndVisuals.fancyAuctionHouse.highlightCheapBIN,
										() -> config.uiAndVisuals.fancyAuctionHouse.highlightCheapBIN,
										newValue -> config.uiAndVisuals.fancyAuctionHouse.highlightCheapBIN = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())
				.build();
	}
}
