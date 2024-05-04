package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.text.Text;

public class DiscordRichPresence {
    public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {

        Option<SkyblockerConfig.Info> info = Option.<SkyblockerConfig.Info>createBuilder()
                .name(Text.translatable("text.autoconfig.skyblocker.option.richPresence.info"))
                .description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.richPresence.info.@Tooltip")))
                .binding(defaults.richPresence.info,
                        () -> config.richPresence.info,
                        newValue -> config.richPresence.info = newValue)
                .controller(ConfigUtils::createEnumCyclingListController)
                .build();
        Option<String> customMessage = Option.<String>createBuilder()
                .name(Text.translatable("text.autoconfig.skyblocker.option.richPresence.customMessage"))
                .binding(defaults.richPresence.customMessage,
                        () -> config.richPresence.customMessage,
                        newValue -> config.richPresence.customMessage = newValue)
                .controller(StringControllerBuilder::create)
                .build();
        Option<Boolean> cycleMode = Option.<Boolean>createBuilder()
                .name(Text.translatable("text.autoconfig.skyblocker.option.richPresence.cycleMode"))
                .binding(defaults.richPresence.cycleMode,
                        () -> config.richPresence.cycleMode,
                        newValue -> config.richPresence.cycleMode = newValue)
                .controller(ConfigUtils::createBooleanController)
                .listener((option, pendingValue) -> {
                    info.setAvailable(!pendingValue);
                })
                .build();
        Option<Boolean> enableRichPresence = Option.<Boolean>createBuilder()
                .name(Text.translatable("text.autoconfig.skyblocker.option.richPresence.enableRichPresence"))
                .binding(defaults.richPresence.enableRichPresence,
                        () -> config.richPresence.enableRichPresence,
                        newValue -> config.richPresence.enableRichPresence = newValue)
                .controller(ConfigUtils::createBooleanController)
                .listener((option, pendingValue) -> {
                    cycleMode.setAvailable(pendingValue);
                    customMessage.setAvailable(pendingValue);
                    info.setAvailable(pendingValue);
                })
                .build();

        return ConfigCategory.createBuilder()
                .name(Text.translatable("text.autoconfig.skyblocker.category.richPresence"))
                .option(enableRichPresence)
                .option(cycleMode)
                .option(info)
                .option(customMessage)
                .build();
    }
}
