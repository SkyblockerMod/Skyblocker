package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.configs.MiscConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.text.Text;

public class DiscordRPCCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.name(Text.translatable("skyblocker.category.richPresence"))

				//Uncategorized Options
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.option.richPresence.enableRichPresence"))
						.binding(defaults.misc.richPresence.enableRichPresence,
								() -> config.misc.richPresence.enableRichPresence,
								newValue -> config.misc.richPresence.enableRichPresence = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<MiscConfig.Info>createBuilder()
						.name(Text.translatable("skyblocker.option.richPresence.info"))
						.description(OptionDescription.of(Text.translatable("skyblocker.option.richPresence.info.@Tooltip")))
						.binding(defaults.misc.richPresence.info,
								() -> config.misc.richPresence.info,
								newValue -> config.misc.richPresence.info = newValue)
						.controller(ConfigUtils::createEnumCyclingListController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("skyblocker.option.richPresence.cycleMode"))
						.binding(defaults.misc.richPresence.cycleMode,
								() -> config.misc.richPresence.cycleMode,
								newValue -> config.misc.richPresence.cycleMode = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<String>createBuilder()
						.name(Text.translatable("skyblocker.option.richPresence.customMessage"))
						.binding(defaults.misc.richPresence.customMessage,
								() -> config.misc.richPresence.customMessage,
								newValue -> config.misc.richPresence.customMessage = newValue)
						.controller(StringControllerBuilder::create)
						.build())
				.build();
	}
}
