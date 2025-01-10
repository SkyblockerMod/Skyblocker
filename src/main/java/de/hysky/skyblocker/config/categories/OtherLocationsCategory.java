package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.end.EndHudWidget;
import de.hysky.skyblocker.skyblock.end.TheEnd;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.utils.Location;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class OtherLocationsCategory {

    public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("skyblocker.config.otherLocations"))

                //Barn
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.otherLocations.barn"))
                        .collapsed(false)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.otherLocations.barn.solveHungryHiker"))
                                .binding(defaults.otherLocations.barn.solveHungryHiker,
                                        () -> config.otherLocations.barn.solveHungryHiker,
                                        newValue -> config.otherLocations.barn.solveHungryHiker = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.otherLocations.barn.solveTreasureHunter"))
                                .binding(defaults.otherLocations.barn.solveTreasureHunter,
                                        () -> config.otherLocations.barn.solveTreasureHunter,
                                        newValue -> config.otherLocations.barn.solveTreasureHunter = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //The Rift
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.otherLocations.rift"))
                        .collapsed(false)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.otherLocations.rift.mirrorverseWaypoints"))
                                .binding(defaults.otherLocations.rift.mirrorverseWaypoints,
                                        () -> config.otherLocations.rift.mirrorverseWaypoints,
                                        newValue -> config.otherLocations.rift.mirrorverseWaypoints = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.otherLocations.rift.blobbercystGlow"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.otherLocations.rift.blobbercystGlow.@Tooltip")))
                                .binding(defaults.otherLocations.rift.blobbercystGlow,
                                        () -> config.otherLocations.rift.blobbercystGlow,
                                        newValue -> config.otherLocations.rift.blobbercystGlow = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.otherLocations.rift.enigmaSoulWaypoints"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.otherLocations.rift.enigmaSoulWaypoints.@Tooltip")))
                                .binding(defaults.otherLocations.rift.enigmaSoulWaypoints,
                                        () -> config.otherLocations.rift.enigmaSoulWaypoints,
                                        newValue -> config.otherLocations.rift.enigmaSoulWaypoints = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.otherLocations.rift.highlightFoundEnigmaSouls"))
                                .binding(defaults.otherLocations.rift.highlightFoundEnigmaSouls,
                                        () -> config.otherLocations.rift.highlightFoundEnigmaSouls,
                                        newValue -> config.otherLocations.rift.highlightFoundEnigmaSouls = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.otherLocations.rift.mcGrubberStacks"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.otherLocations.rift.mcGrubberStacks.@Tooltip")))
                                .binding(defaults.otherLocations.rift.mcGrubberStacks,
                                        () -> config.otherLocations.rift.mcGrubberStacks,
                                        newValue -> config.otherLocations.rift.mcGrubberStacks = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 5).step(1))
                                .build())
                        .build())

                // The end
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.otherLocations.end"))
                        .collapsed(false)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.otherLocations.end.enableEnderNodeHelper"))
                                .binding(defaults.otherLocations.end.enableEnderNodeHelper,
                                        () -> config.otherLocations.end.enableEnderNodeHelper,
                                        newValue -> config.otherLocations.end.enableEnderNodeHelper = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.otherLocations.end.hudEnabled"))
                                .binding(defaults.otherLocations.end.hudEnabled,
                                        () -> config.otherLocations.end.hudEnabled,
                                        newValue -> config.otherLocations.end.hudEnabled = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.otherLocations.end.zealotKillsEnabled"))
                                .binding(defaults.otherLocations.end.zealotKillsEnabled,
                                        () -> config.otherLocations.end.zealotKillsEnabled,
                                        newValue -> {
                                            config.otherLocations.end.zealotKillsEnabled = newValue;
                                            EndHudWidget.getInstance().update();
                                        })
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.otherLocations.end.protectorLocationEnable"))
                                .binding(defaults.otherLocations.end.protectorLocationEnabled,
                                        () -> config.otherLocations.end.protectorLocationEnabled,
                                        newValue -> {
                                            config.otherLocations.end.protectorLocationEnabled = newValue;
                                            EndHudWidget.getInstance().update();
                                        })
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.otherLocations.end.waypoint"))
                                .binding(defaults.otherLocations.end.waypoint,
                                        () -> config.otherLocations.end.waypoint,
                                        newValue -> config.otherLocations.end.waypoint = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("skyblocker.config.otherLocations.end.screen"))
                                .text(Text.translatable("text.skyblocker.open")) // Reusing again lol
                                .action((screen, opt) -> MinecraftClient.getInstance().setScreen(new WidgetsConfigurationScreen(Location.THE_END, EndHudWidget.getInstance().getInternalID(), screen)))
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("skyblocker.config.otherLocations.end.resetName"))
                                .text(Text.translatable("skyblocker.config.otherLocations.end.resetText"))
                                .action((screen, opt) -> TheEnd.PROFILES_STATS.put(TheEnd.EndStats.EMPTY))
                                .build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.otherLocations.end.muteEndermanSounds"))
								.binding(defaults.otherLocations.end.muteEndermanSounds,
										() -> config.otherLocations.end.muteEndermanSounds,
										newValue -> config.otherLocations.end.muteEndermanSounds = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
                        .build())

                //Spider's Den
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.otherLocations.spidersDen"))
                        .collapsed(false)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.otherLocations.spidersDen.relics.enableRelicsHelper"))
                                .binding(defaults.otherLocations.spidersDen.relics.enableRelicsHelper,
                                        () -> config.otherLocations.spidersDen.relics.enableRelicsHelper,
                                        newValue -> config.otherLocations.spidersDen.relics.enableRelicsHelper = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.otherLocations.spidersDen.relics.highlightFoundRelics"))
                                .binding(defaults.otherLocations.spidersDen.relics.highlightFoundRelics,
                                        () -> config.otherLocations.spidersDen.relics.highlightFoundRelics,
                                        newValue -> config.otherLocations.spidersDen.relics.highlightFoundRelics = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                .build();
    }
}
