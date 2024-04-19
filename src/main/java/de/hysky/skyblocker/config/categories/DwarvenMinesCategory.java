package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.dwarven.CrystalsHudConfigScreen;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import de.hysky.skyblocker.skyblock.dwarven.DwarvenHudConfigScreen;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class DwarvenMinesCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines"))

				//Uncategorized Options
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.enableDrillFuel"))
						.binding(defaults.locations.dwarvenMines.enableDrillFuel,
								() -> config.locations.dwarvenMines.enableDrillFuel,
								newValue -> config.locations.dwarvenMines.enableDrillFuel = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.solveFetchur"))
						.binding(defaults.locations.dwarvenMines.solveFetchur,
								() -> config.locations.dwarvenMines.solveFetchur,
								newValue -> config.locations.dwarvenMines.solveFetchur = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.solvePuzzler"))
						.binding(defaults.locations.dwarvenMines.solvePuzzler,
								() -> config.locations.dwarvenMines.solvePuzzler,
								newValue -> config.locations.dwarvenMines.solvePuzzler = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.metalDetectorHelper"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.metalDetectorHelper.@Tooltip")))						.binding(defaults.locations.dwarvenMines.metalDetectorHelper,
								() -> config.locations.dwarvenMines.metalDetectorHelper,
								newValue -> config.locations.dwarvenMines.metalDetectorHelper = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())

				//Dwarven HUD
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.dwarvenHud"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.dwarvenHud.enabledCommissions"))
								.binding(defaults.locations.dwarvenMines.dwarvenHud.enabledCommissions,
										() -> config.locations.dwarvenMines.dwarvenHud.enabledCommissions,
										newValue -> config.locations.dwarvenMines.dwarvenHud.enabledCommissions = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.dwarvenHud.enabledPowder"))
								.binding(defaults.locations.dwarvenMines.dwarvenHud.enabledPowder,
										() -> config.locations.dwarvenMines.dwarvenHud.enabledPowder,
										newValue -> config.locations.dwarvenMines.dwarvenHud.enabledPowder = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<SkyblockerConfig.DwarvenHudStyle>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.dwarvenHud.style"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.dwarvenHud.style.@Tooltip[0]"),
										Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.dwarvenHud.style.@Tooltip[1]"),
										Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.dwarvenHud.style.@Tooltip[2]")))
								.binding(defaults.locations.dwarvenMines.dwarvenHud.style,
										() -> config.locations.dwarvenMines.dwarvenHud.style,
										newValue -> config.locations.dwarvenMines.dwarvenHud.style = newValue)
								.controller(ConfigUtils::createEnumCyclingListController)
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.dwarvenHud.screen"))
								.text(Text.translatable("text.skyblocker.open"))
								.action((screen, opt) -> MinecraftClient.getInstance().setScreen(new DwarvenHudConfigScreen(screen)))
								.build())
						.build())
				//crystal HUD
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.crystalsHud"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.crystalsHud.enabled"))
								.binding(defaults.locations.dwarvenMines.crystalsHud.enabled,
										() -> config.locations.dwarvenMines.crystalsHud.enabled,
										newValue -> config.locations.dwarvenMines.crystalsHud.enabled = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.crystalsHud.screen"))
								.text(Text.translatable("text.skyblocker.open"))
								.action((screen, opt) -> MinecraftClient.getInstance().setScreen(new CrystalsHudConfigScreen(screen)))
								.build())
						.option(Option.<Float>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.crystalsHud.mapScaling"))
								.binding(defaults.locations.dwarvenMines.crystalsHud.mapScaling,
										() -> config.locations.dwarvenMines.crystalsHud.mapScaling,
										newValue -> config.locations.dwarvenMines.crystalsHud.mapScaling = newValue)
								.controller(FloatFieldControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.crystalsHud.showLocations"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.crystalsHud.showLocations.@Tooltip")))
								.binding(defaults.locations.dwarvenMines.crystalsHud.showLocations,
										() -> config.locations.dwarvenMines.crystalsHud.showLocations,
										newValue -> config.locations.dwarvenMines.crystalsHud.showLocations = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.crystalsHud.showLocations.locationSize"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.crystalsHud.showLocations.locationSize.@Tooltip")))
								.binding(defaults.locations.dwarvenMines.crystalsHud.locationSize,
										() -> config.locations.dwarvenMines.crystalsHud.locationSize,
										newValue -> config.locations.dwarvenMines.crystalsHud.locationSize = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(4, 12).step(2))
								.build())
						.build())
				//crystals waypoints
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.crystalsWaypoints"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.crystalsWaypoints.enabled"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.crystalsWaypoints.enabled.@Tooltip")))
								.binding(defaults.locations.dwarvenMines.crystalsWaypoints.enabled,
										() -> config.locations.dwarvenMines.crystalsWaypoints.enabled,
										newValue -> config.locations.dwarvenMines.crystalsWaypoints.enabled = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.crystalsWaypoints.findInChat"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.crystalsWaypoints.findInChat.@Tooltip")))
								.binding(defaults.locations.dwarvenMines.crystalsWaypoints.findInChat,
										() -> config.locations.dwarvenMines.crystalsWaypoints.findInChat,
										newValue -> config.locations.dwarvenMines.crystalsWaypoints.findInChat = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())

						.build())
				.build();
	}
}
