package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.chat.ChatRulesConfigScreen;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class ChatCategory {

    public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("skyblocker.config.chat"))

                //Uncategorized Options
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.chat.filter"))
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideAbility"))
                                .binding(defaults.chat.hideAbility,
                                        () -> config.chat.hideAbility,
                                        newValue -> config.chat.hideAbility = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideHeal"))
                                .binding(defaults.chat.hideHeal,
                                        () -> config.chat.hideHeal,
                                        newValue -> config.chat.hideHeal = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideAOTE"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.chat.filter.hideAOTE.@Tooltip")))
                                .binding(defaults.chat.hideAOTE,
                                        () -> config.chat.hideAOTE,
                                        newValue -> config.chat.hideAOTE = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideImplosion"))
                                .binding(defaults.chat.hideImplosion,
                                        () -> config.chat.hideImplosion,
                                        newValue -> config.chat.hideImplosion = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideMoltenWave"))
                                .binding(defaults.chat.hideMoltenWave,
                                        () -> config.chat.hideMoltenWave,
                                        newValue -> config.chat.hideMoltenWave = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideAds"))
                                .binding(defaults.chat.hideAds,
                                        () -> config.chat.hideAds,
                                        newValue -> config.chat.hideAds = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideTeleportPad"))
                                .binding(defaults.chat.hideTeleportPad,
                                        () -> config.chat.hideTeleportPad,
                                        newValue -> config.chat.hideTeleportPad = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideCombo"))
                                .binding(defaults.chat.hideCombo,
                                        () -> config.chat.hideCombo,
                                        newValue -> config.chat.hideCombo = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideAutopet"))
                                .binding(defaults.chat.hideAutopet,
                                        () -> config.chat.hideAutopet,
                                        newValue -> config.chat.hideAutopet = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideShowOff"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.chat.filter.hideShowOff.@Tooltip")))
                                .binding(defaults.chat.hideShowOff,
                                        () -> config.chat.hideShowOff,
                                        newValue -> config.chat.hideShowOff = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideToggleSkyMall"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.chat.filter.hideToggleSkyMall.@Tooltip")))
                                .binding(defaults.chat.hideToggleSkyMall,
                                        () -> config.chat.hideToggleSkyMall,
                                        newValue -> config.chat.hideToggleSkyMall = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideMana"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.chat.filter.hideMana.@Tooltip")))
                                .binding(defaults.chat.hideMana,
                                        () -> config.chat.hideMana,
                                        newValue -> config.chat.hideMana = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideMimicKill"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.chat.filter.hideMimicKill.@Tooltip")))
                                .binding(defaults.chat.hideMimicKill,
                                        () -> config.chat.hideMimicKill,
                                        newValue -> config.chat.hideMimicKill = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideDeath"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.chat.filter.hideDeath.@Tooltip")))
                                .binding(defaults.chat.hideDeath,
                                        () -> config.chat.hideDeath,
                                        newValue -> config.chat.hideDeath = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideDicer"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.chat.filter.hideDicer.@Tooltip")))
                                .binding(defaults.chat.hideDicer,
                                        () -> config.chat.hideDicer,
                                        newValue -> config.chat.hideDicer = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .build())

                //chat rules options
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.chat.chatRules"))
                        .collapsed(false)
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.chatRules.screen"))
                                .text(Text.translatable("text.skyblocker.open"))
                                .action((screen, opt) -> MinecraftClient.getInstance().setScreen(new ChatRulesConfigScreen(screen)))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.chatRules.announcementLength"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.chat.chatRules.announcementLength.@Tooltip")))
                                .binding(defaults.chat.chatRuleConfig.announcementLength,
                                        () -> config.chat.chatRuleConfig.announcementLength,
                                        newValue -> config.chat.chatRuleConfig.announcementLength = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(5, 200).step(1))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.chatRules.announcementScale"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.chat.chatRules.announcementScale.@Tooltip")))
                                .binding(defaults.chat.chatRuleConfig.announcementScale,
                                        () -> config.chat.chatRuleConfig.announcementScale,
                                        newValue -> config.chat.chatRuleConfig.announcementScale = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 8).step(1))
                                .build())
                        .build())
                .build();
    }
}
