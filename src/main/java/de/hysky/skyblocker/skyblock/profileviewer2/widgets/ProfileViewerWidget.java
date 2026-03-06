package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * The base class of all widgets used in the Profile Viewer.
 *
 * <p>Widgets may be clickable however do note that you must manually set {@link AbstractWidget#active} to {@code true}
 * in order for that to work properly as widgets are automatically set to be inactive.
 */
public abstract sealed class ProfileViewerWidget extends AbstractWidget permits BasicInfoBoxWidget, CompositeWidget, PageTabWidget, PlayerWidget, RulerWidget, TestTextWidget {
	/**
	 * The padding needed to match vanilla in rendering inside of the "content" area of the Profile Viewer's background (leaving space from the border).
	 */
	protected static final int CONTENT_PADDING = 8;
	private final boolean padToContent;
	/**
	 * The X position of this widget relative to the origin point of the Profile Viewer's background.
	 */
	private int relativeX;
	/**
	 * The Y position of this widget relative to the origin point of the Profile Viewer's background.
	 */
	private int relativeY;

	protected ProfileViewerWidget(int x, int y, int width, int height, Component message) {
		this(x, y, width, height, true, message);
	}

	protected ProfileViewerWidget(int x, int y, int width, int height, boolean padToContent, Component message) {
		super(0, 0, width, height, message);
		this.padToContent = padToContent;
		this.relativeX = x;
		this.relativeY = y;
		this.active = false;
	}

	protected static Font getFont() {
		return Minecraft.getInstance().font;
	}

	/*
	 * {@return the widget's x coordinate relative to the origin point of the Profile Viewer's background}
	 */
	protected final int getRelativeX() {
		return this.relativeX;
	}

	protected final void setRelativeX(int relativeX) {
		this.relativeX = relativeX;
	}

	/*
	 * {@return the widget's y coordinate relative to the origin point of the Profile Viewer's background}
	 */
	protected final int getRelativeY() {
		return this.relativeY;
	}

	protected final void setRelativeY(int relativeY) {
		this.relativeY = relativeY;
	}

	/**
	 * Updates this widget's position, highly recommended to do before rendering.
	 *
	 * @param backgroundX the x position of the Profile Viewer's background
	 * @param backgroundY the y position of the Profile Viewer's background
	 */
	public final void updatePosition(int backgroundX, int backgroundY) {
		this.setX(backgroundX + this.relativeX + (this.padToContent ? CONTENT_PADDING : 0));
		this.setY(backgroundY + this.relativeY + (this.padToContent ? CONTENT_PADDING : 0));
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {}
}
