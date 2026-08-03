package de.hysky.skyblocker.config.categories;

import java.awt.Color;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.hunting.LassoHud;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.utils.Location;
import net.azureaaron.dandelion.api.ButtonOption;
import net.azureaaron.dandelion.api.ConfigCategory;
import net.azureaaron.dandelion.api.Option;
import net.azureaaron.dandelion.api.OptionGroup;
import net.azureaaron.dandelion.api.controllers.ColourController;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class HuntingCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.id(SkyblockerMod.id("config/hunting"))
				.name(Component.translatable("skyblocker.config.hunting"))
				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("skyblocker.config.hunting.huntingBoxHelper"))
						.binding(defaults.hunting.huntingBox.enabled,
								() -> config.hunting.huntingBox.enabled,
								value -> config.hunting.huntingBox.enabled = value)
						.controller(ConfigUtils.createBooleanController())
						.description(Component.translatable("skyblocker.config.hunting.huntingBoxHelper.@Tooltip"))
						.build())

				// Moonglade Mob Features
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.hunting.huntingMobs"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.huntingMobs.silencePhantoms"))
								.description(Component.translatable("skyblocker.config.hunting.huntingMobs.silencePhantoms.@Tooltip"))
								.binding(defaults.hunting.huntingMobs.silencePhantoms,
										() -> config.hunting.huntingMobs.silencePhantoms,
										newValue -> config.hunting.huntingMobs.silencePhantoms = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.huntingMobs.highlightHideonleaf"))
								.description(Component.translatable("skyblocker.config.hunting.huntingMobs.highlightHideonleaf.@Tooltip"))
								.binding(defaults.hunting.huntingMobs.highlightHideonleaf,
										() -> config.hunting.huntingMobs.highlightHideonleaf,
										newValue -> config.hunting.huntingMobs.highlightHideonleaf = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.huntingMobs.colorPicker.Hideonleaf"))
								.binding(defaults.hunting.huntingMobs.hideonleafGlowColor,
										() -> config.hunting.huntingMobs.hideonleafGlowColor,
										newValue -> {
											config.hunting.huntingMobs.hideonleafGlowColor = newValue;
										})
								.controller(ColourController.createBuilder().hasAlpha(false).build())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.huntingMobs.highlightShellwise"))
								.description(Component.translatable("skyblocker.config.hunting.huntingMobs.highlightShellwise.@Tooltip"))
								.binding(defaults.hunting.huntingMobs.highlightShellwise,
										() -> config.hunting.huntingMobs.highlightShellwise,
										newValue -> config.hunting.huntingMobs.highlightShellwise = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.huntingMobs.colorPicker.Shellwise"))
								.binding(defaults.hunting.huntingMobs.shellwiseGlowColor,
										() -> config.hunting.huntingMobs.shellwiseGlowColor,
										newValue -> {
											config.hunting.huntingMobs.shellwiseGlowColor = newValue;
										})
								.controller(ColourController.createBuilder().hasAlpha(false).build())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.huntingMobs.highlightCoralot"))
								.description(Component.translatable("skyblocker.config.hunting.huntingMobs.highlightCoralot.@Tooltip"))
								.binding(defaults.hunting.huntingMobs.highlightCoralot,
										() -> config.hunting.huntingMobs.highlightCoralot,
										newValue -> config.hunting.huntingMobs.highlightCoralot = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.huntingMobs.colorPicker.Coralot"))
								.binding(defaults.hunting.huntingMobs.coralotGlowColor,
										() -> config.hunting.huntingMobs.coralotGlowColor,
										newValue -> {
											config.hunting.huntingMobs.coralotGlowColor = newValue;
										})
								.controller(ColourController.createBuilder().hasAlpha(false).build())
								.build())
						.build())

				// Torrhus Mobs
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.hunting.torrhusMobs"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.torrhusMobs.highlightHideonsun"))
								.description(Component.translatable("skyblocker.config.hunting.torrhusMobs.highlightHideonsun.@Tooltip"))
								.binding(defaults.hunting.torrhusMobs.highlightHideonsun,
										() -> config.hunting.torrhusMobs.highlightHideonsun,
										newValue -> config.hunting.torrhusMobs.highlightHideonsun = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.torrhusMobs.hideonsunHighlightColor"))
								.binding(defaults.hunting.torrhusMobs.hideonsunHighlightColor,
										() -> config.hunting.torrhusMobs.hideonsunHighlightColor,
										newValue -> config.hunting.torrhusMobs.hideonsunHighlightColor = newValue)
								.controller(ColourController.createBuilder().hasAlpha(false).build())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.torrhusMobs.highlightBlueJay"))
								.description(Component.translatable("skyblocker.config.hunting.torrhusMobs.highlightBlueJay.@Tooltip"))
								.binding(defaults.hunting.torrhusMobs.highlightBlueJay,
										() -> config.hunting.torrhusMobs.highlightBlueJay,
										newValue -> config.hunting.torrhusMobs.highlightBlueJay = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.torrhusMobs.blueJayHighlightColor"))
								.binding(defaults.hunting.torrhusMobs.blueJayHighlightColor,
										() -> config.hunting.torrhusMobs.blueJayHighlightColor,
										newValue -> config.hunting.torrhusMobs.blueJayHighlightColor = newValue)
								.controller(ColourController.createBuilder().hasAlpha(false).build())
								.build())
						.build())

				// Safari
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.hunting.safari"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.safari.silencePhantoms"))
								.description(Component.translatable("skyblocker.config.hunting.safari.silencePhantoms.@Tooltip"))
								.binding(defaults.hunting.safari.silencePhantoms,
										() -> config.hunting.safari.silencePhantoms,
										newValue -> config.hunting.safari.silencePhantoms = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				// Haunted Biome
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.hunting.hauntedBiome"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.hauntedBiome.highlightDuplico"))
								.description(Component.translatable("skyblocker.config.hunting.hauntedBiome.highlightDuplico.@Tooltip"))
								.binding(defaults.hunting.hauntedBiome.highlightDuplico,
										() -> config.hunting.hauntedBiome.highlightDuplico,
										newValue -> config.hunting.hauntedBiome.highlightDuplico = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.hauntedBiome.duplicoHighlightColor"))
								.binding(defaults.hunting.hauntedBiome.duplicoHighlightColor,
										() -> config.hunting.hauntedBiome.duplicoHighlightColor,
										newValue -> config.hunting.hauntedBiome.duplicoHighlightColor = newValue)
								.controller(ColourController.createBuilder().hasAlpha(false).build())
								.build())
						.build())

				// Forest Biome
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.hunting.forestBiome"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.forestBiome.highlightHideonfloor"))
								.description(Component.translatable("skyblocker.config.hunting.forestBiome.highlightHideonfloor.@Tooltip"))
								.binding(defaults.hunting.forestBiome.highlightHideonfloor,
										() -> config.hunting.forestBiome.highlightHideonfloor,
										newValue -> config.hunting.forestBiome.highlightHideonfloor = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.forestBiome.hideonfloorHighlightColor"))
								.binding(defaults.hunting.forestBiome.hideonfloorHighlightColor,
										() -> config.hunting.forestBiome.hideonfloorHighlightColor,
										newValue -> config.hunting.forestBiome.hideonfloorHighlightColor = newValue)
								.controller(ColourController.createBuilder().hasAlpha(false).build())
								.build())
						.build())

				// Cavern Biome
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.hunting.cavernBiome"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.cavernBiome.highlightRockmiteMounds"))
								.description(Component.translatable("skyblocker.config.hunting.cavernBiome.highlightRockmiteMounds.@Tooltip"))
								.binding(defaults.hunting.cavernBiome.highlightRockmiteMounds,
										() -> config.hunting.cavernBiome.highlightRockmiteMounds,
										newValue -> config.hunting.cavernBiome.highlightRockmiteMounds = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.cavernBiome.rockmiteMoundHighlightColor"))
								.binding(defaults.hunting.cavernBiome.rockmiteMoundHighlightColor,
										() -> config.hunting.cavernBiome.rockmiteMoundHighlightColor,
										newValue -> config.hunting.cavernBiome.rockmiteMoundHighlightColor = newValue)
								.controller(ColourController.createBuilder().hasAlpha(false).build())
								.build())
						.build())

				// Icy Biome
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.hunting.icyBiome"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.icyBiome.hideColdOverlay"))
								.description(Component.translatable("skyblocker.config.hunting.icyBiome.hideColdOverlay.@Tooltip"))
								.binding(defaults.hunting.icyBiome.hideColdOverlay,
										() -> config.hunting.icyBiome.hideColdOverlay,
										newValue -> config.hunting.icyBiome.hideColdOverlay = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				// Floor Drops
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.hunting.floorDrops"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.floorDrops.highlightFloorDrops"))
								.description(Component.translatable("skyblocker.config.hunting.floorDrops.highlightFloorDrops.@Tooltip"))
								.binding(defaults.foraging.galatea.enableForestNodeHelper,
										() -> config.foraging.galatea.enableForestNodeHelper,
										newValue -> config.foraging.galatea.enableForestNodeHelper = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Color>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.floorDrops.floorDropHighlightColor"))
								.binding(defaults.hunting.floorDrops.floorDropHighlightColor,
										() -> config.hunting.floorDrops.floorDropHighlightColor,
										newValue -> config.hunting.floorDrops.floorDropHighlightColor = newValue)
								.controller(ColourController.createBuilder().hasAlpha(false).build())
								.build())
						.build())

				// Lasso Hud
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.hunting.lassoHud"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.lassoHud.enabled"))
								.description(Component.translatable("skyblocker.config.hunting.lassoHud.enabled.@Tooltip"))
								.binding(defaults.hunting.lassoHud.enabled,
										() -> config.hunting.lassoHud.enabled,
										newValue -> config.hunting.lassoHud.enabled = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(ButtonOption.createBuilder()
								.name(Component.translatable("skyblocker.config.hunting.lassoHud.hud.screen"))
								.prompt(Component.translatable("text.skyblocker.open"))
								.action(screen -> Minecraft.getInstance().gui.setScreen(new WidgetsConfigurationScreen(Location.GALATEA, LassoHud.getInstance().getInternalID(), screen)))
								.build())

						.build())
				.build();
	}
}
