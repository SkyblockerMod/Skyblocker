package de.hysky.skyblocker.utils.render.gui;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;

public class ToggleableLayoutWidget implements Layout {
	private final LayoutElement widget;
	private final BooleanSupplier isEnabled;

	private boolean enabled;

	public ToggleableLayoutWidget(LayoutElement widget, BooleanSupplier isEnabled) {
		this.widget = widget;
		this.isEnabled = isEnabled;
	}

	@Override
	public void visitChildren(Consumer<LayoutElement> consumer) {
		if (enabled) consumer.accept(widget);
	}

	@Override
	public void arrangeElements() {
		enabled = this.isEnabled.getAsBoolean();
		Layout.super.arrangeElements();
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
