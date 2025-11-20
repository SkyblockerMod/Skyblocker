package de.hysky.skyblocker.config.categories;

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
import net.azureaaron.dandelion.systems.ButtonOption;
import net.azureaaron.dandelion.systems.ConfigCategory;
import net.azureaaron.dandelion.systems.Option;
import net.azureaaron.dandelion.systems.OptionGroup;
import net.azureaaron.dandelion.systems.controllers.FloatController;
import net.azureaaron.dandelion.systems.controllers.IntegerController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.awt.*;

public class MiningCategory {

    public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
        return ConfigCategory.createBuilder()
        		.id(SkyblockerMod.id("config/mining"))
                .name(Text.translatable("skyblocker.config.mining"))

                //Uncategorized Options
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.mining.enableDrillFuel"))
						.description(Text.translatable("skyblocker.config.mining.enableDrillFuel.@Tooltip"))
                        .binding(defaults.mining.enableDrillFuel,
                                () -> config.mining.enableDrillFuel,
                                newValue -> config.mining.enableDrillFuel = newValue)
                        .controller(ConfigUtils.createBooleanController())
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.mining.commissionHighlight"))
                        .binding(defaults.mining.commissionHighlight,
                                () -> config.mining.commissionHighlight,
                                newValue -> config.mining.commissionHighlight = newValue)
                        .controller(ConfigUtils.createBooleanController())
                        .build())

				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.mining.callMismyla"))
						.description(Text.translatable("skyblocker.config.mining.callMismyla.@Tooltip"))
						.binding(defaults.mining.callMismyla,
								() -> config.mining.callMismyla,
								newValue -> config.mining.callMismyla = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())

				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.mining.redialOnBadSignal"))
						.description(Text.translatable("skyblocker.config.mining.redialOnBadSignal.@Tooltip"))
						.binding(defaults.mining.redialOnBadSignal,
								() -> config.mining.redialOnBadSignal,
								newValue -> config.mining.redialOnBadSignal = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())

				.option(ButtonOption.createBuilder()
						.name(Text.translatable("skyblocker.config.mining.dwarvenHud.screen"))
						.prompt(Text.translatable("text.skyblocker.open"))
						.action(screen -> MinecraftClient.getInstance().setScreen(new WidgetsConfigurationScreen(Location.DWARVEN_MINES, CommsWidget.ID, screen)))
						.build())

                //Dwarven Mines
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.mining.dwarvenMines"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.dwarvenMines.solveFetchur"))
                                .binding(defaults.mining.dwarvenMines.solveFetchur,
                                        () -> config.mining.dwarvenMines.solveFetchur,
                                        newValue -> config.mining.dwarvenMines.solveFetchur = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.dwarvenMines.solvePuzzler"))
                                .binding(defaults.mining.dwarvenMines.solvePuzzler,
                                        () -> config.mining.dwarvenMines.solvePuzzler,
                                        newValue -> config.mining.dwarvenMines.solvePuzzler = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
		                .option(Option.<Boolean>createBuilder()
				                .name(Text.translatable("skyblocker.config.mining.dwarvenMines.enableCarpetHighlight"))
				                .description(Text.translatable("skyblocker.config.mining.dwarvenMines.enableCarpetHighlight.@Tooltip"))
                                .binding(defaults.mining.dwarvenMines.enableCarpetHighlighter,
		                                () -> config.mining.dwarvenMines.enableCarpetHighlighter,
                                       newValue -> config.mining.dwarvenMines.enableCarpetHighlighter = newValue)
                                .controller(ConfigUtils.createBooleanController())
	                            .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.dwarvenMines.carpetHighlightColor"))
                                .description(Text.translatable("skyblocker.config.mining.dwarvenMines.carpetHighlightColor.@Tooltip"))
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
                        .name(Text.translatable("skyblocker.config.mining.crystalHollows"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalHollows.metalDetectorHelper"))
                                .description(Text.translatable("skyblocker.config.mining.crystalHollows.metalDetectorHelper.@Tooltip"))
                                .binding(defaults.mining.crystalHollows.metalDetectorHelper,
                                        () -> config.mining.crystalHollows.metalDetectorHelper,
                                        newValue -> config.mining.crystalHollows.metalDetectorHelper = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalHollows.nucleusWaypoints"))
                                .description(Text.translatable("skyblocker.config.mining.crystalHollows.nucleusWaypoints.@Tooltip"))
                                .binding(defaults.mining.crystalHollows.nucleusWaypoints,
                                        () -> config.mining.crystalHollows.nucleusWaypoints,
                                        newValue -> config.mining.crystalHollows.nucleusWaypoints = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalHollows.chestHighlighter"))
                                .description(Text.translatable("skyblocker.config.mining.crystalHollows.chestHighlighter.@Tooltip"))
                                .binding(defaults.mining.crystalHollows.chestHighlighter,
                                        () -> config.mining.crystalHollows.chestHighlighter,
                                        newValue -> config.mining.crystalHollows.chestHighlighter = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalHollows.chestHighlighter.color"))
                                .description(Text.translatable("skyblocker.config.mining.crystalHollows.chestHighlighter.color.@Tooltip"))
                                .binding(defaults.mining.crystalHollows.chestHighlightColor,
                                        () -> config.mining.crystalHollows.chestHighlightColor,
                                        newValue -> config.mining.crystalHollows.chestHighlightColor = newValue)
                                .controller(ConfigUtils.createColourController(true))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalHollows.enablePowderTracker"))
                                .description(Text.translatable("skyblocker.config.mining.crystalHollows.enablePowderTracker.@Tooltip"))
                                .binding(defaults.mining.crystalHollows.enablePowderTracker,
                                        () -> config.mining.crystalHollows.enablePowderTracker,
                                        newValue -> {
									config.mining.crystalHollows.enablePowderTracker = newValue;
									if (newValue) PowderMiningTracker.INSTANCE.recalculateAll();
								})
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalHollows.powderTrackerFilter"))
                                .description(Text.translatable("skyblocker.config.mining.crystalHollows.powderTrackerFilter.@Tooltip"))
                                .prompt(Text.translatable("text.skyblocker.open"))
                                .action(screen -> MinecraftClient.getInstance().setScreen(new PowderFilterConfigScreen(screen, new ObjectImmutableList<>(PowderMiningTracker.getName2IdMap().keySet()))))
                                .build())
                        .build())

                //Crystal Hollows Map
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.mining.crystalsHud"))
                        .collapsed(false)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalsHud.enabled"))
                                .binding(defaults.mining.crystalsHud.enabled,
                                        () -> config.mining.crystalsHud.enabled,
                                        newValue -> config.mining.crystalsHud.enabled = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalsHud.screen"))
                                .prompt(Text.translatable("text.skyblocker.open"))
                                .action(screen -> MinecraftClient.getInstance().setScreen(new WidgetsConfigurationScreen(Location.CRYSTAL_HOLLOWS, CrystalsHudWidget.getInstance().getInternalID(), screen)))
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalsHud.mapScaling"))
                                .binding(defaults.mining.crystalsHud.mapScaling,
                                        () -> config.mining.crystalsHud.mapScaling,
                                        newValue -> config.mining.crystalsHud.mapScaling = newValue)
                                .controller(FloatController.createBuilder().build())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalsHud.showLocations"))
                                .description(Text.translatable("skyblocker.config.mining.crystalsHud.showLocations.@Tooltip"))
                                .binding(defaults.mining.crystalsHud.showLocations,
                                        () -> config.mining.crystalsHud.showLocations,
                                        newValue -> config.mining.crystalsHud.showLocations = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalsHud.showLocations.locationSize"))
                                .description(Text.translatable("skyblocker.config.mining.crystalsHud.showLocations.locationSize.@Tooltip"))
                                .binding(defaults.mining.crystalsHud.locationSize,
                                        () -> config.mining.crystalsHud.locationSize,
                                        newValue -> config.mining.crystalsHud.locationSize = newValue)
                                .controller(IntegerController.createBuilder().range(4, 12).slider(2).build())
                                .build())
                        .build())

                //Crystal Hollows waypoints
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.mining.crystalsWaypoints"))
                        .collapsed(false)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalsWaypoints.enabled"))
                                .description(Text.translatable("skyblocker.config.mining.crystalsWaypoints.enabled.@Tooltip"))
                                .binding(defaults.mining.crystalsWaypoints.enabled,
                                        () -> config.mining.crystalsWaypoints.enabled,
                                        newValue -> config.mining.crystalsWaypoints.enabled = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalsWaypoints.findInChat"))
                                .description(Text.translatable("skyblocker.config.mining.crystalsWaypoints.findInChat.@Tooltip"))
                                .binding(defaults.mining.crystalsWaypoints.findInChat,
                                        () -> config.mining.crystalsWaypoints.findInChat,
                                        newValue -> config.mining.crystalsWaypoints.findInChat = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalsWaypoints.wishingCompassSolver"))
                                .description(Text.translatable("skyblocker.config.mining.crystalsWaypoints.wishingCompassSolver.@Tooltip"))
                                .binding(defaults.mining.crystalsWaypoints.wishingCompassSolver,
                                        () -> config.mining.crystalsWaypoints.wishingCompassSolver,
                                        newValue -> config.mining.crystalsWaypoints.wishingCompassSolver = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.mining.crystalsWaypoints.shareFairyGrotto"))
								.description(Text.translatable("skyblocker.config.mining.crystalsWaypoints.shareFairyGrotto.@Tooltip"))
								.binding(defaults.mining.crystalsWaypoints.shareFairyGrotto,
										() -> config.mining.crystalsWaypoints.shareFairyGrotto,
										newValue -> config.mining.crystalsWaypoints.shareFairyGrotto = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
                        .build())

                //commission waypoints
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.mining.commissionWaypoints"))
                        .collapsed(false)
                        .option(Option.<MiningConfig.CommissionWaypointMode>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.commissionWaypoints.mode"))
                                .description(Text.translatable("skyblocker.config.mining.commissionWaypoints.mode.@Tooltip[0]"),
                                        Text.translatable("skyblocker.config.mining.commissionWaypoints.mode.@Tooltip[1]"),
                                        Text.translatable("skyblocker.config.mining.commissionWaypoints.mode.@Tooltip[2]"),
                                        Text.translatable("skyblocker.config.mining.commissionWaypoints.mode.@Tooltip[3]"),
                                        Text.translatable("skyblocker.config.mining.commissionWaypoints.mode.@Tooltip[4]"))
                                .binding(defaults.mining.commissionWaypoints.mode,
                                        () -> config.mining.commissionWaypoints.mode,
                                        newValue -> config.mining.commissionWaypoints.mode = newValue)
                                .controller(ConfigUtils.createEnumController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.commissionWaypoints.useColor"))
                                .description(Text.translatable("skyblocker.config.mining.commissionWaypoints.useColor.@Tooltip"))
                                .binding(defaults.mining.commissionWaypoints.useColor,
                                        () -> config.mining.commissionWaypoints.useColor,
                                        newValue -> config.mining.commissionWaypoints.useColor = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.commissionWaypoints.showBaseCamp"))
                                .description(Text.translatable("skyblocker.config.mining.commissionWaypoints.showBaseCamp.@Tooltip"))
                                .binding(defaults.mining.commissionWaypoints.showBaseCamp,
                                        () -> config.mining.commissionWaypoints.showBaseCamp,
                                        newValue -> config.mining.commissionWaypoints.showBaseCamp = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.commissionWaypoints.showEmissary"))
                                .description(Text.translatable("skyblocker.config.mining.commissionWaypoints.showEmissary.@Tooltip"))
                                .binding(defaults.mining.commissionWaypoints.showEmissary,
                                        () -> config.mining.commissionWaypoints.showEmissary,
                                        newValue -> config.mining.commissionWaypoints.showEmissary = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.mining.commissionWaypoints.hideEmissaryOnPigeon"))
								.description(Text.translatable("skyblocker.config.mining.commissionWaypoints.hideEmissaryOnPigeon.@Tooltip"))
								.binding(defaults.mining.commissionWaypoints.hideEmissaryOnPigeon,
										() -> config.mining.commissionWaypoints.hideEmissaryOnPigeon,
										newValue -> config.mining.commissionWaypoints.hideEmissaryOnPigeon = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
                        .build())

                //Glacite Tunnels
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.mining.glacite"))
                        .collapsed(false)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.glacite.coldOverlay"))
                                .description(Text.translatable("skyblocker.config.mining.glacite.coldOverlay.@Tooltip"))
                                .binding(defaults.mining.glacite.coldOverlay,
                                        () -> config.mining.glacite.coldOverlay,
                                        newValue -> config.mining.glacite.coldOverlay = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.mining.glacite.fossilSolver"))
								.description(Text.translatable("skyblocker.config.mining.glacite.fossilSolver.@Tooltip"))
								.binding(defaults.mining.glacite.fossilSolver,
												() -> config.mining.glacite.fossilSolver,
												newValue -> config.mining.glacite.fossilSolver = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.glacite.enableCorpseFinder"))
                                .description(Text.translatable("skyblocker.config.mining.glacite.enableCorpseFinder.@Tooltip"))
                                .binding(defaults.mining.glacite.enableCorpseFinder,
                                        () -> config.mining.glacite.enableCorpseFinder,
                                        newValue -> config.mining.glacite.enableCorpseFinder = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.glacite.enableParsingChatCorpseFinder"))
                                .description(Text.translatable("skyblocker.config.mining.glacite.enableParsingChatCorpseFinder.@Tooltip"))
                                .binding(defaults.mining.glacite.enableParsingChatCorpseFinder,
                                        () -> config.mining.glacite.enableParsingChatCorpseFinder,
                                        newValue -> config.mining.glacite.enableParsingChatCorpseFinder = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
		                .option(Option.<Boolean>createBuilder()
		                        .name(Text.translatable("skyblocker.config.mining.glacite.autoShareCorpses"))
		                        .description(Text.translatable("skyblocker.config.mining.glacite.autoShareCorpses.@Tooltip"))
		                        .binding(defaults.mining.glacite.autoShareCorpses,
		                                () -> config.mining.glacite.autoShareCorpses,
		                                newValue -> config.mining.glacite.autoShareCorpses = newValue)
		                        .controller(ConfigUtils.createBooleanController())
				                .build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.mining.glacite.enableCorpseProfitTracker"))
								.description(Text.translatable("skyblocker.config.mining.glacite.enableCorpseProfitTracker.@Tooltip"))
								.binding(defaults.mining.glacite.enableCorpseProfitTracker,
										() -> config.mining.glacite.enableCorpseProfitTracker,
										newValue -> config.mining.glacite.enableCorpseProfitTracker = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.mining.glacite.forceEnglishCorpseProfitTracker"))
								.description(Text.translatable("skyblocker.config.mining.glacite.forceEnglishCorpseProfitTracker.@Tooltip"))
								.tags(CommonTags.ADDED_IN_5_9_0)
								.binding(defaults.mining.glacite.forceEnglishCorpseProfitTracker,
										() -> config.mining.glacite.forceEnglishCorpseProfitTracker,
										newValue -> config.mining.glacite.forceEnglishCorpseProfitTracker = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
                        .build())
                .build();
    }
}
