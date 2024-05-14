package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
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

                //Enable Extended Quick Nav Buttons
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.enableExtendedQuickNav"))
                        .binding(defaults.quickNav.enableExtendedQuickNav,
                                () -> config.quickNav.enableExtendedQuickNav,
                                newValue -> config.quickNav.enableExtendedQuickNav = newValue)
                        .controller(ConfigUtils::createBooleanController)
                        .build())

                //Button 1
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.button", 1))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.render"))
                                .binding(defaults.quickNav.button1.render,
                                        () -> config.quickNav.button1.render,
                                        newValue -> config.quickNav.button1.render = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.itemName"))
                                .binding(defaults.quickNav.button1.item.id,
                                        () -> config.quickNav.button1.item.id,
                                        newValue -> config.quickNav.button1.item.id = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.count"))
                                .binding(defaults.quickNav.button1.item.count,
                                        () -> config.quickNav.button1.item.count,
                                        newValue -> config.quickNav.button1.item.count = newValue)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 99))
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.components"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.quickNav.button.item.components.@Tooltip")))
                                .binding(defaults.quickNav.button1.item.components,
                                        () -> config.quickNav.button1.item.components,
                                        newValue -> config.quickNav.button1.item.components = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.uiTitle"))
                                .binding(defaults.quickNav.button1.uiTitle,
                                        () -> config.quickNav.button1.uiTitle,
                                        newValue -> config.quickNav.button1.uiTitle = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.clickEvent"))
                                .binding(defaults.quickNav.button1.clickEvent,
                                        () -> config.quickNav.button1.clickEvent,
                                        newValue -> config.quickNav.button1.clickEvent = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .build())

                //Button 2
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.button", 2))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.render"))
                                .binding(defaults.quickNav.button2.render,
                                        () -> config.quickNav.button2.render,
                                        newValue -> config.quickNav.button2.render = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.itemName"))
                                .binding(defaults.quickNav.button2.item.id,
                                        () -> config.quickNav.button2.item.id,
                                        newValue -> config.quickNav.button2.item.id = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.count"))
                                .binding(defaults.quickNav.button2.item.count,
                                        () -> config.quickNav.button2.item.count,
                                        newValue -> config.quickNav.button2.item.count = newValue)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 99))
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.components"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.quickNav.button.item.components.@Tooltip")))
                                .binding(defaults.quickNav.button2.item.components,
                                        () -> config.quickNav.button2.item.components,
                                        newValue -> config.quickNav.button2.item.components = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.uiTitle"))
                                .binding(defaults.quickNav.button2.uiTitle,
                                        () -> config.quickNav.button2.uiTitle,
                                        newValue -> config.quickNav.button2.uiTitle = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.clickEvent"))
                                .binding(defaults.quickNav.button2.clickEvent,
                                        () -> config.quickNav.button2.clickEvent,
                                        newValue -> config.quickNav.button2.clickEvent = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .build())

                //Button 3
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.button", 3))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.render"))
                                .binding(defaults.quickNav.button3.render,
                                        () -> config.quickNav.button3.render,
                                        newValue -> config.quickNav.button3.render = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.itemName"))
                                .binding(defaults.quickNav.button3.item.id,
                                        () -> config.quickNav.button3.item.id,
                                        newValue -> config.quickNav.button3.item.id = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.count"))
                                .binding(defaults.quickNav.button3.item.count,
                                        () -> config.quickNav.button3.item.count,
                                        newValue -> config.quickNav.button3.item.count = newValue)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 99))
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.components"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.quickNav.button.item.components.@Tooltip")))
                                .binding(defaults.quickNav.button3.item.components,
                                        () -> config.quickNav.button3.item.components,
                                        newValue -> config.quickNav.button3.item.components = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.uiTitle"))
                                .binding(defaults.quickNav.button3.uiTitle,
                                        () -> config.quickNav.button3.uiTitle,
                                        newValue -> config.quickNav.button3.uiTitle = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.clickEvent"))
                                .binding(defaults.quickNav.button3.clickEvent,
                                        () -> config.quickNav.button3.clickEvent,
                                        newValue -> config.quickNav.button3.clickEvent = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .build())

                //Button 4
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.button", 4))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.render"))
                                .binding(defaults.quickNav.button4.render,
                                        () -> config.quickNav.button4.render,
                                        newValue -> config.quickNav.button4.render = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.itemName"))
                                .binding(defaults.quickNav.button4.item.id,
                                        () -> config.quickNav.button4.item.id,
                                        newValue -> config.quickNav.button4.item.id = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.count"))
                                .binding(defaults.quickNav.button4.item.count,
                                        () -> config.quickNav.button4.item.count,
                                        newValue -> config.quickNav.button4.item.count = newValue)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 99))
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.components"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.quickNav.button.item.components.@Tooltip")))
                                .binding(defaults.quickNav.button4.item.components,
                                        () -> config.quickNav.button4.item.components,
                                        newValue -> config.quickNav.button4.item.components = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.uiTitle"))
                                .binding(defaults.quickNav.button4.uiTitle,
                                        () -> config.quickNav.button4.uiTitle,
                                        newValue -> config.quickNav.button4.uiTitle = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.clickEvent"))
                                .binding(defaults.quickNav.button4.clickEvent,
                                        () -> config.quickNav.button4.clickEvent,
                                        newValue -> config.quickNav.button4.clickEvent = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .build())

                //Button 5
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.button", 5))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.render"))
                                .binding(defaults.quickNav.button5.render,
                                        () -> config.quickNav.button5.render,
                                        newValue -> config.quickNav.button5.render = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.itemName"))
                                .binding(defaults.quickNav.button5.item.id,
                                        () -> config.quickNav.button5.item.id,
                                        newValue -> config.quickNav.button5.item.id = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.count"))
                                .binding(defaults.quickNav.button5.item.count,
                                        () -> config.quickNav.button5.item.count,
                                        newValue -> config.quickNav.button5.item.count = newValue)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 99))
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.components"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.quickNav.button.item.components.@Tooltip")))
                                .binding(defaults.quickNav.button5.item.components,
                                        () -> config.quickNav.button5.item.components,
                                        newValue -> config.quickNav.button5.item.components = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.uiTitle"))
                                .binding(defaults.quickNav.button5.uiTitle,
                                        () -> config.quickNav.button5.uiTitle,
                                        newValue -> config.quickNav.button5.uiTitle = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.clickEvent"))
                                .binding(defaults.quickNav.button5.clickEvent,
                                        () -> config.quickNav.button5.clickEvent,
                                        newValue -> config.quickNav.button5.clickEvent = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .build())

                //Button 6
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.button", 6))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.render"))
                                .binding(defaults.quickNav.button6.render,
                                        () -> config.quickNav.button6.render,
                                        newValue -> config.quickNav.button6.render = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.itemName"))
                                .binding(defaults.quickNav.button6.item.id,
                                        () -> config.quickNav.button6.item.id,
                                        newValue -> config.quickNav.button6.item.id = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.count"))
                                .binding(defaults.quickNav.button6.item.count,
                                        () -> config.quickNav.button6.item.count,
                                        newValue -> config.quickNav.button6.item.count = newValue)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 99))
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.components"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.quickNav.button.item.components.@Tooltip")))
                                .binding(defaults.quickNav.button6.item.components,
                                        () -> config.quickNav.button6.item.components,
                                        newValue -> config.quickNav.button6.item.components = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.uiTitle"))
                                .binding(defaults.quickNav.button6.uiTitle,
                                        () -> config.quickNav.button6.uiTitle,
                                        newValue -> config.quickNav.button6.uiTitle = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.clickEvent"))
                                .binding(defaults.quickNav.button6.clickEvent,
                                        () -> config.quickNav.button6.clickEvent,
                                        newValue -> config.quickNav.button6.clickEvent = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .build())

                //Button 7
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.button", 7))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.render"))
                                .binding(defaults.quickNav.button7.render,
                                        () -> config.quickNav.button7.render,
                                        newValue -> config.quickNav.button7.render = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.itemName"))
                                .binding(defaults.quickNav.button7.item.id,
                                        () -> config.quickNav.button7.item.id,
                                        newValue -> config.quickNav.button7.item.id = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.count"))
                                .binding(defaults.quickNav.button7.item.count,
                                        () -> config.quickNav.button7.item.count,
                                        newValue -> config.quickNav.button7.item.count = newValue)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 99))
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.components"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.quickNav.button.item.components.@Tooltip")))
                                .binding(defaults.quickNav.button7.item.components,
                                        () -> config.quickNav.button7.item.components,
                                        newValue -> config.quickNav.button7.item.components = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.uiTitle"))
                                .binding(defaults.quickNav.button7.uiTitle,
                                        () -> config.quickNav.button7.uiTitle,
                                        newValue -> config.quickNav.button7.uiTitle = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.clickEvent"))
                                .binding(defaults.quickNav.button7.clickEvent,
                                        () -> config.quickNav.button7.clickEvent,
                                        newValue -> config.quickNav.button7.clickEvent = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .build())

                //Button 8
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.button", 8))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.render"))
                                .binding(defaults.quickNav.button8.render,
                                        () -> config.quickNav.button8.render,
                                        newValue -> config.quickNav.button8.render = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.itemName"))
                                .binding(defaults.quickNav.button8.item.id,
                                        () -> config.quickNav.button8.item.id,
                                        newValue -> config.quickNav.button8.item.id = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.count"))
                                .binding(defaults.quickNav.button8.item.count,
                                        () -> config.quickNav.button8.item.count,
                                        newValue -> config.quickNav.button8.item.count = newValue)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 99))
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.components"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.quickNav.button.item.components.@Tooltip")))
                                .binding(defaults.quickNav.button8.item.components,
                                        () -> config.quickNav.button8.item.components,
                                        newValue -> config.quickNav.button8.item.components = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.uiTitle"))
                                .binding(defaults.quickNav.button8.uiTitle,
                                        () -> config.quickNav.button8.uiTitle,
                                        newValue -> config.quickNav.button8.uiTitle = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.clickEvent"))
                                .binding(defaults.quickNav.button8.clickEvent,
                                        () -> config.quickNav.button8.clickEvent,
                                        newValue -> config.quickNav.button8.clickEvent = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .build())

                //Button 9
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.button", 9))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.render"))
                                .binding(defaults.quickNav.button9.render,
                                        () -> config.quickNav.button9.render,
                                        newValue -> config.quickNav.button9.render = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.itemName"))
                                .binding(defaults.quickNav.button9.item.id,
                                        () -> config.quickNav.button9.item.id,
                                        newValue -> config.quickNav.button9.item.id = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.count"))
                                .binding(defaults.quickNav.button9.item.count,
                                        () -> config.quickNav.button9.item.count,
                                        newValue -> config.quickNav.button9.item.count = newValue)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 99))
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.components"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.quickNav.button.item.components.@Tooltip")))
                                .binding(defaults.quickNav.button9.item.components,
                                        () -> config.quickNav.button9.item.components,
                                        newValue -> config.quickNav.button9.item.components = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.uiTitle"))
                                .binding(defaults.quickNav.button9.uiTitle,
                                        () -> config.quickNav.button9.uiTitle,
                                        newValue -> config.quickNav.button9.uiTitle = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.clickEvent"))
                                .binding(defaults.quickNav.button9.clickEvent,
                                        () -> config.quickNav.button9.clickEvent,
                                        newValue -> config.quickNav.button9.clickEvent = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .build())

                //Button 10
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.button", 10))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.render"))
                                .binding(defaults.quickNav.button10.render,
                                        () -> config.quickNav.button10.render,
                                        newValue -> config.quickNav.button10.render = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.itemName"))
                                .binding(defaults.quickNav.button10.item.id,
                                        () -> config.quickNav.button10.item.id,
                                        newValue -> config.quickNav.button10.item.id = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.count"))
                                .binding(defaults.quickNav.button10.item.count,
                                        () -> config.quickNav.button10.item.count,
                                        newValue -> config.quickNav.button10.item.count = newValue)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 99))
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.components"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.quickNav.button.item.components.@Tooltip")))
                                .binding(defaults.quickNav.button10.item.components,
                                        () -> config.quickNav.button10.item.components,
                                        newValue -> config.quickNav.button10.item.components = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.uiTitle"))
                                .binding(defaults.quickNav.button10.uiTitle,
                                        () -> config.quickNav.button10.uiTitle,
                                        newValue -> config.quickNav.button10.uiTitle = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.clickEvent"))
                                .binding(defaults.quickNav.button10.clickEvent,
                                        () -> config.quickNav.button10.clickEvent,
                                        newValue -> config.quickNav.button10.clickEvent = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .build())

                //Button 11
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.button", 11))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.render"))
                                .binding(defaults.quickNav.button11.render,
                                        () -> config.quickNav.button11.render,
                                        newValue -> config.quickNav.button11.render = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.itemName"))
                                .binding(defaults.quickNav.button11.item.id,
                                        () -> config.quickNav.button11.item.id,
                                        newValue -> config.quickNav.button11.item.id = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.count"))
                                .binding(defaults.quickNav.button11.item.count,
                                        () -> config.quickNav.button11.item.count,
                                        newValue -> config.quickNav.button11.item.count = newValue)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 99))
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.components"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.quickNav.button.item.components.@Tooltip")))
                                .binding(defaults.quickNav.button11.item.components,
                                        () -> config.quickNav.button11.item.components,
                                        newValue -> config.quickNav.button11.item.components = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.uiTitle"))
                                .binding(defaults.quickNav.button11.uiTitle,
                                        () -> config.quickNav.button11.uiTitle,
                                        newValue -> config.quickNav.button11.uiTitle = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.clickEvent"))
                                .binding(defaults.quickNav.button11.clickEvent,
                                        () -> config.quickNav.button11.clickEvent,
                                        newValue -> config.quickNav.button11.clickEvent = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .build())

                //Button 12
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.button", 12))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.render"))
                                .binding(defaults.quickNav.button12.render,
                                        () -> config.quickNav.button12.render,
                                        newValue -> config.quickNav.button12.render = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.itemName"))
                                .binding(defaults.quickNav.button12.item.id,
                                        () -> config.quickNav.button12.item.id,
                                        newValue -> config.quickNav.button12.item.id = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.count"))
                                .binding(defaults.quickNav.button12.item.count,
                                        () -> config.quickNav.button12.item.count,
                                        newValue -> config.quickNav.button12.item.count = newValue)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 99))
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.components"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.quickNav.button.item.components.@Tooltip")))
                                .binding(defaults.quickNav.button12.item.components,
                                        () -> config.quickNav.button12.item.components,
                                        newValue -> config.quickNav.button12.item.components = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.uiTitle"))
                                .binding(defaults.quickNav.button12.uiTitle,
                                        () -> config.quickNav.button12.uiTitle,
                                        newValue -> config.quickNav.button12.uiTitle = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.clickEvent"))
                                .binding(defaults.quickNav.button12.clickEvent,
                                        () -> config.quickNav.button12.clickEvent,
                                        newValue -> config.quickNav.button12.clickEvent = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .build())

                //Button 13
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.button", 13))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.render"))
                                .binding(defaults.quickNav.button13.render,
                                        () -> config.quickNav.button13.render,
                                        newValue -> config.quickNav.button13.render = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.itemName"))
                                .binding(defaults.quickNav.button13.item.id,
                                        () -> config.quickNav.button13.item.id,
                                        newValue -> config.quickNav.button13.item.id = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.count"))
                                .binding(defaults.quickNav.button13.item.count,
                                        () -> config.quickNav.button13.item.count,
                                        newValue -> config.quickNav.button13.item.count = newValue)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 99))
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.components"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.quickNav.button.item.components.@Tooltip")))
                                .binding(defaults.quickNav.button13.item.components,
                                        () -> config.quickNav.button13.item.components,
                                        newValue -> config.quickNav.button13.item.components = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.uiTitle"))
                                .binding(defaults.quickNav.button13.uiTitle,
                                        () -> config.quickNav.button13.uiTitle,
                                        newValue -> config.quickNav.button13.uiTitle = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.clickEvent"))
                                .binding(defaults.quickNav.button13.clickEvent,
                                        () -> config.quickNav.button13.clickEvent,
                                        newValue -> config.quickNav.button13.clickEvent = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .build())

                //Button 14
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("skyblocker.config.quickNav.button", 14))
                        .collapsed(true)
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.render"))
                                .binding(defaults.quickNav.button14.render,
                                        () -> config.quickNav.button14.render,
                                        newValue -> config.quickNav.button14.render = newValue)
                                .controller(ConfigUtils::createBooleanController)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.itemName"))
                                .binding(defaults.quickNav.button14.item.id,
                                        () -> config.quickNav.button14.item.id,
                                        newValue -> config.quickNav.button14.item.id = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.count"))
                                .binding(defaults.quickNav.button14.item.count,
                                        () -> config.quickNav.button14.item.count,
                                        newValue -> config.quickNav.button14.item.count = newValue)
                                .controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 99))
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.item.components"))
                                .description(OptionDescription.of(Text.translatable("skyblocker.config.quickNav.button.item.components.@Tooltip")))
                                .binding(defaults.quickNav.button14.item.components,
                                        () -> config.quickNav.button14.item.components,
                                        newValue -> config.quickNav.button14.item.components = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.uiTitle"))
                                .binding(defaults.quickNav.button14.uiTitle,
                                        () -> config.quickNav.button14.uiTitle,
                                        newValue -> config.quickNav.button14.uiTitle = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("skyblocker.config.quickNav.button.clickEvent"))
                                .binding(defaults.quickNav.button14.clickEvent,
                                        () -> config.quickNav.button14.clickEvent,
                                        newValue -> config.quickNav.button14.clickEvent = newValue)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .build())
                .build();
    }
}
