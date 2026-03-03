package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.configs.QuickNavigationConfig;
import de.hysky.skyblocker.utils.render.gui.ItemSelectionPopup;
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer;
import net.azureaaron.dandelion.api.ButtonOption;
import net.azureaaron.dandelion.api.ConfigCategory;
import net.azureaaron.dandelion.api.Option;
import net.azureaaron.dandelion.api.OptionGroup;
import net.azureaaron.dandelion.api.controllers.IntegerController;
import net.azureaaron.dandelion.api.controllers.ItemController;
import net.azureaaron.dandelion.api.controllers.StringController;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

public class QuickNavigationCategory {
	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.id(SkyblockerMod.id("config/quicknav"))
				.name(Component.translatable("skyblocker.config.quickNav"))

				//Toggle
				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("skyblocker.config.quickNav.enableQuickNav"))
						.description(Component.translatable("skyblocker.config.quickNav.enableQuickNav.@Tooltip"))
						.binding(defaults.quickNav.enableQuickNav,
								() -> config.quickNav.enableQuickNav,
								newValue -> config.quickNav.enableQuickNav = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())

				//Buttons
				.group(quickNavButton(defaults.quickNav.button1, config.quickNav.button1, 1))
				.group(quickNavButton(defaults.quickNav.button2, config.quickNav.button2, 2))
				.group(quickNavButton(defaults.quickNav.button3, config.quickNav.button3, 3))
				.group(quickNavButton(defaults.quickNav.button4, config.quickNav.button4, 4))
				.group(quickNavButton(defaults.quickNav.button5, config.quickNav.button5, 5))
				.group(quickNavButton(defaults.quickNav.button6, config.quickNav.button6, 6))
				.group(quickNavButton(defaults.quickNav.button7, config.quickNav.button7, 7))
				.group(quickNavButton(defaults.quickNav.button8, config.quickNav.button8, 8))
				.group(quickNavButton(defaults.quickNav.button9, config.quickNav.button9, 9))
				.group(quickNavButton(defaults.quickNav.button10, config.quickNav.button10, 10))
				.group(quickNavButton(defaults.quickNav.button11, config.quickNav.button11, 11))
				.group(quickNavButton(defaults.quickNav.button12, config.quickNav.button12, 12))
				.group(quickNavButton(defaults.quickNav.button13, config.quickNav.button13, 13))
				.group(quickNavButton(defaults.quickNav.button14, config.quickNav.button14, 14))
				.build();
	}

	private static OptionGroup quickNavButton(QuickNavigationConfig.QuickNavItem defaultButton, QuickNavigationConfig.QuickNavItem button, int index) {
		return OptionGroup.createBuilder()
				.name(Component.translatable("skyblocker.config.quickNav.button", index))
				.collapsed(true)
				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("skyblocker.config.quickNav.button.render"))
						.binding(defaultButton.render,
								() -> button.render,
								newValue -> button.render = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Component.translatable("skyblocker.config.quickNav.button.doubleClick"))
						.binding(defaultButton.doubleClick,
								() -> button.doubleClick,
								newValue -> button.doubleClick = newValue)
						.controller(ConfigUtils.createBooleanController())
						.build())
				.option(ButtonOption.createBuilder()
						.name(Component.translatable("skyblocker.config.quickNav.button.chooseSkyblockItem"))
						.description(Component.translatable("skyblocker.config.quickNav.button.chooseSkyblockItem.@Tooltip"))
						.action(screen -> Minecraft.getInstance().setScreen(new ItemSelectionPopup(screen, item -> {
							if (item == null) return;
							button.itemData.item = item.getItem();
							button.itemData.components = ItemStackComponentizationFixer.componentsAsString(item);
						})))
						.prompt(Component.translatable("text.skyblocker.open"))
						.build())
				.option(Option.<Item>createBuilder()
						.name(Component.translatable("skyblocker.config.quickNav.button.item.itemName"))
						.binding(defaultButton.itemData.item,
								() -> button.itemData.item,
								newValue -> button.itemData.item = newValue)
						.controller(ItemController.createBuilder().build())
						.build())
				.option(Option.<Integer>createBuilder()
						.name(Component.translatable("skyblocker.config.quickNav.button.item.count"))
						.binding(defaultButton.itemData.count,
								() -> button.itemData.count,
								newValue -> button.itemData.count = newValue)
						.controller(IntegerController.createBuilder().range(1, 99).build())
						.build())
				.option(Option.<String>createBuilder()
						.name(Component.translatable("skyblocker.config.quickNav.button.item.components"))
						.description(Component.translatable("skyblocker.config.quickNav.button.item.components.@Tooltip"))
						.binding(defaultButton.itemData.components,
								() -> button.itemData.components,
								newValue -> button.itemData.components = newValue)
						.controller(StringController.createBuilder().build())
						.build())
				.option(Option.<String>createBuilder()
						.name(Component.translatable("skyblocker.config.quickNav.button.uiTitle"))
						.binding(defaultButton.uiTitle,
								() -> button.uiTitle,
								newValue -> button.uiTitle = newValue)
						.controller(StringController.createBuilder().build())
						.build())
				.option(Option.<String>createBuilder()
						.name(Component.translatable("skyblocker.config.quickNav.button.tooltip"))
						.description(Component.translatable("skyblocker.config.quickNav.button.tooltip.@Tooltip"))
						.binding(defaultButton.tooltip,
								() -> button.tooltip,
								newValue -> button.tooltip = newValue)
						.controller(StringController.createBuilder().build())
						.build())
				.option(Option.<String>createBuilder()
						.name(Component.translatable("skyblocker.config.quickNav.button.clickEvent"))
						.binding(defaultButton.clickEvent,
								() -> button.clickEvent,
								newValue -> button.clickEvent = newValue)
						.controller(StringController.createBuilder().build())
						.build())
				.build();
	}
}
