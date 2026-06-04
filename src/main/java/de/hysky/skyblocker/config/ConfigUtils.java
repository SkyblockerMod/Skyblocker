package de.hysky.skyblocker.config;

import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import net.azureaaron.dandelion.api.ButtonOption;
import net.azureaaron.dandelion.api.controllers.BooleanController;
import net.azureaaron.dandelion.api.controllers.BooleanController.BooleanStyle;
import net.azureaaron.dandelion.api.controllers.ColourController;
import net.azureaaron.dandelion.api.controllers.EnumController;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;

public class ConfigUtils {
	public static final Function<ChatFormatting, Component> FORMATTING_FORMATTER = formatting -> Component.literal(StringUtils.capitalize(formatting.getName().replaceAll("_", " ")));

	public static BooleanController createBooleanController() {
		return BooleanController.createBuilder()
				.coloured(true)
				.booleanStyle(BooleanStyle.YES_NO)
				.build();
	}

	public static ColourController createColourController(boolean hasAlpha) {
		return ColourController.createBuilder()
				.hasAlpha(hasAlpha)
				.build();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> EnumController<T> createEnumController() {
		return (EnumController<T>) EnumController.createBuilder().build();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> EnumController<T> createEnumController(Function<T, Component> formatter) {
		return (EnumController<T>) EnumController.createBuilder().formatter(Function.class.cast(formatter)).build();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> EnumController<T> createEnumDropdownController(Function<T, Component> formatter) {
		return (EnumController<T>) EnumController.createBuilder().dropdown(true).formatter(Function.class.cast(formatter)).build();
	}

	public static ButtonOption createShortcutToKeybindsScreen() {
		Minecraft client = Minecraft.getInstance();
		return ButtonOption.createBuilder()
				.name(Component.translatable("skyblocker.config.shortcutToKeybindsSettings"))
				.action(screen -> client.setScreen(new KeyBindsScreen(screen, client.options)))
				.prompt(Component.translatable("skyblocker.config.shortcutToKeybindsSettings.@Text"))
				.build();
	}

	//FIXME Would probably be a good idea to add a utility method for creating a waypoint type option
}
