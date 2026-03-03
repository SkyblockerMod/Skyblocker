package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.azureaaron.dandelion.api.ConfigCategory;
import net.azureaaron.dandelion.api.Option;
import net.azureaaron.dandelion.api.OptionGroup;
import net.azureaaron.dandelion.api.controllers.IntegerController;
import net.minecraft.network.chat.Component;

public class CrimsonIsleCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.id(SkyblockerMod.id("config/crimsonisle"))
				.name(Component.translatable("skyblocker.config.crimsonIsle"))

				//Kuudra
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.crimsonIsle.kuudra"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.crimsonIsle.kuudra.supplyWaypoints"))
								.binding(defaults.crimsonIsle.kuudra.supplyWaypoints,
										() -> config.crimsonIsle.kuudra.supplyWaypoints,
										newValue -> config.crimsonIsle.kuudra.supplyWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.crimsonIsle.kuudra.fuelWaypoints"))
								.binding(defaults.crimsonIsle.kuudra.fuelWaypoints,
										() -> config.crimsonIsle.kuudra.fuelWaypoints,
										newValue -> config.crimsonIsle.kuudra.fuelWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Waypoint.Type>createBuilder()
								.name(Component.translatable("skyblocker.config.crimsonIsle.kuudra.suppliesAndFuelWaypointType"))
								.description(Component.translatable("skyblocker.config.uiAndVisuals.waypoints.waypointType.@Tooltip"))
								.binding(defaults.crimsonIsle.kuudra.suppliesAndFuelWaypointType,
										() -> config.crimsonIsle.kuudra.suppliesAndFuelWaypointType,
										newValue -> config.crimsonIsle.kuudra.suppliesAndFuelWaypointType = newValue)
								.controller(ConfigUtils.createEnumController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.crimsonIsle.kuudra.ballistaBuildWaypoints"))
								.binding(defaults.crimsonIsle.kuudra.ballistaBuildWaypoints,
										() -> config.crimsonIsle.kuudra.ballistaBuildWaypoints,
										newValue -> config.crimsonIsle.kuudra.ballistaBuildWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.crimsonIsle.kuudra.safeSpotWaypoints"))
								.binding(defaults.crimsonIsle.kuudra.safeSpotWaypoints,
										() -> config.crimsonIsle.kuudra.safeSpotWaypoints,
										newValue -> config.crimsonIsle.kuudra.safeSpotWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.crimsonIsle.kuudra.pearlWaypoints"))
								.binding(defaults.crimsonIsle.kuudra.pearlWaypoints,
										() -> config.crimsonIsle.kuudra.pearlWaypoints,
										newValue -> config.crimsonIsle.kuudra.pearlWaypoints = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.crimsonIsle.kuudra.noArrowPoisonWarning"))
								.description(Component.translatable("skyblocker.config.crimsonIsle.kuudra.noArrowPoisonWarning.@Tooltip"))
								.binding(defaults.crimsonIsle.kuudra.noArrowPoisonWarning,
										() -> config.crimsonIsle.kuudra.noArrowPoisonWarning,
										newValue -> config.crimsonIsle.kuudra.noArrowPoisonWarning = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.translatable("skyblocker.config.crimsonIsle.kuudra.arrowPoisonThreshold"))
								.description(Component.translatable("skyblocker.config.crimsonIsle.kuudra.arrowPoisonThreshold.@Tooltip"))
								.binding(defaults.crimsonIsle.kuudra.arrowPoisonThreshold,
										() -> config.crimsonIsle.kuudra.arrowPoisonThreshold,
										newValue -> config.crimsonIsle.kuudra.arrowPoisonThreshold = newValue)
								.controller(IntegerController.createBuilder().build())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.crimsonIsle.kuudra.kuudraGlow"))
								.description(Component.translatable("skyblocker.config.crimsonIsle.kuudra.kuudraGlow.@Tooltip"))
								.binding(defaults.crimsonIsle.kuudra.kuudraGlow,
										() -> config.crimsonIsle.kuudra.kuudraGlow,
										newValue -> config.crimsonIsle.kuudra.kuudraGlow = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.crimsonIsle.kuudra.dangerWarning"))
								.description(Component.translatable("skyblocker.config.crimsonIsle.kuudra.dangerWarning.@Tooltip"))
								.binding(defaults.crimsonIsle.kuudra.dangerWarning,
										() -> config.crimsonIsle.kuudra.dangerWarning,
										newValue -> config.crimsonIsle.kuudra.dangerWarning = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())
				//dojo
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.crimson.dojo"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.crimson.dojo.forceHelper"))
								.description(Component.translatable("skyblocker.crimson.dojo.forceHelper.@Tooltip"))
								.binding(config.crimsonIsle.dojo.enableForceHelper,
										() -> config.crimsonIsle.dojo.enableForceHelper,
										newValue -> config.crimsonIsle.dojo.enableForceHelper = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.crimson.dojo.staminaHelper"))
								.description(Component.translatable("skyblocker.crimson.dojo.staminaHelper.@Tooltip"))
								.binding(config.crimsonIsle.dojo.enableStaminaHelper,
										() -> config.crimsonIsle.dojo.enableStaminaHelper,
										newValue -> config.crimsonIsle.dojo.enableStaminaHelper = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.crimson.dojo.masteryHelper"))
								.description(Component.translatable("skyblocker.crimson.dojo.masteryHelper.@Tooltip"))
								.binding(config.crimsonIsle.dojo.enableMasteryHelper,
										() -> config.crimsonIsle.dojo.enableMasteryHelper,
										newValue -> config.crimsonIsle.dojo.enableMasteryHelper = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.crimson.dojo.disciplineHelper"))
								.description(Component.translatable("skyblocker.crimson.dojo.disciplineHelper.@Tooltip"))
								.binding(config.crimsonIsle.dojo.enableDisciplineHelper,
										() -> config.crimsonIsle.dojo.enableDisciplineHelper,
										newValue -> config.crimsonIsle.dojo.enableDisciplineHelper = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.crimson.dojo.swiftnessHelper"))
								.description(Component.translatable("skyblocker.crimson.dojo.swiftnessHelper.@Tooltip"))
								.binding(config.crimsonIsle.dojo.enableSwiftnessHelper,
										() -> config.crimsonIsle.dojo.enableSwiftnessHelper,
										newValue -> config.crimsonIsle.dojo.enableSwiftnessHelper = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.crimson.dojo.controlHelper"))
								.description(Component.translatable("skyblocker.crimson.dojo.controlHelper.@Tooltip"))
								.binding(config.crimsonIsle.dojo.enableControlHelper,
										() -> config.crimsonIsle.dojo.enableControlHelper,
										newValue -> config.crimsonIsle.dojo.enableControlHelper = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.crimson.dojo.tenacityHelper"))
								.description(Component.translatable("skyblocker.crimson.dojo.tenacityHelper.@Tooltip"))
								.binding(config.crimsonIsle.dojo.enableTenacityHelper,
										() -> config.crimsonIsle.dojo.enableTenacityHelper,
										newValue -> config.crimsonIsle.dojo.enableTenacityHelper = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				// Extend nether fog
				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("skyblocker.config.crimsonIsle.extendNetherFog"))
						.description(Component.translatable("skyblocker.config.crimsonIsle.extendNetherFog.@Tooltip"))
						.binding(config.crimsonIsle.extendNetherFog,
								() -> config.crimsonIsle.extendNetherFog,
								newValue -> config.crimsonIsle.extendNetherFog = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())

				.build();
	}
}
