package de.hysky.skyblocker.skyblock.item.tooltip;

import de.hysky.skyblocker.utils.container.RegexContainerMatcher;
import de.hysky.skyblocker.utils.container.TooltipAdder;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Simple implementation of a tooltip adder.
 * Extend this class and add annotate it with {@link de.hysky.skyblocker.annotations.RegisterTooltipAdder} to add additional text to tooltips.
 */
public abstract class SimpleTooltipAdder extends RegexContainerMatcher implements TooltipAdder {

	/**
	 * Utility constructor that will compile the given string into a pattern.
	 *
	 * @see #SimpleTooltipAdder(Pattern)
	 */
	protected SimpleTooltipAdder(@NotNull @Language("RegExp") String titlePattern) {
		super(titlePattern);
	}

	/**
	 * Creates a TooltipAdder that will be applied to screens with titles that match the given pattern.
	 *
	 * @param titlePattern The pattern to match the screen title against.
	 */
	protected SimpleTooltipAdder(@NotNull Pattern titlePattern) {
		super(titlePattern);
	}

	/**
	 * Creates a TooltipAdder that will be applied to all screens.
	 *
	 */
	protected SimpleTooltipAdder() {
		super();
	}

}
