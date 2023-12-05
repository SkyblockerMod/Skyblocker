package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.utils.waypoint.Waypoint.Type;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionFlag;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import de.hysky.skyblocker.config.controllers.EnumDropdownControllerBuilder;
import de.hysky.skyblocker.skyblock.dungeon.DungeonMapConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DungeonsCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons"))

				//Dungeon Secret Waypoints
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableSecretWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableSecretWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableSecretWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableSecretWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.noInitSecretWaypoints"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.noInitSecretWaypoints.@Tooltip")))
								.binding(defaults.locations.dungeons.secretWaypoints.noInitSecretWaypoints,
										() -> config.locations.dungeons.secretWaypoints.noInitSecretWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.noInitSecretWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.flag(OptionFlag.GAME_RESTART)
								.build())
						.option(Option.<Type>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.waypoints.waypointType"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.waypoints.waypointType.@Tooltip")))
								.binding(defaults.locations.dungeons.secretWaypoints.waypointType,
										() -> config.locations.dungeons.secretWaypoints.waypointType,
										newValue -> config.locations.dungeons.secretWaypoints.waypointType = newValue)
								.controller(ConfigUtils::createEnumCyclingListController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.showSecretText"))
								.binding(defaults.locations.dungeons.secretWaypoints.showSecretText,
										() -> config.locations.dungeons.secretWaypoints.showSecretText,
										newValue -> config.locations.dungeons.secretWaypoints.showSecretText = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableEntranceWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableEntranceWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableEntranceWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableEntranceWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableSuperboomWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableSuperboomWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableSuperboomWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableSuperboomWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableChestWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableChestWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableChestWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableChestWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableItemWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableItemWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableItemWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableItemWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableBatWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableBatWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableBatWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableBatWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableWitherWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableWitherWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableWitherWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableWitherWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableLeverWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableLeverWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableLeverWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableLeverWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableFairySoulWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableFairySoulWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableFairySoulWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableFairySoulWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableStonkWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableStonkWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableStonkWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableStonkWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableAotvWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableAotvWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableAotvWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableAotvWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enablePearlWaypoints"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enablePearlWaypoints.@Tooltip")))
								.binding(defaults.locations.dungeons.secretWaypoints.enablePearlWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enablePearlWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enablePearlWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableDefaultWaypoints"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableDefaultWaypoints.@Tooltip")))
								.binding(defaults.locations.dungeons.secretWaypoints.enableDefaultWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableDefaultWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableDefaultWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Dungeon Door Highlight
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.doorHighlight"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.doorHighlight.enableDoorHighlight"))
								.binding(defaults.locations.dungeons.doorHighlight.enableDoorHighlight,
										() -> config.locations.dungeons.doorHighlight.enableDoorHighlight,
										newValue -> config.locations.dungeons.doorHighlight.enableDoorHighlight = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<SkyblockerConfig.DoorHighlight.Type>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.doorHighlight.doorHighlightType"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.doorHighlight.doorHighlightType.@Tooltip"), Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.doorHighlight.doorHighlightType.secretWaypointsNote")))
								.binding(defaults.locations.dungeons.doorHighlight.doorHighlightType,
										() -> config.locations.dungeons.doorHighlight.doorHighlightType,
										newValue -> config.locations.dungeons.doorHighlight.doorHighlightType = newValue)
								.controller(ConfigUtils::createEnumCyclingListController)
								.build())
						.build())

				//Dungeon Chest Profit
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.enableProfitCalculator"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.enableProfitCalculator.@Tooltip")))
								.binding(defaults.locations.dungeons.dungeonChestProfit.enableProfitCalculator,
										() -> config.locations.dungeons.dungeonChestProfit.enableProfitCalculator,
										newValue -> config.locations.dungeons.dungeonChestProfit.enableProfitCalculator = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.includeKismet"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.includeKismet.@Tooltip")))
								.binding(defaults.locations.dungeons.dungeonChestProfit.includeKismet,
										() -> config.locations.dungeons.dungeonChestProfit.includeKismet,
										newValue -> config.locations.dungeons.dungeonChestProfit.includeKismet = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.includeEssence"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.includeEssence.@Tooltip")))
								.binding(defaults.locations.dungeons.dungeonChestProfit.includeEssence,
										() -> config.locations.dungeons.dungeonChestProfit.includeEssence,
										newValue -> config.locations.dungeons.dungeonChestProfit.includeEssence = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.neutralThreshold"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.neutralThreshold.@Tooltip")))
								.binding(defaults.locations.dungeons.dungeonChestProfit.neutralThreshold,
										() -> config.locations.dungeons.dungeonChestProfit.neutralThreshold,
										newValue -> config.locations.dungeons.dungeonChestProfit.neutralThreshold = newValue)
								.controller(IntegerFieldControllerBuilder::create)
								.build())
						.option(Option.<Formatting>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.neutralColor"))
								.binding(defaults.locations.dungeons.dungeonChestProfit.neutralColor,
										() -> config.locations.dungeons.dungeonChestProfit.neutralColor,
										newValue -> config.locations.dungeons.dungeonChestProfit.neutralColor = newValue)
								.controller(EnumDropdownControllerBuilder.getFactory(ConfigUtils.FORMATTING_TO_STRING))
								.build())
						.option(Option.<Formatting>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.profitColor"))
								.binding(defaults.locations.dungeons.dungeonChestProfit.profitColor,
										() -> config.locations.dungeons.dungeonChestProfit.profitColor,
										newValue -> config.locations.dungeons.dungeonChestProfit.profitColor = newValue)
								.controller(EnumDropdownControllerBuilder.getFactory(ConfigUtils.FORMATTING_TO_STRING))
								.build())
						.option(Option.<Formatting>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.lossColor"))
								.binding(defaults.locations.dungeons.dungeonChestProfit.lossColor,
										() -> config.locations.dungeons.dungeonChestProfit.lossColor,
										newValue -> config.locations.dungeons.dungeonChestProfit.lossColor = newValue)
								.controller(EnumDropdownControllerBuilder.getFactory(ConfigUtils.FORMATTING_TO_STRING))
								.build())
						.option(Option.<Formatting>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.chestValue.incompleteColor"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.chestValue.incompleteColor.@Tooltip")))
								.binding(defaults.locations.dungeons.dungeonChestProfit.incompleteColor,
										() -> config.locations.dungeons.dungeonChestProfit.incompleteColor,
										newValue -> config.locations.dungeons.dungeonChestProfit.incompleteColor = newValue)
								.controller(EnumDropdownControllerBuilder.getFactory(ConfigUtils.FORMATTING_TO_STRING))
								.build())
						.build())

				//Others
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.croesusHelper"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.croesusHelper.@Tooltip")))
						.binding(defaults.locations.dungeons.croesusHelper,
								() -> config.locations.dungeons.croesusHelper,
								newValue -> config.locations.dungeons.croesusHelper = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.enableMap"))
						.binding(defaults.locations.dungeons.enableMap,
								() -> config.locations.dungeons.enableMap,
								newValue -> config.locations.dungeons.enableMap = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(ButtonOption.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.mapScreen"))
						.text(Text.translatable("text.skyblocker.open"))
						.action((screen, opt) -> MinecraftClient.getInstance().setScreen(new DungeonMapConfigScreen(screen)))
						.build())
				.option(Option.<Float>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.mapScaling"))
						.binding(defaults.locations.dungeons.mapScaling,
								() -> config.locations.dungeons.mapScaling,
								newValue -> config.locations.dungeons.mapScaling = newValue)
						.controller(FloatFieldControllerBuilder::create)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.playerSecretsTracker"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.playerSecretsTracker.@Tooltip")))
						.binding(defaults.locations.dungeons.playerSecretsTracker,
								() -> config.locations.dungeons.playerSecretsTracker,
								newValue -> config.locations.dungeons.playerSecretsTracker = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.starredMobGlow"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.starredMobGlow.@Tooltip")))
						.binding(defaults.locations.dungeons.starredMobGlow,
								() -> config.locations.dungeons.starredMobGlow,
								newValue -> config.locations.dungeons.starredMobGlow = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.solveThreeWeirdos"))
						.binding(defaults.locations.dungeons.solveThreeWeirdos,
								() -> config.locations.dungeons.solveThreeWeirdos,
								newValue -> config.locations.dungeons.solveThreeWeirdos = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.blazeSolver"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.blazeSolver.@Tooltip")))
						.binding(defaults.locations.dungeons.blazeSolver,
								() -> config.locations.dungeons.blazeSolver,
								newValue -> config.locations.dungeons.blazeSolver = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.creeperSolver"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.creeperSolver.@Tooltip")))
						.binding(defaults.locations.dungeons.creeperSolver,
								() -> config.locations.dungeons.creeperSolver,
								newValue -> config.locations.dungeons.creeperSolver = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.solveTrivia"))
						.binding(defaults.locations.dungeons.solveTrivia,
								() -> config.locations.dungeons.solveTrivia,
								newValue -> config.locations.dungeons.solveTrivia = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.solveTicTacToe"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.solveTicTacToe.@Tooltip")))
						.binding(defaults.locations.dungeons.solveTicTacToe,
								() -> config.locations.dungeons.solveTicTacToe,
								newValue -> config.locations.dungeons.solveTicTacToe = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.fireFreezeStaffTimer"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.fireFreezeStaffTimer.@Tooltip")))
						.binding(defaults.locations.dungeons.fireFreezeStaffTimer,
								() -> config.locations.dungeons.fireFreezeStaffTimer,
								newValue -> config.locations.dungeons.fireFreezeStaffTimer = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())

				//Livid Color
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.lividColor"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.lividColor.enableLividColorGlow"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.lividColor.enableLividColorGlow.@Tooltip")))
								.binding(defaults.locations.dungeons.lividColor.enableLividColorGlow,
										() -> config.locations.dungeons.lividColor.enableLividColorGlow,
										newValue -> config.locations.dungeons.lividColor.enableLividColorGlow = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.lividColor.enableLividColorText"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.lividColor.enableLividColorText.@Tooltip")))
								.binding(defaults.locations.dungeons.lividColor.enableLividColorText,
										() -> config.locations.dungeons.lividColor.enableLividColorText,
										newValue -> config.locations.dungeons.lividColor.enableLividColorText = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.lividColor.lividColorText"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.lividColor.lividColorText.@Tooltip")))
								.binding(defaults.locations.dungeons.lividColor.lividColorText,
										() -> config.locations.dungeons.lividColor.lividColorText,
										newValue -> config.locations.dungeons.lividColor.lividColorText = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.build())

				//Terminal Solvers
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.terminals"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.terminals.solveColor"))
								.binding(defaults.locations.dungeons.terminals.solveColor,
										() -> config.locations.dungeons.terminals.solveColor,
										newValue -> config.locations.dungeons.terminals.solveColor = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.terminals.solveOrder"))
								.binding(defaults.locations.dungeons.terminals.solveOrder,
										() -> config.locations.dungeons.terminals.solveOrder,
										newValue -> config.locations.dungeons.terminals.solveOrder = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.terminals.solveStartsWith"))
								.binding(defaults.locations.dungeons.terminals.solveStartsWith,
										() -> config.locations.dungeons.terminals.solveStartsWith,
										newValue -> config.locations.dungeons.terminals.solveStartsWith = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())
				.build();
	}
}
