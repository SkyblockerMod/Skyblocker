package de.hysky.skyblocker.skyblock.item.custom.screen.name.visitor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.*;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class GetClickedPositionVisitor implements StringVisitable.StyledVisitor<Void> {
	private final MutableText text = Text.empty();
	private final TextRenderer textRenderer;
	private int position = -1;
	private final int x;

	public GetClickedPositionVisitor(TextRenderer textRenderer, int x) {
		this.x = x;
		this.textRenderer = textRenderer;
	}

	public GetClickedPositionVisitor(int x) {
		this(MinecraftClient.getInstance().textRenderer, x);
	}

	protected void visit(Style style, String asString) {
		if (position >= 0) return; // already found position
		if (asString.isEmpty()) return;
		MutableText text1 = Text.literal(asString).setStyle(style);
		int originalWidth = textRenderer.getWidth(text);
		if (originalWidth + textRenderer.getWidth(text1) < x) { // if the text is smaller than the x position, we skip it and append it
			text.append(text1);
			return;
		}
		// the x position is within the text, we need to find the position
		int currentWidth = 0;
		AtomicInteger atomicInteger = new AtomicInteger(0);
		OrderedText orderedText = visitor -> {
			visitor.accept(0, style, asString.codePointAt(atomicInteger.get()));
			return true;
		};
		while (atomicInteger.get() < asString.length() && originalWidth + currentWidth + textRenderer.getWidth(orderedText) / 2 <= x) {
			currentWidth += textRenderer.getWidth(orderedText);
			atomicInteger.incrementAndGet();
		}
		position = Math.max(text.getString().length() + atomicInteger.get(), 0);
	}

	public int getPosition() {
		return position;
	}

	@Override
	public Optional<Void> accept(Style style, String asString) {
		visit(style, asString);
		return Optional.empty();
	}
}
