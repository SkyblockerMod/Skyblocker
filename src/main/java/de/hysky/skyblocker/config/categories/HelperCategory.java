package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.CommonTags;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.configs.HelperConfig;
import de.hysky.skyblocker.skyblock.bazaar.BazaarHelper;
import de.hysky.skyblocker.skyblock.fishing.FishingHudWidget;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.azureaaron.dandelion.systems.ButtonOption;
import net.azureaaron.dandelion.systems.ConfigCategory;
import net.azureaaron.dandelion.systems.Option;
import net.azureaaron.dandelion.systems.OptionGroup;
import net.azureaaron.dandelion.systems.controllers.IntegerController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class HelperCategory {
	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.id(SkyblockerMod.id("config/helpers"))
				.name(Text.translatable("skyblocker.config.helpers"))

				//Ungrouped Options
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.helpers.enableNewYearCakesHelper"))
						.description(Text.translatable("skyblocker.config.helpers.enableNewYearCakesHelper.@Tooltip"))
						.binding(defaults.helpers.enableNewYearCakesHelper,
								() -> config.helpers.enableNewYearCakesHelper,
								newValue -> config.helpers.enableNewYearCakesHelper = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())

				// Bits Helper
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.helpers.enableBitsHelper"))
						.description(Text.translatable("skyblocker.config.helpers.enableBitsHelper.@Tooltip"))
						.binding(defaults.helpers.enableBitsTooltip,
								() -> config.helpers.enableBitsTooltip,
								newValue -> config.helpers.enableBitsTooltip = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())

				// Wardrobe Helper
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.helpers.enableWardrobeHelper"))
						.description(Text.translatable("skyblocker.config.helpers.enableWardrobeHelper.@Tooltip"))
						.binding(defaults.helpers.enableWardrobeHelper,
								() -> config.helpers.enableWardrobeHelper,
								newValue -> config.helpers.enableWardrobeHelper = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())

				// Date Calculator
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.helpers.enableDateCalculator"))
						.description(Text.translatable("skyblocker.config.helpers.enableDateCalculator.@Tooltip"))
						.binding(defaults.helpers.enableDateCalculator,
								() -> config.helpers.enableDateCalculator,
								newValue -> config.helpers.enableDateCalculator = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				// Copy Underbid Price
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.helpers.enableCopyUnderbidPrice"))
						.description(Text.translatable("skyblocker.config.helpers.enableCopyUnderbidPrice.@Tooltip"))
						.binding(defaults.helpers.enableCopyUnderbidPrice,
								() -> config.helpers.enableCopyUnderbidPrice,
								newValue -> config.helpers.enableCopyUnderbidPrice = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				// Builder's Wand and Ruler Preview
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.helpers.enableBuildersWandPreview"))
						.binding(defaults.helpers.enableBuildersWandPreview,
								() -> config.helpers.enableBuildersWandPreview,
								newValue -> config.helpers.enableBuildersWandPreview = newValue)
						.controller(ConfigUtils.createBooleanController())
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
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				//Jerry Timer
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.helpers.jerry"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.jerry.enableJerryTimer"))
								.description(Text.translatable("skyblocker.config.helpers.jerry.enableJerryTimer.@Tooltip"))
								.binding(defaults.helpers.jerry.enableJerryTimer,
										() -> config.helpers.jerry.enableJerryTimer,
										newValue -> config.helpers.jerry.enableJerryTimer = newValue)
								.controller(ConfigUtils.createBooleanController())
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
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.experiments.enableSuperpairsSolver"))
								.binding(defaults.helpers.experiments.enableSuperpairsSolver,
										() -> config.helpers.experiments.enableSuperpairsSolver,
										newValue -> config.helpers.experiments.enableSuperpairsSolver = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.experiments.enableUltrasequencerSolver"))
								.binding(defaults.helpers.experiments.enableUltrasequencerSolver,
										() -> config.helpers.experiments.enableUltrasequencerSolver,
										newValue -> config.helpers.experiments.enableUltrasequencerSolver = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.experiments.blockIncorrectClicks"))
								.binding(defaults.helpers.experiments.blockIncorrectClicks,
										() -> config.helpers.experiments.blockIncorrectClicks,
										newValue -> config.helpers.experiments.blockIncorrectClicks = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				//Fishing Helper
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.helpers.fishing"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.fishing.enableFishingHelper"))
								.description(Text.translatable("skyblocker.config.helpers.fishing.enableFishingHelper.@Tooltip"))
								.binding(defaults.helpers.fishing.enableFishingHelper,
										() -> config.helpers.fishing.enableFishingHelper,
										newValue -> config.helpers.fishing.enableFishingHelper = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.fishing.hideOtherPlayers"))
								.description(Text.translatable("skyblocker.config.helpers.fishing.hideOtherPlayers.@Tooltip"))
								.binding(defaults.helpers.fishing.hideOtherPlayersRods,
										() -> config.helpers.fishing.hideOtherPlayersRods,
										newValue -> config.helpers.fishing.hideOtherPlayersRods = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.fishing.enableFishingHud"))
								.binding(defaults.helpers.fishing.enableFishingHud,
										() -> config.helpers.fishing.enableFishingHud,
										newValue -> config.helpers.fishing.enableFishingHud = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.fishing.hud.screen"))
								.prompt(Text.translatable("text.skyblocker.open"))
								.action(screen -> MinecraftClient.getInstance().setScreen(new WidgetsConfigurationScreen(Location.HUB, FishingHudWidget.getInstance().getInternalID(), screen)))
								.build())
						.option(Option.<HelperConfig.Fishing.FishingHookDisplay>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.fishing.fishingHookDisplay"))
								.description(Text.translatable("skyblocker.config.helpers.fishing.fishingHookDisplay.@Tooltip"))
								.binding(defaults.helpers.fishing.fishingHookDisplay,
										() -> config.helpers.fishing.fishingHookDisplay,
										newValue -> config.helpers.fishing.fishingHookDisplay = newValue)
								.controller(ConfigUtils.createEnumController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.fishing.enableFishingTimer"))
								.description(Text.translatable("skyblocker.config.helpers.fishing.enableFishingTimer.@Tooltip"))
								.binding(defaults.helpers.fishing.enableFishingTimer,
										() -> config.helpers.fishing.enableFishingTimer,
										newValue -> config.helpers.fishing.enableFishingTimer = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.fishing.hud.enableSeaCreatureCounter"))
								.description(Text.translatable("skyblocker.config.helpers.fishing.hud.enableSeaCreatureCounter.@Tooltip"))
								.binding(defaults.helpers.fishing.enableSeaCreatureCounter,
										() -> config.helpers.fishing.enableSeaCreatureCounter,
										newValue -> config.helpers.fishing.enableSeaCreatureCounter = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.fishing.hud.onlyShowHudInBarn"))
								.description(Text.translatable("skyblocker.config.helpers.fishing.hud.onlyShowHudInBarn.@Tooltip"))
								.binding(defaults.helpers.fishing.onlyShowHudInBarn,
										() -> config.helpers.fishing.onlyShowHudInBarn,
										newValue -> config.helpers.fishing.onlyShowHudInBarn = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.fishing.timerLength"))
								.description(Text.translatable("skyblocker.config.helpers.fishing.timerLength.@Tooltip"))
								.binding(defaults.helpers.fishing.timerLength,
										() -> config.helpers.fishing.timerLength,
										newValue -> config.helpers.fishing.timerLength = newValue)
								.controller(IntegerController.createBuilder().range(1, 360).slider(1).build())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.fishing.seaCreatureTimerNotification"))
								.description(Text.translatable("skyblocker.config.helpers.fishing.seaCreatureTimerNotification.@Tooltip"))
								.binding(defaults.helpers.fishing.seaCreatureTimerNotification,
										() -> config.helpers.fishing.seaCreatureTimerNotification,
										newValue -> config.helpers.fishing.seaCreatureTimerNotification = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.fishing.seaCreatureCap"))
								.description(Text.translatable("skyblocker.config.helpers.fishing.seaCreatureCap.@Tooltip"))
								.binding(defaults.helpers.fishing.seaCreatureCap,
										() -> config.helpers.fishing.seaCreatureCap,
										newValue -> config.helpers.fishing.seaCreatureCap = newValue)
								.controller(IntegerController.createBuilder().range(1, 60).slider(1).build())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.fishing.seaCreatureCapNotification"))
								.description(Text.translatable("skyblocker.config.helpers.fishing.seaCreatureCapNotification.@Tooltip"))
								.binding(defaults.helpers.fishing.seaCreatureCapNotification,
										() -> config.helpers.fishing.seaCreatureCapNotification,
										newValue -> config.helpers.fishing.seaCreatureCapNotification = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<SkyblockItemRarity>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.fishing.minimumNotificationRarity"))
								.description(Text.translatable("skyblocker.config.helpers.fishing.minimumNotificationRarity.@Tooltip"))
								.binding(defaults.helpers.fishing.minimumNotificationRarity,
										() -> config.helpers.fishing.minimumNotificationRarity,
										rarity -> config.helpers.fishing.minimumNotificationRarity = rarity == SkyblockItemRarity.DIVINE ? SkyblockItemRarity.UNKNOWN : rarity)
								.controller(ConfigUtils.createEnumController())
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
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.fairySouls.highlightFoundSouls"))
								.binding(defaults.helpers.fairySouls.highlightFoundSouls,
										() -> config.helpers.fairySouls.highlightFoundSouls,
										newValue -> config.helpers.fairySouls.highlightFoundSouls = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.fairySouls.highlightOnlyNearbySouls"))
								.description(Text.translatable("skyblocker.config.helpers.fairySouls.highlightOnlyNearbySouls.@Tooltip"))
								.binding(defaults.helpers.fairySouls.highlightOnlyNearbySouls,
										() -> config.helpers.fairySouls.highlightOnlyNearbySouls,
										newValue -> config.helpers.fairySouls.highlightOnlyNearbySouls = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				//Chocolate Factory
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.helpers.chocolateFactory"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.chocolateFactory.enableChocolateFactoryHelper"))
								.description(Text.translatable("skyblocker.config.helpers.chocolateFactory.enableChocolateFactoryHelper.@Tooltip"))
								.binding(defaults.helpers.chocolateFactory.enableChocolateFactoryHelper,
										() -> config.helpers.chocolateFactory.enableChocolateFactoryHelper,
										newValue -> config.helpers.chocolateFactory.enableChocolateFactoryHelper = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.chocolateFactory.enableTimeTowerReminder"))
								.description(Text.translatable("skyblocker.config.helpers.chocolateFactory.enableTimeTowerReminder.@Tooltip"))
								.binding(defaults.helpers.chocolateFactory.enableTimeTowerReminder,
										() -> config.helpers.chocolateFactory.enableTimeTowerReminder,
										newValue -> config.helpers.chocolateFactory.enableTimeTowerReminder = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.chocolateFactory.straySound"))
								.description(Text.translatable("skyblocker.config.helpers.chocolateFactory.straySound.@Tooltip"))
								.binding(defaults.helpers.chocolateFactory.straySound,
										() -> config.helpers.chocolateFactory.straySound,
										newValue -> config.helpers.chocolateFactory.straySound = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				// Hoppity's Hunt
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.helpers.hoppitysHunt"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.hoppitysHunt.enableEggFinder"))
								.description(Text.translatable("skyblocker.config.helpers.hoppitysHunt.enableEggFinder.@Tooltip"))
								.tags(CommonTags.ADDED_IN_5_9_0)
								.binding(defaults.helpers.chocolateFactory.enableEggFinder,
										() -> config.helpers.chocolateFactory.enableEggFinder,
										newValue -> config.helpers.chocolateFactory.enableEggFinder = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.hoppitysHunt.sendEggFoundMessages"))
								.description(Text.translatable("skyblocker.config.helpers.hoppitysHunt.sendEggFoundMessages.@Tooltip"))
								.tags(CommonTags.ADDED_IN_5_9_0)
								.binding(defaults.helpers.chocolateFactory.sendEggFoundMessages,
										() -> config.helpers.chocolateFactory.sendEggFoundMessages,
										newValue -> config.helpers.chocolateFactory.sendEggFoundMessages = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Waypoint.Type>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.hoppitysHunt.waypointType"))
								.description(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.waypointType.@Tooltip"))
								.tags(CommonTags.ADDED_IN_5_9_0)
								.binding(defaults.helpers.chocolateFactory.waypointType,
										() -> config.helpers.chocolateFactory.waypointType,
										newValue -> config.helpers.chocolateFactory.waypointType = newValue)
								.controller(ConfigUtils.createEnumController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.hoppitysHunt.showThroughWalls"))
								.description(Text.translatable("skyblocker.config.helpers.hoppitysHunt.showThroughWalls.@Tooltip"))
								.tags(CommonTags.ADDED_IN_5_9_0)
								.binding(defaults.helpers.chocolateFactory.showThroughWalls,
										() -> config.helpers.chocolateFactory.showThroughWalls,
										newValue -> config.helpers.chocolateFactory.showThroughWalls = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.helpers.carnival"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.carnival.catchAFishHelper"))
								.description(Text.translatable("skyblocker.config.helpers.carnival.catchAFishHelper.@Tooltip"))
								.binding(defaults.helpers.carnival.catchAFishHelper,
										() -> config.helpers.carnival.catchAFishHelper,
										newValue -> config.helpers.carnival.catchAFishHelper = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.carnival.zombieShootoutHelper"))
								.description(Text.translatable("skyblocker.config.helpers.carnival.zombieShootoutHelper.@Tooltip"))
								.binding(defaults.helpers.carnival.zombieShootoutHelper,
										() -> config.helpers.carnival.zombieShootoutHelper,
										newValue -> config.helpers.carnival.zombieShootoutHelper = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				//Bazaar
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.helpers.bazaar"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.bazaar.enableBazaarHelper"))
								.description(Text.translatable("skyblocker.config.helpers.bazaar.enableBazaarHelper.@Tooltip", BazaarHelper.getExpiringIcon(), BazaarHelper.getExpiredIcon(), BazaarHelper.getFilledIcon(69), BazaarHelper.getFilledIcon(100)))
								.binding(defaults.helpers.bazaar.enableBazaarHelper,
										() -> config.helpers.bazaar.enableBazaarHelper,
										newValue -> config.helpers.bazaar.enableBazaarHelper = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.bazaar.enableReorderHelper"))
								.description(Text.translatable("skyblocker.config.helpers.bazaar.enableReorderHelper.@Tooltip"))
								.binding(defaults.helpers.bazaar.enableReorderHelper,
										() -> config.helpers.bazaar.enableReorderHelper,
										newValue -> config.helpers.bazaar.enableReorderHelper = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.bazaar.enableOrderTracker"))
								.description(Text.translatable("skyblocker.config.helpers.bazaar.enableOrderTracker.@Tooltip"))
								.binding(defaults.helpers.bazaar.enableOrderTracker,
										() -> config.helpers.bazaar.enableOrderTracker,
										newValue -> config.helpers.bazaar.enableOrderTracker = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.itemPrice.enableItemPriceLookup"))
								.description(Text.translatable("skyblocker.config.helpers.itemPrice.enableItemPriceLookup.@Tooltip"))
								.binding(defaults.helpers.itemPrice.enableItemPriceLookup,
										() -> config.helpers.itemPrice.enableItemPriceLookup,
										newValue -> config.helpers.itemPrice.enableItemPriceLookup = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.itemPrice.enableItemPriceRefresh"))
								.description(Text.translatable("skyblocker.config.helpers.itemPrice.enableItemPriceRefresh.@Tooltip"))
								.binding(defaults.helpers.itemPrice.enableItemPriceRefresh,
										() -> config.helpers.itemPrice.enableItemPriceRefresh,
										newValue -> config.helpers.itemPrice.enableItemPriceRefresh = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(ConfigUtils.createShortcutToKeybindsScreen())
						.build())
				// Great Spook Event
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.helpers.greatSpook"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.helpers.greatSpook.enableMathTeacherHelper"))
								.description(Text.translatable("skyblocker.config.helpers.greatSpook.enableMathTeacherHelper.@Tooltip"))
								.binding(defaults.helpers.greatSpookEvent.enableMathTeacherHelper,
										() -> config.helpers.greatSpookEvent.enableMathTeacherHelper,
										newValue -> config.helpers.greatSpookEvent.enableMathTeacherHelper = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())
				.build();
	}
}
