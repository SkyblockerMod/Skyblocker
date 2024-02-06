package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
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
				.name(Text.translatable("text.autoconfig.skyblocker.category.general"))

				//Ungrouped Options
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.enableTips"))
						.binding(defaults.general.enableTips,
								() -> config.general.enableTips,
								newValue -> config.general.enableTips = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.acceptReparty"))
						.binding(defaults.general.acceptReparty,
								() -> config.general.acceptReparty,
								newValue -> config.general.acceptReparty = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.betterPartyFinder"))
						.binding(defaults.general.betterPartyFinder,
								() -> config.general.betterPartyFinder,
								newValue -> config.general.betterPartyFinder = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.backpackPreviewWithoutShift"))
						.binding(defaults.general.backpackPreviewWithoutShift,
								() -> config.general.backpackPreviewWithoutShift,
								newValue -> config.general.backpackPreviewWithoutShift = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.compactorDeletorPreview"))
						.binding(defaults.general.compactorDeletorPreview,
								() -> config.general.compactorDeletorPreview,
								newValue -> config.general.compactorDeletorPreview = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.hideEmptyTooltips"))
						.binding(defaults.general.hideEmptyTooltips,
								() -> config.general.hideEmptyTooltips,
								newValue -> config.general.hideEmptyTooltips = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.hideStatusEffectOverlay"))
						.binding(defaults.general.hideStatusEffectOverlay,
								() -> config.general.hideStatusEffectOverlay,
								newValue -> config.general.hideStatusEffectOverlay = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.dontStripSkinAlphaValues"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.dontStripSkinAlphaValues.@Tooltip")))
						.binding(defaults.general.dontStripSkinAlphaValues,
								() -> config.general.dontStripSkinAlphaValues,
								newValue -> config.general.dontStripSkinAlphaValues = newValue)
						.controller(ConfigUtils::createBooleanController)
						.flag(OptionFlag.ASSET_RELOAD)
						.build())

				//Tab Hud
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.tabHud"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.tabHud.tabHudEnabled"))
								.binding(defaults.general.tabHud.tabHudEnabled,
										() -> config.general.tabHud.tabHudEnabled,
										newValue -> config.general.tabHud.tabHudEnabled = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.tabHud.tabHudScale"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.tabHud.tabHudScale.@Tooltip")))
								.binding(defaults.general.tabHud.tabHudScale,
										() -> config.general.tabHud.tabHudScale,
										newValue -> config.general.tabHud.tabHudScale = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(10, 200).step(1))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.tabHud.plainPlayerNames"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.tabHud.plainPlayerNames.@Tooltip")))
								.binding(defaults.general.tabHud.plainPlayerNames,
										() -> config.general.tabHud.plainPlayerNames,
										newValue -> config.general.tabHud.plainPlayerNames = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<SkyblockerConfig.NameSorting>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.tabHud.nameSorting"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.tabHud.nameSorting.@Tooltip")))
								.binding(defaults.general.tabHud.nameSorting,
										() -> config.general.tabHud.nameSorting,
										newValue -> config.general.tabHud.nameSorting = newValue)
								.controller(ConfigUtils::createEnumCyclingListController)
								.build())
						.build())

				//Fancy Bars
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.bars"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.bars.enableBars"))
								.binding(defaults.general.bars.enableBars,
										() -> config.general.bars.enableBars,
										newValue -> config.general.bars.enableBars = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<SkyblockerConfig.BarPosition>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.bars.barpositions.healthBarPosition"))
								.binding(defaults.general.bars.barPositions.healthBarPosition,
										() -> config.general.bars.barPositions.healthBarPosition,
										newValue -> config.general.bars.barPositions.healthBarPosition = newValue)
								.controller(ConfigUtils::createEnumCyclingListController)
								.build())
						.option(Option.<SkyblockerConfig.BarPosition>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.bars.barpositions.manaBarPosition"))
								.binding(defaults.general.bars.barPositions.manaBarPosition,
										() -> config.general.bars.barPositions.manaBarPosition,
										newValue -> config.general.bars.barPositions.manaBarPosition = newValue)
								.controller(ConfigUtils::createEnumCyclingListController)
								.build())
						.option(Option.<SkyblockerConfig.BarPosition>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.bars.barpositions.defenceBarPosition"))
								.binding(defaults.general.bars.barPositions.defenceBarPosition,
										() -> config.general.bars.barPositions.defenceBarPosition,
										newValue -> config.general.bars.barPositions.defenceBarPosition = newValue)
								.controller(ConfigUtils::createEnumCyclingListController)
								.build())
						.option(Option.<SkyblockerConfig.BarPosition>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.bars.barpositions.experienceBarPosition"))
								.binding(defaults.general.bars.barPositions.experienceBarPosition,
										() -> config.general.bars.barPositions.experienceBarPosition,
										newValue -> config.general.bars.barPositions.experienceBarPosition = newValue)
								.controller(ConfigUtils::createEnumCyclingListController)
								.build())
						.build())

				//Experiments Solver
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.experiments"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.experiments.enableChronomatronSolver"))
								.binding(defaults.general.experiments.enableChronomatronSolver,
										() -> config.general.experiments.enableChronomatronSolver,
										newValue -> config.general.experiments.enableChronomatronSolver = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.experiments.enableSuperpairsSolver"))
								.binding(defaults.general.experiments.enableSuperpairsSolver,
										() -> config.general.experiments.enableSuperpairsSolver,
										newValue -> config.general.experiments.enableSuperpairsSolver = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.experiments.enableUltrasequencerSolver"))
								.binding(defaults.general.experiments.enableUltrasequencerSolver,
										() -> config.general.experiments.enableUltrasequencerSolver,
										newValue -> config.general.experiments.enableUltrasequencerSolver = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Fishing Helper
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.fishing"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.fishing.enableFishingHelper"))
								.binding(defaults.general.fishing.enableFishingHelper,
										() -> config.general.fishing.enableFishingHelper,
										newValue -> config.general.fishing.enableFishingHelper = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Fairy Souls Helper
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.fairySouls"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.fairySouls.enableFairySoulsHelper"))
								.binding(defaults.general.fairySouls.enableFairySoulsHelper,
										() -> config.general.fairySouls.enableFairySoulsHelper,
										newValue -> config.general.fairySouls.enableFairySoulsHelper = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.fairySouls.highlightFoundSouls"))
								.binding(defaults.general.fairySouls.highlightFoundSouls,
										() -> config.general.fairySouls.highlightFoundSouls,
										newValue -> config.general.fairySouls.highlightFoundSouls = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.fairySouls.highlightOnlyNearbySouls"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.fairySouls.highlightOnlyNearbySouls.@Tooltip")))
								.binding(defaults.general.fairySouls.highlightOnlyNearbySouls,
										() -> config.general.fairySouls.highlightOnlyNearbySouls,
										newValue -> config.general.fairySouls.highlightOnlyNearbySouls = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Mythological Ritual
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.mythologicalRitual"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.mythologicalRitual.enableMythologicalRitualHelper"))
								.binding(defaults.general.mythologicalRitual.enableMythologicalRitualHelper,
										() -> config.general.mythologicalRitual.enableMythologicalRitualHelper,
										newValue -> config.general.mythologicalRitual.enableMythologicalRitualHelper = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Item Cooldown
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemCooldown"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemCooldown.enableItemCooldowns"))
								.binding(defaults.general.itemCooldown.enableItemCooldowns,
										() -> config.general.itemCooldown.enableItemCooldowns,
										newValue -> config.general.itemCooldown.enableItemCooldowns = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Shortcuts
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.shortcuts"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.shortcuts.enableShortcuts"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.shortcuts.enableShortcuts.@Tooltip")))
								.binding(defaults.general.shortcuts.enableShortcuts,
										() -> config.general.shortcuts.enableShortcuts,
										newValue -> config.general.shortcuts.enableShortcuts = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.shortcuts.enableCommandShortcuts"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.shortcuts.enableCommandShortcuts.@Tooltip")))
								.binding(defaults.general.shortcuts.enableCommandShortcuts,
										() -> config.general.shortcuts.enableCommandShortcuts,
										newValue -> config.general.shortcuts.enableCommandShortcuts = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.shortcuts.enableCommandArgShortcuts"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.shortcuts.enableCommandArgShortcuts.@Tooltip")))
								.binding(defaults.general.shortcuts.enableCommandArgShortcuts,
										() -> config.general.shortcuts.enableCommandArgShortcuts,
										newValue -> config.general.shortcuts.enableCommandArgShortcuts = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.shortcuts.config"))
								.text(Text.translatable("text.skyblocker.open"))
								.action((screen, opt) -> MinecraftClient.getInstance().setScreen(new ShortcutsConfigScreen(screen)))
								.build())
						.build())

				//Waypoints
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.waypoints"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.waypoints.enableWaypoints"))
								.binding(defaults.general.waypoints.enableWaypoints,
										() -> config.general.waypoints.enableWaypoints,
										newValue -> config.general.waypoints.enableWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Waypoint.Type>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.waypoints.waypointType"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.waypoints.waypointType.@Tooltip"), Text.translatable("text.autoconfig.skyblocker.option.general.waypoints.waypointType.generalNote")))
								.binding(defaults.general.waypoints.waypointType,
										() -> config.general.waypoints.waypointType,
										newValue -> config.general.waypoints.waypointType = newValue)
								.controller(ConfigUtils::createEnumCyclingListController)
								.build())
						.build())

				//Quiver Warning
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.quiverWarning"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.quiverWarning.enableQuiverWarning"))
								.binding(defaults.general.quiverWarning.enableQuiverWarning,
										() -> config.general.quiverWarning.enableQuiverWarning,
										newValue -> config.general.quiverWarning.enableQuiverWarning = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.quiverWarning.enableQuiverWarningInDungeons"))
								.binding(defaults.general.quiverWarning.enableQuiverWarningInDungeons,
										() -> config.general.quiverWarning.enableQuiverWarningInDungeons,
										newValue -> config.general.quiverWarning.enableQuiverWarningInDungeons = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.quiverWarning.enableQuiverWarningAfterDungeon"))
								.binding(defaults.general.quiverWarning.enableQuiverWarningAfterDungeon,
										() -> config.general.quiverWarning.enableQuiverWarningAfterDungeon,
										newValue -> config.general.quiverWarning.enableQuiverWarningAfterDungeon = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Item List
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemList"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemList.enableItemList"))
								.binding(defaults.general.itemList.enableItemList,
										() -> config.general.itemList.enableItemList,
										newValue -> config.general.itemList.enableItemList = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Item Tooltip
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemTooltip"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemTooltip.enableNPCPrice"))
								.binding(defaults.general.itemTooltip.enableNPCPrice,
										() -> config.general.itemTooltip.enableNPCPrice,
										newValue -> config.general.itemTooltip.enableNPCPrice = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemTooltip.enableMotesPrice"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.itemTooltip.enableMotesPrice.@Tooltip")))
								.binding(defaults.general.itemTooltip.enableMotesPrice,
										() -> config.general.itemTooltip.enableMotesPrice,
										newValue -> config.general.itemTooltip.enableMotesPrice = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemTooltip.enableAvgBIN"))
								.binding(defaults.general.itemTooltip.enableAvgBIN,
										() -> config.general.itemTooltip.enableAvgBIN,
										newValue -> config.general.itemTooltip.enableAvgBIN = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<SkyblockerConfig.Average>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemTooltip.avg"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.itemTooltip.avg.@Tooltip")))
								.binding(defaults.general.itemTooltip.avg,
										() -> config.general.itemTooltip.avg,
										newValue -> config.general.itemTooltip.avg = newValue)
								.controller(ConfigUtils::createEnumCyclingListController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemTooltip.enableLowestBIN"))
								.binding(defaults.general.itemTooltip.enableLowestBIN,
										() -> config.general.itemTooltip.enableLowestBIN,
										newValue -> config.general.itemTooltip.enableLowestBIN = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemTooltip.enableBazaarPrice"))
								.binding(defaults.general.itemTooltip.enableBazaarPrice,
										() -> config.general.itemTooltip.enableBazaarPrice,
										newValue -> config.general.itemTooltip.enableBazaarPrice = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemTooltip.enableObtainedDate"))
								.binding(defaults.general.itemTooltip.enableObtainedDate,
										() -> config.general.itemTooltip.enableObtainedDate,
										newValue -> config.general.itemTooltip.enableObtainedDate = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemTooltip.enableMuseumInfo"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.itemTooltip.enableMuseumInfo.@Tooltip")))
								.binding(defaults.general.itemTooltip.enableMuseumInfo,
										() -> config.general.itemTooltip.enableMuseumInfo,
										newValue -> config.general.itemTooltip.enableMuseumInfo = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemTooltip.enableExoticTooltip"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.itemTooltip.enableExoticTooltip.@Tooltip")))
								.binding(defaults.general.itemTooltip.enableExoticTooltip,
										() -> config.general.itemTooltip.enableExoticTooltip,
										newValue -> config.general.itemTooltip.enableExoticTooltip = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Item Info Display
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemInfoDisplay"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemInfoDisplay.attributeShardInfo"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.itemInfoDisplay.attributeShardInfo.@Tooltip")))
								.binding(defaults.general.itemInfoDisplay.attributeShardInfo,
										() -> config.general.itemInfoDisplay.attributeShardInfo,
										newValue -> config.general.itemInfoDisplay.attributeShardInfo = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemInfoDisplay.itemRarityBackgrounds"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.itemInfoDisplay.itemRarityBackgrounds.@Tooltip")))
								.binding(defaults.general.itemInfoDisplay.itemRarityBackgrounds,
										() -> config.general.itemInfoDisplay.itemRarityBackgrounds,
										newValue -> config.general.itemInfoDisplay.itemRarityBackgrounds = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<SkyblockerConfig.RarityBackgroundStyle>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemInfoDisplay.itemRarityBackgroundStyle"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.itemInfoDisplay.itemRarityBackgroundStyle.@Tooltip")))
								.binding(defaults.general.itemInfoDisplay.itemRarityBackgroundStyle,
										() -> config.general.itemInfoDisplay.itemRarityBackgroundStyle,
										newValue -> config.general.itemInfoDisplay.itemRarityBackgroundStyle = newValue)
								.controller(ConfigUtils::createEnumCyclingListController)
								.build())
						.option(Option.<Float>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemInfoDisplay.itemRarityBackgroundsOpacity"))
								.binding(defaults.general.itemInfoDisplay.itemRarityBackgroundsOpacity,
										() -> config.general.itemInfoDisplay.itemRarityBackgroundsOpacity,
										newValue -> config.general.itemInfoDisplay.itemRarityBackgroundsOpacity = newValue)
								.controller(opt -> FloatSliderControllerBuilder.create(opt).range(0f, 1f).step(0.05f).formatValue(ConfigUtils.FLOAT_TWO_FORMATTER))
								.build())
						.build())

				//Item Protection
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemProtection"))
						.collapsed(true)
						.option(Option.<SkyblockerConfig.SlotLockStyle>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.itemProtection.slotLockStyle"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.itemProtection.slotLockStyle.@Tooltip")))
								.binding(defaults.general.itemProtection.slotLockStyle,
										() -> config.general.itemProtection.slotLockStyle,
										newValue -> config.general.itemProtection.slotLockStyle = newValue)
								.controller(ConfigUtils::createEnumCyclingListController)
								.build())
						.build())

				//Wiki Lookup
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.wikiLookup"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.wikiLookup.enableWikiLookup"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.wikiLookup.enableWikiLookup.@Tooltip")))
								.binding(defaults.general.wikiLookup.enableWikiLookup,
										() -> config.general.wikiLookup.enableWikiLookup,
										newValue -> config.general.wikiLookup.enableWikiLookup = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.wikiLookup.officialWiki"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.wikiLookup.officialWiki.@Tooltip")))
								.binding(defaults.general.wikiLookup.officialWiki,
										() -> config.general.wikiLookup.officialWiki,
										newValue -> config.general.wikiLookup.officialWiki = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Chest Value
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.chestValue"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.chestValue.enableChestValue"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.chestValue.enableChestValue.@Tooltip")))
								.binding(defaults.general.chestValue.enableChestValue,
										() -> config.general.chestValue.enableChestValue,
										newValue -> config.general.chestValue.enableChestValue = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Formatting>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.chestValue.color"))
								.binding(defaults.general.chestValue.color,
										() -> config.general.chestValue.color,
										newValue -> config.general.chestValue.color = newValue)
								.controller(ConfigUtils.getEnumDropdownControllerFactory(ConfigUtils.FORMATTING_FORMATTER))
								.build())
						.option(Option.<Formatting>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.chestValue.incompleteColor"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.chestValue.incompleteColor.@Tooltip")))
								.binding(defaults.general.chestValue.incompleteColor,
										() -> config.general.chestValue.incompleteColor,
										newValue -> config.general.chestValue.incompleteColor = newValue)
								.controller(ConfigUtils.getEnumDropdownControllerFactory(ConfigUtils.FORMATTING_FORMATTER))
								.build())
						.build())

				//Special Effects
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.specialEffects"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.specialEffects.rareDungeonDropEffects"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.specialEffects.rareDungeonDropEffects.@Tooltip")))
								.binding(defaults.general.specialEffects.rareDungeonDropEffects,
										() -> config.general.specialEffects.rareDungeonDropEffects,
										newValue -> config.general.specialEffects.rareDungeonDropEffects = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Hitboxes
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.hitbox"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.hitbox.oldFarmlandHitbox"))
								.binding(defaults.general.hitbox.oldFarmlandHitbox,
										() -> config.general.hitbox.oldFarmlandHitbox,
										newValue -> config.general.hitbox.oldFarmlandHitbox = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.hitbox.oldLeverHitbox"))
								.binding(defaults.general.hitbox.oldLeverHitbox,
										() -> config.general.hitbox.oldLeverHitbox,
										newValue -> config.general.hitbox.oldLeverHitbox = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Title Container
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.titleContainer"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.titleContainer.@Tooltip")))
						.collapsed(true)
						.option(Option.<Float>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.titleContainer.titleContainerScale"))
								.binding(defaults.general.titleContainer.titleContainerScale,
										() -> config.general.titleContainer.titleContainerScale,
										newValue -> config.general.titleContainer.titleContainerScale = newValue)
								.controller(opt -> FloatFieldControllerBuilder.create(opt).range(30f, 140f))
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.titleContainer.config"))
								.text(Text.translatable("text.skyblocker.open"))
								.action((screen, opt) -> MinecraftClient.getInstance().setScreen(new TitleContainerConfigScreen(screen)))
								.build())
						.build())

				//Teleport Overlays
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.teleportOverlay"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.teleportOverlay.enableTeleportOverlays"))
								.binding(defaults.general.teleportOverlay.enableTeleportOverlays,
										() -> config.general.teleportOverlay.enableTeleportOverlays,
										newValue -> config.general.teleportOverlay.enableTeleportOverlays = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.teleportOverlay.enableWeirdTransmission"))
								.binding(defaults.general.teleportOverlay.enableWeirdTransmission,
										() -> config.general.teleportOverlay.enableWeirdTransmission,
										newValue -> config.general.teleportOverlay.enableWeirdTransmission = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.teleportOverlay.enableInstantTransmission"))
								.binding(defaults.general.teleportOverlay.enableInstantTransmission,
										() -> config.general.teleportOverlay.enableInstantTransmission,
										newValue -> config.general.teleportOverlay.enableInstantTransmission = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.teleportOverlay.enableEtherTransmission"))
								.binding(defaults.general.teleportOverlay.enableEtherTransmission,
										() -> config.general.teleportOverlay.enableEtherTransmission,
										newValue -> config.general.teleportOverlay.enableEtherTransmission = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.teleportOverlay.enableSinrecallTransmission"))
								.binding(defaults.general.teleportOverlay.enableSinrecallTransmission,
										() -> config.general.teleportOverlay.enableSinrecallTransmission,
										newValue -> config.general.teleportOverlay.enableSinrecallTransmission = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.teleportOverlay.enableWitherImpact"))
								.binding(defaults.general.teleportOverlay.enableWitherImpact,
										() -> config.general.teleportOverlay.enableWitherImpact,
										newValue -> config.general.teleportOverlay.enableWitherImpact = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Flame Overlay
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.flameOverlay"))
						.collapsed(true)
						.option(Option.<Float>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.flameOverlay.flameHeight"))
								.binding(defaults.general.flameOverlay.flameHeight,
										() -> config.general.flameOverlay.flameHeight,
										newValue -> config.general.flameOverlay.flameHeight = newValue)
								.controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.0f, 0.5f).step(0.01f))
								.build())
						.option(Option.<Float>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.flameOverlay.flameOpacity"))
								.binding(defaults.general.flameOverlay.flameOpacity,
										() -> config.general.flameOverlay.flameOpacity,
										newValue -> config.general.flameOverlay.flameOpacity = newValue)
								.controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.0f, 0.8f).step(0.1f))
								.build())
						.build())
				//Search overlay
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.general.searchOverlay")) //todo description
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.searchOverlay.enableBazaar"))
								.binding(defaults.general.searchOverlay.enableBazaar,
										() -> config.general.searchOverlay.enableBazaar,
										newValue -> config.general.searchOverlay.enableBazaar = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.searchOverlay.enableAuctionHouse"))
								.binding(defaults.general.searchOverlay.enableAuctionHouse,
										() -> config.general.searchOverlay.enableAuctionHouse,
										newValue -> config.general.searchOverlay.enableAuctionHouse = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.searchOverlay.maxSuggestions"))
								.binding(defaults.general.searchOverlay.maxSuggestions,
										() -> config.general.searchOverlay.maxSuggestions,
										newValue -> config.general.searchOverlay.maxSuggestions = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 5).step(1))
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.searchOverlay.historyLength"))
								.binding(defaults.general.searchOverlay.historyLength,
										() -> config.general.searchOverlay.historyLength,
										newValue -> config.general.searchOverlay.historyLength = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 5).step(1))
								.build())
						.build())
				.build();
	}
}
