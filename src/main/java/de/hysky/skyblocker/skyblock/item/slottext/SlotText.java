package de.hysky.skyblocker.skyblock.item.slottext;

import net.minecraft.text.Text;

public record SlotText(Text text, TextPosition position) {
	public static SlotText bottomLeft(Text text) {
		return new SlotText(text, TextPosition.BOTTOM_LEFT);
	}

	public static SlotText bottomRight(Text text) {
		return new SlotText(text, TextPosition.BOTTOM_RIGHT);
	}

	public static SlotText topLeft(Text text) {
		return new SlotText(text, TextPosition.TOP_LEFT);
	}

	public static SlotText topRight(Text text) {
		return new SlotText(text, TextPosition.TOP_RIGHT);
	}
}
