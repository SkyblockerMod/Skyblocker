package me.xmrvizzy.skyblocker.config.categories;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionFlag;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import me.xmrvizzy.skyblocker.config.ConfigModel;
import me.xmrvizzy.skyblocker.config.ConfigModel.FormattingOption;
import me.xmrvizzy.skyblocker.config.ConfigUtils;
import me.xmrvizzy.skyblocker.skyblock.dungeon.DungeonMapConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class DungeonsCategory {

	public static ConfigCategory create(ConfigModel defaults, ConfigModel config) {
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
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.noInitSecretWaypoints"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.noInitSecretWaypoints.@Tooltip")))
								.binding(defaults.locations.dungeons.secretWaypoints.noInitSecretWaypoints,
										() -> config.locations.dungeons.secretWaypoints.noInitSecretWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.noInitSecretWaypoints = newValue)
								.controller(BooleanControllerBuilder::create)
								.flag(OptionFlag.GAME_RESTART)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableEntranceWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableEntranceWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableEntranceWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableEntranceWaypoints = newValue)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableSuperboomWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableSuperboomWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableSuperboomWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableSuperboomWaypoints = newValue)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableChestWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableChestWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableChestWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableChestWaypoints = newValue)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableItemWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableItemWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableItemWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableItemWaypoints = newValue)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableBatWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableBatWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableBatWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableBatWaypoints = newValue)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableWitherWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableWitherWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableWitherWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableWitherWaypoints = newValue)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableLeverWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableLeverWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableLeverWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableLeverWaypoints = newValue)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableFairySoulWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableFairySoulWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableFairySoulWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableFairySoulWaypoints = newValue)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableStonkWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableStonkWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableStonkWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableStonkWaypoints = newValue)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableDefaultWaypoints"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableDefaultWaypoints.@Tooltip")))
								.binding(defaults.locations.dungeons.secretWaypoints.enableDefaultWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableDefaultWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableDefaultWaypoints = newValue)
								.controller(BooleanControllerBuilder::create)
								.build())
						.build())
				
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.enableProfitCalculator"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.enableProfitCalculator.@Tooltip")))
								.binding(defaults.locations.dungeons.dungeonChestProfit.enableProfitCalculator,
										() -> config.locations.dungeons.dungeonChestProfit.enableProfitCalculator,
										newValue -> config.locations.dungeons.dungeonChestProfit.enableProfitCalculator = newValue)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.includeKismet"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.includeKismet.@Tooltip")))
								.binding(defaults.locations.dungeons.dungeonChestProfit.includeKismet,
										() -> config.locations.dungeons.dungeonChestProfit.includeKismet,
										newValue -> config.locations.dungeons.dungeonChestProfit.includeKismet = newValue)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.includeEssence"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.includeEssence.@Tooltip")))
								.binding(defaults.locations.dungeons.dungeonChestProfit.includeEssence,
										() -> config.locations.dungeons.dungeonChestProfit.includeEssence,
										newValue -> config.locations.dungeons.dungeonChestProfit.includeEssence = newValue)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.neutralThreshold"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.neutralThreshold.@Tooltip")))
								.binding(defaults.locations.dungeons.dungeonChestProfit.neutralThreshold,
										() -> config.locations.dungeons.dungeonChestProfit.neutralThreshold,
										newValue -> config.locations.dungeons.dungeonChestProfit.neutralThreshold = newValue)
								.controller(IntegerFieldControllerBuilder::create)
								.build())
						.option(Option.<FormattingOption>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.neutralColor"))
								.binding(defaults.locations.dungeons.dungeonChestProfit.neutralColor,
										() -> config.locations.dungeons.dungeonChestProfit.neutralColor,
										newValue -> config.locations.dungeons.dungeonChestProfit.neutralColor = newValue)
								.controller(ConfigUtils::createCyclingListController4Enum)
								.build())
						.option(Option.<FormattingOption>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.profitColor"))
								.binding(defaults.locations.dungeons.dungeonChestProfit.profitColor,
										() -> config.locations.dungeons.dungeonChestProfit.profitColor,
										newValue -> config.locations.dungeons.dungeonChestProfit.profitColor = newValue)
								.controller(ConfigUtils::createCyclingListController4Enum)
								.build())
						.option(Option.<FormattingOption>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.lossColor"))
								.binding(defaults.locations.dungeons.dungeonChestProfit.lossColor,
										() -> config.locations.dungeons.dungeonChestProfit.lossColor,
										newValue -> config.locations.dungeons.dungeonChestProfit.lossColor = newValue)
								.controller(ConfigUtils::createCyclingListController4Enum)
								.build())
						.option(Option.<FormattingOption>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.incompleteColor"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.incompleteColor.@Tooltip")))
								.binding(defaults.locations.dungeons.dungeonChestProfit.incompleteColor,
										() -> config.locations.dungeons.dungeonChestProfit.incompleteColor,
										newValue -> config.locations.dungeons.dungeonChestProfit.incompleteColor = newValue)
								.controller(ConfigUtils::createCyclingListController4Enum)
								.build())
						.build())
				
				//Others
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.croesusHelper"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.croesusHelper.@Tooltip")))
						.binding(defaults.locations.dungeons.croesusHelper,
								() -> config.locations.dungeons.croesusHelper,
								newValue -> config.locations.dungeons.croesusHelper = newValue)
						.controller(BooleanControllerBuilder::create)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.enableMap"))
						.binding(defaults.locations.dungeons.enableMap,
								() -> config.locations.dungeons.enableMap,
								newValue -> config.locations.dungeons.enableMap = newValue)
						.controller(BooleanControllerBuilder::create)
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
				.option(Option.<Integer>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.mapX"))
						.binding(defaults.locations.dungeons.mapX,
								() -> config.locations.dungeons.mapX,
								newValue -> config.locations.dungeons.mapX = newValue)
						.controller(IntegerFieldControllerBuilder::create)
						.build())
				.option(Option.<Integer>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.mapY"))
						.binding(defaults.locations.dungeons.mapY,
								() -> config.locations.dungeons.mapY,
								newValue -> config.locations.dungeons.mapY = newValue)
						.controller(IntegerFieldControllerBuilder::create)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.starredMobGlow"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.starredMobGlow.@Tooltip")))
						.binding(defaults.locations.dungeons.starredMobGlow,
								() -> config.locations.dungeons.starredMobGlow,
								newValue -> config.locations.dungeons.starredMobGlow = newValue)
						.controller(BooleanControllerBuilder::create)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.solveThreeWeirdos"))
						.binding(defaults.locations.dungeons.solveThreeWeirdos,
								() -> config.locations.dungeons.solveThreeWeirdos,
								newValue -> config.locations.dungeons.solveThreeWeirdos = newValue)
						.controller(BooleanControllerBuilder::create)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.blazesolver"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.blazesolver.@Tooltip")))
						.binding(defaults.locations.dungeons.blazesolver,
								() -> config.locations.dungeons.blazesolver,
								newValue -> config.locations.dungeons.blazesolver = newValue)
						.controller(BooleanControllerBuilder::create)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.solveTrivia"))
						.binding(defaults.locations.dungeons.solveTrivia,
								() -> config.locations.dungeons.solveTrivia,
								newValue -> config.locations.dungeons.solveTrivia = newValue)
						.controller(BooleanControllerBuilder::create)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.solveTicTacToe"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.solveTicTacToe.@Tooltip")))
						.binding(defaults.locations.dungeons.solveTicTacToe,
								() -> config.locations.dungeons.solveTicTacToe,
								newValue -> config.locations.dungeons.solveTicTacToe = newValue)
						.controller(BooleanControllerBuilder::create)
						.build())
				
				//Livid Color
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.lividColor"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.lividColor.enableLividColor"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.lividColor.enableLividColor.@Tooltip")))
								.binding(defaults.locations.dungeons.lividColor.enableLividColor,
										() -> config.locations.dungeons.lividColor.enableLividColor,
										newValue -> config.locations.dungeons.lividColor.enableLividColor = newValue)
								.controller(BooleanControllerBuilder::create)
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
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.terminals.solveOrder"))
								.binding(defaults.locations.dungeons.terminals.solveOrder,
										() -> config.locations.dungeons.terminals.solveOrder,
										newValue -> config.locations.dungeons.terminals.solveOrder = newValue)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.terminals.solveStartsWith"))
								.binding(defaults.locations.dungeons.terminals.solveStartsWith,
										() -> config.locations.dungeons.terminals.solveStartsWith,
										newValue -> config.locations.dungeons.terminals.solveStartsWith = newValue)
								.controller(BooleanControllerBuilder::create)
								.build())
						.build())
				.build();
	}
}
