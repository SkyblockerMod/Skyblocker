package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.text.Text;

public class HuntingCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
							 .name(Text.translatable("skyblocker.config.hunting"))
							 .option(Option.<Boolean>createBuilder()
										   .name(Text.translatable("skyblocker.config.hunting.huntingBoxHelper"))
										   .binding(defaults.hunting.huntingBox.enabled,
												   () -> config.hunting.huntingBox.enabled,
												   value -> config.hunting.huntingBox.enabled = value)
										   .controller(ConfigUtils::createBooleanController)
										   .description(OptionDescription.of(Text.translatable("skyblocker.config.hunting.huntingBoxHelper.@Tooltip")))
										   .build())
							 .build();
	}
}
