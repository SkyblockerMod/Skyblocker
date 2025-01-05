package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.SkyblockerScreen;
import de.hysky.skyblocker.UpdateNotifications;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.configs.GeneralConfig;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.CraftPriceTooltip;
import de.hysky.skyblocker.skyblock.shortcut.ShortcutsConfigScreen;
import de.hysky.skyblocker.skyblock.speedPreset.SpeedPresetsScreen;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GeneralCategory {

    public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("skyblocker.config.general"))

                //Skyblocker Screen
                .option(ButtonOption.createBuilder()
                        .name(Text.translatable("skyblocker.skyblockerScreen"))
                        .text(Text.translatable("text.skyblocker.open"))
                        .action((screen, opt) -> MinecraftClient.getInstance().setScreen(new SkyblockerScreen()))
                        .build())

                //Ungrouped Options
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.general.enableTips"))
                        .binding(defaults.general.enableTips,
                                () -> config.general.enableTips,
                                newValue -> config.general.enableTips = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.general.updateNotifications"))
                        .binding(UpdateNotifications.Config.DEFAULT.enabled(),
                                () -> UpdateNotifications.config.enabled(),
                                newValue -> UpdateNotifications.config = UpdateNotifications.config.withEnabled(newValue))
                        .controller(ConfigUtils::createBooleanController)
                        .build())
                .option(Option.<UpdateNotifications.Channel>createBuilder()
                        .name(Text.translatable("skyblocker.config.general.updateChannel"))
                        .description(OptionDescription.of(Text.translatable("skyblocker.config.general.updateChannel.@Tooltip")))
                        .binding(UpdateNotifications.Config.DEFAULT.channel(),
                                () -> UpdateNotifications.config.channel(),
                                newValue -> UpdateNotifications.config = UpdateNotifications.config.withChannel(newValue))
                        .controller(ConfigUtils::createEnumCyclingListController)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.general.acceptReparty"))
                        .binding(defaults.general.acceptReparty,
                                () -> config.general.acceptReparty,
                                newValue -> config.general.acceptReparty = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .build())

				.group(OptionGroup.createBuilder()
						.name(Text.translatable("skyblocker.config.general.speedPresets"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.general.speedPresets.enableSpeedPresets"))
								.binding(defaults.general.speedPresets.enableSpeedPresets,
										() -> config.general.speedPresets.enableSpeedPresets,
										newValue -> config.general.speedPresets.enableSpeedPresets = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("skyblocker.config.general.speedPresets.config"))
								.text(Text.translatable("text.skyblocker.open"))
								.action((screen, opt) -> MinecraftClient.getInstance().setScreen(new SpeedPresetsScreen(screen)))
								.build())
						.build())

                //Shortcuts
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.general.shortcuts"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.shortcuts.enableShortcuts"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.general.shortcuts.enableShortcuts.@Tooltip")))
                                .binding(defaults.general.shortcuts.enableShortcuts,
                                        () -> config.general.shortcuts.enableShortcuts,
                                        newValue -> config.general.shortcuts.enableShortcuts = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.shortcuts.enableCommandShortcuts"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.general.shortcuts.enableCommandShortcuts.@Tooltip")))
                                .binding(defaults.general.shortcuts.enableCommandShortcuts,
                                        () -> config.general.shortcuts.enableCommandShortcuts,
                                        newValue -> config.general.shortcuts.enableCommandShortcuts = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.shortcuts.enableCommandArgShortcuts"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.general.shortcuts.enableCommandArgShortcuts.@Tooltip")))
                                .binding(defaults.general.shortcuts.enableCommandArgShortcuts,
                                        () -> config.general.shortcuts.enableCommandArgShortcuts,
                                        newValue -> config.general.shortcuts.enableCommandArgShortcuts = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("skyblocker.config.general.shortcuts.config"))
                                .text(Text.translatable("text.skyblocker.open"))
                                .action((screen, opt) -> MinecraftClient.getInstance().setScreen(new ShortcutsConfigScreen(screen)))
                                .build())
                        .build())

                //Quiver Warning
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.general.quiverWarning"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.quiverWarning.enableQuiverWarning"))
                                .binding(defaults.general.quiverWarning.enableQuiverWarning,
                                        () -> config.general.quiverWarning.enableQuiverWarning,
                                        newValue -> config.general.quiverWarning.enableQuiverWarning = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.quiverWarning.enableQuiverWarningInDungeons"))
                                .binding(defaults.general.quiverWarning.enableQuiverWarningInDungeons,
                                        () -> config.general.quiverWarning.enableQuiverWarningInDungeons,
                                        newValue -> config.general.quiverWarning.enableQuiverWarningInDungeons = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.quiverWarning.enableQuiverWarningAfterDungeon"))
                                .binding(defaults.general.quiverWarning.enableQuiverWarningAfterDungeon,
                                        () -> config.general.quiverWarning.enableQuiverWarningAfterDungeon,
                                        newValue -> config.general.quiverWarning.enableQuiverWarningAfterDungeon = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Item List
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.general.itemList"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.itemList.enableItemList"))
                                .binding(defaults.general.itemList.enableItemList,
                                        () -> config.general.itemList.enableItemList,
                                        newValue -> config.general.itemList.enableItemList = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Item Tooltip
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.general.itemTooltip"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.itemTooltip.enableNPCPrice"))
                                .binding(defaults.general.itemTooltip.enableNPCPrice,
                                        () -> config.general.itemTooltip.enableNPCPrice,
                                        newValue -> config.general.itemTooltip.enableNPCPrice = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.itemTooltip.enableMotesPrice"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.general.itemTooltip.enableMotesPrice.@Tooltip")))
                                .binding(defaults.general.itemTooltip.enableMotesPrice,
                                        () -> config.general.itemTooltip.enableMotesPrice,
                                        newValue -> config.general.itemTooltip.enableMotesPrice = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.itemTooltip.enableAvgBIN"))
                                .binding(defaults.general.itemTooltip.enableAvgBIN,
                                        () -> config.general.itemTooltip.enableAvgBIN,
                                        newValue -> config.general.itemTooltip.enableAvgBIN = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<GeneralConfig.Average>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.itemTooltip.avg"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.general.itemTooltip.avg.@Tooltip")))
                                .binding(defaults.general.itemTooltip.avg,
                                        () -> config.general.itemTooltip.avg,
                                        newValue -> config.general.itemTooltip.avg = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.itemTooltip.enableLowestBIN"))
                                .binding(defaults.general.itemTooltip.enableLowestBIN,
                                        () -> config.general.itemTooltip.enableLowestBIN,
                                        newValue -> config.general.itemTooltip.enableLowestBIN = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.itemTooltip.enableBazaarPrice"))
                                .binding(defaults.general.itemTooltip.enableBazaarPrice,
                                        () -> config.general.itemTooltip.enableBazaarPrice,
                                        newValue -> config.general.itemTooltip.enableBazaarPrice = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<GeneralConfig.Craft>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.itemTooltip.craft"))
                                .binding(defaults.general.itemTooltip.enableCraftingCost,
                                        () -> config.general.itemTooltip.enableCraftingCost,
                                        newValue -> config.general.itemTooltip.enableCraftingCost = newValue)
                                .addListener((ignored, event) -> {
                                	if (event == OptionEventListener.Event.STATE_CHANGE) CraftPriceTooltip.clearCache();
                                })
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.itemTooltip.enableObtainedDate"))
                                .binding(defaults.general.itemTooltip.enableObtainedDate,
                                        () -> config.general.itemTooltip.enableObtainedDate,
                                        newValue -> config.general.itemTooltip.enableObtainedDate = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.itemTooltip.enableMuseumInfo"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.general.itemTooltip.enableMuseumInfo.@Tooltip")))
                                .binding(defaults.general.itemTooltip.enableMuseumInfo,
                                        () -> config.general.itemTooltip.enableMuseumInfo,
                                        newValue -> config.general.itemTooltip.enableMuseumInfo = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.itemTooltip.enableExoticTooltip"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.general.itemTooltip.enableExoticTooltip.@Tooltip")))
                                .binding(defaults.general.itemTooltip.enableExoticTooltip,
                                        () -> config.general.itemTooltip.enableExoticTooltip,
                                        newValue -> config.general.itemTooltip.enableExoticTooltip = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.itemTooltip.enableAccessoriesHelper"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.general.itemTooltip.enableAccessoriesHelper.@Tooltip[0]"),
                                        Text.literal("\n\n✔ Collected").formatted(Formatting.GREEN),
                                        Text.translatable("skyblocker.config.general.itemTooltip.enableAccessoriesHelper.@Tooltip[1]"),
                                        Text.literal("\n✦ Upgrade").withColor(0x218bff),
                                        Text.translatable("skyblocker.config.general.itemTooltip.enableAccessoriesHelper.@Tooltip[2]"),
                                        Text.literal("\n↑ Upgradable").withColor(0xf8d048),
                                        Text.translatable("skyblocker.config.general.itemTooltip.enableAccessoriesHelper.@Tooltip[3]"),
                                        Text.literal("\n↓ Downgrade").formatted(Formatting.GRAY),
                                        Text.translatable("skyblocker.config.general.itemTooltip.enableAccessoriesHelper.@Tooltip[4]"),
                                        Text.literal("\n✖ Missing").formatted(Formatting.RED),
                                        Text.translatable("skyblocker.config.general.itemTooltip.enableAccessoriesHelper.@Tooltip[5]")))
                                .binding(defaults.general.itemTooltip.enableAccessoriesHelper,
                                        () -> config.general.itemTooltip.enableAccessoriesHelper,
                                        newValue -> config.general.itemTooltip.enableAccessoriesHelper = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.itemTooltip.dungeonQuality"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.general.itemTooltip.dungeonQuality.@Tooltip")))
                                .binding(defaults.general.itemTooltip.dungeonQuality,
                                        () -> config.general.itemTooltip.dungeonQuality,
                                        newValue -> config.general.itemTooltip.dungeonQuality = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.itemTooltip.showEssenceCost"))
                                .binding(defaults.general.itemTooltip.showEssenceCost,
                                        () -> config.general.itemTooltip.showEssenceCost,
                                        newValue -> config.general.itemTooltip.showEssenceCost = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.itemTooltip.enableEstimatedItemValue"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.general.itemTooltip.enableEstimatedItemValue.@Tooltip")))
                                .binding(defaults.general.itemTooltip.enableEstimatedItemValue,
                                        () -> config.general.itemTooltip.enableEstimatedItemValue,
                                        newValue -> config.general.itemTooltip.enableEstimatedItemValue = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.itemTooltip.enableStackingEnchantProgress"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.general.itemTooltip.enableStackingEnchantProgress.@Tooltip")))
                                .binding(defaults.general.itemTooltip.enableStackingEnchantProgress,
                                        () -> config.general.itemTooltip.enableStackingEnchantProgress,
                                        newValue -> config.general.itemTooltip.enableStackingEnchantProgress = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Item Info Display
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.general.itemInfoDisplay"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.itemInfoDisplay.itemRarityBackgrounds"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.general.itemInfoDisplay.itemRarityBackgrounds.@Tooltip")))
                                .binding(defaults.general.itemInfoDisplay.itemRarityBackgrounds,
                                        () -> config.general.itemInfoDisplay.itemRarityBackgrounds,
                                        newValue -> config.general.itemInfoDisplay.itemRarityBackgrounds = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<GeneralConfig.RarityBackgroundStyle>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.itemInfoDisplay.itemRarityBackgroundStyle"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.general.itemInfoDisplay.itemRarityBackgroundStyle.@Tooltip")))
                                .binding(defaults.general.itemInfoDisplay.itemRarityBackgroundStyle,
                                        () -> config.general.itemInfoDisplay.itemRarityBackgroundStyle,
                                        newValue -> config.general.itemInfoDisplay.itemRarityBackgroundStyle = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.itemInfoDisplay.itemRarityBackgroundsOpacity"))
                                .binding(defaults.general.itemInfoDisplay.itemRarityBackgroundsOpacity,
                                        () -> config.general.itemInfoDisplay.itemRarityBackgroundsOpacity,
                                        newValue -> config.general.itemInfoDisplay.itemRarityBackgroundsOpacity = newValue)
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0f, 1f).step(0.05f).formatValue(ConfigUtils.FLOAT_TWO_FORMATTER))
                                .build())
                        .build())

                //Item Protection
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.general.itemProtection"))
                        .collapsed(true)
                        .option(Option.<GeneralConfig.SlotLockStyle>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.itemProtection.slotLockStyle"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.general.itemProtection.slotLockStyle.@Tooltip")))
                                .binding(defaults.general.itemProtection.slotLockStyle,
                                        () -> config.general.itemProtection.slotLockStyle,
                                        newValue -> config.general.itemProtection.slotLockStyle = newValue)
                                .controller(ConfigUtils::createEnumCyclingListController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                        		.name(Text.translatable("skyblocker.config.general.itemProtection.protectValuableConsumables"))
                        		.description(OptionDescription.of(Text.translatable("skyblocker.config.general.itemProtection.protectValuableConsumables.@Tooltip")))
                        		.binding(defaults.general.itemProtection.protectValuableConsumables,
                        				() -> config.general.itemProtection.protectValuableConsumables,
                        				newValue -> config.general.itemProtection.protectValuableConsumables = newValue)
                        		.controller(ConfigUtils::createBooleanController)
                        		.build())
                        .build())

                //Wiki Lookup
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.general.wikiLookup"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.wikiLookup.enableWikiLookup"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.general.wikiLookup.enableWikiLookup.@Tooltip")))
                                .binding(defaults.general.wikiLookup.enableWikiLookup,
                                        () -> config.general.wikiLookup.enableWikiLookup,
                                        newValue -> config.general.wikiLookup.enableWikiLookup = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(ConfigUtils.createShortcutToKeybindsScreen())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.wikiLookup.officialWiki"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.general.wikiLookup.officialWiki.@Tooltip")))
                                .binding(defaults.general.wikiLookup.officialWiki,
                                        () -> config.general.wikiLookup.officialWiki,
                                        newValue -> config.general.wikiLookup.officialWiki = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Special Effects
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.general.specialEffects"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.specialEffects.rareDungeonDropEffects"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.general.specialEffects.rareDungeonDropEffects.@Tooltip")))
                                .binding(defaults.general.specialEffects.rareDungeonDropEffects,
                                        () -> config.general.specialEffects.rareDungeonDropEffects,
                                        newValue -> config.general.specialEffects.rareDungeonDropEffects = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.specialEffects.rareDyeDropEffects"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.general.specialEffects.rareDyeDropEffects.@Tooltip")))
                                .binding(defaults.general.specialEffects.rareDyeDropEffects,
                                        () -> config.general.specialEffects.rareDyeDropEffects,
                                        newValue -> config.general.specialEffects.rareDyeDropEffects = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .build())

                //Hitboxes
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.general.hitbox"))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.hitbox.oldFarmlandHitbox"))
                                .binding(defaults.general.hitbox.oldFarmlandHitbox,
                                        () -> config.general.hitbox.oldFarmlandHitbox,
                                        newValue -> config.general.hitbox.oldFarmlandHitbox = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.general.hitbox.oldLeverHitbox"))
                                .binding(defaults.general.hitbox.oldLeverHitbox,
                                        () -> config.general.hitbox.oldLeverHitbox,
                                        newValue -> config.general.hitbox.oldLeverHitbox = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("skyblocker.config.general.hitbox.oldMushroomHitbox"))
								.binding(defaults.general.hitbox.oldMushroomHitbox,
										() -> config.general.hitbox.oldMushroomHitbox,
										newValue -> config.general.hitbox.oldMushroomHitbox = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
                        .build())

                .build();
    }
}
