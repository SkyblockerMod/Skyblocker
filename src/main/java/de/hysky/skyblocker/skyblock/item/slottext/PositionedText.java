package de.hysky.skyblocker.skyblock.item.slottext;

import net.minecraft.text.Text;

public record PositionedText(Text text, TextPosition position) {
	public static PositionedText BOTTOM_LEFT(Text text) {
		return new PositionedText(text, TextPosition.BOTTOM_LEFT);
	}

	public static PositionedText BOTTOM_RIGHT(Text text) {
		return new PositionedText(text, TextPosition.BOTTOM_RIGHT);
	}

	public static PositionedText TOP_LEFT(Text text) {
		return new PositionedText(text, TextPosition.TOP_LEFT);
	}

	public static PositionedText TOP_RIGHT(Text text) {
		return new PositionedText(text, TextPosition.TOP_RIGHT);
	}
}
