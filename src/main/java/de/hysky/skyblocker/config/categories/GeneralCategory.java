package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.SkyblockerScreen;
import de.hysky.skyblocker.UpdateNotifications;
import de.hysky.skyblocker.config.CommonTags;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.backup.ConfigBackupScreen;
import de.hysky.skyblocker.config.configs.GeneralConfig;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.AccessoryTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.CraftPriceTooltip;
import de.hysky.skyblocker.skyblock.item.wikilookup.WikiLookupManager;
import de.hysky.skyblocker.skyblock.shortcut.ShortcutsConfigScreen;
import de.hysky.skyblocker.skyblock.speedpreset.SpeedPresetsScreen;
import net.azureaaron.dandelion.api.ButtonOption;
import net.azureaaron.dandelion.api.ConfigCategory;
import net.azureaaron.dandelion.api.Option;
import net.azureaaron.dandelion.api.OptionGroup;
import net.azureaaron.dandelion.api.OptionListener.UpdateType;
import net.azureaaron.dandelion.api.controllers.FloatController;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class GeneralCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.id(SkyblockerMod.id("config/general"))
				.name(Component.translatable("skyblocker.config.general"))

				//Skyblocker Screen
				.option(ButtonOption.createBuilder()
						.name(Component.translatable("skyblocker.skyblockerScreen"))
						.prompt(Component.translatable("text.skyblocker.open"))
						.action(screen -> Minecraft.getInstance().setScreen(new SkyblockerScreen()))
						.build())

				//Ungrouped Options
				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("skyblocker.config.general.enableTips"))
						.binding(defaults.general.enableTips,
								() -> config.general.enableTips,
								newValue -> config.general.enableTips = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("skyblocker.config.general.updateNotifications"))
						.binding(UpdateNotifications.Config.DEFAULT.enabled(),
								() -> UpdateNotifications.config.getData().enabled(),
								newValue -> UpdateNotifications.config.setData(UpdateNotifications.config.getData().withEnabled(newValue)))
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<UpdateNotifications.Channel>createBuilder()
						.name(Component.translatable("skyblocker.config.general.updateNotifications.updateChannel"))
						.description(Component.translatable("skyblocker.config.general.updateNotifications.updateChannel.@Tooltip"))
						.binding(UpdateNotifications.Config.DEFAULT.channel(),
								() -> UpdateNotifications.config.getData().channel(),
								newValue -> UpdateNotifications.config.setData(UpdateNotifications.config.getData().withChannel(newValue)))
						.controller(ConfigUtils.createEnumController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("skyblocker.config.general.acceptReparty"))
						.binding(defaults.general.acceptReparty,
								() -> config.general.acceptReparty,
								newValue -> config.general.acceptReparty = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				//Config Backups
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.general.backup"))
						.collapsed(true)
						.option(ButtonOption.createBuilder()
								.name(Component.translatable("skyblocker.config.general.backup.manage"))
								.prompt(Component.translatable("text.skyblocker.open"))
								.action(screen -> Minecraft.getInstance().setScreen(new ConfigBackupScreen(screen)))
								.build())
						.build())
				// Speed Presets
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.general.speedPresets"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.speedPresets.enableSpeedPresets"))
								.binding(defaults.general.speedPresets.enableSpeedPresets,
										() -> config.general.speedPresets.enableSpeedPresets,
										newValue -> config.general.speedPresets.enableSpeedPresets = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(ButtonOption.createBuilder()
								.name(Component.translatable("skyblocker.config.general.speedPresets.config"))
								.prompt(Component.translatable("text.skyblocker.open"))
								.action(screen -> Minecraft.getInstance().setScreen(new SpeedPresetsScreen(screen)))
								.build())
						.build())

				//Shortcuts
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.general.shortcuts"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.shortcuts.enableShortcuts"))
								.description(Component.translatable("skyblocker.config.general.shortcuts.enableShortcuts.@Tooltip"))
								.binding(defaults.general.shortcuts.enableShortcuts,
										() -> config.general.shortcuts.enableShortcuts,
										newValue -> config.general.shortcuts.enableShortcuts = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.shortcuts.enableCommandShortcuts"))
								.description(Component.translatable("skyblocker.config.general.shortcuts.enableCommandShortcuts.@Tooltip"))
								.binding(defaults.general.shortcuts.enableCommandShortcuts,
										() -> config.general.shortcuts.enableCommandShortcuts,
										newValue -> config.general.shortcuts.enableCommandShortcuts = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.shortcuts.enableCommandArgShortcuts"))
								.description(Component.translatable("skyblocker.config.general.shortcuts.enableCommandArgShortcuts.@Tooltip"))
								.binding(defaults.general.shortcuts.enableCommandArgShortcuts,
										() -> config.general.shortcuts.enableCommandArgShortcuts,
										newValue -> config.general.shortcuts.enableCommandArgShortcuts = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.shortcuts.enableKeyBindingShortcuts"))
								.description(Component.translatable("skyblocker.config.general.shortcuts.enableKeyBindingShortcuts.@Tooltip"))
								.binding(defaults.general.shortcuts.enableKeyBindingShortcuts,
										() -> config.general.shortcuts.enableKeyBindingShortcuts,
										newValue -> config.general.shortcuts.enableKeyBindingShortcuts = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(ButtonOption.createBuilder()
								.name(Component.translatable("skyblocker.config.general.shortcuts.config"))
								.prompt(Component.translatable("text.skyblocker.open"))
								.action(screen -> Minecraft.getInstance().setScreen(new ShortcutsConfigScreen(screen)))
								.build())
						.build())

				//Quiver Warning
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.general.quiverWarning"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.quiverWarning.enableQuiverWarning"))
								.binding(defaults.general.quiverWarning.enableQuiverWarning,
										() -> config.general.quiverWarning.enableQuiverWarning,
										newValue -> config.general.quiverWarning.enableQuiverWarning = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.quiverWarning.enableQuiverWarningInDungeons"))
								.binding(defaults.general.quiverWarning.enableQuiverWarningInDungeons,
										() -> config.general.quiverWarning.enableQuiverWarningInDungeons,
										newValue -> config.general.quiverWarning.enableQuiverWarningInDungeons = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.quiverWarning.enableQuiverWarningAfterDungeon"))
								.binding(defaults.general.quiverWarning.enableQuiverWarningAfterDungeon,
										() -> config.general.quiverWarning.enableQuiverWarningAfterDungeon,
										newValue -> config.general.quiverWarning.enableQuiverWarningAfterDungeon = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				//Item List
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.general.itemList"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemList.enableItemList"))
								.binding(defaults.general.itemList.enableItemList,
										() -> config.general.itemList.enableItemList,
										newValue -> config.general.itemList.enableItemList = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemList.enableCollapsibleEntries"))
								.description(Component.translatable("skyblocker.config.general.itemList.enableCollapsibleEntries.@Tooltip"))
								.binding(defaults.general.itemList.enableCollapsibleEntries,
										() -> config.general.itemList.enableCollapsibleEntries,
										newValue -> config.general.itemList.enableCollapsibleEntries = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				//Item Tooltip
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.general.itemTooltip"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemTooltip.enableNPCPrice"))
								.binding(defaults.general.itemTooltip.enableNPCPrice,
										() -> config.general.itemTooltip.enableNPCPrice,
										newValue -> config.general.itemTooltip.enableNPCPrice = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemTooltip.enableMotesPrice"))
								.description(Component.translatable("skyblocker.config.general.itemTooltip.enableMotesPrice.@Tooltip"))
								.binding(defaults.general.itemTooltip.enableMotesPrice,
										() -> config.general.itemTooltip.enableMotesPrice,
										newValue -> config.general.itemTooltip.enableMotesPrice = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemTooltip.enableAvgBIN"))
								.binding(defaults.general.itemTooltip.enableAvgBIN,
										() -> config.general.itemTooltip.enableAvgBIN,
										newValue -> config.general.itemTooltip.enableAvgBIN = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<GeneralConfig.Average>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemTooltip.avg"))
								.description(Component.translatable("skyblocker.config.general.itemTooltip.avg.@Tooltip"))
								.binding(defaults.general.itemTooltip.avg,
										() -> config.general.itemTooltip.avg,
										newValue -> config.general.itemTooltip.avg = newValue)
								.controller(ConfigUtils.createEnumController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemTooltip.enableLowestBIN"))
								.binding(defaults.general.itemTooltip.enableLowestBIN,
										() -> config.general.itemTooltip.enableLowestBIN,
										newValue -> config.general.itemTooltip.enableLowestBIN = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemTooltip.enableBazaarPrice"))
								.binding(defaults.general.itemTooltip.enableBazaarPrice,
										() -> config.general.itemTooltip.enableBazaarPrice,
										newValue -> config.general.itemTooltip.enableBazaarPrice = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<GeneralConfig.Craft>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemTooltip.craft"))
								.binding(defaults.general.itemTooltip.enableCraftingCost,
										() -> config.general.itemTooltip.enableCraftingCost,
										newValue -> config.general.itemTooltip.enableCraftingCost = newValue)
								.listener((ignored, updateType) -> {
									if (updateType == UpdateType.VALUE_CHANGE) CraftPriceTooltip.clearCache();
								})
								.controller(ConfigUtils.createEnumController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemTooltip.enableObtainedDate"))
								.binding(defaults.general.itemTooltip.enableObtainedDate,
										() -> config.general.itemTooltip.enableObtainedDate,
										newValue -> config.general.itemTooltip.enableObtainedDate = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemTooltip.enableMuseumInfo"))
								.description(Component.translatable("skyblocker.config.general.itemTooltip.enableMuseumInfo.@Tooltip"))
								.binding(defaults.general.itemTooltip.enableMuseumInfo,
										() -> config.general.itemTooltip.enableMuseumInfo,
										newValue -> config.general.itemTooltip.enableMuseumInfo = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemTooltip.enableExoticTooltip"))
								.description(Component.translatable("skyblocker.config.general.itemTooltip.enableExoticTooltip.@Tooltip"))
								.binding(defaults.general.itemTooltip.enableExoticTooltip,
										() -> config.general.itemTooltip.enableExoticTooltip,
										newValue -> config.general.itemTooltip.enableExoticTooltip = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemTooltip.enableAccessoriesHelper"))
								.description(Component.translatable("skyblocker.config.general.itemTooltip.enableAccessoriesHelper.@Tooltip[0]"),
										Component.literal("\n\n✔ Collected").withColor(AccessoryTooltip.COLLECTED_COLOUR),
										Component.translatable("skyblocker.config.general.itemTooltip.enableAccessoriesHelper.@Tooltip[1]"),
										Component.literal("\n✦ Upgrade").withColor(AccessoryTooltip.UPGRADE_COLOUR),
										Component.translatable("skyblocker.config.general.itemTooltip.enableAccessoriesHelper.@Tooltip[2]"),
										Component.literal("\n↑ Upgradable").withColor(AccessoryTooltip.UPGRADABLE_COLOUR),
										Component.translatable("skyblocker.config.general.itemTooltip.enableAccessoriesHelper.@Tooltip[3]"),
										Component.literal("\n↓ Downgrade").withColor(AccessoryTooltip.DOWNGRADE_COLOUR),
										Component.translatable("skyblocker.config.general.itemTooltip.enableAccessoriesHelper.@Tooltip[4]"),
										Component.literal("\n✖ Missing").withColor(AccessoryTooltip.MISSING_COLOUR),
										Component.translatable("skyblocker.config.general.itemTooltip.enableAccessoriesHelper.@Tooltip[5]"))
								.binding(defaults.general.itemTooltip.enableAccessoriesHelper,
										() -> config.general.itemTooltip.enableAccessoriesHelper,
										newValue -> config.general.itemTooltip.enableAccessoriesHelper = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemTooltip.dungeonQuality"))
								.description(Component.translatable("skyblocker.config.general.itemTooltip.dungeonQuality.@Tooltip"))
								.binding(defaults.general.itemTooltip.dungeonQuality,
										() -> config.general.itemTooltip.dungeonQuality,
										newValue -> config.general.itemTooltip.dungeonQuality = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemTooltip.showEssenceCost"))
								.binding(defaults.general.itemTooltip.showEssenceCost,
										() -> config.general.itemTooltip.showEssenceCost,
										newValue -> config.general.itemTooltip.showEssenceCost = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemTooltip.enableEstimatedItemValue"))
								.description(Component.translatable("skyblocker.config.general.itemTooltip.enableEstimatedItemValue.@Tooltip"))
								.binding(defaults.general.itemTooltip.enableEstimatedItemValue,
										() -> config.general.itemTooltip.enableEstimatedItemValue,
										newValue -> config.general.itemTooltip.enableEstimatedItemValue = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemTooltip.enableStackingEnchantProgress"))
								.description(Component.translatable("skyblocker.config.general.itemTooltip.enableStackingEnchantProgress.@Tooltip"))
								.binding(defaults.general.itemTooltip.enableStackingEnchantProgress,
										() -> config.general.itemTooltip.enableStackingEnchantProgress,
										newValue -> config.general.itemTooltip.enableStackingEnchantProgress = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemTooltip.enableEvolvingItemProgress"))
								.description(Component.translatable("skyblocker.config.general.itemTooltip.enableEvolvingItemProgress.@Tooltip"))
								.tags(CommonTags.ADDED_IN_6_0_0)
								.binding(defaults.general.itemTooltip.enableEvolvingItemProgress,
										() -> config.general.itemTooltip.enableEvolvingItemProgress,
										newValue -> config.general.itemTooltip.enableEvolvingItemProgress = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				//Item Info Display
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.general.itemInfoDisplay"))
						.collapsed(true)
						.option(Option.<GeneralConfig.ItemBackgroundStyle>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemInfoDisplay.itemBackgroundStyle"))
								.description(Component.translatable("skyblocker.config.general.itemInfoDisplay.itemBackgroundStyle.@Tooltip"))
								.binding(defaults.general.itemInfoDisplay.itemBackgroundStyle,
										() -> config.general.itemInfoDisplay.itemBackgroundStyle,
										newValue -> config.general.itemInfoDisplay.itemBackgroundStyle = newValue)
								.controller(ConfigUtils.createEnumController())
								.build())
						.option(Option.<Float>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemInfoDisplay.itemBackgroundOpacity"))
								.binding(defaults.general.itemInfoDisplay.itemBackgroundOpacity,
										() -> config.general.itemInfoDisplay.itemBackgroundOpacity,
										newValue -> config.general.itemInfoDisplay.itemBackgroundOpacity = newValue)
								.controller(FloatController.createBuilder().range(0f, 1f).slider(0.05f).build())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemInfoDisplay.itemRarityBackgrounds"))
								.description(Component.translatable("skyblocker.config.general.itemInfoDisplay.itemRarityBackgrounds.@Tooltip"))
								.binding(defaults.general.itemInfoDisplay.itemRarityBackgrounds,
										() -> config.general.itemInfoDisplay.itemRarityBackgrounds,
										newValue -> config.general.itemInfoDisplay.itemRarityBackgrounds = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemInfoDisplay.jacobMedalBackgrounds"))
								.description(Component.translatable("skyblocker.config.general.itemInfoDisplay.jacobMedalBackgrounds.@Tooltip"))
								.binding(defaults.general.itemInfoDisplay.jacobMedalBackgrounds,
										() -> config.general.itemInfoDisplay.jacobMedalBackgrounds,
										newValue -> config.general.itemInfoDisplay.jacobMedalBackgrounds = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemInfoDisplay.legacyAttributeBackgrounds"))
								.description(Component.translatable("skyblocker.config.general.itemInfoDisplay.legacyAttributeBackgrounds.@Tooltip"))
								.binding(defaults.general.itemInfoDisplay.legacyAttributeBackgrounds,
										() -> config.general.itemInfoDisplay.legacyAttributeBackgrounds,
										newValue -> config.general.itemInfoDisplay.legacyAttributeBackgrounds = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				//Item Protection
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.general.itemProtection"))
						.collapsed(true)
						.option(Option.<GeneralConfig.SlotLockStyle>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemProtection.slotLockStyle"))
								.description(Component.translatable("skyblocker.config.general.itemProtection.slotLockStyle.@Tooltip"))
								.binding(defaults.general.itemProtection.slotLockStyle,
										() -> config.general.itemProtection.slotLockStyle,
										newValue -> config.general.itemProtection.slotLockStyle = newValue)
								.controller(ConfigUtils.createEnumController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemProtection.protectValuableConsumables"))
								.description(Component.translatable("skyblocker.config.general.itemProtection.protectValuableConsumables.@Tooltip"))
								.binding(defaults.general.itemProtection.protectValuableConsumables,
										() -> config.general.itemProtection.protectValuableConsumables,
										newValue -> config.general.itemProtection.protectValuableConsumables = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.itemProtection.displayChatNotification"))
								.description(Component.translatable("skyblocker.config.general.itemProtection.displayChatNotification.@Tooltip"))
								.binding(defaults.general.itemProtection.displayChatNotification,
										() -> config.general.itemProtection.displayChatNotification,
										newValue -> config.general.itemProtection.displayChatNotification = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				//Wiki Lookup
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.general.wikiLookup"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.wikiLookup.enableWikiLookup"))
								.description(Component.translatable("skyblocker.config.general.wikiLookup.enableWikiLookup.@Tooltip",
										WikiLookupManager.officialWikiLookup.getTranslatedKeyMessage(), WikiLookupManager.fandomWikiLookup.getTranslatedKeyMessage()))
								.binding(defaults.general.wikiLookup.enableWikiLookup,
										() -> config.general.wikiLookup.enableWikiLookup,
										newValue -> config.general.wikiLookup.enableWikiLookup = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(ConfigUtils.createShortcutToKeybindsScreen())
						.build())

				//Special Effects
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.general.specialEffects"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.specialEffects.rareDungeonDropEffects"))
								.description(Component.translatable("skyblocker.config.general.specialEffects.rareDungeonDropEffects.@Tooltip"))
								.binding(defaults.general.specialEffects.rareDungeonDropEffects,
										() -> config.general.specialEffects.rareDungeonDropEffects,
										newValue -> config.general.specialEffects.rareDungeonDropEffects = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.specialEffects.rareDropEffects"))
								.description(Component.translatable("skyblocker.config.general.specialEffects.rareDropEffects.@Tooltip"))
								.tags(CommonTags.ADDED_IN_5_11_0)
								.binding(defaults.general.specialEffects.rareDropEffects,
										() -> config.general.specialEffects.rareDropEffects,
										newValue -> config.general.specialEffects.rareDropEffects = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.specialEffects.rareDyeDropEffects"))
								.description(Component.translatable("skyblocker.config.general.specialEffects.rareDyeDropEffects.@Tooltip"))
								.binding(defaults.general.specialEffects.rareDyeDropEffects,
										() -> config.general.specialEffects.rareDyeDropEffects,
										newValue -> config.general.specialEffects.rareDyeDropEffects = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())
				//Hitboxes
				.group(OptionGroup.createBuilder()
						.name(Component.translatable("skyblocker.config.general.hitbox"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.hitbox.oldCactusHitbox"))
								.binding(defaults.general.hitbox.oldCactusHitbox,
										() -> config.general.hitbox.oldCactusHitbox,
										newValue -> config.general.hitbox.oldCactusHitbox = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.hitbox.oldLeverHitbox"))
								.binding(defaults.general.hitbox.oldLeverHitbox,
										() -> config.general.hitbox.oldLeverHitbox,
										newValue -> config.general.hitbox.oldLeverHitbox = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("skyblocker.config.general.hitbox.oldMushroomHitbox"))
								.binding(defaults.general.hitbox.oldMushroomHitbox,
										() -> config.general.hitbox.oldMushroomHitbox,
										newValue -> config.general.hitbox.oldMushroomHitbox = newValue)
								.controller(ConfigUtils.createBooleanController())
								.build())
						.build())

				.build();
	}
}
