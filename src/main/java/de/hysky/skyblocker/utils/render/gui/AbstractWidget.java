package de.hysky.skyblocker.utils.render.gui;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.ScreenRectangle;

/**
 * Implements a few things so you don't have to!
 */
public abstract class AbstractWidget implements LayoutElement, GuiEventListener, Renderable {

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

	public void setWidth(int width) {
		this.w = width;
	}

	public final int getHeight() {
		return this.h;
	}

	@Override
	public void visitWidgets(Consumer<net.minecraft.client.gui.components.AbstractWidget> consumer) {}

	public void setHeight(int height) {
		this.h = height;
	}

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
	public ScreenRectangle getRectangle() {
		return LayoutElement.super.getRectangle();
	}
}
