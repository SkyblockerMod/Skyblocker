package de.hysky.skyblocker.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

public final class WidgetUtils {
	private WidgetUtils() {}

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
