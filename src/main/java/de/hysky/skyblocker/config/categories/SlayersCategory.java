package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.text.Text;

public class SlayersCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.name(Text.translatable("text.autoconfig.skyblocker.category.slayer"))

				//Vampire Slayer
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.slayer.vampireSlayer"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.slayer.vampireSlayer.enableEffigyWaypoints"))
								.binding(defaults.slayer.vampireSlayer.enableEffigyWaypoints,
										() -> config.slayer.vampireSlayer.enableEffigyWaypoints,
										newValue -> config.slayer.vampireSlayer.enableEffigyWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.slayer.vampireSlayer.compactEffigyWaypoints"))
								.binding(defaults.slayer.vampireSlayer.compactEffigyWaypoints,
										() -> config.slayer.vampireSlayer.compactEffigyWaypoints,
										newValue -> config.slayer.vampireSlayer.compactEffigyWaypoints = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.slayer.vampireSlayer.effigyUpdateFrequency"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.slayer.vampireSlayer.effigyUpdateFrequency.@Tooltip")))
								.binding(defaults.slayer.vampireSlayer.effigyUpdateFrequency,
										() -> config.slayer.vampireSlayer.effigyUpdateFrequency,
										newValue -> config.slayer.vampireSlayer.effigyUpdateFrequency = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 10).step(1))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.slayer.vampireSlayer.enableHolyIceIndicator"))
								.binding(defaults.slayer.vampireSlayer.enableHolyIceIndicator,
										() -> config.slayer.vampireSlayer.enableHolyIceIndicator,
										newValue -> config.slayer.vampireSlayer.enableHolyIceIndicator = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.slayer.vampireSlayer.holyIceIndicatorTickDelay"))
								.binding(defaults.slayer.vampireSlayer.holyIceIndicatorTickDelay,
										() -> config.slayer.vampireSlayer.holyIceIndicatorTickDelay,
										newValue -> config.slayer.vampireSlayer.holyIceIndicatorTickDelay = newValue)
								.controller(IntegerFieldControllerBuilder::create)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.slayer.vampireSlayer.holyIceUpdateFrequency"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.slayer.vampireSlayer.holyIceUpdateFrequency.@Tooltip")))
								.binding(defaults.slayer.vampireSlayer.holyIceUpdateFrequency,
										() -> config.slayer.vampireSlayer.holyIceUpdateFrequency,
										newValue -> config.slayer.vampireSlayer.holyIceUpdateFrequency = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 10).step(1))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.slayer.vampireSlayer.enableHealingMelonIndicator"))
								.binding(defaults.slayer.vampireSlayer.enableHealingMelonIndicator,
										() -> config.slayer.vampireSlayer.enableHealingMelonIndicator,
										newValue -> config.slayer.vampireSlayer.enableHealingMelonIndicator = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Float>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.slayer.vampireSlayer.healingMelonHealthThreshold"))
								.binding(defaults.slayer.vampireSlayer.healingMelonHealthThreshold,
										() -> config.slayer.vampireSlayer.healingMelonHealthThreshold,
										newValue -> config.slayer.vampireSlayer.healingMelonHealthThreshold = newValue)
								.controller(FloatFieldControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.slayer.vampireSlayer.enableSteakStakeIndicator"))
								.binding(defaults.slayer.vampireSlayer.enableSteakStakeIndicator,
										() -> config.slayer.vampireSlayer.enableSteakStakeIndicator,
										newValue -> config.slayer.vampireSlayer.enableSteakStakeIndicator = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.slayer.vampireSlayer.steakStakeUpdateFrequency"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.slayer.vampireSlayer.steakStakeUpdateFrequency.@Tooltip")))
								.binding(defaults.slayer.vampireSlayer.steakStakeUpdateFrequency,
										() -> config.slayer.vampireSlayer.steakStakeUpdateFrequency,
										newValue -> config.slayer.vampireSlayer.steakStakeUpdateFrequency = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 10).step(1))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.slayer.vampireSlayer.enableManiaIndicator"))
								.binding(defaults.slayer.vampireSlayer.enableManiaIndicator,
										() -> config.slayer.vampireSlayer.enableManiaIndicator,
										newValue -> config.slayer.vampireSlayer.enableManiaIndicator = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.slayer.vampireSlayer.maniaUpdateFrequency"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.slayer.vampireSlayer.maniaUpdateFrequency.@Tooltip")))
								.binding(defaults.slayer.vampireSlayer.maniaUpdateFrequency,
										() -> config.slayer.vampireSlayer.maniaUpdateFrequency,
										newValue -> config.slayer.vampireSlayer.maniaUpdateFrequency = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 10).step(1))
								.build())
						.build())

				.build();
	}
}
