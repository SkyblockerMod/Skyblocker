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
                                .binding(defaults.chats.hideAbility,
                                        () -> config.chats.hideAbility,
                                        newValue -> config.chats.hideAbility = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideHeal"))
                                .binding(defaults.chats.hideHeal,
                                        () -> config.chats.hideHeal,
                                        newValue -> config.chats.hideHeal = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideAOTE"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.chat.filter.hideAOTE.@Tooltip")))
                                .binding(defaults.chats.hideAOTE,
                                        () -> config.chats.hideAOTE,
                                        newValue -> config.chats.hideAOTE = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideImplosion"))
                                .binding(defaults.chats.hideImplosion,
                                        () -> config.chats.hideImplosion,
                                        newValue -> config.chats.hideImplosion = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideMoltenWave"))
                                .binding(defaults.chats.hideMoltenWave,
                                        () -> config.chats.hideMoltenWave,
                                        newValue -> config.chats.hideMoltenWave = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideAds"))
                                .binding(defaults.chats.hideAds,
                                        () -> config.chats.hideAds,
                                        newValue -> config.chats.hideAds = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideTeleportPad"))
                                .binding(defaults.chats.hideTeleportPad,
                                        () -> config.chats.hideTeleportPad,
                                        newValue -> config.chats.hideTeleportPad = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideCombo"))
                                .binding(defaults.chats.hideCombo,
                                        () -> config.chats.hideCombo,
                                        newValue -> config.chats.hideCombo = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideAutopet"))
                                .binding(defaults.chats.hideAutopet,
                                        () -> config.chats.hideAutopet,
                                        newValue -> config.chats.hideAutopet = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideShowOff"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.chat.filter.hideShowOff.@Tooltip")))
                                .binding(defaults.chats.hideShowOff,
                                        () -> config.chats.hideShowOff,
                                        newValue -> config.chats.hideShowOff = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideToggleSkyMall"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.chat.filter.hideToggleSkyMall.@Tooltip")))
                                .binding(defaults.chats.hideToggleSkyMall,
                                        () -> config.chats.hideToggleSkyMall,
                                        newValue -> config.chats.hideToggleSkyMall = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideMana"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.chat.filter.hideMana.@Tooltip")))
                                .binding(defaults.chats.hideMana,
                                        () -> config.chats.hideMana,
                                        newValue -> config.chats.hideMana = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideMimicKill"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.chat.filter.hideMimicKill.@Tooltip")))
                                .binding(defaults.chats.hideMimicKill,
                                        () -> config.chats.hideMimicKill,
                                        newValue -> config.chats.hideMimicKill = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideDeath"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.chat.filter.hideDeath.@Tooltip")))
                                .binding(defaults.chats.hideDeath,
                                        () -> config.chats.hideDeath,
                                        newValue -> config.chats.hideDeath = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideDicer"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.chat.filter.hideDicer.@Tooltip")))
                                .binding(defaults.chats.hideDicer,
                                        () -> config.chats.hideDicer,
                                        newValue -> config.chats.hideDicer = newValue)
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
                                .binding(defaults.chats.chatRuleConfig.announcementLength,
                                        () -> config.chats.chatRuleConfig.announcementLength,
                                        newValue -> config.chats.chatRuleConfig.announcementLength = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(5, 200).step(1))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.chatRules.announcementScale"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.chat.chatRules.announcementScale.@Tooltip")))
                                .binding(defaults.chats.chatRuleConfig.announcementScale,
                                        () -> config.chats.chatRuleConfig.announcementScale,
                                        newValue -> config.chats.chatRuleConfig.announcementScale = newValue)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 8).step(1))
                                .build())
                        .build())
                .build();
    }
}
