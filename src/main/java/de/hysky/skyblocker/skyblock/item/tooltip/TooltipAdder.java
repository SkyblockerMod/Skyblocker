package de.hysky.skyblocker.skyblock.item.tooltip;

import de.hysky.skyblocker.utils.container.RegexContainerMatcher;
import de.hysky.skyblocker.utils.container.AbstractTooltipAdder;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Simple implementation of a tooltip adder.
 * Extend this class and add it to {@link TooltipManager#adders} to add additional text to tooltips.
 */
public abstract class TooltipAdder extends RegexContainerMatcher implements AbstractTooltipAdder {
	/**
	 * The priority of this adder. Lower priority means it will be applied first.
	 * @apiNote Consider adding this as a parameter to your class' constructor and
	 *          setting it from {@link TooltipManager#adders} to make it easy to read and maintain.
	 */
	private final int priority;

	/**
	 * Utility constructor that will compile the given string into a pattern.
	 *
	 * @see #TooltipAdder(Pattern, int)
	 */
	protected TooltipAdder(@NotNull @Language("RegExp") String titlePattern, int priority) {
		super(titlePattern);
		this.priority = priority;
	}

	/**
	 * Creates a TooltipAdder that will be applied to screens with titles that match the given pattern.
	 *
	 * @param titlePattern The pattern to match the screen title against.
	 * @param priority The priority of this adder. Lower priority means it will be applied first.
	 */
	protected TooltipAdder(@NotNull Pattern titlePattern, int priority) {
		super(titlePattern);
		this.priority = priority;
	}

	/**
	 * Creates a TooltipAdder that will be applied to all screens.
	 *
	 * @param priority The priority of this adder. Lower priority means it will be applied first.
	 */
	protected TooltipAdder(int priority) {
		super();
		this.priority = priority;
	}

	@Override
	public int getPriority() {
		return priority;
	}
}
