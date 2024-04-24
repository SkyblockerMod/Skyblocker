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
				.name(Text.translatable("text.autoconfig.skyblocker.category.locations"))

				//Barn
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.barn"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.barn.solveHungryHiker"))
								.binding(defaults.locations.barn.solveHungryHiker,
										() -> config.locations.barn.solveHungryHiker,
										newValue -> config.locations.barn.solveHungryHiker = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.barn.solveTreasureHunter"))
								.binding(defaults.locations.barn.solveTreasureHunter,
										() -> config.locations.barn.solveTreasureHunter,
										newValue -> config.locations.barn.solveTreasureHunter = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//The Rift
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.rift"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.rift.mirrorverseWaypoints"))
								.binding(defaults.locations.rift.mirrorverseWaypoints,
										() -> config.locations.rift.mirrorverseWaypoints,
										newValue -> config.locations.rift.mirrorverseWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.rift.blobbercystGlow"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.rift.blobbercystGlow.@Tooltip")))
								.binding(defaults.locations.rift.blobbercystGlow,
										() -> config.locations.rift.blobbercystGlow,
										newValue -> config.locations.rift.blobbercystGlow = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.rift.enigmaSoulWaypoints"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.rift.enigmaSoulWaypoints.@Tooltip")))
								.binding(defaults.locations.rift.enigmaSoulWaypoints,
										() -> config.locations.rift.enigmaSoulWaypoints,
										newValue -> config.locations.rift.enigmaSoulWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.rift.highlightFoundEnigmaSouls"))
								.binding(defaults.locations.rift.highlightFoundEnigmaSouls,
										() -> config.locations.rift.highlightFoundEnigmaSouls,
										newValue -> config.locations.rift.highlightFoundEnigmaSouls = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.rift.mcGrubberStacks"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.rift.mcGrubberStacks.@Tooltip")))
								.binding(defaults.locations.rift.mcGrubberStacks,
										() -> config.locations.rift.mcGrubberStacks,
										newValue -> config.locations.rift.mcGrubberStacks = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 5).step(1))
								.build())
						.build())

				// The end
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.end"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.end.enableEnderNodeHelper"))
								.binding(defaults.locations.end.enableEnderNodeHelper,
										() -> config.locations.end.enableEnderNodeHelper,
										newValue -> config.locations.end.enableEnderNodeHelper = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.end.hudEnabled"))
								.binding(defaults.locations.end.hudEnabled,
										() -> config.locations.end.hudEnabled,
										newValue -> config.locations.end.hudEnabled = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.end.zealotKillsEnabled"))
								.binding(defaults.locations.end.zealotKillsEnabled,
										() -> config.locations.end.zealotKillsEnabled,
										newValue -> {
											config.locations.end.zealotKillsEnabled = newValue;
											EndHudWidget.INSTANCE.update();
										})
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.end.protectorLocationEnable"))
								.binding(defaults.locations.end.protectorLocationEnabled,
										() -> config.locations.end.protectorLocationEnabled,
										newValue -> {
											config.locations.end.protectorLocationEnabled = newValue;
											EndHudWidget.INSTANCE.update();
										})
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.end.waypoint"))
								.binding(defaults.locations.end.waypoint,
										() -> config.locations.end.waypoint,
										newValue -> config.locations.end.waypoint = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.end.screen"))
								.text(Text.translatable("text.skyblocker.open")) // Reusing again lol
								.action((screen, opt) -> MinecraftClient.getInstance().setScreen(new EndHudConfigScreen(screen)))
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.end.resetName"))
								.text(Text.translatable("text.autoconfig.skyblocker.option.locations.end.resetText"))
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
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.spidersDen"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.spidersDen.relics.enableRelicsHelper"))
								.binding(defaults.locations.spidersDen.relics.enableRelicsHelper,
										() -> config.locations.spidersDen.relics.enableRelicsHelper,
										newValue -> config.locations.spidersDen.relics.enableRelicsHelper = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.spidersDen.relics.highlightFoundRelics"))
								.binding(defaults.locations.spidersDen.relics.highlightFoundRelics,
										() -> config.locations.spidersDen.relics.highlightFoundRelics,
										newValue -> config.locations.spidersDen.relics.highlightFoundRelics = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())

				//Garden
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.garden"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.garden.farmingHud.enableHud"))
								.binding(defaults.locations.garden.farmingHud.enableHud,
										() -> config.locations.garden.farmingHud.enableHud,
										newValue -> config.locations.garden.farmingHud.enableHud = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.garden.farmingHud.config"))
								.text(Text.translatable("text.skyblocker.open"))
								.action((screen, opt) -> MinecraftClient.getInstance().setScreen(new FarmingHudConfigScreen(screen)))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.garden.dicerTitlePrevent"))
								.binding(defaults.locations.garden.dicerTitlePrevent,
										() -> config.locations.garden.dicerTitlePrevent,
										newValue -> config.locations.garden.dicerTitlePrevent = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.garden.visitorHelper"))
								.binding(defaults.locations.garden.visitorHelper,
										() -> config.locations.garden.visitorHelper,
										newValue -> config.locations.garden.visitorHelper = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.garden.lockMouseTool"))
								.binding(defaults.locations.garden.lockMouseTool,
										() -> config.locations.garden.lockMouseTool,
										newValue -> config.locations.garden.lockMouseTool = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.garden.lockMouseGround"))
								.binding(defaults.locations.garden.lockMouseGroundOnly,
										() -> config.locations.garden.lockMouseGroundOnly,
										newValue -> config.locations.garden.lockMouseGroundOnly = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.build())
				.build();
	}
}
