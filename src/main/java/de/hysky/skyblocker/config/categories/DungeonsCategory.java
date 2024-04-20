package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.dungeon.DungeonMapConfigScreen;
import de.hysky.skyblocker.utils.waypoint.Waypoint.Type;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
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
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableRoomMatching"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableRoomMatching.@Tooltip")))
								.binding(defaults.locations.dungeons.secretWaypoints.enableRoomMatching,
										() -> config.locations.dungeons.secretWaypoints.enableRoomMatching,
										newValue -> config.locations.dungeons.secretWaypoints.enableRoomMatching = newValue)
								.controller(ConfigUtils::createBooleanController)
								.flag(OptionFlag.GAME_RESTART)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.secretWaypoints.enableSecretWaypoints"))
								.binding(defaults.locations.dungeons.secretWaypoints.enableSecretWaypoints,
										() -> config.locations.dungeons.secretWaypoints.enableSecretWaypoints,
										newValue -> config.locations.dungeons.secretWaypoints.enableSecretWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
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

				//Dungeon Score
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.enableDungeonScoreMessage", 270))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.enableDungeonScoreMessage.@Tooltip", 270)))
								.binding(defaults.locations.dungeons.dungeonScore.enableDungeonScore270Message,
										() -> config.locations.dungeons.dungeonScore.enableDungeonScore270Message,
										newValue -> config.locations.dungeons.dungeonScore.enableDungeonScore270Message = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.enableDungeonScoreTitle", 270))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.enableDungeonScoreTitle.@Tooltip", 270)))
								.binding(defaults.locations.dungeons.dungeonScore.enableDungeonScore270Title,
										() -> config.locations.dungeons.dungeonScore.enableDungeonScore270Title,
										newValue -> config.locations.dungeons.dungeonScore.enableDungeonScore270Title = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.enableDungeonScoreSound", 270))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.enableDungeonScoreSound.@Tooltip", 270)))
								.binding(defaults.locations.dungeons.dungeonScore.enableDungeonScore270Sound,
										() -> config.locations.dungeons.dungeonScore.enableDungeonScore270Sound,
										newValue -> config.locations.dungeons.dungeonScore.enableDungeonScore270Sound = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.dungeonScoreMessage", 270))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.dungeonScoreMessage.@Tooltip", 270, 270)))
								.binding(defaults.locations.dungeons.dungeonScore.dungeonScore270Message,
										() -> config.locations.dungeons.dungeonScore.dungeonScore270Message,
										newValue -> config.locations.dungeons.dungeonScore.dungeonScore270Message = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.enableDungeonScoreMessage", 300))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.enableDungeonScoreMessage.@Tooltip", 300)))
								.binding(defaults.locations.dungeons.dungeonScore.enableDungeonScore300Message,
										() -> config.locations.dungeons.dungeonScore.enableDungeonScore300Message,
										newValue -> config.locations.dungeons.dungeonScore.enableDungeonScore300Message = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.enableDungeonScoreTitle", 300))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.enableDungeonScoreTitle.@Tooltip", 300)))
								.binding(defaults.locations.dungeons.dungeonScore.enableDungeonScore300Title,
										() -> config.locations.dungeons.dungeonScore.enableDungeonScore300Title,
										newValue -> config.locations.dungeons.dungeonScore.enableDungeonScore300Title = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.enableDungeonScoreSound", 300))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.enableDungeonScoreSound.@Tooltip", 300)))
								.binding(defaults.locations.dungeons.dungeonScore.enableDungeonScore300Sound,
										() -> config.locations.dungeons.dungeonScore.enableDungeonScore300Sound,
										newValue -> config.locations.dungeons.dungeonScore.enableDungeonScore300Sound = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.dungeonScoreMessage", 300))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.dungeonScoreMessage.@Tooltip", 300, 300)))
								.binding(defaults.locations.dungeons.dungeonScore.dungeonScore300Message,
										() -> config.locations.dungeons.dungeonScore.dungeonScore300Message,
										newValue -> config.locations.dungeons.dungeonScore.dungeonScore300Message = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.enableDungeonCryptsMessage"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.enableDungeonCryptsMessage.@Tooltip")))
								.binding(defaults.locations.dungeons.dungeonScore.enableDungeonCryptsMessage,
										() -> config.locations.dungeons.dungeonScore.enableDungeonCryptsMessage,
										newValue -> config.locations.dungeons.dungeonScore.enableDungeonCryptsMessage = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.dungeonCryptsMessageThreshold"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.dungeonCryptsMessageThreshold.@Tooltip")))
								.binding(defaults.locations.dungeons.dungeonScore.dungeonCryptsMessageThreshold,
										() -> config.locations.dungeons.dungeonScore.dungeonCryptsMessageThreshold,
										newValue -> config.locations.dungeons.dungeonScore.dungeonCryptsMessageThreshold = newValue)
								.controller(IntegerFieldControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.dungeonCryptsMessage"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.dungeonCryptsMessage.@Tooltip")))
								.binding(defaults.locations.dungeons.dungeonScore.dungeonCryptsMessage,
										() -> config.locations.dungeons.dungeonScore.dungeonCryptsMessage,
										newValue -> config.locations.dungeons.dungeonScore.dungeonCryptsMessage = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.enableScoreHUD"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.enableScoreHUD.@Tooltip"), Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.enableScoreHUD.deathMessagesNote")))
								.binding(defaults.locations.dungeons.dungeonScore.enableScoreHUD,
										() -> config.locations.dungeons.dungeonScore.enableScoreHUD,
										newValue -> config.locations.dungeons.dungeonScore.enableScoreHUD = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Float>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonScore.scoreScaling"))
								.binding(defaults.locations.dungeons.dungeonScore.scoreScaling,
										() -> config.locations.dungeons.dungeonScore.scoreScaling,
										newValue -> {
											config.locations.dungeons.dungeonScore.scoreX = config.locations.dungeons.dungeonScore.scoreX + (int) ((config.locations.dungeons.dungeonScore.scoreScaling - newValue) * 38.0);
											config.locations.dungeons.dungeonScore.scoreY = config.locations.dungeons.dungeonScore.scoreY + (int) ((config.locations.dungeons.dungeonScore.scoreScaling - newValue) * MinecraftClient.getInstance().textRenderer.fontHeight / 2.0);
											config.locations.dungeons.dungeonScore.scoreScaling = newValue;
										})
								.controller(FloatFieldControllerBuilder::create)
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
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.croesusProfit"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.croesusProfit.@Tooltip")))
								.binding(defaults.locations.dungeons.dungeonChestProfit.croesusProfit,
										() -> config.locations.dungeons.dungeonChestProfit.croesusProfit,
										newValue -> config.locations.dungeons.dungeonChestProfit.croesusProfit = newValue)
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
								.controller(ConfigUtils.getEnumDropdownControllerFactory(ConfigUtils.FORMATTING_FORMATTER))
								.build())
						.option(Option.<Formatting>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.profitColor"))
								.binding(defaults.locations.dungeons.dungeonChestProfit.profitColor,
										() -> config.locations.dungeons.dungeonChestProfit.profitColor,
										newValue -> config.locations.dungeons.dungeonChestProfit.profitColor = newValue)
								.controller(ConfigUtils.getEnumDropdownControllerFactory(ConfigUtils.FORMATTING_FORMATTER))
								.build())
						.option(Option.<Formatting>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.dungeonChestProfit.lossColor"))
								.binding(defaults.locations.dungeons.dungeonChestProfit.lossColor,
										() -> config.locations.dungeons.dungeonChestProfit.lossColor,
										newValue -> config.locations.dungeons.dungeonChestProfit.lossColor = newValue)
								.controller(ConfigUtils.getEnumDropdownControllerFactory(ConfigUtils.FORMATTING_FORMATTER))
								.build())
						.option(Option.<Formatting>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.chestValue.incompleteColor"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.chestValue.incompleteColor.@Tooltip")))
								.binding(defaults.locations.dungeons.dungeonChestProfit.incompleteColor,
										() -> config.locations.dungeons.dungeonChestProfit.incompleteColor,
										newValue -> config.locations.dungeons.dungeonChestProfit.incompleteColor = newValue)
								.controller(ConfigUtils.getEnumDropdownControllerFactory(ConfigUtils.FORMATTING_FORMATTER))
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
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.starredMobBoundingBoxes"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.starredMobBoundingBoxes.@Tooltip")))
						.binding(defaults.locations.dungeons.starredMobBoundingBoxes,
								() -> config.locations.dungeons.starredMobBoundingBoxes,
								newValue -> config.locations.dungeons.starredMobBoundingBoxes = newValue)
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
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.solveWaterboard"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.solveWaterboard.@Tooltip")))
						.binding(defaults.locations.dungeons.solveWaterboard,
								() -> config.locations.dungeons.solveWaterboard,
								newValue -> config.locations.dungeons.solveWaterboard = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.solveBoulder"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.solveBoulder.@Tooltip")))
						.binding(defaults.locations.dungeons.solveBoulder,
								() -> config.locations.dungeons.solveBoulder,
								newValue -> config.locations.dungeons.solveBoulder = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.solveIceFill"))
						.binding(defaults.locations.dungeons.solveIceFill,
								() -> config.locations.dungeons.solveIceFill,
								newValue -> config.locations.dungeons.solveIceFill = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.solveSilverfish"))
						.binding(defaults.locations.dungeons.solveSilverfish,
								() -> config.locations.dungeons.solveSilverfish,
								newValue -> config.locations.dungeons.solveSilverfish = newValue)
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
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.floor3GuardianHealthDisplay"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.floor3GuardianHealthDisplay.@Tooltip")))
						.binding(defaults.locations.dungeons.floor3GuardianHealthDisplay,
								() -> config.locations.dungeons.floor3GuardianHealthDisplay,
								newValue -> config.locations.dungeons.floor3GuardianHealthDisplay = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.allowDroppingProtectedItems"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.allowDroppingProtectedItems.@Tooltip")))
						.binding(defaults.locations.dungeons.allowDroppingProtectedItems,
								() -> config.locations.dungeons.allowDroppingProtectedItems,
								newValue -> config.locations.dungeons.allowDroppingProtectedItems = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())

				//Mimic Message
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.mimicMessage"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.mimicMessage.sendMimicMessage"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.mimicMessage.sendMimicMessage.@Tooltip")))
								.binding(defaults.locations.dungeons.mimicMessage.sendMimicMessage,
										() -> config.locations.dungeons.mimicMessage.sendMimicMessage,
										newValue -> config.locations.dungeons.mimicMessage.sendMimicMessage = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.mimicMessage.mimicMessage"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.mimicMessage.mimicMessage.@Tooltip")))
								.binding(defaults.locations.dungeons.mimicMessage.mimicMessage,
										() -> config.locations.dungeons.mimicMessage.mimicMessage,
										newValue -> config.locations.dungeons.mimicMessage.mimicMessage = newValue)
								.controller(StringControllerBuilder::create)
								.build())
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
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.lividColor.enableLividColorTitle"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dungeons.lividColor.enableLividColorTitle.@Tooltip")))
								.binding(defaults.locations.dungeons.lividColor.enableLividColorTitle,
										() -> config.locations.dungeons.lividColor.enableLividColorTitle,
										newValue -> config.locations.dungeons.lividColor.enableLividColorTitle = newValue)
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
