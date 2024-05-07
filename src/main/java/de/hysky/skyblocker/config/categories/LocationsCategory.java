package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.end.EndHudConfigScreen;
import de.hysky.skyblocker.skyblock.end.EndHudWidget;
import de.hysky.skyblocker.skyblock.end.TheEnd;
import de.hysky.skyblocker.skyblock.garden.FarmingHudConfigScreen;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class LocationsCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.name(Text.translatable("skyblocker.category.locations"))

				//Barn
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.locations.barn"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.barn.solveHungryHiker"))
								.binding(defaults.otherLocations.barn.solveHungryHiker,
										() -> config.otherLocations.barn.solveHungryHiker,
										newValue -> config.otherLocations.barn.solveHungryHiker = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.barn.solveTreasureHunter"))
								.binding(defaults.otherLocations.barn.solveTreasureHunter,
										() -> config.otherLocations.barn.solveTreasureHunter,
										newValue -> config.otherLocations.barn.solveTreasureHunter = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//The Rift
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.locations.rift"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.rift.mirrorverseWaypoints"))
								.binding(defaults.otherLocations.rift.mirrorverseWaypoints,
										() -> config.otherLocations.rift.mirrorverseWaypoints,
										newValue -> config.otherLocations.rift.mirrorverseWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.rift.blobbercystGlow"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.locations.rift.blobbercystGlow.@Tooltip")))
								.binding(defaults.otherLocations.rift.blobbercystGlow,
										() -> config.otherLocations.rift.blobbercystGlow,
										newValue -> config.otherLocations.rift.blobbercystGlow = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.rift.enigmaSoulWaypoints"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.locations.rift.enigmaSoulWaypoints.@Tooltip")))
								.binding(defaults.otherLocations.rift.enigmaSoulWaypoints,
										() -> config.otherLocations.rift.enigmaSoulWaypoints,
										newValue -> config.otherLocations.rift.enigmaSoulWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.rift.highlightFoundEnigmaSouls"))
								.binding(defaults.otherLocations.rift.highlightFoundEnigmaSouls,
										() -> config.otherLocations.rift.highlightFoundEnigmaSouls,
										newValue -> config.otherLocations.rift.highlightFoundEnigmaSouls = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.rift.mcGrubberStacks"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.locations.rift.mcGrubberStacks.@Tooltip")))
								.binding(defaults.otherLocations.rift.mcGrubberStacks,
										() -> config.otherLocations.rift.mcGrubberStacks,
										newValue -> config.otherLocations.rift.mcGrubberStacks = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 5).step(1))
								.build())
						.build())

				// The end
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.locations.end"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.end.enableEnderNodeHelper"))
								.binding(defaults.otherLocations.end.enableEnderNodeHelper,
										() -> config.otherLocations.end.enableEnderNodeHelper,
										newValue -> config.otherLocations.end.enableEnderNodeHelper = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.end.hudEnabled"))
								.binding(defaults.otherLocations.end.hudEnabled,
										() -> config.otherLocations.end.hudEnabled,
										newValue -> config.otherLocations.end.hudEnabled = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.end.zealotKillsEnabled"))
								.binding(defaults.otherLocations.end.zealotKillsEnabled,
										() -> config.otherLocations.end.zealotKillsEnabled,
										newValue -> {
											config.otherLocations.end.zealotKillsEnabled = newValue;
											EndHudWidget.INSTANCE.update();
										})
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.end.protectorLocationEnable"))
								.binding(defaults.otherLocations.end.protectorLocationEnabled,
										() -> config.otherLocations.end.protectorLocationEnabled,
										newValue -> {
											config.otherLocations.end.protectorLocationEnabled = newValue;
											EndHudWidget.INSTANCE.update();
										})
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.end.waypoint"))
								.binding(defaults.otherLocations.end.waypoint,
										() -> config.otherLocations.end.waypoint,
										newValue -> config.otherLocations.end.waypoint = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("skyblocker.option.locations.end.screen"))
								.text(Text.translatable("text.skyblocker.open")) // Reusing again lol
								.action((screen, opt) -> MinecraftClient.getInstance().setScreen(new EndHudConfigScreen(screen)))
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("skyblocker.option.locations.end.resetName"))
								.text(Text.translatable("skyblocker.option.locations.end.resetText"))
								.action((screen, opt) -> {
									TheEnd.zealotsKilled = 0;
									TheEnd.zealotsSinceLastEye = 0;
									TheEnd.eyes = 0;
								})
								.build())
						.build()

				)

				//Spider's Den
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.locations.spidersDen"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.spidersDen.relics.enableRelicsHelper"))
								.binding(defaults.otherLocations.spidersDen.relics.enableRelicsHelper,
										() -> config.otherLocations.spidersDen.relics.enableRelicsHelper,
										newValue -> config.otherLocations.spidersDen.relics.enableRelicsHelper = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.spidersDen.relics.highlightFoundRelics"))
								.binding(defaults.otherLocations.spidersDen.relics.highlightFoundRelics,
										() -> config.otherLocations.spidersDen.relics.highlightFoundRelics,
										newValue -> config.otherLocations.spidersDen.relics.highlightFoundRelics = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Garden
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.locations.garden"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.garden.farmingHud.enableHud"))
								.binding(defaults.farming.garden.farmingHud.enableHud,
										() -> config.farming.garden.farmingHud.enableHud,
										newValue -> config.farming.garden.farmingHud.enableHud = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("skyblocker.option.locations.garden.farmingHud.config"))
								.text(Text.translatable("text.skyblocker.open"))
								.action((screen, opt) -> MinecraftClient.getInstance().setScreen(new FarmingHudConfigScreen(screen)))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.garden.dicerTitlePrevent"))
								.binding(defaults.farming.garden.dicerTitlePrevent,
										() -> config.farming.garden.dicerTitlePrevent,
										newValue -> config.farming.garden.dicerTitlePrevent = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.garden.visitorHelper"))
								.binding(defaults.farming.garden.visitorHelper,
										() -> config.farming.garden.visitorHelper,
										newValue -> config.farming.garden.visitorHelper = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.garden.lockMouseTool"))
								.binding(defaults.farming.garden.lockMouseTool,
										() -> config.farming.garden.lockMouseTool,
										newValue -> config.farming.garden.lockMouseTool = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.garden.lockMouseGround"))
								.binding(defaults.farming.garden.lockMouseGroundOnly,
										() -> config.farming.garden.lockMouseGroundOnly,
										newValue -> config.farming.garden.lockMouseGroundOnly = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())
				.build();
	}
}
