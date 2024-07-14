package de.hysky.skyblocker.skyblock.item.slottext;

import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.text.Text;

import java.util.List;

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

	public static List<SlotText> topLeftList(Text text) {
		return ObjectLists.singleton(topLeft(text));
	}

	public static List<SlotText> topRightList(Text text) {
		return ObjectLists.singleton(topRight(text));
	}

	public static List<SlotText> bottomLeftList(Text text) {
		return ObjectLists.singleton(bottomLeft(text));
	}

	public static List<SlotText> bottomRightList(Text text) {
		return ObjectLists.singleton(bottomRight(text));
	}
}
