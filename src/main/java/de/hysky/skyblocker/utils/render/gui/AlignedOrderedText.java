package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;

import java.util.List;

public record AlignedOrderedText(List<Segment> segments) implements OrderedText {
	@Override
	public boolean accept(CharacterVisitor visitor) {
		if (!(visitor instanceof TextRenderer.Drawer drawer)) return true;
		float initialX = drawer.x;
		float lastX = 0;
		for (Segment segment : segments) {
			float xOffset = lastX > segment.xOffset ? lastX : segment.xOffset;
			drawer.x = initialX + xOffset;
			boolean accepted = segment.text.accept(visitor);
			if (!accepted) return false;
			lastX = drawer.x - initialX;
		}
		return true;
	}

	public record Segment(OrderedText text, int xOffset) {}
}
