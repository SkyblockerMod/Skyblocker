package me.xmrvizzy.skyblocker.config.categories;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.config.ConfigUtils;
import me.xmrvizzy.skyblocker.skyblock.dwarven.DwarvenHudConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class DwarvenMinesCategory {

	public static ConfigCategory create(SkyblockerConfig defaults, SkyblockerConfig config) {
		return ConfigCategory.createBuilder()
				.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines"))
				
				//Uncategorized Options
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.enableDrillFuel"))
						.binding(defaults.locations.dwarvenMines.enableDrillFuel,
								() -> config.locations.dwarvenMines.enableDrillFuel,
								newValue -> config.locations.dwarvenMines.enableDrillFuel = newValue)
						.controller(BooleanControllerBuilder::create)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.solveFetchur"))
						.binding(defaults.locations.dwarvenMines.solveFetchur,
								() -> config.locations.dwarvenMines.solveFetchur,
								newValue -> config.locations.dwarvenMines.solveFetchur = newValue)
						.controller(BooleanControllerBuilder::create)
						.build())
				.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.solvePuzzler"))
						.binding(defaults.locations.dwarvenMines.solvePuzzler,
								() -> config.locations.dwarvenMines.solvePuzzler,
								newValue -> config.locations.dwarvenMines.solvePuzzler = newValue)
						.controller(BooleanControllerBuilder::create)
						.build())
				
				//Dwarven HUD
				.group(OptionGroup.createBuilder()
						.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.dwarvenHud"))
						.collapsed(false)
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.dwarvenHud.enabled"))
								.binding(defaults.locations.dwarvenMines.dwarvenHud.enabled,
										() -> config.locations.dwarvenMines.dwarvenHud.enabled,
										newValue -> config.locations.dwarvenMines.dwarvenHud.enabled = newValue)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<SkyblockerConfig.DwarvenHudStyle>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.dwarvenHud.style"))
								.description(OptionDescription.of(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.dwarvenHud.style.@Tooltip[0]", 
										Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.dwarvenHud.style.@Tooltip[1]"),
										Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.dwarvenHud.style.@Tooltip[2]"))))
								.binding(defaults.locations.dwarvenMines.dwarvenHud.style,
										() -> config.locations.dwarvenMines.dwarvenHud.style,
										newValue -> config.locations.dwarvenMines.dwarvenHud.style = newValue)
								.controller(ConfigUtils::createCyclingListController4Enum)
								.build())
						.option(ButtonOption.createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.dwarvenHud.screen"))
								.text(Text.translatable("text.skyblocker.open"))
								.action((screen, opt) -> MinecraftClient.getInstance().setScreen(new DwarvenHudConfigScreen(screen)))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.dwarvenHud.enableBackground"))
								.binding(defaults.locations.dwarvenMines.dwarvenHud.enableBackground,
										() -> config.locations.dwarvenMines.dwarvenHud.enableBackground,
										newValue -> config.locations.dwarvenMines.dwarvenHud.enableBackground = newValue)
								.controller(BooleanControllerBuilder::create)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.dwarvenHud.x"))
								.binding(defaults.locations.dwarvenMines.dwarvenHud.x,
										() -> config.locations.dwarvenMines.dwarvenHud.x,
										newValue -> config.locations.dwarvenMines.dwarvenHud.x = newValue)
								.controller(IntegerFieldControllerBuilder::create)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.dwarvenHud.y"))
								.binding(defaults.locations.dwarvenMines.dwarvenHud.y,
										() -> config.locations.dwarvenMines.dwarvenHud.y,
										newValue -> config.locations.dwarvenMines.dwarvenHud.y = newValue)
								.controller(IntegerFieldControllerBuilder::create)
								.build())
						.build())
				.build();
	}
}
