package de.hysky.skyblocker.skyblock.profileviewer2.utils;

import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RelativeLayout implements Layout {
	private int x, y;
	private final List<Child> children = new ArrayList<>();

	public <T extends LayoutElement> T addChild(T element, int relativeX, int relativeY) {
		this.children.add(new Child(element, relativeX, relativeY));
		return element;
	}

	@Override
	public void setX(int i) {
		this.x = i;
		for (Child child : children) {
			child.layoutElement.setX(this.x + child.relativeX);
		}
	}

	@Override
	public void setY(int i) {
		this.y = i;
		for (Child child : children) {
			child.layoutElement.setY(this.y + child.relativeY);
		}
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	// Leaving those unimplemented for now as I don't think they would be useful
	@Override
	public int getWidth() {
		return 0;
	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public void arrangeElements() {
		for (Child element : children) {
			element.layoutElement.setPosition(x + element.relativeX, y + element.relativeY);
		}
	}

	@Override
	public void visitChildren(Consumer<LayoutElement> consumer) {
		children.forEach(e -> consumer.accept(e.layoutElement));
	}

	private record Child(LayoutElement layoutElement, int relativeX, int relativeY) {}
}
