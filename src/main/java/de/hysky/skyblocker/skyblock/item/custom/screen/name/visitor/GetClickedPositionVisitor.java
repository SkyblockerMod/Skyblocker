package de.hysky.skyblocker.skyblock.item.custom.screen.name.visitor;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public class GetClickedPositionVisitor implements FormattedText.StyledContentConsumer<Void> {
	private final MutableComponent text = Component.empty();
	private final Font textRenderer;
	private int position = -1;
	private final int x;

	public GetClickedPositionVisitor(Font textRenderer, int x) {
		this.x = x;
		this.textRenderer = textRenderer;
	}

	public GetClickedPositionVisitor(int x) {
		this(Minecraft.getInstance().font, x);
	}

	protected void visit(Style style, String asString) {
		if (position >= 0) return; // already found position
		if (asString.isEmpty()) return;
		MutableComponent text1 = Component.literal(asString).setStyle(style);
		int originalWidth = textRenderer.width(text);
		if (originalWidth + textRenderer.width(text1) < x) { // if the text is smaller than the x position, we skip it and append it
			text.append(text1);
			return;
		}
		// the x position is within the text, we need to find the position
		int currentWidth = 0;
		AtomicInteger atomicInteger = new AtomicInteger(0);
		FormattedCharSequence orderedText = visitor -> {
			visitor.accept(0, style, asString.codePointAt(atomicInteger.get()));
			return true;
		};
		while (atomicInteger.get() < asString.length() && originalWidth + currentWidth + textRenderer.width(orderedText) / 2 <= x) {
			currentWidth += textRenderer.width(orderedText);
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
