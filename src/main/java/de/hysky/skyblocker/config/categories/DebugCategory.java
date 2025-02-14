package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.debug.Debug;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.text.Text;

public class DebugCategory {
	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.name(Text.translatable("skyblocker.config.debug"))
				.option(Option.<Integer>createBuilder()
						.name(Text.translatable("skyblocker.config.debug.dumpRange"))
						.description(OptionDescription.of(Text.translatable("skyblocker.config.debug.dumpRange.@Tooltip")))
						.binding(defaults.debug.dumpRange,
								() -> config.debug.dumpRange,
								newValue -> config.debug.dumpRange = newValue)
						.controller(option -> IntegerSliderControllerBuilder.create(option).range(1, 25).step(1))
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.debug.showInvisibleArmorStands"))
						.binding(defaults.debug.showInvisibleArmorStands,
								() -> config.debug.showInvisibleArmorStands,
								newValue -> config.debug.showInvisibleArmorStands = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.debug.debugWebSockets"))
						.binding(defaults.debug.webSocketDebug,
								() -> config.debug.webSocketDebug,
								newValue -> config.debug.webSocketDebug = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<Debug.DumpFormat>createBuilder()
						.name(Text.translatable("skyblocker.config.debug.dumpFormat"))
						.description(OptionDescription.of(Text.translatable("skyblocker.config.debug.dumpFormat.@Tooltip")))
						.binding(defaults.debug.dumpFormat,
								() -> config.debug.dumpFormat,
								newValue -> config.debug.dumpFormat = newValue)
						.controller(opt -> EnumControllerBuilder.create(opt).enumClass(Debug.DumpFormat.class)) // ConfigUtils::createEnumCyclingListController causes a NPE for some reason
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.debug.corpseFinderDebug"))
						.binding(defaults.debug.corpseFinderDebug,
								() -> config.debug.corpseFinderDebug,
								newValue -> config.debug.corpseFinderDebug = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.build();
	}
}
