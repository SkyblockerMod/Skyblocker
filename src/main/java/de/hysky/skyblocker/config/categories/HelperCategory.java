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
                .name(Text.translatable("skyblocker.category.helper"))

                //Ungrouped Options
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.option.general.enableNewYearCakesHelper"))
                        .description(OptionDescription.of(Text.translatable("skyblocker.option.general.enableNewYearCakesHelper.@Tooltip")))
                        .binding(defaults.helper.enableNewYearCakesHelper,
                                () -> config.helper.enableNewYearCakesHelper,
                                newValue -> config.helper.enableNewYearCakesHelper = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .build())

                //Mythological Ritual
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.option.general.mythologicalRitual"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.option.general.mythologicalRitual.enableMythologicalRitualHelper"))
                                .binding(defaults.helper.mythologicalRitual.enableMythologicalRitualHelper,
                                        () -> config.helper.mythologicalRitual.enableMythologicalRitualHelper,
                                        newValue -> config.helper.mythologicalRitual.enableMythologicalRitualHelper = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Experiments Solver
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.option.general.experiments"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.option.general.experiments.enableChronomatronSolver"))
                                .binding(defaults.helper.experiments.enableChronomatronSolver,
                                        () -> config.helper.experiments.enableChronomatronSolver,
                                        newValue -> config.helper.experiments.enableChronomatronSolver = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.option.general.experiments.enableSuperpairsSolver"))
                                .binding(defaults.helper.experiments.enableSuperpairsSolver,
                                        () -> config.helper.experiments.enableSuperpairsSolver,
                                        newValue -> config.helper.experiments.enableSuperpairsSolver = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.option.general.experiments.enableUltrasequencerSolver"))
                                .binding(defaults.helper.experiments.enableUltrasequencerSolver,
                                        () -> config.helper.experiments.enableUltrasequencerSolver,
                                        newValue -> config.helper.experiments.enableUltrasequencerSolver = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Fishing Helper
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.option.general.fishing"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.option.general.fishing.enableFishingHelper"))
                                .binding(defaults.helper.fishing.enableFishingHelper,
                                        () -> config.helper.fishing.enableFishingHelper,
                                        newValue -> config.helper.fishing.enableFishingHelper = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.option.general.fishing.enableFishingTimer"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.option.general.fishing.enableFishingTimer.@Tooltip")))
                                .binding(defaults.helper.fishing.enableFishingTimer,
                                        () -> config.helper.fishing.enableFishingTimer,
                                        newValue -> config.helper.fishing.enableFishingTimer = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.option.general.fishing.changeTimerColor"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.option.general.fishing.changeTimerColor.@Tooltip")))
                                .binding(defaults.helper.fishing.changeTimerColor,
                                        () -> config.helper.fishing.changeTimerColor,
                                        newValue -> config.helper.fishing.changeTimerColor = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.translatable("skyblocker.option.general.fishing.fishingTimerScale"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.option.general.fishing.fishingTimerScale.@Tooltip")))
                                .binding(defaults.helper.fishing.fishingTimerScale,
                                        () -> config.helper.fishing.fishingTimerScale,
                                        newValue -> config.helper.fishing.fishingTimerScale = newValue)
                                .controller(FloatFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.option.general.fishing.hideOtherPlayers"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.option.general.fishing.hideOtherPlayers.@Tooltip")))
                                .binding(defaults.helper.fishing.hideOtherPlayersRods,
                                        () -> config.helper.fishing.hideOtherPlayersRods,
                                        newValue -> config.helper.fishing.hideOtherPlayersRods = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Fairy Souls Helper
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.option.general.fairySouls"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.option.general.fairySouls.enableFairySoulsHelper"))
                                .binding(defaults.helper.fairySouls.enableFairySoulsHelper,
                                        () -> config.helper.fairySouls.enableFairySoulsHelper,
                                        newValue -> config.helper.fairySouls.enableFairySoulsHelper = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.option.general.fairySouls.highlightFoundSouls"))
                                .binding(defaults.helper.fairySouls.highlightFoundSouls,
                                        () -> config.helper.fairySouls.highlightFoundSouls,
                                        newValue -> config.helper.fairySouls.highlightFoundSouls = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.option.general.fairySouls.highlightOnlyNearbySouls"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.option.general.fairySouls.highlightOnlyNearbySouls.@Tooltip")))
                                .binding(defaults.helper.fairySouls.highlightOnlyNearbySouls,
                                        () -> config.helper.fairySouls.highlightOnlyNearbySouls,
                                        newValue -> config.helper.fairySouls.highlightOnlyNearbySouls = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                .build();
    }
}
