package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import net.minecraft.text.Text;

public class HelperCategory {
    public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("skyblocker.config.helper"))

                //Ungrouped Options
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.helper.enableNewYearCakesHelper"))
                        .description(OptionDescription.of(Text.translatable("skyblocker.config.helper.enableNewYearCakesHelper.@Tooltip")))
                        .binding(defaults.helpers.enableNewYearCakesHelper,
                                () -> config.helpers.enableNewYearCakesHelper,
                                newValue -> config.helpers.enableNewYearCakesHelper = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .build())

                //Mythological Ritual
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.helper.mythologicalRitual"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helper.mythologicalRitual.enableMythologicalRitualHelper"))
                                .binding(defaults.helpers.mythologicalRitual.enableMythologicalRitualHelper,
                                        () -> config.helpers.mythologicalRitual.enableMythologicalRitualHelper,
                                        newValue -> config.helpers.mythologicalRitual.enableMythologicalRitualHelper = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Experiments Solver
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.helper.experiments"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helper.experiments.enableChronomatronSolver"))
                                .binding(defaults.helpers.experiments.enableChronomatronSolver,
                                        () -> config.helpers.experiments.enableChronomatronSolver,
                                        newValue -> config.helpers.experiments.enableChronomatronSolver = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helper.experiments.enableSuperpairsSolver"))
                                .binding(defaults.helpers.experiments.enableSuperpairsSolver,
                                        () -> config.helpers.experiments.enableSuperpairsSolver,
                                        newValue -> config.helpers.experiments.enableSuperpairsSolver = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helper.experiments.enableUltrasequencerSolver"))
                                .binding(defaults.helpers.experiments.enableUltrasequencerSolver,
                                        () -> config.helpers.experiments.enableUltrasequencerSolver,
                                        newValue -> config.helpers.experiments.enableUltrasequencerSolver = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Fishing Helper
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.helper.fishing"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helper.fishing.enableFishingHelper"))
                                .binding(defaults.helpers.fishing.enableFishingHelper,
                                        () -> config.helpers.fishing.enableFishingHelper,
                                        newValue -> config.helpers.fishing.enableFishingHelper = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helper.fishing.enableFishingTimer"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.helper.fishing.enableFishingTimer.@Tooltip")))
                                .binding(defaults.helpers.fishing.enableFishingTimer,
                                        () -> config.helpers.fishing.enableFishingTimer,
                                        newValue -> config.helpers.fishing.enableFishingTimer = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helper.fishing.changeTimerColor"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.helper.fishing.changeTimerColor.@Tooltip")))
                                .binding(defaults.helpers.fishing.changeTimerColor,
                                        () -> config.helpers.fishing.changeTimerColor,
                                        newValue -> config.helpers.fishing.changeTimerColor = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.translatable("skyblocker.config.helper.fishing.fishingTimerScale"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.helper.fishing.fishingTimerScale.@Tooltip")))
                                .binding(defaults.helpers.fishing.fishingTimerScale,
                                        () -> config.helpers.fishing.fishingTimerScale,
                                        newValue -> config.helpers.fishing.fishingTimerScale = newValue)
                                .controller(FloatFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helper.fishing.hideOtherPlayers"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.helper.fishing.hideOtherPlayers.@Tooltip")))
                                .binding(defaults.helpers.fishing.hideOtherPlayersRods,
                                        () -> config.helpers.fishing.hideOtherPlayersRods,
                                        newValue -> config.helpers.fishing.hideOtherPlayersRods = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Fairy Souls Helper
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.helper.fairySouls"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helper.fairySouls.enableFairySoulsHelper"))
                                .binding(defaults.helpers.fairySouls.enableFairySoulsHelper,
                                        () -> config.helpers.fairySouls.enableFairySoulsHelper,
                                        newValue -> config.helpers.fairySouls.enableFairySoulsHelper = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helper.fairySouls.highlightFoundSouls"))
                                .binding(defaults.helpers.fairySouls.highlightFoundSouls,
                                        () -> config.helpers.fairySouls.highlightFoundSouls,
                                        newValue -> config.helpers.fairySouls.highlightFoundSouls = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helper.fairySouls.highlightOnlyNearbySouls"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.helper.fairySouls.highlightOnlyNearbySouls.@Tooltip")))
                                .binding(defaults.helpers.fairySouls.highlightOnlyNearbySouls,
                                        () -> config.helpers.fairySouls.highlightOnlyNearbySouls,
                                        newValue -> config.helpers.fairySouls.highlightOnlyNearbySouls = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                .build();
    }
}
