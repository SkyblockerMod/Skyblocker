package de.hysky.skyblocker.skyblock.item.slottext;

import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record SlotText(@NotNull Text text, @NotNull TextPosition position) {
	/**
	 * The "default" slot text color.
	 */
	public static final int CREAM = 0xFFDDC1;
	public static final int GOLD = 0xE5B80B;
	public static final int LIGHT_BLUE = 0xCFF8F8;
	public static final int LIGHT_ORANGE = 0xfab387;
	public static final int LIGHT_YELLOW = 0xf9e2af;
	public static final int LIGHT_PURPLE = 0xcba6f7;
	public static final int LIGHT_RED = 0xFF7276;
	public static final int LIGHT_GREEN = 0x90ee90;
	public static final int MID_BLUE = 0x74c7ec;
	public static final int WHITE = 0xFFFFFF;

	public static SlotText bottomLeft(@NotNull Text text) {
		return new SlotText(text, TextPosition.BOTTOM_LEFT);
	}

	public static SlotText bottomRight(@NotNull Text text) {
		return new SlotText(text, TextPosition.BOTTOM_RIGHT);
	}

	public static SlotText topLeft(@NotNull Text text) {
		return new SlotText(text, TextPosition.TOP_LEFT);
	}

	public static SlotText topRight(@NotNull Text text) {
		return new SlotText(text, TextPosition.TOP_RIGHT);
	}

	// The methods below use ObjectLists.singleton rather than List.of because List.of
	// has 1 more method call (a null check) and 1 more field set for no good reason.

	/**
	 * Convenience method for creating a singleton list containing this SlotText. Useful for returning a single SlotText from a method that returns a list.
	 * @return A singleton list containing a SlotText with the {@link TextPosition#TOP_LEFT top left} position and the given text.
	 */
	public static List<SlotText> topLeftList(@NotNull Text text) {
		return ObjectLists.singleton(topLeft(text));
	}

	/**
	 *  Convenience method for creating a singleton list containing this SlotText. Useful for returning a single SlotText from a method that returns a list.
	 * @return A singleton list containing a SlotText with the {@link TextPosition#TOP_RIGHT top right} position and the given text.
	 */
	public static List<SlotText> topRightList(@NotNull Text text) {
		return ObjectLists.singleton(topRight(text));
	}

	/**
	 *  Convenience method for creating a singleton list containing this SlotText. Useful for returning a single SlotText from a method that returns a list.
	 * @return A singleton list containing a SlotText with the {@link TextPosition#BOTTOM_LEFT bottom left} position and the given text.
	 */
	public static List<SlotText> bottomLeftList(@NotNull Text text) {
		return ObjectLists.singleton(bottomLeft(text));
	}

	/**
	 *  Convenience method for creating a singleton list containing this SlotText. Useful for returning a single SlotText from a method that returns a list.
	 * @return A singleton list containing a SlotText with the {@link TextPosition#BOTTOM_RIGHT bottom right} position and the given text.
	 */
	public static List<SlotText> bottomRightList(@NotNull Text text) {
		return ObjectLists.singleton(bottomRight(text));
	}
}
