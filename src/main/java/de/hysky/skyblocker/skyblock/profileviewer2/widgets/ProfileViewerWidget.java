package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public abstract sealed class ProfileViewerWidget extends AbstractWidget permits PageTabWidget, RulerWidget, TestTextWidget {
	/**
	 * The X position of this widget relative to the origin point of the Profile Viewer's background.
	 */
	private int relativeX;
	/**
	 * The Y position of this widget relative to the origin point of the Profile Viewer's background.
	 */
	private int relativeY;

	protected ProfileViewerWidget(int x, int y, int width, int height, Component message) {
		super(0, 0, width, height, message);
		this.relativeX = x;
		this.relativeY = y;
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
		this.setX(backgroundX + this.relativeX);
		this.setY(backgroundY + this.relativeY);
	}
}
