package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import net.minecraft.text.Text;

public class CrimsonIsleCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.name(Text.translatable("text.autoconfig.skyblocker.category.crimsonIsle"))

				//Kuudra
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.crimsonIsle.kuudra"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.crimsonIsle.kuudra.supplyWaypoints"))
								.binding(defaults.locations.crimsonIsle.kuudra.supplyWaypoints,
										() -> config.locations.crimsonIsle.kuudra.supplyWaypoints,
										newValue -> config.locations.crimsonIsle.kuudra.supplyWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.crimsonIsle.kuudra.supplyPileWaypoints"))
								.binding(defaults.locations.crimsonIsle.kuudra.supplyPileWaypoints,
										() -> config.locations.crimsonIsle.kuudra.supplyPileWaypoints,
										newValue -> config.locations.crimsonIsle.kuudra.supplyPileWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.crimsonIsle.kuudra.fuelWaypoints"))
								.binding(defaults.locations.crimsonIsle.kuudra.fuelWaypoints,
										() -> config.locations.crimsonIsle.kuudra.fuelWaypoints,
										newValue -> config.locations.crimsonIsle.kuudra.fuelWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.crimsonIsle.kuudra.safeSpotWaypoints"))
								.binding(defaults.locations.crimsonIsle.kuudra.safeSpotWaypoints,
										() -> config.locations.crimsonIsle.kuudra.safeSpotWaypoints,
										newValue -> config.locations.crimsonIsle.kuudra.safeSpotWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.crimsonIsle.kuudra.pearlWaypoints"))
								.binding(defaults.locations.crimsonIsle.kuudra.pearlWaypoints,
										() -> config.locations.crimsonIsle.kuudra.pearlWaypoints,
										newValue -> config.locations.crimsonIsle.kuudra.pearlWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Waypoint.Type>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.general.waypoints.waypointType"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.general.waypoints.waypointType.@Tooltip")))
								.binding(defaults.locations.crimsonIsle.kuudra.waypointType,
										() -> config.locations.crimsonIsle.kuudra.waypointType,
										newValue -> config.locations.crimsonIsle.kuudra.waypointType = newValue)
								.controller(ConfigUtils::createEnumCyclingListController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.crimsonIsle.kuudra.noArrowPoisonWarning"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.crimsonIsle.kuudra.noArrowPoisonWarning.@Tooltip")))
								.binding(defaults.locations.crimsonIsle.kuudra.noArrowPoisonWarning,
										() -> config.locations.crimsonIsle.kuudra.noArrowPoisonWarning,
										newValue -> config.locations.crimsonIsle.kuudra.noArrowPoisonWarning = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.crimsonIsle.kuudra.arrowPoisonThreshold"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.crimsonIsle.kuudra.arrowPoisonThreshold.@Tooltip")))
								.binding(defaults.locations.crimsonIsle.kuudra.arrowPoisonThreshold,
										() -> config.locations.crimsonIsle.kuudra.arrowPoisonThreshold,
										newValue -> config.locations.crimsonIsle.kuudra.arrowPoisonThreshold = newValue)
								.controller(IntegerFieldControllerBuilder::create)
								.build())
						.build())
				.build();
	}
}
