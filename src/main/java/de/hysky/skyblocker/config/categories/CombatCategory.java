package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import net.azureaaron.dandelion.api.ConfigCategory;
import net.azureaaron.dandelion.api.Option;
import net.minecraft.network.chat.Component;

public class CombatCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.id(SkyblockerMod.id("config/combat"))
				.name(Component.translatable("skyblocker.config.combat"))

				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("skyblocker.config.combat.powerOrbRange.show"))
						.description(Component.translatable("skyblocker.config.combat.powerOrbRange.show.@Tooltip"))
						.binding(
								defaults.combat.powerOrbRange.showPowerOrbRange,
								() -> config.combat.powerOrbRange.showPowerOrbRange,
								newValue -> config.combat.powerOrbRange.showPowerOrbRange = newValue
						)
						.controller(ConfigUtils.createBooleanController())
						.build())

				.build();
	}
}
