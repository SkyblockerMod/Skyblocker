package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.configs.MiningConfig;
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
				.name(Text.translatable("skyblocker.option.locations.dwarvenMines"))

				//Uncategorized Options
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.option.locations.dwarvenMines.enableDrillFuel"))
						.binding(defaults.mining.enableDrillFuel,
								() -> config.mining.enableDrillFuel,
								newValue -> config.mining.enableDrillFuel = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.option.locations.dwarvenMines.solveFetchur"))
						.binding(defaults.mining.dwarvenMines.solveFetchur,
								() -> config.mining.dwarvenMines.solveFetchur,
								newValue -> config.mining.dwarvenMines.solveFetchur = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.option.locations.dwarvenMines.solvePuzzler"))
						.binding(defaults.mining.dwarvenMines.solvePuzzler,
								() -> config.mining.dwarvenMines.solvePuzzler,
								newValue -> config.mining.dwarvenMines.solvePuzzler = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.option.locations.dwarvenMines.metalDetectorHelper"))
						.description(OptionDescription.of(Text.translatable("skyblocker.option.locations.dwarvenMines.metalDetectorHelper.@Tooltip")))
						.binding(defaults.mining.crystalHollows.metalDetectorHelper,
								() -> config.mining.crystalHollows.metalDetectorHelper,
								newValue -> config.mining.crystalHollows.metalDetectorHelper = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())

				//Dwarven HUD
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.locations.dwarvenMines.dwarvenHud"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.dwarvenMines.dwarvenHud.enabledCommissions"))
								.binding(defaults.mining.dwarvenHud.enabledCommissions,
										() -> config.mining.dwarvenHud.enabledCommissions,
										newValue -> config.mining.dwarvenHud.enabledCommissions = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.dwarvenMines.dwarvenHud.enabledPowder"))
								.binding(defaults.mining.dwarvenHud.enabledPowder,
										() -> config.mining.dwarvenHud.enabledPowder,
										newValue -> config.mining.dwarvenHud.enabledPowder = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<MiningConfig.DwarvenHudStyle>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.dwarvenMines.dwarvenHud.style"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.locations.dwarvenMines.dwarvenHud.style.@Tooltip[0]"),
										Text.translatable("skyblocker.option.locations.dwarvenMines.dwarvenHud.style.@Tooltip[1]"),
										Text.translatable("skyblocker.option.locations.dwarvenMines.dwarvenHud.style.@Tooltip[2]")))
								.binding(defaults.mining.dwarvenHud.style,
										() -> config.mining.dwarvenHud.style,
										newValue -> config.mining.dwarvenHud.style = newValue)
								.controller(ConfigUtils::createEnumCyclingListController)
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("skyblocker.option.locations.dwarvenMines.dwarvenHud.screen"))
								.text(Text.translatable("text.skyblocker.open"))
								.action((screen, opt) -> MinecraftClient.getInstance().setScreen(new DwarvenHudConfigScreen(screen)))
								.build())
						.build())
				//crystal HUD
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.locations.dwarvenMines.crystalsHud"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.dwarvenMines.crystalsHud.enabled"))
								.binding(defaults.mining.crystalsHud.enabled,
										() -> config.mining.crystalsHud.enabled,
										newValue -> config.mining.crystalsHud.enabled = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("skyblocker.option.locations.dwarvenMines.crystalsHud.screen"))
								.text(Text.translatable("text.skyblocker.open"))
								.action((screen, opt) -> MinecraftClient.getInstance().setScreen(new CrystalsHudConfigScreen(screen)))
								.build())
						.option(Option.<Float>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.dwarvenMines.crystalsHud.mapScaling"))
								.binding(defaults.mining.crystalsHud.mapScaling,
										() -> config.mining.crystalsHud.mapScaling,
										newValue -> config.mining.crystalsHud.mapScaling = newValue)
								.controller(FloatFieldControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.dwarvenMines.crystalsHud.showLocations"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.locations.dwarvenMines.crystalsHud.showLocations.@Tooltip")))
								.binding(defaults.mining.crystalsHud.showLocations,
										() -> config.mining.crystalsHud.showLocations,
										newValue -> config.mining.crystalsHud.showLocations = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.dwarvenMines.crystalsHud.showLocations.locationSize"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.locations.dwarvenMines.crystalsHud.showLocations.locationSize.@Tooltip")))
								.binding(defaults.mining.crystalsHud.locationSize,
										() -> config.mining.crystalsHud.locationSize,
										newValue -> config.mining.crystalsHud.locationSize = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(4, 12).step(2))
								.build())
						.build())
				//crystals waypoints
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.option.locations.dwarvenMines.crystalsWaypoints"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.dwarvenMines.crystalsWaypoints.enabled"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.locations.dwarvenMines.crystalsWaypoints.enabled.@Tooltip")))
								.binding(defaults.mining.crystalsWaypoints.enabled,
										() -> config.mining.crystalsWaypoints.enabled,
										newValue -> config.mining.crystalsWaypoints.enabled = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.option.locations.dwarvenMines.crystalsWaypoints.findInChat"))
								.description(OptionDescription.of(Text.translatable("skyblocker.option.locations.dwarvenMines.crystalsWaypoints.findInChat.@Tooltip")))
								.binding(defaults.mining.crystalsWaypoints.findInChat,
										() -> config.mining.crystalsWaypoints.findInChat,
										newValue -> config.mining.crystalsWaypoints.findInChat = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())

						.build())
				.build();
	}
}
