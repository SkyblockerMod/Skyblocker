package de.hysky.skyblocker.utils;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import de.hysky.skyblocker.mixins.accessors.PopupScreenAccessor;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;

public final class ScreenUtils {
	private ScreenUtils() {}

	public static @Nullable Screen getUnderlyingScreen() {
		return getUnderlyingScreen(Minecraft.getInstance().gui.screen());
	}

	@Contract("null -> null; !null -> !null")
	public static @Nullable Screen getUnderlyingScreen(@Nullable Screen screen) {
		return switch (screen) {
			case PopupScreen popupScreen -> getUnderlyingScreen(((PopupScreenAccessor) popupScreen).getUnderlyingScreen());
			case AbstractPopupScreen popupScreen -> getUnderlyingScreen(popupScreen.backgroundScreen);
			case null, default -> screen;
		};
	}

	/**
	 * Convenience method for creating a text widget with only x and y positions. The widget's width and height are calculated from the {@link Minecraft#getInstance() static client instance}'s text renderer.
	 * @param x The x position of the widget.
	 * @param y The y position of the widget.
	 * @param text The text to display.
	 * @return A new text widget.
	 */
	public static StringWidget textWidget(int x, int y, Component text) {
		return new StringWidget(
				x,
				y,
				Minecraft.getInstance().font.width(text),
				Minecraft.getInstance().font.lineHeight,
				text,
				Minecraft.getInstance().font
		);
	}
}
