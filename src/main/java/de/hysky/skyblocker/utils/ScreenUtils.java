package de.hysky.skyblocker.utils;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
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

	/// Returns a rectangle that can be used as a collision box for snapping.
	///
	/// The rectangle is the border along the given side, extended by 5 pixels in the outward direction and by half the widget's width/height in the inward direction.
	public static ScreenRectangle getSnapBox(ScreenRectangle rect, ScreenDirection side) {
		int extraX = rect.width() / 2;
		int extraY = rect.height() / 2;
		int primaryPos = rect.getBoundInDirection(side);
		final int primarySize = 5 + (side.getAxis() == ScreenAxis.HORIZONTAL ? extraX : extraY);
		ScreenAxis otherAxis = side.getAxis().orthogonal();
		int secondaryPos = rect.getBoundInDirection(otherAxis.getNegative());
		int secondarySize = rect.getLength(otherAxis);
		ScreenRectangle screenRect = ScreenRectangle.of(side.getAxis(), primaryPos, secondaryPos, primarySize, secondarySize);
		// Plus 1 because getBoundInDirection returns the last pixel when side is positive, but we want the first pixel outside the widget.
		int offsetX = side.getAxis() == ScreenAxis.HORIZONTAL ? (side.isPositive() ? -extraX + 1 : -5) : 0;
		int offsetY = side.getAxis() == ScreenAxis.VERTICAL ? (side.isPositive() ? -extraY + 1 : -5) : 0;
		return new ScreenRectangle(screenRect.left() + offsetX, screenRect.top() + offsetY, screenRect.width(), screenRect.height());
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
