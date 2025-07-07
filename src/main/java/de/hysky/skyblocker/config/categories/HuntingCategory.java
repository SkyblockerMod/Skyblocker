package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import net.azureaaron.dandelion.systems.ConfigCategory;
import net.azureaaron.dandelion.systems.Option;
import net.azureaaron.dandelion.systems.OptionGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class HuntingCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.id(Identifier.of(SkyblockerMod.NAMESPACE, "config/hunting"))
				.name(Text.translatable("skyblocker.config.hunting"))
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.config.hunting.huntingBoxHelper"))
						.binding(defaults.hunting.huntingBox.enabled,
								() -> config.hunting.huntingBox.enabled,
								value -> config.hunting.huntingBox.enabled = value)
						.controller(ConfigUtils.createBooleanController())
						.description(Text.translatable("skyblocker.config.hunting.huntingBoxHelper.@Tooltip"))
						.build())

				//Hunting Mob Features
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.hunting.huntingMobs"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.hunting.huntingMobs.silencePhantoms"))
								.description(Text.translatable("skyblocker.config.hunting.huntingMobs.silencePhantoms.@Tooltip"))
								.binding(defaults.hunting.huntingMobs.silencePhantoms,
										() -> config.hunting.huntingMobs.silencePhantoms,
										newValue -> config.hunting.huntingMobs.silencePhantoms = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.hunting.huntingMobs.highlightHideonleaf"))
								.description(Text.translatable("skyblocker.config.hunting.huntingMobs.highlightHideonleaf.@Tooltip"))
								.binding(defaults.hunting.huntingMobs.highlightHideonleaf,
										() -> config.hunting.huntingMobs.highlightHideonleaf,
										newValue -> config.hunting.huntingMobs.highlightHideonleaf = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())
				.build();
	}
}
