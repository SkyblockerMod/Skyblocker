package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;

import java.util.function.Consumer;

/**
 * Implements a few things so you don't have to!
 */
public abstract class AbstractWidget implements Widget, Element, Drawable {

	protected int w = 0, h = 0;
	protected int x = 0, y = 0;

	public final int getX() {
		return this.x;
	}

	public final void setX(int x) {
		this.x = x;
	}

	public final int getY() {
		return this.y;
	}

	public final void setY(int y) {
		this.y = y;
	}

	public final int getWidth() {
		return this.w;
	}

	public final int getHeight() {
		return this.h;
	}

	public final int getRight() {
		return getX() + getWidth();
	}

	public final int getBottom() {
		return getY() + getHeight();
	}

	public void setWidth(int width) {
		this.w = width;
	}

	public void setHeight(int height) {
		this.h = height;
	}

	@Override
	public void forEachChild(Consumer<ClickableWidget> consumer) {}

	public void setDimensions(int size) {
		setDimensions(size, size);
	}

	public void setDimensions(int width, int height) {
		this.w = width;
		this.h = height;
	}

	private boolean focused = false;

	@Override
	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	@Override
	public boolean isFocused() {
		return focused;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= getX() && mouseX <= getX() + getWidth() && mouseY >= getY() && mouseY <= getY() + getHeight();
	}

	@Override
	public ScreenRect getNavigationFocus() {
		return Widget.super.getNavigationFocus();
	}
}
