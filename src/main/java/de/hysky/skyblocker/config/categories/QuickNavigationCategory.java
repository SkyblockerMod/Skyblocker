package de.hysky.skyblocker.config.categories;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.text.Text;

public class QuickNavigationCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.name(Text.translatable("text.autoconfig.skyblocker.category.quickNav"))
				
				//Toggle
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.enableQuickNav"))
						.binding(defaults.quickNav.enableQuickNav,
								() -> config.quickNav.enableQuickNav,
								newValue -> config.quickNav.enableQuickNav = newValue)
						.controller(ConfigUtils::createBooleanController)
						.build())
				
				//Button 1
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.render"))
								.binding(defaults.quickNav.button1.render,
										() -> config.quickNav.button1.render,
										newValue -> config.quickNav.button1.render = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.itemName"))
								.binding(defaults.quickNav.button1.item.itemName,
										() -> config.quickNav.button1.item.itemName,
										newValue -> config.quickNav.button1.item.itemName = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.count"))
								.binding(defaults.quickNav.button1.item.count,
										() -> config.quickNav.button1.item.count,
										newValue -> config.quickNav.button1.item.count = newValue)
								.controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 64))
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.nbt"))
								.binding(defaults.quickNav.button1.item.nbt,
										() -> config.quickNav.button1.item.nbt,
										newValue -> config.quickNav.button1.item.nbt = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.uiTitle"))
								.binding(defaults.quickNav.button1.uiTitle,
										() -> config.quickNav.button1.uiTitle,
										newValue -> config.quickNav.button1.uiTitle = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.clickEvent"))
								.binding(defaults.quickNav.button1.clickEvent,
										() -> config.quickNav.button1.clickEvent,
										newValue -> config.quickNav.button1.clickEvent = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.build())
				
				//Button 2
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button2"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.render"))
								.binding(defaults.quickNav.button2.render,
										() -> config.quickNav.button2.render,
										newValue -> config.quickNav.button2.render = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.itemName"))
								.binding(defaults.quickNav.button2.item.itemName,
										() -> config.quickNav.button2.item.itemName,
										newValue -> config.quickNav.button2.item.itemName = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.count"))
								.binding(defaults.quickNav.button2.item.count,
										() -> config.quickNav.button2.item.count,
										newValue -> config.quickNav.button2.item.count = newValue)
								.controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 64))
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.nbt"))
								.binding(defaults.quickNav.button2.item.nbt,
										() -> config.quickNav.button2.item.nbt,
										newValue -> config.quickNav.button2.item.nbt = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.uiTitle"))
								.binding(defaults.quickNav.button2.uiTitle,
										() -> config.quickNav.button2.uiTitle,
										newValue -> config.quickNav.button2.uiTitle = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.clickEvent"))
								.binding(defaults.quickNav.button2.clickEvent,
										() -> config.quickNav.button2.clickEvent,
										newValue -> config.quickNav.button2.clickEvent = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.build())
				
				//Button 3
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button3"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.render"))
								.binding(defaults.quickNav.button3.render,
										() -> config.quickNav.button3.render,
										newValue -> config.quickNav.button3.render = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.itemName"))
								.binding(defaults.quickNav.button3.item.itemName,
										() -> config.quickNav.button3.item.itemName,
										newValue -> config.quickNav.button3.item.itemName = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.count"))
								.binding(defaults.quickNav.button3.item.count,
										() -> config.quickNav.button3.item.count,
										newValue -> config.quickNav.button3.item.count = newValue)
								.controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 64))
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.nbt"))
								.binding(defaults.quickNav.button3.item.nbt,
										() -> config.quickNav.button3.item.nbt,
										newValue -> config.quickNav.button3.item.nbt = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.uiTitle"))
								.binding(defaults.quickNav.button3.uiTitle,
										() -> config.quickNav.button3.uiTitle,
										newValue -> config.quickNav.button3.uiTitle = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.clickEvent"))
								.binding(defaults.quickNav.button3.clickEvent,
										() -> config.quickNav.button3.clickEvent,
										newValue -> config.quickNav.button3.clickEvent = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.build())
				
				//Button 4
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button4"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.render"))
								.binding(defaults.quickNav.button4.render,
										() -> config.quickNav.button4.render,
										newValue -> config.quickNav.button4.render = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.itemName"))
								.binding(defaults.quickNav.button4.item.itemName,
										() -> config.quickNav.button4.item.itemName,
										newValue -> config.quickNav.button4.item.itemName = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.count"))
								.binding(defaults.quickNav.button4.item.count,
										() -> config.quickNav.button4.item.count,
										newValue -> config.quickNav.button4.item.count = newValue)
								.controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 64))
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.nbt"))
								.binding(defaults.quickNav.button4.item.nbt,
										() -> config.quickNav.button4.item.nbt,
										newValue -> config.quickNav.button4.item.nbt = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.uiTitle"))
								.binding(defaults.quickNav.button4.uiTitle,
										() -> config.quickNav.button4.uiTitle,
										newValue -> config.quickNav.button4.uiTitle = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.clickEvent"))
								.binding(defaults.quickNav.button4.clickEvent,
										() -> config.quickNav.button4.clickEvent,
										newValue -> config.quickNav.button4.clickEvent = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.build())
				
				//Button 5
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button5"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.render"))
								.binding(defaults.quickNav.button5.render,
										() -> config.quickNav.button5.render,
										newValue -> config.quickNav.button5.render = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.itemName"))
								.binding(defaults.quickNav.button5.item.itemName,
										() -> config.quickNav.button5.item.itemName,
										newValue -> config.quickNav.button5.item.itemName = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.count"))
								.binding(defaults.quickNav.button5.item.count,
										() -> config.quickNav.button5.item.count,
										newValue -> config.quickNav.button5.item.count = newValue)
								.controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 64))
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.nbt"))
								.binding(defaults.quickNav.button5.item.nbt,
										() -> config.quickNav.button5.item.nbt,
										newValue -> config.quickNav.button5.item.nbt = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.uiTitle"))
								.binding(defaults.quickNav.button5.uiTitle,
										() -> config.quickNav.button5.uiTitle,
										newValue -> config.quickNav.button5.uiTitle = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.clickEvent"))
								.binding(defaults.quickNav.button5.clickEvent,
										() -> config.quickNav.button5.clickEvent,
										newValue -> config.quickNav.button5.clickEvent = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.build())
				
				//Button 6
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button6"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.render"))
								.binding(defaults.quickNav.button6.render,
										() -> config.quickNav.button6.render,
										newValue -> config.quickNav.button6.render = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.itemName"))
								.binding(defaults.quickNav.button6.item.itemName,
										() -> config.quickNav.button6.item.itemName,
										newValue -> config.quickNav.button6.item.itemName = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.count"))
								.binding(defaults.quickNav.button6.item.count,
										() -> config.quickNav.button6.item.count,
										newValue -> config.quickNav.button6.item.count = newValue)
								.controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 64))
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.nbt"))
								.binding(defaults.quickNav.button6.item.nbt,
										() -> config.quickNav.button6.item.nbt,
										newValue -> config.quickNav.button6.item.nbt = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.uiTitle"))
								.binding(defaults.quickNav.button6.uiTitle,
										() -> config.quickNav.button6.uiTitle,
										newValue -> config.quickNav.button6.uiTitle = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.clickEvent"))
								.binding(defaults.quickNav.button6.clickEvent,
										() -> config.quickNav.button6.clickEvent,
										newValue -> config.quickNav.button6.clickEvent = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.build())
				
				//Button 7
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button7"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.render"))
								.binding(defaults.quickNav.button7.render,
										() -> config.quickNav.button7.render,
										newValue -> config.quickNav.button7.render = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.itemName"))
								.binding(defaults.quickNav.button7.item.itemName,
										() -> config.quickNav.button7.item.itemName,
										newValue -> config.quickNav.button7.item.itemName = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.count"))
								.binding(defaults.quickNav.button7.item.count,
										() -> config.quickNav.button7.item.count,
										newValue -> config.quickNav.button7.item.count = newValue)
								.controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 64))
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.nbt"))
								.binding(defaults.quickNav.button7.item.nbt,
										() -> config.quickNav.button7.item.nbt,
										newValue -> config.quickNav.button7.item.nbt = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.uiTitle"))
								.binding(defaults.quickNav.button7.uiTitle,
										() -> config.quickNav.button7.uiTitle,
										newValue -> config.quickNav.button7.uiTitle = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.clickEvent"))
								.binding(defaults.quickNav.button7.clickEvent,
										() -> config.quickNav.button7.clickEvent,
										newValue -> config.quickNav.button7.clickEvent = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.build())
				
				//Button 8
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button8"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.render"))
								.binding(defaults.quickNav.button8.render,
										() -> config.quickNav.button8.render,
										newValue -> config.quickNav.button8.render = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.itemName"))
								.binding(defaults.quickNav.button8.item.itemName,
										() -> config.quickNav.button8.item.itemName,
										newValue -> config.quickNav.button8.item.itemName = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.count"))
								.binding(defaults.quickNav.button8.item.count,
										() -> config.quickNav.button8.item.count,
										newValue -> config.quickNav.button8.item.count = newValue)
								.controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 64))
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.nbt"))
								.binding(defaults.quickNav.button8.item.nbt,
										() -> config.quickNav.button8.item.nbt,
										newValue -> config.quickNav.button8.item.nbt = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.uiTitle"))
								.binding(defaults.quickNav.button8.uiTitle,
										() -> config.quickNav.button8.uiTitle,
										newValue -> config.quickNav.button8.uiTitle = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.clickEvent"))
								.binding(defaults.quickNav.button8.clickEvent,
										() -> config.quickNav.button8.clickEvent,
										newValue -> config.quickNav.button8.clickEvent = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.build())
				
				//Button 9
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button9"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.render"))
								.binding(defaults.quickNav.button9.render,
										() -> config.quickNav.button9.render,
										newValue -> config.quickNav.button9.render = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.itemName"))
								.binding(defaults.quickNav.button9.item.itemName,
										() -> config.quickNav.button9.item.itemName,
										newValue -> config.quickNav.button9.item.itemName = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.count"))
								.binding(defaults.quickNav.button9.item.count,
										() -> config.quickNav.button9.item.count,
										newValue -> config.quickNav.button9.item.count = newValue)
								.controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 64))
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.nbt"))
								.binding(defaults.quickNav.button9.item.nbt,
										() -> config.quickNav.button9.item.nbt,
										newValue -> config.quickNav.button9.item.nbt = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.uiTitle"))
								.binding(defaults.quickNav.button9.uiTitle,
										() -> config.quickNav.button9.uiTitle,
										newValue -> config.quickNav.button9.uiTitle = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.clickEvent"))
								.binding(defaults.quickNav.button9.clickEvent,
										() -> config.quickNav.button9.clickEvent,
										newValue -> config.quickNav.button9.clickEvent = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.build())
				
				//Button 10
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button10"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.render"))
								.binding(defaults.quickNav.button10.render,
										() -> config.quickNav.button10.render,
										newValue -> config.quickNav.button10.render = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.itemName"))
								.binding(defaults.quickNav.button10.item.itemName,
										() -> config.quickNav.button10.item.itemName,
										newValue -> config.quickNav.button10.item.itemName = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.count"))
								.binding(defaults.quickNav.button10.item.count,
										() -> config.quickNav.button10.item.count,
										newValue -> config.quickNav.button10.item.count = newValue)
								.controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 64))
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.nbt"))
								.binding(defaults.quickNav.button10.item.nbt,
										() -> config.quickNav.button10.item.nbt,
										newValue -> config.quickNav.button10.item.nbt = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.uiTitle"))
								.binding(defaults.quickNav.button10.uiTitle,
										() -> config.quickNav.button10.uiTitle,
										newValue -> config.quickNav.button10.uiTitle = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.clickEvent"))
								.binding(defaults.quickNav.button10.clickEvent,
										() -> config.quickNav.button10.clickEvent,
										newValue -> config.quickNav.button10.clickEvent = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.build())
				
				//Button 11
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button11"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.render"))
								.binding(defaults.quickNav.button11.render,
										() -> config.quickNav.button11.render,
										newValue -> config.quickNav.button11.render = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.itemName"))
								.binding(defaults.quickNav.button11.item.itemName,
										() -> config.quickNav.button11.item.itemName,
										newValue -> config.quickNav.button11.item.itemName = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.count"))
								.binding(defaults.quickNav.button11.item.count,
										() -> config.quickNav.button11.item.count,
										newValue -> config.quickNav.button11.item.count = newValue)
								.controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 64))
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.nbt"))
								.binding(defaults.quickNav.button11.item.nbt,
										() -> config.quickNav.button11.item.nbt,
										newValue -> config.quickNav.button11.item.nbt = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.uiTitle"))
								.binding(defaults.quickNav.button11.uiTitle,
										() -> config.quickNav.button11.uiTitle,
										newValue -> config.quickNav.button11.uiTitle = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.clickEvent"))
								.binding(defaults.quickNav.button11.clickEvent,
										() -> config.quickNav.button11.clickEvent,
										newValue -> config.quickNav.button11.clickEvent = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.build())
				
				//Button 12
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button12"))
						.collapsed(true)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.render"))
								.binding(defaults.quickNav.button12.render,
										() -> config.quickNav.button12.render,
										newValue -> config.quickNav.button12.render = newValue)
								.controller(ConfigUtils::createBooleanController)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.itemName"))
								.binding(defaults.quickNav.button12.item.itemName,
										() -> config.quickNav.button12.item.itemName,
										newValue -> config.quickNav.button12.item.itemName = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.count"))
								.binding(defaults.quickNav.button12.item.count,
										() -> config.quickNav.button12.item.count,
										newValue -> config.quickNav.button12.item.count = newValue)
								.controller(opt -> IntegerFieldControllerBuilder.create(opt).range(1, 64))
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.item.nbt"))
								.binding(defaults.quickNav.button12.item.nbt,
										() -> config.quickNav.button12.item.nbt,
										newValue -> config.quickNav.button12.item.nbt = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.uiTitle"))
								.binding(defaults.quickNav.button12.uiTitle,
										() -> config.quickNav.button12.uiTitle,
										newValue -> config.quickNav.button12.uiTitle = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.option(Option.<String>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.quickNav.button1.clickEvent"))
								.binding(defaults.quickNav.button12.clickEvent,
										() -> config.quickNav.button12.clickEvent,
										newValue -> config.quickNav.button12.clickEvent = newValue)
								.controller(StringControllerBuilder::create)
								.build())
						.build())
				
				.build();
	}
}
