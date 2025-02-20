package de.hysky.skyblocker.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

public final class WidgetUtils {
	private WidgetUtils() {}

	/**
	 * Convenience method for creating a text widget with only x and y positions. The widget's width and height are calculated from the {@link MinecraftClient#getInstance() static client instance}'s text renderer.
	 * @param x The x position of the widget.
	 * @param y The y position of the widget.
	 * @param text The text to display.
	 * @return A new text widget.
	 */
	public static TextWidget textWidget(int x, int y, Text text) {
		return new TextWidget(
				x,
				y,
				MinecraftClient.getInstance().textRenderer.getWidth(text),
				MinecraftClient.getInstance().textRenderer.fontHeight,
				text,
				MinecraftClient.getInstance().textRenderer
		);
	}
}
