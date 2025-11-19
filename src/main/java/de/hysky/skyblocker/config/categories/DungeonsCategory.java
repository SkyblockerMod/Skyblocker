package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.CommonTags;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.configs.DungeonsConfig;
import de.hysky.skyblocker.skyblock.dungeon.DungeonMapConfigScreen;
import de.hysky.skyblocker.utils.waypoint.Waypoint.Type;
import net.azureaaron.dandelion.systems.ButtonOption;
import net.azureaaron.dandelion.systems.ConfigCategory;
import net.azureaaron.dandelion.systems.Option;
import net.azureaaron.dandelion.systems.OptionGroup;
import net.azureaaron.dandelion.systems.controllers.FloatController;
import net.azureaaron.dandelion.systems.controllers.IntegerController;
import net.azureaaron.dandelion.systems.controllers.StringController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.Color;

public class DungeonsCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.id(SkyblockerMod.id("config/dungeons"))
				.name(Text.translatable("skyblocker.config.dungeons"))

				//Ungrouped Options
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.fancyPartyFinder"))
						.binding(defaults.dungeons.fancyPartyFinder,
								() -> config.dungeons.fancyPartyFinder,
								newValue -> config.dungeons.fancyPartyFinder = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.croesusHelper"))
						.description(Text.translatable("skyblocker.config.dungeons.croesusHelper.@Tooltip"))
						.binding(defaults.dungeons.croesusHelper,
								() -> config.dungeons.croesusHelper,
								newValue -> config.dungeons.croesusHelper = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.salvageHelper"))
						.description(Text.translatable("skyblocker.config.dungeons.salvageHelper.@Tooltip"))
						.binding(defaults.dungeons.salvageHelper,
								() -> config.dungeons.salvageHelper,
								newValue -> config.dungeons.salvageHelper = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.salvageHelper.onlyDonated"))
						.tags(CommonTags.ADDED_IN_5_9_0)
						.binding(defaults.dungeons.onlyHighlightDonatedItems,
								() -> config.dungeons.onlyHighlightDonatedItems,
								newValue -> config.dungeons.onlyHighlightDonatedItems = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
                .option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.sellableItemsHighlighter"))
						.description(Text.translatable("skyblocker.config.dungeons.sellableItemsHighlighter.@Tooltip"))
						.binding(defaults.dungeons.sellableItemsHighlighter,
								() -> config.dungeons.sellableItemsHighlighter,
								newValue -> config.dungeons.sellableItemsHighlighter = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.playerSecretsTracker"))
						.description(Text.translatable("skyblocker.config.dungeons.playerSecretsTracker.@Tooltip"))
						.binding(defaults.dungeons.playerSecretsTracker,
								() -> config.dungeons.playerSecretsTracker,
								newValue -> config.dungeons.playerSecretsTracker = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.classBasedPlayerGlow"))
						.description(Text.translatable("skyblocker.config.dungeons.classBasedPlayerGlow.@Tooltip"))
						.binding(defaults.dungeons.classBasedPlayerGlow,
								() -> config.dungeons.classBasedPlayerGlow,
								newValue -> config.dungeons.classBasedPlayerGlow = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.starredMobGlow"))
						.description(Text.translatable("skyblocker.config.dungeons.starredMobGlow.@Tooltip"))
						.binding(defaults.dungeons.starredMobGlow,
								() -> config.dungeons.starredMobGlow,
								newValue -> config.dungeons.starredMobGlow = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.starredMobBoundingBoxes"))
						.description(Text.translatable("skyblocker.config.dungeons.starredMobBoundingBoxes.@Tooltip"))
						.binding(defaults.dungeons.starredMobBoundingBoxes,
								() -> config.dungeons.starredMobBoundingBoxes,
								newValue -> config.dungeons.starredMobBoundingBoxes = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.highlightDoorKeys"))
						.description(Text.translatable("skyblocker.config.dungeons.highlightDoorKeys.@Tooltip"))
						.binding(defaults.dungeons.highlightDoorKeys,
								() -> config.dungeons.highlightDoorKeys,
								newValue -> config.dungeons.highlightDoorKeys = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.allowDroppingProtectedItems"))
						.description(Text.translatable("skyblocker.config.dungeons.allowDroppingProtectedItems.@Tooltip"))
						.binding(defaults.dungeons.allowDroppingProtectedItems,
								() -> config.dungeons.allowDroppingProtectedItems,
								newValue -> config.dungeons.allowDroppingProtectedItems = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.hideSoulweaverSkulls"))
						.description(Text.translatable("skyblocker.config.dungeons.hideSoulweaverSkulls.@Tooltip"))
						.binding(defaults.dungeons.hideSoulweaverSkulls,
								() -> config.dungeons.hideSoulweaverSkulls,
								newValue -> config.dungeons.hideSoulweaverSkulls = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.dungeonSplits"))
						.description(Text.translatable("skyblocker.config.dungeons.dungeonSplits.@Tooltip"))
						.binding(defaults.dungeons.dungeonSplits,
								() -> config.dungeons.dungeonSplits,
								newValue -> config.dungeons.dungeonSplits = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.bloodCampHelper"))
						.description(Text.translatable("skyblocker.config.dungeons.bloodCampHelper.@Tooltip"))
						.binding(defaults.dungeons.bloodCampHelper,
								() -> config.dungeons.bloodCampHelper,
								newValue -> config.dungeons.bloodCampHelper = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())

				// Map
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.map"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.map.enableMap"))
								.binding(defaults.dungeons.dungeonMap.enableMap,
										() -> config.dungeons.dungeonMap.enableMap,
										newValue -> config.dungeons.dungeonMap.enableMap = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.map.fancyMap"))
								.binding(defaults.dungeons.dungeonMap.fancyMap,
										() -> config.dungeons.dungeonMap.fancyMap,
										newValue -> config.dungeons.dungeonMap.fancyMap = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.map.showSelfHead"))
								.binding(defaults.dungeons.dungeonMap.showSelfHead,
										() -> config.dungeons.dungeonMap.showSelfHead,
										newValue -> config.dungeons.dungeonMap.showSelfHead = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.map.showRoomLabels"))
								.tags(CommonTags.ADDED_IN_5_9_0)
								.binding(defaults.dungeons.dungeonMap.showRoomLabels,
										() -> config.dungeons.dungeonMap.showRoomLabels,
										newValue -> config.dungeons.dungeonMap.showRoomLabels = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Float>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.map.mapScaling"))
								.binding(defaults.dungeons.dungeonMap.mapScaling,
										() -> config.dungeons.dungeonMap.mapScaling,
										newValue -> config.dungeons.dungeonMap.mapScaling = newValue)
								.controller(FloatController.createBuilder().build())
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.map.mapScreen"))
								.prompt(Text.translatable("text.skyblocker.open"))
								.action(screen -> MinecraftClient.getInstance().setScreen(new DungeonMapConfigScreen(screen)))
								.build())
						.build())

				// Spirit Leap Overlay
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.spiritLeapOverlay"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.spiritLeapOverlay.enableLeapOverlay"))
								.description(Text.translatable("skyblocker.config.dungeons.spiritLeapOverlay.enableLeapOverlay.@Tooltip"))
								.binding(defaults.dungeons.leapOverlay.enableLeapOverlay,
										() -> config.dungeons.leapOverlay.enableLeapOverlay,
										newValue -> config.dungeons.leapOverlay.enableLeapOverlay = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.spiritLeapOverlay.leapKeybinds"))
								.description(Text.translatable("skyblocker.config.dungeons.spiritLeapOverlay.leapKeybinds.@Tooltip"))
								.binding(defaults.dungeons.leapOverlay.leapKeybinds,
										() -> config.dungeons.leapOverlay.leapKeybinds,
										newValue -> config.dungeons.leapOverlay.leapKeybinds = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.spiritLeapOverlay.showMap"))
								.binding(defaults.dungeons.leapOverlay.showMap,
										() -> config.dungeons.leapOverlay.showMap,
										newValue -> config.dungeons.leapOverlay.showMap = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Float>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.spiritLeapOverlay.scale"))
								.binding(defaults.dungeons.leapOverlay.scale,
										() -> config.dungeons.leapOverlay.scale,
										newValue -> config.dungeons.leapOverlay.scale = newValue)
								.controller(FloatController.createBuilder().range(1f, 2f).slider(0.05f).build())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.spiritLeapOverlay.enableLeapMessage"))
								.description(Text.translatable("skyblocker.config.dungeons.spiritLeapOverlay.enableLeapMessage.@Tooltip"))
								.binding(defaults.dungeons.leapOverlay.enableLeapMessage,
										() -> config.dungeons.leapOverlay.enableLeapMessage,
										newValue -> config.dungeons.leapOverlay.enableLeapMessage = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.spiritLeapOverlay.leapMessage"))
								.description(Text.translatable("skyblocker.config.dungeons.spiritLeapOverlay.leapMessage.@Tooltip"))
								.binding(defaults.dungeons.leapOverlay.leapMessage,
										() -> config.dungeons.leapOverlay.leapMessage,
										newValue -> config.dungeons.leapOverlay.leapMessage = newValue)
								.controller(StringController.createBuilder().build())
								.build())
						.build())

				// Puzzle Solver
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.puzzle"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.puzzle.solveTicTacToe"))
								.description(Text.translatable("skyblocker.config.dungeons.puzzle.solveTicTacToe.@Tooltip"))
								.binding(defaults.dungeons.puzzleSolvers.solveTicTacToe,
										() -> config.dungeons.puzzleSolvers.solveTicTacToe,
										newValue -> config.dungeons.puzzleSolvers.solveTicTacToe = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.puzzle.solveThreeWeirdos"))
								.binding(defaults.dungeons.puzzleSolvers.solveThreeWeirdos,
										() -> config.dungeons.puzzleSolvers.solveThreeWeirdos,
										newValue -> config.dungeons.puzzleSolvers.solveThreeWeirdos = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.puzzle.creeperSolver"))
								.description(Text.translatable("skyblocker.config.dungeons.puzzle.creeperSolver.@Tooltip"))
								.binding(defaults.dungeons.puzzleSolvers.creeperSolver,
										() -> config.dungeons.puzzleSolvers.creeperSolver,
										newValue -> config.dungeons.puzzleSolvers.creeperSolver = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.puzzle.solveWaterboard"))
								.description(Text.translatable("skyblocker.config.dungeons.puzzle.solveWaterboard.@Tooltip"))
								.binding(defaults.dungeons.puzzleSolvers.waterboardOneFlow,
										() -> config.dungeons.puzzleSolvers.waterboardOneFlow,
										newValue -> config.dungeons.puzzleSolvers.waterboardOneFlow = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.puzzle.previewWaterPath"))
								.binding(defaults.dungeons.puzzleSolvers.previewWaterPath,
										() -> config.dungeons.puzzleSolvers.previewWaterPath,
										newValue -> config.dungeons.puzzleSolvers.previewWaterPath = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.puzzle.previewLeverEffects"))
								.binding(defaults.dungeons.puzzleSolvers.previewLeverEffects,
										() -> config.dungeons.puzzleSolvers.previewLeverEffects,
										newValue -> config.dungeons.puzzleSolvers.previewLeverEffects = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.puzzle.blazeSolver"))
								.description(Text.translatable("skyblocker.config.dungeons.puzzle.blazeSolver.@Tooltip"))
								.binding(defaults.dungeons.puzzleSolvers.blazeSolver,
										() -> config.dungeons.puzzleSolvers.blazeSolver,
										newValue -> config.dungeons.puzzleSolvers.blazeSolver = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.puzzle.solveBoulder"))
								.description(Text.translatable("skyblocker.config.dungeons.puzzle.solveBoulder.@Tooltip"))
								.binding(defaults.dungeons.puzzleSolvers.solveBoulder,
										() -> config.dungeons.puzzleSolvers.solveBoulder,
										newValue -> config.dungeons.puzzleSolvers.solveBoulder = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.puzzle.solveIceFill"))
								.binding(defaults.dungeons.puzzleSolvers.solveIceFill,
										() -> config.dungeons.puzzleSolvers.solveIceFill,
										newValue -> config.dungeons.puzzleSolvers.solveIceFill = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.puzzle.solveSilverfish"))
								.binding(defaults.dungeons.puzzleSolvers.solveSilverfish,
										() -> config.dungeons.puzzleSolvers.solveSilverfish,
										newValue -> config.dungeons.puzzleSolvers.solveSilverfish = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.puzzle.solveTrivia"))
								.binding(defaults.dungeons.puzzleSolvers.solveTrivia,
										() -> config.dungeons.puzzleSolvers.solveTrivia,
										newValue -> config.dungeons.puzzleSolvers.solveTrivia = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.puzzle.solveTeleportMaze"))
								.binding(defaults.dungeons.puzzleSolvers.solveTeleportMaze,
										() -> config.dungeons.puzzleSolvers.solveTeleportMaze,
										newValue -> config.dungeons.puzzleSolvers.solveTeleportMaze = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				// The Professor (F3/M3)
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.professor"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.professor.fireFreezeStaffTimer"))
								.description(Text.translatable("skyblocker.config.dungeons.professor.fireFreezeStaffTimer.@Tooltip"))
								.binding(defaults.dungeons.theProfessor.fireFreezeStaffTimer,
										() -> config.dungeons.theProfessor.fireFreezeStaffTimer,
										newValue -> config.dungeons.theProfessor.fireFreezeStaffTimer = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.professor.floor3GuardianHealthDisplay"))
								.description(Text.translatable("skyblocker.config.dungeons.professor.floor3GuardianHealthDisplay.@Tooltip"))
								.binding(defaults.dungeons.theProfessor.floor3GuardianHealthDisplay,
										() -> config.dungeons.theProfessor.floor3GuardianHealthDisplay,
										newValue -> config.dungeons.theProfessor.floor3GuardianHealthDisplay = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				// Livid (F5/M5)
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.livid"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.livid.enableSolidColor"))
								.description(Text.translatable("skyblocker.config.dungeons.livid.enableSolidColor.@Tooltip"))
								.binding(defaults.dungeons.livid.enableSolidColor,
										() -> config.dungeons.livid.enableSolidColor,
										newValue -> config.dungeons.livid.enableSolidColor = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Color>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.livid.customColor"))
								.binding(defaults.dungeons.livid.customColor,
										() -> config.dungeons.livid.customColor,
										newValue -> config.dungeons.livid.customColor = newValue)
								.controller(ConfigUtils.createColourController(false))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.livid.enableLividColorGlow"))
								.description(Text.translatable("skyblocker.config.dungeons.livid.enableLividColorGlow.@Tooltip"))
								.binding(defaults.dungeons.livid.enableLividColorGlow,
										() -> config.dungeons.livid.enableLividColorGlow,
										newValue -> config.dungeons.livid.enableLividColorGlow = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.livid.enableLividColorBoundingBox"))
								.description(Text.translatable("skyblocker.config.dungeons.livid.enableLividColorBoundingBox.@Tooltip"))
								.binding(defaults.dungeons.livid.enableLividColorBoundingBox,
										() -> config.dungeons.livid.enableLividColorBoundingBox,
										newValue -> config.dungeons.livid.enableLividColorBoundingBox = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.livid.enableLividColorTitle"))
								.description(Text.translatable("skyblocker.config.dungeons.livid.enableLividColorTitle.@Tooltip"))
								.binding(defaults.dungeons.livid.enableLividColorTitle,
										() -> config.dungeons.livid.enableLividColorTitle,
										newValue -> config.dungeons.livid.enableLividColorTitle = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.livid.enableLividColorText"))
								.description(Text.translatable("skyblocker.config.dungeons.livid.enableLividColorText.@Tooltip"))
								.binding(defaults.dungeons.livid.enableLividColorText,
										() -> config.dungeons.livid.enableLividColorText,
										newValue -> config.dungeons.livid.enableLividColorText = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.livid.lividColorText"))
								.description(Text.translatable("skyblocker.config.dungeons.livid.lividColorText.@Tooltip"))
								.binding(defaults.dungeons.livid.lividColorText,
										() -> config.dungeons.livid.lividColorText,
										newValue -> config.dungeons.livid.lividColorText = newValue)
								.controller(StringController.createBuilder().build())
								.build())
						.build())

				// Terminal (F7/M7)
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.terminals"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.terminals.solveColor"))
								.binding(defaults.dungeons.terminals.solveColor,
										() -> config.dungeons.terminals.solveColor,
										newValue -> config.dungeons.terminals.solveColor = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.terminals.solveOrder"))
								.binding(defaults.dungeons.terminals.solveOrder,
										() -> config.dungeons.terminals.solveOrder,
										newValue -> config.dungeons.terminals.solveOrder = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.terminals.solveStartsWith"))
								.binding(defaults.dungeons.terminals.solveStartsWith,
										() -> config.dungeons.terminals.solveStartsWith,
										newValue -> config.dungeons.terminals.solveStartsWith = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.terminals.solveSameColor"))
								.binding(defaults.dungeons.terminals.solveSameColor,
										() -> config.dungeons.terminals.solveSameColor,
										newValue -> config.dungeons.terminals.solveSameColor = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.terminals.blockIncorrectClicks"))
								.binding(defaults.dungeons.terminals.blockIncorrectClicks,
										() -> config.dungeons.terminals.blockIncorrectClicks,
										newValue -> config.dungeons.terminals.blockIncorrectClicks = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				// Devices (F7/M7)
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.devices"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.devices.solveSimonSays"))
								.description(Text.translatable("skyblocker.config.dungeons.devices.solveSimonSays.@Tooltip"))
								.binding(defaults.dungeons.devices.solveSimonSays,
										() -> config.dungeons.devices.solveSimonSays,
										newValue -> config.dungeons.devices.solveSimonSays = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.devices.solveLightsOn"))
								.description(Text.translatable("skyblocker.config.dungeons.devices.solveLightsOn.@Tooltip"))
								.binding(defaults.dungeons.devices.solveLightsOn,
										() -> config.dungeons.devices.solveLightsOn,
										newValue -> config.dungeons.devices.solveLightsOn = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.devices.solveArrowAlign"))
								.tags(CommonTags.ADDED_IN_5_9_0)
								.description(Text.translatable("skyblocker.config.dungeons.devices.solveArrowAlign.@Tooltip"))
								.binding(defaults.dungeons.devices.solveArrowAlign,
										() -> config.dungeons.devices.solveArrowAlign,
										newValue -> config.dungeons.devices.solveArrowAlign = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.devices.solveTargetPractice"))
								.tags(CommonTags.ADDED_IN_5_9_0)
								.description(Text.translatable("skyblocker.config.dungeons.devices.solveTargetPractice.@Tooltip"))
								.binding(defaults.dungeons.devices.solveTargetPractice,
										() -> config.dungeons.devices.solveTargetPractice,
										newValue -> config.dungeons.devices.solveTargetPractice = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				// Waypoints for goldor phase in f7/m7
				.group(OptionGroup.createBuilder().name(Text.translatable("skyblocker.config.dungeons.goldorWaypoints"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.goldorWaypoints.enableGoldorWaypoints"))
								.binding(defaults.dungeons.goldor.enableGoldorWaypoints,
										() -> config.dungeons.goldor.enableGoldorWaypoints,
										newValue -> config.dungeons.goldor.enableGoldorWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Type>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.goldorWaypoints.waypointType"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.waypointType.@Tooltip"))
								.binding(defaults.dungeons.goldor.waypointType,
										() -> config.dungeons.goldor.waypointType,
										newValue -> config.dungeons.goldor.waypointType = newValue)
								.controller(ConfigUtils.createEnumController())
								.build())
						.build())

				// Dungeon Secret Waypoints
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.secretWaypoints"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.secretWaypoints.enableSecretWaypoints"))
								.binding(defaults.dungeons.secretWaypoints.enableSecretWaypoints,
										() -> config.dungeons.secretWaypoints.enableSecretWaypoints,
										newValue -> config.dungeons.secretWaypoints.enableSecretWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Type>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.secretWaypoints.waypointType"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.waypointType.@Tooltip"),
										Text.translatable("skyblocker.config.uiAndVisuals.waypoints.waypointType.generalNote"))
								.binding(defaults.dungeons.secretWaypoints.waypointType,
										() -> config.dungeons.secretWaypoints.waypointType,
										newValue -> config.dungeons.secretWaypoints.waypointType = newValue)
								.controller(ConfigUtils.createEnumController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.secretWaypoints.showSecretText"))
								.binding(defaults.dungeons.secretWaypoints.showSecretText,
										() -> config.dungeons.secretWaypoints.showSecretText,
										newValue -> config.dungeons.secretWaypoints.showSecretText = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.secretWaypoints.enableEntranceWaypoints"))
								.binding(defaults.dungeons.secretWaypoints.enableEntranceWaypoints,
										() -> config.dungeons.secretWaypoints.enableEntranceWaypoints,
										newValue -> config.dungeons.secretWaypoints.enableEntranceWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.secretWaypoints.enableSuperboomWaypoints"))
								.binding(defaults.dungeons.secretWaypoints.enableSuperboomWaypoints,
										() -> config.dungeons.secretWaypoints.enableSuperboomWaypoints,
										newValue -> config.dungeons.secretWaypoints.enableSuperboomWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.secretWaypoints.enableChestWaypoints"))
								.binding(defaults.dungeons.secretWaypoints.enableChestWaypoints,
										() -> config.dungeons.secretWaypoints.enableChestWaypoints,
										newValue -> config.dungeons.secretWaypoints.enableChestWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.secretWaypoints.enableItemWaypoints"))
								.binding(defaults.dungeons.secretWaypoints.enableItemWaypoints,
										() -> config.dungeons.secretWaypoints.enableItemWaypoints,
										newValue -> config.dungeons.secretWaypoints.enableItemWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.secretWaypoints.enableBatWaypoints"))
								.binding(defaults.dungeons.secretWaypoints.enableBatWaypoints,
										() -> config.dungeons.secretWaypoints.enableBatWaypoints,
										newValue -> config.dungeons.secretWaypoints.enableBatWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.secretWaypoints.enableWitherWaypoints"))
								.binding(defaults.dungeons.secretWaypoints.enableWitherWaypoints,
										() -> config.dungeons.secretWaypoints.enableWitherWaypoints,
										newValue -> config.dungeons.secretWaypoints.enableWitherWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.secretWaypoints.enableLeverWaypoints"))
								.binding(defaults.dungeons.secretWaypoints.enableLeverWaypoints,
										() -> config.dungeons.secretWaypoints.enableLeverWaypoints,
										newValue -> config.dungeons.secretWaypoints.enableLeverWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.secretWaypoints.enableFairySoulWaypoints"))
								.binding(defaults.dungeons.secretWaypoints.enableFairySoulWaypoints,
										() -> config.dungeons.secretWaypoints.enableFairySoulWaypoints,
										newValue -> config.dungeons.secretWaypoints.enableFairySoulWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.secretWaypoints.enableStonkWaypoints"))
								.binding(defaults.dungeons.secretWaypoints.enableStonkWaypoints,
										() -> config.dungeons.secretWaypoints.enableStonkWaypoints,
										newValue -> config.dungeons.secretWaypoints.enableStonkWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.secretWaypoints.enableAotvWaypoints"))
								.binding(defaults.dungeons.secretWaypoints.enableAotvWaypoints,
										() -> config.dungeons.secretWaypoints.enableAotvWaypoints,
										newValue -> config.dungeons.secretWaypoints.enableAotvWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.secretWaypoints.enablePearlWaypoints"))
								.description(Text.translatable("skyblocker.config.dungeons.secretWaypoints.enablePearlWaypoints.@Tooltip"))
								.binding(defaults.dungeons.secretWaypoints.enablePearlWaypoints,
										() -> config.dungeons.secretWaypoints.enablePearlWaypoints,
										newValue -> config.dungeons.secretWaypoints.enablePearlWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.secretWaypoints.enablePrinceWaypoints"))
								.description(Text.translatable("skyblocker.config.dungeons.secretWaypoints.enablePrinceWaypoints.@Tooltip"))
								.binding(defaults.dungeons.secretWaypoints.enablePrinceWaypoints,
										() -> config.dungeons.secretWaypoints.enablePrinceWaypoints,
										newValue -> config.dungeons.secretWaypoints.enablePrinceWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.secretWaypoints.enableDefaultWaypoints"))
								.description(Text.translatable("skyblocker.config.dungeons.secretWaypoints.enableDefaultWaypoints.@Tooltip"))
								.binding(defaults.dungeons.secretWaypoints.enableDefaultWaypoints,
										() -> config.dungeons.secretWaypoints.enableDefaultWaypoints,
										newValue -> config.dungeons.secretWaypoints.enableDefaultWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				// Mimic Message
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.mimicMessage"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.mimicMessage.sendMimicMessage"))
								.description(Text.translatable("skyblocker.config.dungeons.mimicMessage.sendMimicMessage.@Tooltip"))
								.binding(defaults.dungeons.mimicMessage.sendMimicMessage,
										() -> config.dungeons.mimicMessage.sendMimicMessage,
										newValue -> config.dungeons.mimicMessage.sendMimicMessage = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				// Prince Message
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.princeMessage"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.princeMessage.sendPrinceMessage"))
								.description(Text.translatable("skyblocker.config.dungeons.princeMessage.sendPrinceMessage.@Tooltip"))
								.binding(defaults.dungeons.princeMessage.sendPrinceMessage,
										() -> config.dungeons.princeMessage.sendPrinceMessage,
										newValue -> config.dungeons.princeMessage.sendPrinceMessage = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				// Door Highlight
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.doorHighlight"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.doorHighlight.enableDoorHighlight"))
								.description(Text.translatable("skyblocker.config.dungeons.doorHighlight.enableDoorHighlight.@Tooltip"))
								.binding(defaults.dungeons.doorHighlight.enableDoorHighlight,
										() -> config.dungeons.doorHighlight.enableDoorHighlight,
										newValue -> config.dungeons.doorHighlight.enableDoorHighlight = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<DungeonsConfig.DoorHighlight.Type>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.doorHighlight.doorHighlightType"))
								.description(Text.translatable("skyblocker.config.dungeons.doorHighlight.doorHighlightType.@Tooltip"),
										Text.translatable("skyblocker.config.dungeons.doorHighlight.doorHighlightType.secretWaypointsNote"))
								.binding(defaults.dungeons.doorHighlight.doorHighlightType,
										() -> config.dungeons.doorHighlight.doorHighlightType,
										newValue -> config.dungeons.doorHighlight.doorHighlightType = newValue)
								.controller(ConfigUtils.createEnumController())
								.build())
						.build())

				// Dungeon Score
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.dungeonScore"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonScore.enableDungeonScoreMessage", 270))
								.description(Text.translatable("skyblocker.config.dungeons.dungeonScore.enableDungeonScoreMessage.@Tooltip", 270))
								.binding(defaults.dungeons.dungeonScore.enableDungeonScore270Message,
										() -> config.dungeons.dungeonScore.enableDungeonScore270Message,
										newValue -> config.dungeons.dungeonScore.enableDungeonScore270Message = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonScore.enableDungeonScoreTitle", 270))
								.description(Text.translatable("skyblocker.config.dungeons.dungeonScore.enableDungeonScoreTitle.@Tooltip", 270))
								.binding(defaults.dungeons.dungeonScore.enableDungeonScore270Title,
										() -> config.dungeons.dungeonScore.enableDungeonScore270Title,
										newValue -> config.dungeons.dungeonScore.enableDungeonScore270Title = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonScore.enableDungeonScoreSound", 270))
								.description(Text.translatable("skyblocker.config.dungeons.dungeonScore.enableDungeonScoreSound.@Tooltip", 270))
								.binding(defaults.dungeons.dungeonScore.enableDungeonScore270Sound,
										() -> config.dungeons.dungeonScore.enableDungeonScore270Sound,
										newValue -> config.dungeons.dungeonScore.enableDungeonScore270Sound = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonScore.dungeonScoreMessage", 270))
								.description(Text.translatable("skyblocker.config.dungeons.dungeonScore.dungeonScoreMessage.@Tooltip", 270, 270))
								.binding(defaults.dungeons.dungeonScore.dungeonScore270Message,
										() -> config.dungeons.dungeonScore.dungeonScore270Message,
										newValue -> config.dungeons.dungeonScore.dungeonScore270Message = newValue)
								.controller(StringController.createBuilder().build())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonScore.enableDungeonScoreMessage", 300))
								.description(Text.translatable("skyblocker.config.dungeons.dungeonScore.enableDungeonScoreMessage.@Tooltip", 300))
								.binding(defaults.dungeons.dungeonScore.enableDungeonScore300Message,
										() -> config.dungeons.dungeonScore.enableDungeonScore300Message,
										newValue -> config.dungeons.dungeonScore.enableDungeonScore300Message = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonScore.enableDungeonScoreTitle", 300))
								.description(Text.translatable("skyblocker.config.dungeons.dungeonScore.enableDungeonScoreTitle.@Tooltip", 300))
								.binding(defaults.dungeons.dungeonScore.enableDungeonScore300Title,
										() -> config.dungeons.dungeonScore.enableDungeonScore300Title,
										newValue -> config.dungeons.dungeonScore.enableDungeonScore300Title = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonScore.enableDungeonScoreSound", 300))
								.description(Text.translatable("skyblocker.config.dungeons.dungeonScore.enableDungeonScoreSound.@Tooltip", 300))
								.binding(defaults.dungeons.dungeonScore.enableDungeonScore300Sound,
										() -> config.dungeons.dungeonScore.enableDungeonScore300Sound,
										newValue -> config.dungeons.dungeonScore.enableDungeonScore300Sound = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonScore.dungeonScoreMessage", 300))
								.description(Text.translatable("skyblocker.config.dungeons.dungeonScore.dungeonScoreMessage.@Tooltip", 300, 300))
								.binding(defaults.dungeons.dungeonScore.dungeonScore300Message,
										() -> config.dungeons.dungeonScore.dungeonScore300Message,
										newValue -> config.dungeons.dungeonScore.dungeonScore300Message = newValue)
								.controller(StringController.createBuilder().build())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonScore.enableDungeonCryptsMessage"))
								.description(Text.translatable("skyblocker.config.dungeons.dungeonScore.enableDungeonCryptsMessage.@Tooltip"))
								.binding(defaults.dungeons.dungeonScore.enableDungeonCryptsMessage,
										() -> config.dungeons.dungeonScore.enableDungeonCryptsMessage,
										newValue -> config.dungeons.dungeonScore.enableDungeonCryptsMessage = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonScore.dungeonCryptsMessageThreshold"))
								.description(Text.translatable("skyblocker.config.dungeons.dungeonScore.dungeonCryptsMessageThreshold.@Tooltip"))
								.binding(defaults.dungeons.dungeonScore.dungeonCryptsMessageThreshold,
										() -> config.dungeons.dungeonScore.dungeonCryptsMessageThreshold,
										newValue -> config.dungeons.dungeonScore.dungeonCryptsMessageThreshold = newValue)
								.controller(IntegerController.createBuilder().range(0, 330).build())
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonScore.dungeonCryptsMessage"))
								.description(Text.translatable("skyblocker.config.dungeons.dungeonScore.dungeonCryptsMessage.@Tooltip"))
								.binding(defaults.dungeons.dungeonScore.dungeonCryptsMessage,
										() -> config.dungeons.dungeonScore.dungeonCryptsMessage,
										newValue -> config.dungeons.dungeonScore.dungeonCryptsMessage = newValue)
								.controller(StringController.createBuilder().build())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonScore.enableScoreHUD"))
								.description(Text.translatable("skyblocker.config.dungeons.dungeonScore.enableScoreHUD.@Tooltip"),
										Text.translatable("skyblocker.config.dungeons.dungeonScore.enableScoreHUD.deathMessagesNote"))
								.binding(defaults.dungeons.dungeonScore.enableScoreHUD,
										() -> config.dungeons.dungeonScore.enableScoreHUD,
										newValue -> config.dungeons.dungeonScore.enableScoreHUD = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Float>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonScore.scoreScaling"))
								.binding(defaults.dungeons.dungeonScore.scoreScaling,
										() -> config.dungeons.dungeonScore.scoreScaling,
										newValue -> {
											config.dungeons.dungeonScore.scoreX = config.dungeons.dungeonScore.scoreX + (int) ((config.dungeons.dungeonScore.scoreScaling - newValue) * 38.0);
											config.dungeons.dungeonScore.scoreY = config.dungeons.dungeonScore.scoreY + (int) ((config.dungeons.dungeonScore.scoreScaling - newValue) * MinecraftClient.getInstance().textRenderer.fontHeight / 2.0);
											config.dungeons.dungeonScore.scoreScaling = newValue;
										})
								.controller(FloatController.createBuilder().build())
								.build())
						.build())

				// Dungeon Chest Profit Calculator
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.dungeons.dungeonChestProfit"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonChestProfit.enableProfitCalculator"))
								.description(Text.translatable("skyblocker.config.dungeons.dungeonChestProfit.enableProfitCalculator.@Tooltip"))
								.binding(defaults.dungeons.dungeonChestProfit.enableProfitCalculator,
										() -> config.dungeons.dungeonChestProfit.enableProfitCalculator,
										newValue -> config.dungeons.dungeonChestProfit.enableProfitCalculator = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonChestProfit.croesusProfit"))
								.description(Text.translatable("skyblocker.config.dungeons.dungeonChestProfit.croesusProfit.@Tooltip"))
								.binding(defaults.dungeons.dungeonChestProfit.croesusProfit,
										() -> config.dungeons.dungeonChestProfit.croesusProfit,
										newValue -> config.dungeons.dungeonChestProfit.croesusProfit = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonChestProfit.includeKismet"))
								.description(Text.translatable("skyblocker.config.dungeons.dungeonChestProfit.includeKismet.@Tooltip"))
								.binding(defaults.dungeons.dungeonChestProfit.includeKismet,
										() -> config.dungeons.dungeonChestProfit.includeKismet,
										newValue -> config.dungeons.dungeonChestProfit.includeKismet = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonChestProfit.includeEssence"))
								.description(Text.translatable("skyblocker.config.dungeons.dungeonChestProfit.includeEssence.@Tooltip"))
								.binding(defaults.dungeons.dungeonChestProfit.includeEssence,
										() -> config.dungeons.dungeonChestProfit.includeEssence,
										newValue -> config.dungeons.dungeonChestProfit.includeEssence = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						//FIXME maybe use color controller
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonChestProfit.neutralThreshold"))
								.description(Text.translatable("skyblocker.config.dungeons.dungeonChestProfit.neutralThreshold.@Tooltip"))
								.binding(defaults.dungeons.dungeonChestProfit.neutralThreshold,
										() -> config.dungeons.dungeonChestProfit.neutralThreshold,
										newValue -> config.dungeons.dungeonChestProfit.neutralThreshold = newValue)
								.controller(IntegerController.createBuilder().build())
								.build())
						.option(Option.<Formatting>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonChestProfit.neutralColor"))
								.binding(defaults.dungeons.dungeonChestProfit.neutralColor,
										() -> config.dungeons.dungeonChestProfit.neutralColor,
										newValue -> config.dungeons.dungeonChestProfit.neutralColor = newValue)
								.controller(ConfigUtils.createEnumDropdownController(ConfigUtils.FORMATTING_FORMATTER))
								.build())
						.option(Option.<Formatting>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonChestProfit.profitColor"))
								.binding(defaults.dungeons.dungeonChestProfit.profitColor,
										() -> config.dungeons.dungeonChestProfit.profitColor,
										newValue -> config.dungeons.dungeonChestProfit.profitColor = newValue)
								.controller(ConfigUtils.createEnumDropdownController(ConfigUtils.FORMATTING_FORMATTER))
								.build())
						.option(Option.<Formatting>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonChestProfit.lossColor"))
								.binding(defaults.dungeons.dungeonChestProfit.lossColor,
										() -> config.dungeons.dungeonChestProfit.lossColor,
										newValue -> config.dungeons.dungeonChestProfit.lossColor = newValue)
								.controller(ConfigUtils.createEnumDropdownController(ConfigUtils.FORMATTING_FORMATTER))
								.build())
						.option(Option.<Formatting>createBuilder()
								.name(Text.translatable("skyblocker.config.dungeons.dungeonChestProfit.incompleteColor"))
								.description(Text.translatable("skyblocker.config.dungeons.dungeonChestProfit.incompleteColor.@Tooltip"))
								.binding(defaults.dungeons.dungeonChestProfit.incompleteColor,
										() -> config.dungeons.dungeonChestProfit.incompleteColor,
										newValue -> config.dungeons.dungeonChestProfit.incompleteColor = newValue)
								.controller(ConfigUtils.createEnumDropdownController(ConfigUtils.FORMATTING_FORMATTER))
								.build())
						.build())

				.build();
	}
}
