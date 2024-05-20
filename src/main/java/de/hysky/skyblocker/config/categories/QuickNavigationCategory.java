package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.configs.QuickNavigationConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.ItemControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.item.Item;
import net.minecraft.text.Text;

public class QuickNavigationCategory {
    public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("skyblocker.config.quickNav"))

                //Toggle
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.enableQuickNav"))
                        .binding(defaults.quickNav.enableQuickNav,
                                () -> config.quickNav.enableQuickNav,
                                newValue -> config.quickNav.enableQuickNav = newValue)
                        .controller(ConfigUtils::createBooleanController)
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
                .name(Text.translatable("skyblocker.config.quickNav.button", index))
                .collapsed(true)
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.button.render"))
                        .binding(defaultButton.render,
                                () -> button.render,
                                newValue -> button.render = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .build())
                .option(Option.<Item>createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.button.item.itemName"))
                        .binding(defaultButton.itemData.item,
                                () -> button.itemData.item,
                                newValue -> button.itemData.item = newValue)
                        .controller(ItemControllerBuilder::create)
                        .build())
                .option(Option.<Integer>createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.button.item.count"))
                        .binding(defaultButton.itemData.count,
                                () -> button.itemData.count,
                                newValue -> button.itemData.count = newValue)
                        .controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 99))
                        .build())
                .option(Option.<String>createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.button.item.components"))
                        .description(OptionDescription.of(Text.translatable("skyblocker.config.quickNav.button.item.components.@Tooltip")))
                        .binding(defaultButton.itemData.components,
                                () -> button.itemData.components,
                                newValue -> button.itemData.components = newValue)
                        .controller(StringControllerBuilder::create)
                        .build())
                .option(Option.<String>createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.button.uiTitle"))
                        .binding(defaultButton.uiTitle,
                                () -> button.uiTitle,
                                newValue -> button.uiTitle = newValue)
                        .controller(StringControllerBuilder::create)
                        .build())
                .option(Option.<String>createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.button.clickEvent"))
                        .binding(defaultButton.clickEvent,
                                () -> button.clickEvent,
                                newValue -> button.clickEvent = newValue)
                        .controller(StringControllerBuilder::create)
                        .build())
                .build();
    }
}
