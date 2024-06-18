package de.hysky.skyblocker.injected;

import de.hysky.skyblocker.utils.render.gui.AlignedTooltipComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AlignedText {
	/**
	 * This is used to display text at a certain x offset after the current text in tooltips.
	 * This allows for aligned text when used on multiple rows.
	 * <p>
	 * This method allows chaining for a grid-like display.
	 * @param text The text to render after this text
	 * @param xOffset The x offset to apply to the given {@code text},
	 *                relative to the start of the text object this method is called upon.
	 * @return The {@code text} object passed in, for chaining purposes
	 * @implNote Call {@link #getFirstOfChain()} at the end of the chain to get the first element
	 *           of the chain when adding to a list of text, otherwise only the last added text will be rendered.
	 * @see AlignedTooltipComponent
	 */
	default @NotNull MutableText chainAlign(@NotNull MutableText text, int xOffset) {
		return text;
	}

	/**
	 * This is used to display text at a certain x offset after the current text in tooltips.
	 * This allows for aligned text when used on multiple rows.
	 * @param text The text to render after this text
	 * @param xOffset The x offset to apply to the given {@code text},
	 *                relative to the start of the text object this method is called upon.
	 * @return The text object this method is called upon, with the aligned text added
	 * @see AlignedTooltipComponent
	 * @see #chainAlign(MutableText, int)
	 */
	default @NotNull Text align(@NotNull MutableText text, int xOffset) {
		return null; // Dummy return value
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