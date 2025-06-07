package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.utils.Location;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import net.minecraft.text.Text;

import java.awt.*;

public class ForagingCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.name(Text.translatable("skyblocker.config.foraging"))

				// Park (Modern Foraging Island)
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.foraging.park"))
						.collapsed(false)

						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.park.foragingHud.enableHud"))
								.description(OptionDescription.of(Text.translatable("skyblocker.config.foraging.park.foragingHud.enableHud.@Tooltip")))
								.binding(
										defaults.foraging.park.foragingHud.enableHud,
										() -> config.foraging.park.foragingHud.enableHud,
										newValue -> config.foraging.park.foragingHud.enableHud = newValue
								)
								.controller(BooleanControllerBuilder::create)
								.build())

						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.park.foragingHud.x"))
								.binding(
										defaults.foraging.park.foragingHud.x,
										() -> config.foraging.park.foragingHud.x,
										newValue -> config.foraging.park.foragingHud.x = newValue
								)
								.controller(opt -> IntegerFieldControllerBuilder.create(opt).min(0).max(500))
								.build())

						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.park.foragingHud.y"))
								.binding(
										defaults.foraging.park.foragingHud.y,
										() -> config.foraging.park.foragingHud.y,
										newValue -> config.foraging.park.foragingHud.y = newValue
								)
								.controller(opt -> IntegerFieldControllerBuilder.create(opt).min(0).max(500))
								.build())

						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.park.highlightConnectedTree"))
								.binding(
										defaults.foraging.park.highlightConnectedTree,
										() -> config.foraging.park.highlightConnectedTree,
										newValue -> config.foraging.park.highlightConnectedTree = newValue
								)
								.controller(BooleanControllerBuilder::create)
								.build())

						.option(Option.<Color>createBuilder()
								.name(Text.translatable("skyblocker.config.foraging.park.highlightColor"))
								.binding(
										new Color(defaults.foraging.park.highlightColor, true),
										() -> new Color(config.foraging.park.highlightColor, true),
										newValue -> config.foraging.park.highlightColor = newValue.getRGB()
								)
								.controller(ColorControllerBuilder::create)
								.build())

						.build()

				)

				.build();
	}
}
