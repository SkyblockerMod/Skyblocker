package de.hysky.skyblocker.injected;

import de.hysky.skyblocker.utils.render.gui.AlignedTooltipComponent;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AlignedText {
	/**
	 * This method is used to display text at a certain x offset after the current text in tooltips.
	 * This allows for aligned text when used on multiple rows.
	 * <p>
	 * This method can be chained to achieve a grid-like layout in tooltips.
	 * @param text The text to render after this text
	 * @param xOffset The x offset to apply to the given {@code text},
	 *                relative to the start of the text object this method is called upon.
	 * @return The {@code text} object passed in, for chaining purposes
	 * @see AlignedTooltipComponent
	 */
	default @NotNull MutableText align(@NotNull MutableText text, int xOffset) {
		return text;
	}

	default @Nullable MutableText getAlignedText() {
		return null;
	}

	/**
	 * @return The x offset to apply to the text, or {@link Integer#MIN_VALUE } if there's no aligned text
	 */
	default int getXOffset() {
		return Integer.MIN_VALUE;
	}

	default MutableText getFirstOfChain() {
		return null;
	}

	default void setFirstOfChain(MutableText text) {}
}
