package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.configs.SlayersConfig;
import de.hysky.skyblocker.skyblock.slayers.hud.SlayerHudWidget;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.utils.Location;
import net.azureaaron.dandelion.api.ButtonOption;
import net.azureaaron.dandelion.api.ConfigCategory;
import net.azureaaron.dandelion.api.Option;
import net.azureaaron.dandelion.api.OptionGroup;
import net.azureaaron.dandelion.api.controllers.FloatController;
import net.azureaaron.dandelion.api.controllers.IntegerController;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class SlayersCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.id(SkyblockerMod.id("config/slayers"))
				.name(Component.translatable("skyblocker.config.slayer"))

				//General Slayers Options
				.option(Option.<SlayersConfig.HighlightSlayerEntities>createBuilder()
						.name(Component.translatable("skyblocker.config.slayer.highlightMinis"))
						.description(Component.translatable("skyblocker.config.slayer.highlightMinis.@Tooltip[0]"),
								Component.translatable("skyblocker.config.slayer.highlightMinis.@Tooltip[1]"),
								Component.translatable("skyblocker.config.slayer.highlightMinis.@Tooltip[2]"))
						.binding(defaults.slayers.highlightMinis,
								() -> config.slayers.highlightMinis,
								newValue -> config.slayers.highlightMinis = newValue)
						.controller(ConfigUtils.createEnumController())
						.build())
				.option(Option.<SlayersConfig.HighlightSlayerEntities>createBuilder()
						.name(Component.translatable("skyblocker.config.slayer.highlightBosses"))
						.description(Component.translatable("skyblocker.config.slayer.highlightBosses.@Tooltip[0]"),
								Component.translatable("skyblocker.config.slayer.highlightBosses.@Tooltip[1]"),
								Component.translatable("skyblocker.config.slayer.highlightBosses.@Tooltip[2]"),
								Component.translatable("skyblocker.config.slayer.highlightBosses.@Tooltip[3]"))
						.binding(defaults.slayers.highlightBosses,
								() -> config.slayers.highlightBosses,
								newValue -> config.slayers.highlightBosses = newValue)
						.controller(ConfigUtils.createEnumController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("skyblocker.config.slayer.bossbar"))
						.description(Component.translatable("skyblocker.config.slayer.bossbar.@Tooltip"))
						.binding(defaults.slayers.displayBossbar,
								() -> config.slayers.displayBossbar,
								newValue -> config.slayers.displayBossbar = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("skyblocker.config.slayer.bossSpawnAlert"))
						.description(Component.translatable("skyblocker.config.slayer.bossSpawnAlert.@Tooltip"))
						.binding(defaults.slayers.bossSpawnAlert,
								() -> config.slayers.bossSpawnAlert,
								newValue -> config.slayers.bossSpawnAlert = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("skyblocker.config.slayer.minibossSpawnAlert"))
						.description(Component.translatable("skyblocker.config.slayer.minibossSpawnAlert.@Tooltip"))
						.binding(defaults.slayers.miniBossSpawnAlert,
								() -> config.slayers.miniBossSpawnAlert,
								newValue -> config.slayers.miniBossSpawnAlert = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("skyblocker.config.slayer.slainTime"))
						.description(Component.translatable("skyblocker.config.slayer.slainTime.@Tooltip"))
						.binding(defaults.slayers.slainTime,
								() -> config.slayers.slainTime,
								newValue -> config.slayers.slainTime = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("skyblocker.config.slayer.enableHud"))
						.description(Component.translatable("skyblocker.config.slayer.enableHud.@Tooltip"))
						.binding(defaults.slayers.enableHud,
								() -> config.slayers.enableHud,
								newValue -> config.slayers.enableHud = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(ButtonOption.createBuilder()
						.name(Component.translatable("skyblocker.config.slayer.slayerHud"))
						.prompt(Component.translatable("text.skyblocker.open"))
						.action(screen -> Minecraft.getInstance().setScreen(new WidgetsConfigurationScreen(Location.HUB, SlayerHudWidget.getInstance().getInternalID(), screen)))
						.build())

				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.slayer.callMaddox"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.slayer.callMaddox.sendMessageOnFail"))
								.description(Component.translatable("skyblocker.config.slayer.callMaddox.sendMessageOnFail.@Tooltip"))
								.binding(defaults.slayers.callMaddox.sendMessageOnFail,
										() -> config.slayers.callMaddox.sendMessageOnFail,
										newValue -> config.slayers.callMaddox.sendMessageOnFail = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.slayer.callMaddox.sendMessageOnKill"))
								.description(Component.translatable("skyblocker.config.slayer.callMaddox.sendMessageOnFail.@Tooltip")) // Same tooltip as above
								.binding(defaults.slayers.callMaddox.sendMessageOnKill,
										() -> config.slayers.callMaddox.sendMessageOnKill,
										newValue -> config.slayers.callMaddox.sendMessageOnKill = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				//Enderman Slayer
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.slayer.endermanSlayer"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.slayer.endermanSlayer.enableYangGlyphsNotification"))
								.binding(defaults.slayers.endermanSlayer.enableYangGlyphsNotification,
										() -> config.slayers.endermanSlayer.enableYangGlyphsNotification,
										newValue -> config.slayers.endermanSlayer.enableYangGlyphsNotification = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.slayer.endermanSlayer.highlightBeacons"))
								.binding(defaults.slayers.endermanSlayer.highlightBeacons,
										() -> config.slayers.endermanSlayer.highlightBeacons,
										newValue -> config.slayers.endermanSlayer.highlightBeacons = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.slayer.endermanSlayer.highlightNukekubiHeads"))
								.binding(defaults.slayers.endermanSlayer.highlightNukekubiHeads,
										() -> config.slayers.endermanSlayer.highlightNukekubiHeads,
										newValue -> config.slayers.endermanSlayer.highlightNukekubiHeads = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.slayer.endermanSlayer.lazerTimer"))
								.binding(defaults.slayers.endermanSlayer.lazerTimer,
										() -> config.slayers.endermanSlayer.lazerTimer,
										newValue -> config.slayers.endermanSlayer.lazerTimer = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				//Vampire Slayer
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.slayer.vampireSlayer"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.slayer.vampireSlayer.enableEffigyWaypoints"))
								.binding(defaults.slayers.vampireSlayer.enableEffigyWaypoints,
										() -> config.slayers.vampireSlayer.enableEffigyWaypoints,
										newValue -> config.slayers.vampireSlayer.enableEffigyWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.slayer.vampireSlayer.compactEffigyWaypoints"))
								.binding(defaults.slayers.vampireSlayer.compactEffigyWaypoints,
										() -> config.slayers.vampireSlayer.compactEffigyWaypoints,
										newValue -> config.slayers.vampireSlayer.compactEffigyWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.translatable("skyblocker.config.slayer.vampireSlayer.effigyUpdateFrequency"))
								.description(Component.translatable("skyblocker.config.slayer.vampireSlayer.effigyUpdateFrequency.@Tooltip"))
								.binding(defaults.slayers.vampireSlayer.effigyUpdateFrequency,
										() -> config.slayers.vampireSlayer.effigyUpdateFrequency,
										newValue -> config.slayers.vampireSlayer.effigyUpdateFrequency = newValue)
								.controller(IntegerController.createBuilder().range(1, 10).slider(1).build())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.slayer.vampireSlayer.enableHolyIceIndicator"))
								.binding(defaults.slayers.vampireSlayer.enableHolyIceIndicator,
										() -> config.slayers.vampireSlayer.enableHolyIceIndicator,
										newValue -> config.slayers.vampireSlayer.enableHolyIceIndicator = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.translatable("skyblocker.config.slayer.vampireSlayer.holyIceIndicatorTickDelay"))
								.binding(defaults.slayers.vampireSlayer.holyIceIndicatorTickDelay,
										() -> config.slayers.vampireSlayer.holyIceIndicatorTickDelay,
										newValue -> config.slayers.vampireSlayer.holyIceIndicatorTickDelay = newValue)
								.controller(IntegerController.createBuilder().build())
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.translatable("skyblocker.config.slayer.vampireSlayer.holyIceUpdateFrequency"))
								.description(Component.translatable("skyblocker.config.slayer.vampireSlayer.holyIceUpdateFrequency.@Tooltip"))
								.binding(defaults.slayers.vampireSlayer.holyIceUpdateFrequency,
										() -> config.slayers.vampireSlayer.holyIceUpdateFrequency,
										newValue -> config.slayers.vampireSlayer.holyIceUpdateFrequency = newValue)
								.controller(IntegerController.createBuilder().range(1, 10).slider(1).build())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.slayer.vampireSlayer.enableHealingMelonIndicator"))
								.binding(defaults.slayers.vampireSlayer.enableHealingMelonIndicator,
										() -> config.slayers.vampireSlayer.enableHealingMelonIndicator,
										newValue -> config.slayers.vampireSlayer.enableHealingMelonIndicator = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Float>createBuilder()
								.name(Component.translatable("skyblocker.config.slayer.vampireSlayer.healingMelonHealthThreshold"))
								.binding(defaults.slayers.vampireSlayer.healingMelonHealthThreshold,
										() -> config.slayers.vampireSlayer.healingMelonHealthThreshold,
										newValue -> config.slayers.vampireSlayer.healingMelonHealthThreshold = newValue)
								.controller(FloatController.createBuilder().build())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.slayer.vampireSlayer.enableSteakStakeIndicator"))
								.binding(defaults.slayers.vampireSlayer.enableSteakStakeIndicator,
										() -> config.slayers.vampireSlayer.enableSteakStakeIndicator,
										newValue -> config.slayers.vampireSlayer.enableSteakStakeIndicator = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.translatable("skyblocker.config.slayer.vampireSlayer.steakStakeUpdateFrequency"))
								.description(Component.translatable("skyblocker.config.slayer.vampireSlayer.steakStakeUpdateFrequency.@Tooltip"))
								.binding(defaults.slayers.vampireSlayer.steakStakeUpdateFrequency,
										() -> config.slayers.vampireSlayer.steakStakeUpdateFrequency,
										newValue -> config.slayers.vampireSlayer.steakStakeUpdateFrequency = newValue)
								.controller(IntegerController.createBuilder().range(1, 10).slider(1).build())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.slayer.vampireSlayer.enableManiaIndicator"))
								.binding(defaults.slayers.vampireSlayer.enableManiaIndicator,
										() -> config.slayers.vampireSlayer.enableManiaIndicator,
										newValue -> config.slayers.vampireSlayer.enableManiaIndicator = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.translatable("skyblocker.config.slayer.vampireSlayer.maniaUpdateFrequency"))
								.description(Component.translatable("skyblocker.config.slayer.vampireSlayer.maniaUpdateFrequency.@Tooltip"))
								.binding(defaults.slayers.vampireSlayer.maniaUpdateFrequency,
										() -> config.slayers.vampireSlayer.maniaUpdateFrequency,
										newValue -> config.slayers.vampireSlayer.maniaUpdateFrequency = newValue)
								.controller(IntegerController.createBuilder().range(1, 10).slider(1).build())
								.build())
						.build())

				//Blaze Slayer
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.slayer.blazeSlayer"))
						.collapsed(true)
						.option(Option.<SlayersConfig.BlazeSlayer.FirePillar>createBuilder()
								.name(Component.translatable("skyblocker.config.slayer.blazeSlayer.enableFirePillarAnnouncer"))
								.description(Component.translatable("skyblocker.config.slayer.blazeSlayer.enableFirePillarAnnouncer.@Tooltip"))
								.binding(defaults.slayers.blazeSlayer.firePillarCountdown,
										() -> config.slayers.blazeSlayer.firePillarCountdown,
										newValue -> config.slayers.blazeSlayer.firePillarCountdown = newValue)
								.controller(ConfigUtils.createEnumController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.slayer.blazeSlayer.attunementHighlights"))
								.description(Component.translatable("skyblocker.config.slayer.blazeSlayer.attunementHighlights.@Tooltip"))
								.binding(defaults.slayers.blazeSlayer.attunementHighlights,
										() -> config.slayers.blazeSlayer.attunementHighlights,
										newValue -> config.slayers.blazeSlayer.attunementHighlights = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				.build();
	}
}
