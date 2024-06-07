package de.hysky.skyblocker.skyblock.item.tooltip;

import de.hysky.skyblocker.skyblock.ChestValue;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Extend this class and add it to {@link TooltipManager#adders} to add additional text to tooltips.
 */
public abstract class TooltipAdder {
	/**
	 * The title of the screen must match this pattern for this adder to be applied. Null means it will be applied to all screens.
	 * @implNote Don't end your regex with a {@code $} as {@link ChestValue} appends text to the end of the title,
	 * so the regex will stop matching if the player uses it.
	 */
	public final Pattern titlePattern;
	/**
	 * The priority of this adder. Lower priority means it will be applied first.
	 * @apiNote Consider taking this value on your class' constructor and setting it from {@link TooltipManager#adders} to make it easy to read and maintain.
	 */
	public final int priority;

	protected TooltipAdder(String titlePattern, int priority) {
		this(Pattern.compile(titlePattern), priority);
	}

	protected TooltipAdder(Pattern titlePattern, int priority) {
		this.titlePattern = titlePattern;
		this.priority = priority;
	}

	/**
	 * Creates a TooltipAdder that will be applied to all screens.
	 */
	protected TooltipAdder(int priority) {
		this.titlePattern = null;
		this.priority = priority;
	}

	/**
	 * @implNote The first element of the lines list holds the item's display name,
	 * as it's a list of all lines that will be displayed in the tooltip.
	 */
	public abstract void addToTooltip(List<Text> lines, Slot focusedSlot);
}
