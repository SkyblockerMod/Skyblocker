package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.configs.MiscConfig;
import net.azureaaron.dandelion.platform.ConfigType;
import net.azureaaron.dandelion.systems.ConfigCategory;
import net.azureaaron.dandelion.systems.Option;
import net.azureaaron.dandelion.systems.OptionGroup;
import net.azureaaron.dandelion.systems.controllers.StringController;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MiscCategory {

    public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
        return ConfigCategory.createBuilder()
        		.id(Identifier.of(SkyblockerMod.NAMESPACE, "config/misc"))
                .name(Text.translatable("skyblocker.config.misc"))

                //Uncategorized Options
                .option(Option.<ConfigType>createBuilder()
                		.name(Text.translatable("skyblocker.config.misc.configBackend"))
                		.description(Text.translatable("skyblocker.config.misc.configBackend.@Tooltip"))
                		.binding(defaults.misc.configBackend,
                				() -> config.misc.configBackend,
                				newValue -> config.misc.configBackend = newValue)
                		.controller(ConfigUtils.createEnumController())
                		.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.literal("cat"))
						.binding(
								defaults.misc.cat,
								() -> config.misc.cat,
								newValue -> config.misc.cat = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build()
				)

                //Discord RPC
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.misc.richPresence"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.misc.richPresence.enableRichPresence"))
                                .binding(defaults.misc.richPresence.enableRichPresence,
                                        () -> config.misc.richPresence.enableRichPresence,
                                        newValue -> config.misc.richPresence.enableRichPresence = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<MiscConfig.Info>createBuilder()
                                .name(Text.translatable("skyblocker.config.misc.richPresence.info"))
                                .description(Text.translatable("skyblocker.config.misc.richPresence.info.@Tooltip"))
                                .binding(defaults.misc.richPresence.info,
                                        () -> config.misc.richPresence.info,
                                        newValue -> config.misc.richPresence.info = newValue)
                                .controller(ConfigUtils.createEnumController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.misc.richPresence.cycleMode"))
                                .binding(defaults.misc.richPresence.cycleMode,
                                        () -> config.misc.richPresence.cycleMode,
                                        newValue -> config.misc.richPresence.cycleMode = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.misc.richPresence.customMessage"))
                                .binding(defaults.misc.richPresence.customMessage,
                                        () -> config.misc.richPresence.customMessage,
                                        newValue -> config.misc.richPresence.customMessage = newValue)
                                .controller(StringController.createBuilder().build())
                                .build())
                        .build())
                .build();
    }
}
