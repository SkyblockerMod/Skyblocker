package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.bazaar.BazaarHelper;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import net.minecraft.text.Text;

public class HelperCategory {
    public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("skyblocker.config.helpers"))

                //Ungrouped Options
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.helpers.enableNewYearCakesHelper"))
                        .description(OptionDescription.of(Text.translatable("skyblocker.config.helpers.enableNewYearCakesHelper.@Tooltip")))
                        .binding(defaults.helpers.enableNewYearCakesHelper,
                                () -> config.helpers.enableNewYearCakesHelper,
                                newValue -> config.helpers.enableNewYearCakesHelper = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .build())

		        .option(Option.<Boolean>createBuilder()
		                .name(Text.translatable("skyblocker.config.helpers.enableWardrobeHelper"))
		                .description(OptionDescription.of(Text.translatable("skyblocker.config.helpers.enableWardrobeHelper.@Tooltip")))
		                .binding(defaults.helpers.enableWardrobeHelper,
				                () -> config.helpers.enableWardrobeHelper,
				                newValue -> config.helpers.enableWardrobeHelper = newValue)
		                .controller(ConfigUtils::createBooleanController)
				        .build())

                //Mythological Ritual
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.helpers.mythologicalRitual"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.mythologicalRitual.enableMythologicalRitualHelper"))
                                .binding(defaults.helpers.mythologicalRitual.enableMythologicalRitualHelper,
                                        () -> config.helpers.mythologicalRitual.enableMythologicalRitualHelper,
                                        newValue -> config.helpers.mythologicalRitual.enableMythologicalRitualHelper = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Jerry Timer
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.helpers.jerry"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.jerry.enableJerryTimer"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.helpers.jerry.enableJerryTimer.@Tooltip")))
                                .binding(defaults.helpers.jerry.enableJerryTimer,
                                        () -> config.helpers.jerry.enableJerryTimer,
                                        newValue -> config.helpers.jerry.enableJerryTimer = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Experiments Solver
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.helpers.experiments"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.experiments.enableChronomatronSolver"))
                                .binding(defaults.helpers.experiments.enableChronomatronSolver,
                                        () -> config.helpers.experiments.enableChronomatronSolver,
                                        newValue -> config.helpers.experiments.enableChronomatronSolver = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.experiments.enableSuperpairsSolver"))
                                .binding(defaults.helpers.experiments.enableSuperpairsSolver,
                                        () -> config.helpers.experiments.enableSuperpairsSolver,
                                        newValue -> config.helpers.experiments.enableSuperpairsSolver = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.experiments.enableUltrasequencerSolver"))
                                .binding(defaults.helpers.experiments.enableUltrasequencerSolver,
                                        () -> config.helpers.experiments.enableUltrasequencerSolver,
                                        newValue -> config.helpers.experiments.enableUltrasequencerSolver = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Fishing Helper
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.helpers.fishing"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.fishing.enableFishingHelper"))
                                .binding(defaults.helpers.fishing.enableFishingHelper,
                                        () -> config.helpers.fishing.enableFishingHelper,
                                        newValue -> config.helpers.fishing.enableFishingHelper = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.fishing.enableFishingTimer"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.helpers.fishing.enableFishingTimer.@Tooltip")))
                                .binding(defaults.helpers.fishing.enableFishingTimer,
                                        () -> config.helpers.fishing.enableFishingTimer,
                                        newValue -> config.helpers.fishing.enableFishingTimer = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.fishing.changeTimerColor"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.helpers.fishing.changeTimerColor.@Tooltip")))
                                .binding(defaults.helpers.fishing.changeTimerColor,
                                        () -> config.helpers.fishing.changeTimerColor,
                                        newValue -> config.helpers.fishing.changeTimerColor = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.fishing.fishingTimerScale"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.helpers.fishing.fishingTimerScale.@Tooltip")))
                                .binding(defaults.helpers.fishing.fishingTimerScale,
                                        () -> config.helpers.fishing.fishingTimerScale,
                                        newValue -> config.helpers.fishing.fishingTimerScale = newValue)
                                .controller(FloatFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.fishing.hideOtherPlayers"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.helpers.fishing.hideOtherPlayers.@Tooltip")))
                                .binding(defaults.helpers.fishing.hideOtherPlayersRods,
                                        () -> config.helpers.fishing.hideOtherPlayersRods,
                                        newValue -> config.helpers.fishing.hideOtherPlayersRods = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Fairy Souls Helper
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.helpers.fairySouls"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.fairySouls.enableFairySoulsHelper"))
                                .binding(defaults.helpers.fairySouls.enableFairySoulsHelper,
                                        () -> config.helpers.fairySouls.enableFairySoulsHelper,
                                        newValue -> config.helpers.fairySouls.enableFairySoulsHelper = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.fairySouls.highlightFoundSouls"))
                                .binding(defaults.helpers.fairySouls.highlightFoundSouls,
                                        () -> config.helpers.fairySouls.highlightFoundSouls,
                                        newValue -> config.helpers.fairySouls.highlightFoundSouls = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.fairySouls.highlightOnlyNearbySouls"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.helpers.fairySouls.highlightOnlyNearbySouls.@Tooltip")))
                                .binding(defaults.helpers.fairySouls.highlightOnlyNearbySouls,
                                        () -> config.helpers.fairySouls.highlightOnlyNearbySouls,
                                        newValue -> config.helpers.fairySouls.highlightOnlyNearbySouls = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Chocolate Factory
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.helpers.chocolateFactory"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.chocolateFactory.enableChocolateFactoryHelper"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.helpers.chocolateFactory.enableChocolateFactoryHelper.@Tooltip")))
                                .binding(defaults.helpers.chocolateFactory.enableChocolateFactoryHelper,
                                        () -> config.helpers.chocolateFactory.enableChocolateFactoryHelper,
                                        newValue -> config.helpers.chocolateFactory.enableChocolateFactoryHelper = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.chocolateFactory.enableEggFinder"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.helpers.chocolateFactory.enableEggFinder.@Tooltip")))
                                .binding(defaults.helpers.chocolateFactory.enableEggFinder,
                                        () -> config.helpers.chocolateFactory.enableEggFinder,
                                        newValue -> config.helpers.chocolateFactory.enableEggFinder = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.chocolateFactory.sendEggFoundMessages"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.helpers.chocolateFactory.sendEggFoundMessages.@Tooltip")))
                                .binding(defaults.helpers.chocolateFactory.sendEggFoundMessages,
                                        () -> config.helpers.chocolateFactory.sendEggFoundMessages,
                                        newValue -> config.helpers.chocolateFactory.sendEggFoundMessages = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Waypoint.Type>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.chocolateFactory.waypointType"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.helpers.chocolateFactory.waypointType.@Tooltip")))
                                .binding(defaults.helpers.chocolateFactory.waypointType,
                                        () -> config.helpers.chocolateFactory.waypointType,
                                        newValue -> config.helpers.chocolateFactory.waypointType = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.chocolateFactory.enableTimeTowerReminder"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.helpers.chocolateFactory.enableTimeTowerReminder.@Tooltip")))
                                .binding(defaults.helpers.chocolateFactory.enableTimeTowerReminder,
                                        () -> config.helpers.chocolateFactory.enableTimeTowerReminder,
                                        newValue -> config.helpers.chocolateFactory.enableTimeTowerReminder = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.chocolateFactory.straySound"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.helpers.chocolateFactory.straySound.@Tooltip")))
                                .binding(defaults.helpers.chocolateFactory.straySound,
                                        () -> config.helpers.chocolateFactory.straySound,
                                        newValue -> config.helpers.chocolateFactory.straySound = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Bazaar
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.helpers.bazaar"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                 .name(Text.translatable("skyblocker.config.helpers.bazaar.enableBazaarHelper"))
                                 .description(OptionDescription.of(Text.translatable("skyblocker.config.helpers.bazaar.enableBazaarHelper.@Tooltip", BazaarHelper.getExpiringIcon(), BazaarHelper.getExpiredIcon(), BazaarHelper.getFilledIcon(69), BazaarHelper.getFilledIcon(100))))
                                 .binding(defaults.helpers.bazaar.enableBazaarHelper,
		                                 () -> config.helpers.bazaar.enableBazaarHelper,
		                                 newValue -> config.helpers.bazaar.enableBazaarHelper = newValue)
                                 .controller(ConfigUtils::createBooleanController)
                                 .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.itemPrice.enableItemPriceLookup"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.helpers.itemPrice.enableItemPriceLookup.@Tooltip")))
                                .binding(defaults.helpers.itemPrice.enableItemPriceLookup,
                                        () -> config.helpers.itemPrice.enableItemPriceLookup,
                                        newValue -> config.helpers.itemPrice.enableItemPriceLookup = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.helpers.itemPrice.enableItemPriceRefresh"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.helpers.itemPrice.enableItemPriceRefresh.@Tooltip")))
                                .binding(defaults.helpers.itemPrice.enableItemPriceRefresh,
                                        () -> config.helpers.itemPrice.enableItemPriceRefresh,
                                        newValue -> config.helpers.itemPrice.enableItemPriceRefresh = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(ConfigUtils.createShortcutToKeybindsScreen())
		                .build())
                .build();
    }
}
