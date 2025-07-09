package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.chat.ChatRulesConfigScreen;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import net.azureaaron.dandelion.systems.ButtonOption;
import net.azureaaron.dandelion.systems.ConfigCategory;
import net.azureaaron.dandelion.systems.Option;
import net.azureaaron.dandelion.systems.OptionGroup;
import net.azureaaron.dandelion.systems.controllers.IntegerController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ChatCategory {

    public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
        return ConfigCategory.createBuilder()
        		.id(Identifier.of(SkyblockerMod.NAMESPACE, "config/chat"))
                .name(Text.translatable("skyblocker.config.chat"))
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.chat.skyblockXpMessages"))
                        .description(Text.translatable("skyblocker.config.chat.skyblockXpMessages.@Tooltip"))
                        .binding(defaults.chat.skyblockXpMessages,
                                () -> config.chat.skyblockXpMessages,
                                newValue -> config.chat.skyblockXpMessages = newValue)
                        .controller(ConfigUtils.createBooleanController())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.chat.confirmationPromptHelper"))
                        .description(Text.translatable("skyblocker.config.chat.confirmationPromptHelper.@Tooltip"))
                        .binding(defaults.chat.confirmationPromptHelper,
                                () -> config.chat.confirmationPromptHelper,
                                newValue -> config.chat.confirmationPromptHelper = newValue)
                        .controller(ConfigUtils.createBooleanController())
                        .build())

                //Uncategorized Options
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.chat.filter"))
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideAbility"))
                                .binding(defaults.chat.hideAbility,
                                        () -> config.chat.hideAbility,
                                        newValue -> config.chat.hideAbility = newValue)
                                .controller(ConfigUtils.createEnumController())
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideHeal"))
                                .binding(defaults.chat.hideHeal,
                                        () -> config.chat.hideHeal,
                                        newValue -> config.chat.hideHeal = newValue)
                                .controller(ConfigUtils.createEnumController())
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideAOTE"))
                                .description(Text.translatable("skyblocker.config.chat.filter.hideAOTE.@Tooltip"))
                                .binding(defaults.chat.hideAOTE,
                                        () -> config.chat.hideAOTE,
                                        newValue -> config.chat.hideAOTE = newValue)
                                .controller(ConfigUtils.createEnumController())
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideImplosion"))
                                .binding(defaults.chat.hideImplosion,
                                        () -> config.chat.hideImplosion,
                                        newValue -> config.chat.hideImplosion = newValue)
                                .controller(ConfigUtils.createEnumController())
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideMoltenWave"))
                                .binding(defaults.chat.hideMoltenWave,
                                        () -> config.chat.hideMoltenWave,
                                        newValue -> config.chat.hideMoltenWave = newValue)
                                .controller(ConfigUtils.createEnumController())
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideAds"))
                                .binding(defaults.chat.hideAds,
                                        () -> config.chat.hideAds,
                                        newValue -> config.chat.hideAds = newValue)
                                .controller(ConfigUtils.createEnumController())
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideTeleportPad"))
                                .binding(defaults.chat.hideTeleportPad,
                                        () -> config.chat.hideTeleportPad,
                                        newValue -> config.chat.hideTeleportPad = newValue)
                                .controller(ConfigUtils.createEnumController())
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideCombo"))
                                .binding(defaults.chat.hideCombo,
                                        () -> config.chat.hideCombo,
                                        newValue -> config.chat.hideCombo = newValue)
                                .controller(ConfigUtils.createEnumController())
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideAutopet"))
                                .binding(defaults.chat.hideAutopet,
                                        () -> config.chat.hideAutopet,
                                        newValue -> config.chat.hideAutopet = newValue)
                                .controller(ConfigUtils.createEnumController())
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideShowOff"))
                                .description(Text.translatable("skyblocker.config.chat.filter.hideShowOff.@Tooltip"))
                                .binding(defaults.chat.hideShowOff,
                                        () -> config.chat.hideShowOff,
                                        newValue -> config.chat.hideShowOff = newValue)
                                .controller(ConfigUtils.createEnumController())
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideToggleSkyMall"))
                                .description(Text.translatable("skyblocker.config.chat.filter.hideToggleSkyMall.@Tooltip"))
                                .binding(defaults.chat.hideToggleSkyMall,
                                        () -> config.chat.hideToggleSkyMall,
                                        newValue -> config.chat.hideToggleSkyMall = newValue)
                                .controller(ConfigUtils.createEnumController())
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideToggleLottery"))
                                .description(Text.translatable("skyblocker.config.chat.filter.hideToggleLottery.@Tooltip"))
                                .binding(defaults.chat.hideToggleLottery,
                                        () -> config.chat.hideToggleLottery,
                                        newValue -> config.chat.hideToggleLottery = newValue)
                                .controller(ConfigUtils.createEnumController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideMana"))
                                .description(Text.translatable("skyblocker.config.chat.filter.hideMana.@Tooltip"))
                                .binding(defaults.chat.hideMana,
                                        () -> config.chat.hideMana,
                                        newValue -> config.chat.hideMana = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideMimicKill"))
                                .description(Text.translatable("skyblocker.config.chat.filter.hideMimicKill.@Tooltip"))
                                .binding(defaults.chat.hideMimicKill,
                                        () -> config.chat.hideMimicKill,
                                        newValue -> config.chat.hideMimicKill = newValue)
                                .controller(ConfigUtils.createEnumController())
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideDeath"))
                                .description(Text.translatable("skyblocker.config.chat.filter.hideDeath.@Tooltip"))
                                .binding(defaults.chat.hideDeath,
                                        () -> config.chat.hideDeath,
                                        newValue -> config.chat.hideDeath = newValue)
                                .controller(ConfigUtils.createEnumController())
                                .build())
                        .option(Option.<ChatFilterResult>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.filter.hideDicer"))
                                .description(Text.translatable("skyblocker.config.chat.filter.hideDicer.@Tooltip"))
                                .binding(defaults.chat.hideDicer,
                                        () -> config.chat.hideDicer,
                                        newValue -> config.chat.hideDicer = newValue)
                                .controller(ConfigUtils.createEnumController())
                                .build())
                        .build())

                //chat rules options
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.chat.chatRules"))
                        .collapsed(false)
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.chatRules.screen"))
                                .prompt(Text.translatable("text.skyblocker.open"))
                                .action(screen -> MinecraftClient.getInstance().setScreen(new ChatRulesConfigScreen(screen)))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.chat.chatRules.announcementLength"))
                                .description(Text.translatable("skyblocker.config.chat.chatRules.announcementLength.@Tooltip"))
                                .binding(defaults.chat.chatRuleConfig.announcementLength,
                                        () -> config.chat.chatRuleConfig.announcementLength,
                                        newValue -> config.chat.chatRuleConfig.announcementLength = newValue)
                                .controller(IntegerController.createBuilder().range(5, 200).slider(1).build())
                                .build())
                        .build())
                .build();
    }
}
