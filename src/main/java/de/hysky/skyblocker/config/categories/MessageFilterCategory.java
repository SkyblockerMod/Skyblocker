package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
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
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.messages.hideMana"))
						.binding(defaults.messages.hideMana,
								() -> config.messages.hideMana,
								newValue -> config.messages.hideMana = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				.build();
	}
}
