package me.xmrvizzy.skyblocker.config.categories;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import me.xmrvizzy.skyblocker.config.ConfigUtils;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
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
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.rift.mcGrubberStacks"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.rift.mcGrubberStacks.@Tooltip")))
								.binding(defaults.locations.rift.mcGrubberStacks,
										() -> config.locations.rift.mcGrubberStacks,
										newValue -> config.locations.rift.mcGrubberStacks = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 5).step(1))
								.build())
						.build())
				
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
				.build();
	}
}
