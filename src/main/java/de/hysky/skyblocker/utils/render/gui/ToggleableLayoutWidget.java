package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.Widget;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ToggleableLayoutWidget implements LayoutWidget {
	private final LayoutWidget layout;
	private final BooleanSupplier isEnabled;

	private boolean enabled;

	public ToggleableLayoutWidget(LayoutWidget layout, BooleanSupplier isEnabled) {
		this.layout = layout;
		this.isEnabled = isEnabled;
	}

	@Override
	public void forEachElement(Consumer<Widget> consumer) {
		if (enabled) consumer.accept(layout);
	}

	@Override
	public void refreshPositions() {
		enabled = this.isEnabled.getAsBoolean();
		LayoutWidget.super.refreshPositions();
	}

	@Override
	public void setX(int x) {
		layout.setX(x);
	}

	@Override
	public void setY(int y) {
		layout.setY(y);
	}

	@Override
	public int getX() {
		return layout.getX();
	}

	@Override
	public int getY() {
		return layout.getY();
	}

	@Override
	public int getWidth() {
		return enabled ? layout.getWidth() : 0;
	}

	@Override
	public int getHeight() {
		return enabled ? layout.getHeight() : 0;
	}
}
