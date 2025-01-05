package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.configs.MiningConfig;
import de.hysky.skyblocker.config.screens.powdertracker.PowderFilterConfigScreen;
import de.hysky.skyblocker.skyblock.dwarven.CrystalsHudWidget;
import de.hysky.skyblocker.skyblock.dwarven.CarpetHighlighter;
import de.hysky.skyblocker.skyblock.dwarven.PowderMiningTracker;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.utils.Location;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.awt.*;

public class MiningCategory {

    public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("skyblocker.config.mining"))

                //Uncategorized Options
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.mining.enableDrillFuel"))
                        .binding(defaults.mining.enableDrillFuel,
                                () -> config.mining.enableDrillFuel,
                                newValue -> config.mining.enableDrillFuel = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .build())

                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.mining.commissionHighlight"))
                        .binding(defaults.mining.commissionHighlight,
                                () -> config.mining.commissionHighlight,
                                newValue -> config.mining.commissionHighlight = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .build())

                //Dwarven Mines
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.mining.dwarvenMines"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.dwarvenMines.solveFetchur"))
                                .binding(defaults.mining.dwarvenMines.solveFetchur,
                                        () -> config.mining.dwarvenMines.solveFetchur,
                                        newValue -> config.mining.dwarvenMines.solveFetchur = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.dwarvenMines.solvePuzzler"))
                                .binding(defaults.mining.dwarvenMines.solvePuzzler,
                                        () -> config.mining.dwarvenMines.solvePuzzler,
                                        newValue -> config.mining.dwarvenMines.solvePuzzler = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
		                .option(Option.<Boolean>createBuilder()
				                .name(Text.translatable("skyblocker.config.mining.dwarvenMines.enableCarpetHighlight"))
				                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.dwarvenMines.enableCarpetHighlight.@Tooltip")))
                                .binding(defaults.mining.dwarvenMines.enableCarpetHighlighter,
		                                () -> config.mining.dwarvenMines.enableCarpetHighlighter,
                                       newValue -> config.mining.dwarvenMines.enableCarpetHighlighter = newValue)
                                .controller(ConfigUtils::createBooleanController)
	                            .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.dwarvenMines.carpetHighlightColor"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.dwarvenMines.carpetHighlightColor.@Tooltip")))
                                .binding(defaults.mining.dwarvenMines.carpetHighlightColor,
		                                () -> config.mining.dwarvenMines.carpetHighlightColor,
		                                newValue -> {
											config.mining.dwarvenMines.carpetHighlightColor = newValue;
			                                CarpetHighlighter.INSTANCE.configCallback(newValue);
		                                })
                                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                                .build())
                        .build())

                //Crystal Hollows
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.mining.crystalHollows"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalHollows.metalDetectorHelper"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.crystalHollows.metalDetectorHelper.@Tooltip")))
                                .binding(defaults.mining.crystalHollows.metalDetectorHelper,
                                        () -> config.mining.crystalHollows.metalDetectorHelper,
                                        newValue -> config.mining.crystalHollows.metalDetectorHelper = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalHollows.nucleusWaypoints"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.crystalHollows.nucleusWaypoints.@Tooltip")))
                                .binding(defaults.mining.crystalHollows.nucleusWaypoints,
                                        () -> config.mining.crystalHollows.nucleusWaypoints,
                                        newValue -> config.mining.crystalHollows.nucleusWaypoints = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalHollows.chestHighlighter"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.crystalHollows.chestHighlighter.@Tooltip")))
                                .binding(defaults.mining.crystalHollows.chestHighlighter,
                                        () -> config.mining.crystalHollows.chestHighlighter,
                                        newValue -> config.mining.crystalHollows.chestHighlighter = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalHollows.chestHighlighter.color"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.crystalHollows.chestHighlighter.color.@Tooltip")))
                                .binding(defaults.mining.crystalHollows.chestHighlightColor,
                                        () -> config.mining.crystalHollows.chestHighlightColor,
                                        newValue -> config.mining.crystalHollows.chestHighlightColor = newValue)
                                .controller(v -> ColorControllerBuilder.create(v).allowAlpha(true))
                                .build())
		                .option(ButtonOption.createBuilder()
				                .name(Text.translatable("skyblocker.config.mining.crystalHollows.powderTrackerFilter"))
				                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.crystalHollows.powderTrackerFilter.@Tooltip")))
				                .text(Text.translatable("text.skyblocker.open"))
				                .action((screen, opt) -> MinecraftClient.getInstance().setScreen(new PowderFilterConfigScreen(screen, new ObjectImmutableList<>(PowderMiningTracker.getName2IdMap().keySet()))))
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
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalsHud.screen"))
                                .text(Text.translatable("text.skyblocker.open"))
                                .action((screen, opt) -> MinecraftClient.getInstance().setScreen(new WidgetsConfigurationScreen(Location.CRYSTAL_HOLLOWS, CrystalsHudWidget.getInstance().getInternalID(), screen)))
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalsHud.mapScaling"))
                                .binding(defaults.mining.crystalsHud.mapScaling,
                                        () -> config.mining.crystalsHud.mapScaling,
                                        newValue -> config.mining.crystalsHud.mapScaling = newValue)
                                .controller(FloatFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalsHud.showLocations"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.crystalsHud.showLocations.@Tooltip")))
                                .binding(defaults.mining.crystalsHud.showLocations,
                                        () -> config.mining.crystalsHud.showLocations,
                                        newValue -> config.mining.crystalsHud.showLocations = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalsHud.showLocations.locationSize"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.crystalsHud.showLocations.locationSize.@Tooltip")))
                                .binding(defaults.mining.crystalsHud.locationSize,
                                        () -> config.mining.crystalsHud.locationSize,
                                        newValue -> config.mining.crystalsHud.locationSize = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(4, 12).step(2))
                                .build())
                        .build())

                //Crystal Hollows waypoints
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.mining.crystalsWaypoints"))
                        .collapsed(false)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalsWaypoints.enabled"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.crystalsWaypoints.enabled.@Tooltip")))
                                .binding(defaults.mining.crystalsWaypoints.enabled,
                                        () -> config.mining.crystalsWaypoints.enabled,
                                        newValue -> config.mining.crystalsWaypoints.enabled = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalsWaypoints.findInChat"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.crystalsWaypoints.findInChat.@Tooltip")))
                                .binding(defaults.mining.crystalsWaypoints.findInChat,
                                        () -> config.mining.crystalsWaypoints.findInChat,
                                        newValue -> config.mining.crystalsWaypoints.findInChat = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalsWaypoints.wishingCompassSolver"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.crystalsWaypoints.wishingCompassSolver.@Tooltip")))
                                .binding(defaults.mining.crystalsWaypoints.wishingCompassSolver,
                                        () -> config.mining.crystalsWaypoints.wishingCompassSolver,
                                        newValue -> config.mining.crystalsWaypoints.wishingCompassSolver = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.mining.crystalsWaypoints.shareFairyGrotto"))
								.description(OptionDescription.of(Text.translatable("skyblocker.config.mining.crystalsWaypoints.shareFairyGrotto.@Tooltip")))
								.binding(defaults.mining.crystalsWaypoints.shareFairyGrotto,
										() -> config.mining.crystalsWaypoints.shareFairyGrotto,
										newValue -> config.mining.crystalsWaypoints.shareFairyGrotto = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
                        .build())

                //commission waypoints
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.mining.commissionWaypoints"))
                        .collapsed(false)
                        .option(Option.<MiningConfig.CommissionWaypointMode>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.commissionWaypoints.mode"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.commissionWaypoints.mode.@Tooltip[0]"),
                                        Text.translatable("skyblocker.config.mining.commissionWaypoints.mode.@Tooltip[1]"),
                                        Text.translatable("skyblocker.config.mining.commissionWaypoints.mode.@Tooltip[2]"),
                                        Text.translatable("skyblocker.config.mining.commissionWaypoints.mode.@Tooltip[3]"),
                                        Text.translatable("skyblocker.config.mining.commissionWaypoints.mode.@Tooltip[4]")))
                                .binding(defaults.mining.commissionWaypoints.mode,
                                        () -> config.mining.commissionWaypoints.mode,
                                        newValue -> config.mining.commissionWaypoints.mode = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.commissionWaypoints.useColor"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.commissionWaypoints.useColor.@Tooltip")))
                                .binding(defaults.mining.commissionWaypoints.useColor,
                                        () -> config.mining.commissionWaypoints.useColor,
                                        newValue -> config.mining.commissionWaypoints.useColor = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.commissionWaypoints.showBaseCamp"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.commissionWaypoints.showBaseCamp.@Tooltip")))
                                .binding(defaults.mining.commissionWaypoints.showBaseCamp,
                                        () -> config.mining.commissionWaypoints.showBaseCamp,
                                        newValue -> config.mining.commissionWaypoints.showBaseCamp = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.commissionWaypoints.showEmissary"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.commissionWaypoints.showEmissary.@Tooltip")))
                                .binding(defaults.mining.commissionWaypoints.showEmissary,
                                        () -> config.mining.commissionWaypoints.showEmissary,
                                        newValue -> config.mining.commissionWaypoints.showEmissary = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Glacite Tunnels
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.mining.glacite"))
                        .collapsed(false)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.glacite.coldOverlay"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.glacite.coldOverlay.@Tooltip")))
                                .binding(defaults.mining.glacite.coldOverlay,
                                        () -> config.mining.glacite.coldOverlay,
                                        newValue -> config.mining.glacite.coldOverlay = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.glacite.enableCorpseFinder"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.glacite.enableCorpseFinder.@Tooltip")))
                                .binding(defaults.mining.glacite.enableCorpseFinder,
                                        () -> config.mining.glacite.enableCorpseFinder,
                                        newValue -> config.mining.glacite.enableCorpseFinder = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.glacite.enableParsingChatCorpseFinder"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.glacite.enableParsingChatCorpseFinder.@Tooltip")))
                                .binding(defaults.mining.glacite.enableParsingChatCorpseFinder,
                                        () -> config.mining.glacite.enableParsingChatCorpseFinder,
                                        newValue -> config.mining.glacite.enableParsingChatCorpseFinder = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
		                .option(Option.<Boolean>createBuilder()
		                        .name(Text.translatable("skyblocker.config.mining.glacite.autoShareCorpses"))
		                        .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.glacite.autoShareCorpses.@Tooltip")))
		                        .binding(defaults.mining.glacite.autoShareCorpses,
		                                () -> config.mining.glacite.autoShareCorpses,
		                                newValue -> config.mining.glacite.autoShareCorpses = newValue)
		                        .controller(ConfigUtils::createBooleanController)
				                .build())
                        .build())
                .build();
    }
}
