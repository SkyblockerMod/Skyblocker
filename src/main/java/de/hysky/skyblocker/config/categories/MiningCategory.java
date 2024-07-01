package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.configs.MiningConfig;
import de.hysky.skyblocker.skyblock.dwarven.CrystalsHudConfigScreen;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import de.hysky.skyblocker.skyblock.dwarven.DwarvenHudConfigScreen;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

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
                        .build())

                //Dwarven HUD
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.mining.dwarvenHud"))
                        .collapsed(false)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.dwarvenHud.enabledCommissions"))
                                .binding(defaults.mining.dwarvenHud.enabledCommissions,
                                        () -> config.mining.dwarvenHud.enabledCommissions,
                                        newValue -> config.mining.dwarvenHud.enabledCommissions = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.dwarvenHud.enabledPowder"))
                                .binding(defaults.mining.dwarvenHud.enabledPowder,
                                        () -> config.mining.dwarvenHud.enabledPowder,
                                        newValue -> config.mining.dwarvenHud.enabledPowder = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<MiningConfig.DwarvenHudStyle>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.dwarvenHud.style"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.dwarvenHud.style.@Tooltip[0]"),
                                        Text.translatable("skyblocker.config.mining.dwarvenHud.style.@Tooltip[1]"),
                                        Text.translatable("skyblocker.config.mining.dwarvenHud.style.@Tooltip[2]")))
                                .binding(defaults.mining.dwarvenHud.style,
                                        () -> config.mining.dwarvenHud.style,
                                        newValue -> config.mining.dwarvenHud.style = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.dwarvenHud.screen"))
                                .text(Text.translatable("text.skyblocker.open"))
                                .action((screen, opt) -> MinecraftClient.getInstance().setScreen(new DwarvenHudConfigScreen(screen)))
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
                                .action((screen, opt) -> MinecraftClient.getInstance().setScreen(new CrystalsHudConfigScreen(screen)))
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
                        .option(Option.<Float>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.crystalsWaypoints.textScale"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.crystalsWaypoints.textScale.@Tooltip")))
                                .binding(defaults.mining.crystalsWaypoints.textScale,
                                        () -> config.mining.crystalsWaypoints.textScale,
                                        newValue -> config.mining.crystalsWaypoints.textScale = newValue)
                                .controller(FloatFieldControllerBuilder::create)
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
                                .binding(defaults.mining.crystalsWaypoints.WishingCompassSolver,
                                        () -> config.mining.crystalsWaypoints.WishingCompassSolver,
                                        newValue -> config.mining.crystalsWaypoints.WishingCompassSolver = newValue)
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
                        .option(Option.<Float>createBuilder()
                                .name(Text.translatable("skyblocker.config.mining.commissionWaypoints.textScale"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.commissionWaypoints.textScale.@Tooltip")))
                                .binding(defaults.mining.commissionWaypoints.textScale,
                                        () -> config.mining.commissionWaypoints.textScale,
                                        newValue -> config.mining.commissionWaypoints.textScale = newValue)
                                .controller(FloatFieldControllerBuilder::create)
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
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.mining.glacite.coldOverlay@Tooltip")))
                                .binding(defaults.mining.glacite.coldOverlay,
                                        () -> config.mining.glacite.coldOverlay,
                                        newValue -> config.mining.glacite.coldOverlay = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())
                .build();
    }
}
