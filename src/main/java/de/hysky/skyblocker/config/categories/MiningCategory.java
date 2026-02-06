package de.hysky.skyblocker.config.categories;

import java.awt.Color;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.CommonTags;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.configs.MiningConfig;
import de.hysky.skyblocker.config.screens.powdertracker.PowderFilterConfigScreen;
import de.hysky.skyblocker.skyblock.dwarven.CrystalsHudWidget;
import de.hysky.skyblocker.skyblock.dwarven.CarpetHighlighter;
import de.hysky.skyblocker.skyblock.dwarven.profittrackers.PowderMiningTracker;
import de.hysky.skyblocker.skyblock.tabhud.widget.CommsWidget;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.utils.Location;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import net.azureaaron.dandelion.api.ButtonOption;
import net.azureaaron.dandelion.api.ConfigCategory;
import net.azureaaron.dandelion.api.Option;
import net.azureaaron.dandelion.api.OptionGroup;
import net.azureaaron.dandelion.api.controllers.FloatController;
import net.azureaaron.dandelion.api.controllers.IntegerController;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class MiningCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.id(SkyblockerMod.id("config/mining"))
				.name(Component.translatable("skyblocker.config.mining"))

				//Uncategorized Options
				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("skyblocker.config.mining.enableDrillFuel"))
						.description(Component.translatable("skyblocker.config.mining.enableDrillFuel.@Tooltip"))
						.binding(defaults.mining.enableDrillFuel,
								() -> config.mining.enableDrillFuel,
								newValue -> config.mining.enableDrillFuel = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())

				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("skyblocker.config.mining.commissionHighlight"))
						.binding(defaults.mining.commissionHighlight,
								() -> config.mining.commissionHighlight,
								newValue -> config.mining.commissionHighlight = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())

				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("skyblocker.config.mining.callMismyla"))
						.description(Component.translatable("skyblocker.config.mining.callMismyla.@Tooltip"))
						.binding(defaults.mining.callMismyla,
								() -> config.mining.callMismyla,
								newValue -> config.mining.callMismyla = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())

				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("skyblocker.config.mining.redialOnBadSignal"))
						.description(Component.translatable("skyblocker.config.mining.redialOnBadSignal.@Tooltip"))
						.binding(defaults.mining.redialOnBadSignal,
								() -> config.mining.redialOnBadSignal,
								newValue -> config.mining.redialOnBadSignal = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())

				.option(ButtonOption.createBuilder()
						.name(Component.translatable("skyblocker.config.mining.dwarvenHud.screen"))
						.prompt(Component.translatable("text.skyblocker.open"))
						.action(screen -> Minecraft.getInstance().setScreen(new WidgetsConfigurationScreen(Location.DWARVEN_MINES, CommsWidget.ID, screen)))
						.build())

				//Pickobulus Helper
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.mining.pickobulusHelper"))
						.tags(CommonTags.ADDED_IN_5_11_0)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.pickobulusHelper.enable"))
								.description(Component.translatable("skyblocker.config.mining.pickobulusHelper.enable.@Tooltip"))
								.tags(CommonTags.ADDED_IN_5_10_0)
								.binding(defaults.mining.enablePickobulusHelper,
										() -> config.mining.enablePickobulusHelper,
										newValue -> config.mining.enablePickobulusHelper = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.pickobulusHelper.enableHud"))
								.description(Component.translatable("skyblocker.config.mining.pickobulusHelper.enableHud.@Tooltip"))
								.tags(CommonTags.ADDED_IN_5_11_0)
								.binding(defaults.mining.pickobulusHelper.enablePickobulusHud,
										() -> config.mining.pickobulusHelper.enablePickobulusHud,
										newValue -> config.mining.pickobulusHelper.enablePickobulusHud = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.pickobulusHelper.hideOnCooldown"))
								.description(Component.translatable("skyblocker.config.mining.pickobulusHelper.hideOnCooldown.@Tooltip"))
								.tags(CommonTags.ADDED_IN_5_11_0)
								.binding(defaults.mining.pickobulusHelper.hideHudOnCooldown,
										() -> config.mining.pickobulusHelper.hideHudOnCooldown,
										newValue -> config.mining.pickobulusHelper.hideHudOnCooldown = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				//Dwarven Mines
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.mining.dwarvenMines"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.dwarvenMines.solveFetchur"))
								.binding(defaults.mining.dwarvenMines.solveFetchur,
										() -> config.mining.dwarvenMines.solveFetchur,
										newValue -> config.mining.dwarvenMines.solveFetchur = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.dwarvenMines.solvePuzzler"))
								.binding(defaults.mining.dwarvenMines.solvePuzzler,
										() -> config.mining.dwarvenMines.solvePuzzler,
										newValue -> config.mining.dwarvenMines.solvePuzzler = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.dwarvenMines.enableCarpetHighlight"))
								.description(Component.translatable("skyblocker.config.mining.dwarvenMines.enableCarpetHighlight.@Tooltip"))
								.binding(defaults.mining.dwarvenMines.enableCarpetHighlighter,
										() -> config.mining.dwarvenMines.enableCarpetHighlighter,
									newValue -> config.mining.dwarvenMines.enableCarpetHighlighter = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.dwarvenMines.carpetHighlightColor"))
								.description(Component.translatable("skyblocker.config.mining.dwarvenMines.carpetHighlightColor.@Tooltip"))
								.binding(defaults.mining.dwarvenMines.carpetHighlightColor,
										() -> config.mining.dwarvenMines.carpetHighlightColor,
										newValue -> {
											config.mining.dwarvenMines.carpetHighlightColor = newValue;
											CarpetHighlighter.INSTANCE.configCallback(newValue);
										})
								.controller(ConfigUtils.createColourController(true))
								.build())
						.build())

				//Crystal Hollows
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.mining.crystalHollows"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.crystalHollows.metalDetectorHelper"))
								.description(Component.translatable("skyblocker.config.mining.crystalHollows.metalDetectorHelper.@Tooltip"))
								.binding(defaults.mining.crystalHollows.metalDetectorHelper,
										() -> config.mining.crystalHollows.metalDetectorHelper,
										newValue -> config.mining.crystalHollows.metalDetectorHelper = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.crystalHollows.nucleusWaypoints"))
								.description(Component.translatable("skyblocker.config.mining.crystalHollows.nucleusWaypoints.@Tooltip"))
								.binding(defaults.mining.crystalHollows.nucleusWaypoints,
										() -> config.mining.crystalHollows.nucleusWaypoints,
										newValue -> config.mining.crystalHollows.nucleusWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.crystalHollows.chestHighlighter"))
								.description(Component.translatable("skyblocker.config.mining.crystalHollows.chestHighlighter.@Tooltip"))
								.binding(defaults.mining.crystalHollows.chestHighlighter,
										() -> config.mining.crystalHollows.chestHighlighter,
										newValue -> config.mining.crystalHollows.chestHighlighter = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.crystalHollows.chestHighlighter.color"))
								.description(Component.translatable("skyblocker.config.mining.crystalHollows.chestHighlighter.color.@Tooltip"))
								.binding(defaults.mining.crystalHollows.chestHighlightColor,
										() -> config.mining.crystalHollows.chestHighlightColor,
										newValue -> config.mining.crystalHollows.chestHighlightColor = newValue)
								.controller(ConfigUtils.createColourController(true))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.crystalHollows.enablePowderTracker"))
								.description(Component.translatable("skyblocker.config.mining.crystalHollows.enablePowderTracker.@Tooltip"))
								.binding(defaults.mining.crystalHollows.enablePowderTracker,
										() -> config.mining.crystalHollows.enablePowderTracker,
										newValue -> {
									config.mining.crystalHollows.enablePowderTracker = newValue;
									if (newValue) PowderMiningTracker.INSTANCE.recalculateAll();
								})
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(ButtonOption.createBuilder()
								.name(Component.translatable("skyblocker.config.mining.crystalHollows.powderTrackerFilter"))
								.description(Component.translatable("skyblocker.config.mining.crystalHollows.powderTrackerFilter.@Tooltip"))
								.prompt(Component.translatable("text.skyblocker.open"))
								.action(screen -> Minecraft.getInstance().setScreen(new PowderFilterConfigScreen(screen, new ObjectImmutableList<>(PowderMiningTracker.getName2IdMap().keySet()))))
								.build())
						.build())

				//Crystal Hollows Map
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.mining.crystalsHud"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.crystalsHud.enabled"))
								.binding(defaults.mining.crystalsHud.enabled,
										() -> config.mining.crystalsHud.enabled,
										newValue -> config.mining.crystalsHud.enabled = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(ButtonOption.createBuilder()
								.name(Component.translatable("skyblocker.config.mining.crystalsHud.screen"))
								.prompt(Component.translatable("text.skyblocker.open"))
								.action(screen -> Minecraft.getInstance().setScreen(new WidgetsConfigurationScreen(Location.CRYSTAL_HOLLOWS, CrystalsHudWidget.getInstance().getInternalID(), screen)))
								.build())
						.option(Option.<Float>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.crystalsHud.mapScaling"))
								.binding(defaults.mining.crystalsHud.mapScaling,
										() -> config.mining.crystalsHud.mapScaling,
										newValue -> config.mining.crystalsHud.mapScaling = newValue)
								.controller(FloatController.createBuilder().build())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.crystalsHud.showLocations"))
								.description(Component.translatable("skyblocker.config.mining.crystalsHud.showLocations.@Tooltip"))
								.binding(defaults.mining.crystalsHud.showLocations,
										() -> config.mining.crystalsHud.showLocations,
										newValue -> config.mining.crystalsHud.showLocations = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.crystalsHud.showLocations.locationSize"))
								.description(Component.translatable("skyblocker.config.mining.crystalsHud.showLocations.locationSize.@Tooltip"))
								.binding(defaults.mining.crystalsHud.locationSize,
										() -> config.mining.crystalsHud.locationSize,
										newValue -> config.mining.crystalsHud.locationSize = newValue)
								.controller(IntegerController.createBuilder().range(4, 12).slider(2).build())
								.build())
						.build())

				//Crystal Hollows waypoints
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.mining.crystalsWaypoints"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.crystalsWaypoints.enabled"))
								.description(Component.translatable("skyblocker.config.mining.crystalsWaypoints.enabled.@Tooltip"))
								.binding(defaults.mining.crystalsWaypoints.enabled,
										() -> config.mining.crystalsWaypoints.enabled,
										newValue -> config.mining.crystalsWaypoints.enabled = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.crystalsWaypoints.findInChat"))
								.description(Component.translatable("skyblocker.config.mining.crystalsWaypoints.findInChat.@Tooltip"))
								.binding(defaults.mining.crystalsWaypoints.findInChat,
										() -> config.mining.crystalsWaypoints.findInChat,
										newValue -> config.mining.crystalsWaypoints.findInChat = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.crystalsWaypoints.wishingCompassSolver"))
								.description(Component.translatable("skyblocker.config.mining.crystalsWaypoints.wishingCompassSolver.@Tooltip"))
								.binding(defaults.mining.crystalsWaypoints.wishingCompassSolver,
										() -> config.mining.crystalsWaypoints.wishingCompassSolver,
										newValue -> config.mining.crystalsWaypoints.wishingCompassSolver = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.crystalsWaypoints.shareFairyGrotto"))
								.description(Component.translatable("skyblocker.config.mining.crystalsWaypoints.shareFairyGrotto.@Tooltip"))
								.binding(defaults.mining.crystalsWaypoints.shareFairyGrotto,
										() -> config.mining.crystalsWaypoints.shareFairyGrotto,
										newValue -> config.mining.crystalsWaypoints.shareFairyGrotto = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				//commission waypoints
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.mining.commissionWaypoints"))
						.collapsed(false)
						.option(Option.<MiningConfig.CommissionWaypointMode>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.commissionWaypoints.mode"))
								.description(Component.translatable("skyblocker.config.mining.commissionWaypoints.mode.@Tooltip[0]"),
										Component.translatable("skyblocker.config.mining.commissionWaypoints.mode.@Tooltip[1]"),
										Component.translatable("skyblocker.config.mining.commissionWaypoints.mode.@Tooltip[2]"),
										Component.translatable("skyblocker.config.mining.commissionWaypoints.mode.@Tooltip[3]"),
										Component.translatable("skyblocker.config.mining.commissionWaypoints.mode.@Tooltip[4]"))
								.binding(defaults.mining.commissionWaypoints.mode,
										() -> config.mining.commissionWaypoints.mode,
										newValue -> config.mining.commissionWaypoints.mode = newValue)
								.controller(ConfigUtils.createEnumController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.commissionWaypoints.useColor"))
								.description(Component.translatable("skyblocker.config.mining.commissionWaypoints.useColor.@Tooltip"))
								.binding(defaults.mining.commissionWaypoints.useColor,
										() -> config.mining.commissionWaypoints.useColor,
										newValue -> config.mining.commissionWaypoints.useColor = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.commissionWaypoints.showBaseCamp"))
								.description(Component.translatable("skyblocker.config.mining.commissionWaypoints.showBaseCamp.@Tooltip"))
								.binding(defaults.mining.commissionWaypoints.showBaseCamp,
										() -> config.mining.commissionWaypoints.showBaseCamp,
										newValue -> config.mining.commissionWaypoints.showBaseCamp = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.commissionWaypoints.showEmissary"))
								.description(Component.translatable("skyblocker.config.mining.commissionWaypoints.showEmissary.@Tooltip"))
								.binding(defaults.mining.commissionWaypoints.showEmissary,
										() -> config.mining.commissionWaypoints.showEmissary,
										newValue -> config.mining.commissionWaypoints.showEmissary = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.commissionWaypoints.hideEmissaryOnPigeon"))
								.description(Component.translatable("skyblocker.config.mining.commissionWaypoints.hideEmissaryOnPigeon.@Tooltip"))
								.binding(defaults.mining.commissionWaypoints.hideEmissaryOnPigeon,
										() -> config.mining.commissionWaypoints.hideEmissaryOnPigeon,
										newValue -> config.mining.commissionWaypoints.hideEmissaryOnPigeon = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				//Glacite Tunnels
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.mining.glacite"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.glacite.coldOverlay"))
								.description(Component.translatable("skyblocker.config.mining.glacite.coldOverlay.@Tooltip"))
								.binding(defaults.mining.glacite.coldOverlay,
										() -> config.mining.glacite.coldOverlay,
										newValue -> config.mining.glacite.coldOverlay = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.glacite.fossilSolver"))
								.description(Component.translatable("skyblocker.config.mining.glacite.fossilSolver.@Tooltip"))
								.binding(defaults.mining.glacite.fossilSolver,
												() -> config.mining.glacite.fossilSolver,
												newValue -> config.mining.glacite.fossilSolver = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.glacite.enableCorpseFinder"))
								.description(Component.translatable("skyblocker.config.mining.glacite.enableCorpseFinder.@Tooltip"))
								.binding(defaults.mining.glacite.enableCorpseFinder,
										() -> config.mining.glacite.enableCorpseFinder,
										newValue -> config.mining.glacite.enableCorpseFinder = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.glacite.enableParsingChatCorpseFinder"))
								.description(Component.translatable("skyblocker.config.mining.glacite.enableParsingChatCorpseFinder.@Tooltip"))
								.binding(defaults.mining.glacite.enableParsingChatCorpseFinder,
										() -> config.mining.glacite.enableParsingChatCorpseFinder,
										newValue -> config.mining.glacite.enableParsingChatCorpseFinder = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.glacite.autoShareCorpses"))
								.description(Component.translatable("skyblocker.config.mining.glacite.autoShareCorpses.@Tooltip"))
								.binding(defaults.mining.glacite.autoShareCorpses,
										() -> config.mining.glacite.autoShareCorpses,
										newValue -> config.mining.glacite.autoShareCorpses = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.glacite.enableCorpseProfitTracker"))
								.description(Component.translatable("skyblocker.config.mining.glacite.enableCorpseProfitTracker.@Tooltip"))
								.binding(defaults.mining.glacite.enableCorpseProfitTracker,
										() -> config.mining.glacite.enableCorpseProfitTracker,
										newValue -> config.mining.glacite.enableCorpseProfitTracker = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.glacite.forceEnglishCorpseProfitTracker"))
								.description(Component.translatable("skyblocker.config.mining.glacite.forceEnglishCorpseProfitTracker.@Tooltip"))
								.tags(CommonTags.ADDED_IN_5_9_0)
								.binding(defaults.mining.glacite.forceEnglishCorpseProfitTracker,
										() -> config.mining.glacite.forceEnglishCorpseProfitTracker,
										newValue -> config.mining.glacite.forceEnglishCorpseProfitTracker = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())
				//Block break prediction
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.mining.blockBreakPrediction"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.blockBreakPrediction.enabled"))
								.binding(defaults.mining.blockBreakPrediction.enabled,
										() -> config.mining.blockBreakPrediction.enabled,
										newValue -> config.mining.blockBreakPrediction.enabled = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.mining.blockBreakPrediction.playSound"))
								.description(Component.translatable("skyblocker.config.mining.blockBreakPrediction.playSound.@Tooltip"))
								.binding(defaults.mining.blockBreakPrediction.playSound,
										() -> config.mining.blockBreakPrediction.playSound,
										newValue -> config.mining.blockBreakPrediction.playSound = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())

						.build())
				.build();
	}
}
