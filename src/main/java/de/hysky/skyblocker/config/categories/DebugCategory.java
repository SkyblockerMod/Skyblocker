package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.debug.Debug;
import net.azureaaron.dandelion.systems.ConfigCategory;
import net.azureaaron.dandelion.systems.Option;
import net.azureaaron.dandelion.systems.controllers.IntegerController;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DebugCategory {
	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.id(Identifier.of(SkyblockerMod.NAMESPACE, "config/debug"))
				.name(Text.translatable("skyblocker.config.debug"))
				.option(Option.<Integer>createBuilder()
						.name(Text.translatable("skyblocker.config.debug.dumpRange"))
						.description(Text.translatable("skyblocker.config.debug.dumpRange.@Tooltip"))
						.binding(defaults.debug.dumpRange,
								() -> config.debug.dumpRange,
								newValue -> config.debug.dumpRange = newValue)
						.controller(IntegerController.createBuilder().range(1, 25).slider(1).build())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.debug.showInvisibleArmorStands"))
						.binding(defaults.debug.showInvisibleArmorStands,
								() -> config.debug.showInvisibleArmorStands,
								newValue -> config.debug.showInvisibleArmorStands = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.debug.debugWebSockets"))
						.binding(defaults.debug.webSocketDebug,
								() -> config.debug.webSocketDebug,
								newValue -> config.debug.webSocketDebug = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Debug.DumpFormat>createBuilder()
						.name(Text.translatable("skyblocker.config.debug.dumpFormat"))
						.description(Text.translatable("skyblocker.config.debug.dumpFormat.@Tooltip"))
						.binding(defaults.debug.dumpFormat,
								() -> config.debug.dumpFormat,
								newValue -> config.debug.dumpFormat = newValue)
						.controller(ConfigUtils.createEnumController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.debug.corpseFinderDebug"))
						.binding(defaults.debug.corpseFinderDebug,
								() -> config.debug.corpseFinderDebug,
								newValue -> config.debug.corpseFinderDebug = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.build();
	}
}
