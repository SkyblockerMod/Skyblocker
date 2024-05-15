package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.SkyblockerConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import net.minecraft.text.Text;

public class ForagingCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.name(Text.translatable("skyblocker.config.foraging"))

				//Modern Foraging island

				//Hunting - YACL doesn't like empty option groups
				/*.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.foraging.hunting"))

						.build())*/
				
				.build();
	}
}
