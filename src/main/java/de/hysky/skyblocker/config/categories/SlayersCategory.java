package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.configs.SlayersConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.text.Text;

public class SlayersCategory {

    public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("skyblocker.config.slayer"))

                //Enderman Slayer
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.slayer.endermanSlayer"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.slayer.endermanSlayer.enableYangGlyphsNotification"))
                                .binding(defaults.slayers.endermanSlayer.enableYangGlyphsNotification,
                                        () -> config.slayers.endermanSlayer.enableYangGlyphsNotification,
                                        newValue -> config.slayers.endermanSlayer.enableYangGlyphsNotification = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.slayer.endermanSlayer.highlightBeacons"))
                                .binding(defaults.slayers.endermanSlayer.highlightBeacons,
                                        () -> config.slayers.endermanSlayer.highlightBeacons,
                                        newValue -> config.slayers.endermanSlayer.highlightBeacons = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.slayer.endermanSlayer.highlightNukekubiHeads"))
                                .binding(defaults.slayers.endermanSlayer.highlightNukekubiHeads,
                                        () -> config.slayers.endermanSlayer.highlightNukekubiHeads,
                                        newValue -> config.slayers.endermanSlayer.highlightNukekubiHeads = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Vampire Slayer
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.slayer.vampireSlayer"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.slayer.vampireSlayer.enableEffigyWaypoints"))
                                .binding(defaults.slayers.vampireSlayer.enableEffigyWaypoints,
                                        () -> config.slayers.vampireSlayer.enableEffigyWaypoints,
                                        newValue -> config.slayers.vampireSlayer.enableEffigyWaypoints = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.slayer.vampireSlayer.compactEffigyWaypoints"))
                                .binding(defaults.slayers.vampireSlayer.compactEffigyWaypoints,
                                        () -> config.slayers.vampireSlayer.compactEffigyWaypoints,
                                        newValue -> config.slayers.vampireSlayer.compactEffigyWaypoints = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.slayer.vampireSlayer.effigyUpdateFrequency"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.slayer.vampireSlayer.effigyUpdateFrequency.@Tooltip")))
                                .binding(defaults.slayers.vampireSlayer.effigyUpdateFrequency,
                                        () -> config.slayers.vampireSlayer.effigyUpdateFrequency,
                                        newValue -> config.slayers.vampireSlayer.effigyUpdateFrequency = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 10).step(1))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.slayer.vampireSlayer.enableHolyIceIndicator"))
                                .binding(defaults.slayers.vampireSlayer.enableHolyIceIndicator,
                                        () -> config.slayers.vampireSlayer.enableHolyIceIndicator,
                                        newValue -> config.slayers.vampireSlayer.enableHolyIceIndicator = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.slayer.vampireSlayer.holyIceIndicatorTickDelay"))
                                .binding(defaults.slayers.vampireSlayer.holyIceIndicatorTickDelay,
                                        () -> config.slayers.vampireSlayer.holyIceIndicatorTickDelay,
                                        newValue -> config.slayers.vampireSlayer.holyIceIndicatorTickDelay = newValue)
                                .controller(IntegerFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.slayer.vampireSlayer.holyIceUpdateFrequency"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.slayer.vampireSlayer.holyIceUpdateFrequency.@Tooltip")))
                                .binding(defaults.slayers.vampireSlayer.holyIceUpdateFrequency,
                                        () -> config.slayers.vampireSlayer.holyIceUpdateFrequency,
                                        newValue -> config.slayers.vampireSlayer.holyIceUpdateFrequency = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 10).step(1))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.slayer.vampireSlayer.enableHealingMelonIndicator"))
                                .binding(defaults.slayers.vampireSlayer.enableHealingMelonIndicator,
                                        () -> config.slayers.vampireSlayer.enableHealingMelonIndicator,
                                        newValue -> config.slayers.vampireSlayer.enableHealingMelonIndicator = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.translatable("skyblocker.config.slayer.vampireSlayer.healingMelonHealthThreshold"))
                                .binding(defaults.slayers.vampireSlayer.healingMelonHealthThreshold,
                                        () -> config.slayers.vampireSlayer.healingMelonHealthThreshold,
                                        newValue -> config.slayers.vampireSlayer.healingMelonHealthThreshold = newValue)
                                .controller(FloatFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.slayer.vampireSlayer.enableSteakStakeIndicator"))
                                .binding(defaults.slayers.vampireSlayer.enableSteakStakeIndicator,
                                        () -> config.slayers.vampireSlayer.enableSteakStakeIndicator,
                                        newValue -> config.slayers.vampireSlayer.enableSteakStakeIndicator = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.slayer.vampireSlayer.steakStakeUpdateFrequency"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.slayer.vampireSlayer.steakStakeUpdateFrequency.@Tooltip")))
                                .binding(defaults.slayers.vampireSlayer.steakStakeUpdateFrequency,
                                        () -> config.slayers.vampireSlayer.steakStakeUpdateFrequency,
                                        newValue -> config.slayers.vampireSlayer.steakStakeUpdateFrequency = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 10).step(1))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.slayer.vampireSlayer.enableManiaIndicator"))
                                .binding(defaults.slayers.vampireSlayer.enableManiaIndicator,
                                        () -> config.slayers.vampireSlayer.enableManiaIndicator,
                                        newValue -> config.slayers.vampireSlayer.enableManiaIndicator = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.slayer.vampireSlayer.maniaUpdateFrequency"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.slayer.vampireSlayer.maniaUpdateFrequency.@Tooltip")))
                                .binding(defaults.slayers.vampireSlayer.maniaUpdateFrequency,
                                        () -> config.slayers.vampireSlayer.maniaUpdateFrequency,
                                        newValue -> config.slayers.vampireSlayer.maniaUpdateFrequency = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 10).step(1))
                                .build())
                        .build())

                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.slayer.blazeSlayer"))
                        .collapsed(true)
                        .option(Option.<SlayersConfig.BlazeSlayer.FirePillar>createBuilder()
                                .name(Text.translatable("skyblocker.config.slayer.blazeSlayer.enableFirePillarAnnouncer"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.slayer.blazeSlayer.enableFirePillarAnnouncer.@Tooltip")))
                                .binding(defaults.slayers.blazeSlayer.FirePillarCountdown,
                                        () -> config.slayers.blazeSlayer.FirePillarCountdown,
                                        newValue -> config.slayers.blazeSlayer.FirePillarCountdown = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .build())
                .build();
    }
}
