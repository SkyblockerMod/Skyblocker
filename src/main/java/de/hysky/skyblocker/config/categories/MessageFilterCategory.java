package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.chat.ChatRulesConfigScreen;
import de.hysky.skyblocker.skyblock.dwarven.CrystalsHudConfigScreen;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class MessageFilterCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.name(Text.translatable("text.autoconfig.skyblocker.category.messages"))

				//Uncategorized Options
				.option(Option.<ChatFilterResult>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.messages.hideAbility"))
						.binding(defaults.messages.hideAbility,
								() -> config.messages.hideAbility,
								newValue -> config.messages.hideAbility = newValue)
						.controller(ConfigUtils::createEnumCyclingListController)
						.build())
				.option(Option.<ChatFilterResult>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.messages.hideHeal"))
						.binding(defaults.messages.hideHeal,
								() -> config.messages.hideHeal,
								newValue -> config.messages.hideHeal = newValue)
						.controller(ConfigUtils::createEnumCyclingListController)
						.build())
				.option(Option.<ChatFilterResult>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.messages.hideAOTE"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.messages.hideAOTE.@Tooltip")))
						.binding(defaults.messages.hideAOTE,
								() -> config.messages.hideAOTE,
								newValue -> config.messages.hideAOTE = newValue)
						.controller(ConfigUtils::createEnumCyclingListController)
						.build())
				.option(Option.<ChatFilterResult>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.messages.hideImplosion"))
						.binding(defaults.messages.hideImplosion,
								() -> config.messages.hideImplosion,
								newValue -> config.messages.hideImplosion = newValue)
						.controller(ConfigUtils::createEnumCyclingListController)
						.build())
				.option(Option.<ChatFilterResult>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.messages.hideMoltenWave"))
						.binding(defaults.messages.hideMoltenWave,
								() -> config.messages.hideMoltenWave,
								newValue -> config.messages.hideMoltenWave = newValue)
						.controller(ConfigUtils::createEnumCyclingListController)
						.build())
				.option(Option.<ChatFilterResult>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.messages.hideAds"))
						.binding(defaults.messages.hideAds,
								() -> config.messages.hideAds,
								newValue -> config.messages.hideAds = newValue)
						.controller(ConfigUtils::createEnumCyclingListController)
						.build())
				.option(Option.<ChatFilterResult>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.messages.hideTeleportPad"))
						.binding(defaults.messages.hideTeleportPad,
								() -> config.messages.hideTeleportPad,
								newValue -> config.messages.hideTeleportPad = newValue)
						.controller(ConfigUtils::createEnumCyclingListController)
						.build())
				.option(Option.<ChatFilterResult>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.messages.hideCombo"))
						.binding(defaults.messages.hideCombo,
								() -> config.messages.hideCombo,
								newValue -> config.messages.hideCombo = newValue)
						.controller(ConfigUtils::createEnumCyclingListController)
						.build())
				.option(Option.<ChatFilterResult>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.messages.hideAutopet"))
						.binding(defaults.messages.hideAutopet,
								() -> config.messages.hideAutopet,
								newValue -> config.messages.hideAutopet = newValue)
						.controller(ConfigUtils::createEnumCyclingListController)
						.build())
				.option(Option.<ChatFilterResult>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.messages.hideShowOff"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.messages.hideShowOff.@Tooltip")))
						.binding(defaults.messages.hideShowOff,
								() -> config.messages.hideShowOff,
								newValue -> config.messages.hideShowOff = newValue)
						.controller(ConfigUtils::createEnumCyclingListController)
						.build())
				.option(Option.<ChatFilterResult>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.messages.hideToggleSkyMall"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.messages.hideToggleSkyMall.@Tooltip")))
						.binding(defaults.messages.hideToggleSkyMall,
								() -> config.messages.hideToggleSkyMall,
								newValue -> config.messages.hideToggleSkyMall = newValue)
						.controller(ConfigUtils::createEnumCyclingListController)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.messages.hideMana"))
						.binding(defaults.messages.hideMana,
								() -> config.messages.hideMana,
								newValue -> config.messages.hideMana = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.option(Option.<ChatFilterResult>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.messages.hideMimicKill"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.messages.hideMimicKill.@Tooltip")))
						.binding(defaults.messages.hideMimicKill,
								() -> config.messages.hideMimicKill,
								newValue -> config.messages.hideMimicKill = newValue)
						.controller(ConfigUtils::createEnumCyclingListController)
						.build())
				.option(Option.<ChatFilterResult>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.messages.hideDeath"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.messages.hideDeath.@Tooltip")))
						.binding(defaults.messages.hideDeath,
								() -> config.messages.hideDeath,
								newValue -> config.messages.hideDeath = newValue)
						.controller(ConfigUtils::createEnumCyclingListController)
						.build())
				.option(Option.<ChatFilterResult>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.messages.hideDicer"))
						.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.messages.hideDicer.@Tooltip")))
						.binding(defaults.messages.hideDicer,
								() -> config.messages.hideDicer,
								newValue -> config.messages.hideDicer = newValue)
						.controller(ConfigUtils::createEnumCyclingListController)
						.build())
				//chat rules options
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules"))
						.collapsed(false)
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.screen"))
								.text(Text.translatable("text.skyblocker.open"))
								.action((screen, opt) -> MinecraftClient.getInstance().setScreen(new ChatRulesConfigScreen(screen)))
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.announcementLength"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.announcementLength.@Tooltip")))
								.binding(defaults.messages.chatRuleConfig.announcementLength,
										() -> config.messages.chatRuleConfig.announcementLength,
										newValue -> config.messages.chatRuleConfig.announcementLength = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(5, 200).step(1))
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.announcementScale"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.messages.chatRules.announcementScale.@Tooltip")))
								.binding(defaults.messages.chatRuleConfig.announcementScale,
										() -> config.messages.chatRuleConfig.announcementScale,
										newValue -> config.messages.chatRuleConfig.announcementScale = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 8).step(1))
								.build())
						.build())
				.build();
	}
}
