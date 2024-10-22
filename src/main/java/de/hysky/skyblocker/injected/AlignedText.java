package de.hysky.skyblocker.injected;

import de.hysky.skyblocker.utils.render.gui.AlignedTooltipComponent;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AlignedText {
	/**
	 * <h3>
	 * Aligned Text
	 * </h3>
	 * <p>
	 * This method is used to display text at a certain x offset after the current text in tooltips.
	 * This allows for aligned text when used on multiple rows with the same offset.
	 * </p>
	 * <p>
	 * This method can be chained to achieve a grid-like layout in tooltips.
	 * </p>
	 * <h3>
	 * Styling
	 * </h3>
	 * <p>
	 * The way styling applies to aligned text is slightly different from normal text, where the styling of the parent text is applied to children as well
	 * (which causes almost all uses of text with formatting to be appended on an empty parent text when there is more than 1 style in the same line).
	 * </p>
	 * <p>
	 * For aligned text, each node has their own formatting and there is no style inheritance between them.
	 * </p>
	 * <p>
	 * However, each aligned text node can still have their own children elements like normal text,
	 * where the children will inherit the style of the parent and their text content will be appended to the parent.
	 * </p>
	 *
	 * @param text The text to render after this text
	 * @param xOffset The x offset to apply to the given {@code text},
	 * 		relative to the start of the text object this method is called upon.
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
