package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.Widget;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ToggleableLayoutWidget implements LayoutWidget {
	private final Widget widget;
	private final BooleanSupplier isEnabled;

	private boolean enabled;

	public ToggleableLayoutWidget(Widget widget, BooleanSupplier isEnabled) {
		this.widget = widget;
		this.isEnabled = isEnabled;
	}

	@Override
	public void forEachElement(Consumer<Widget> consumer) {
		if (enabled) consumer.accept(widget);
	}

	@Override
	public void refreshPositions() {
		enabled = this.isEnabled.getAsBoolean();
		LayoutWidget.super.refreshPositions();
	}

	@Override
	public void setX(int x) {
		widget.setX(x);
	}

	@Override
	public void setY(int y) {
		widget.setY(y);
	}

	@Override
	public int getX() {
		return widget.getX();
	}

	@Override
	public int getY() {
		return widget.getY();
	}

	@Override
	public int getWidth() {
		return enabled ? widget.getWidth() : 0;
	}

	@Override
	public int getHeight() {
		return enabled ? widget.getHeight() : 0;
	}
}
