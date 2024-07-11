package de.hysky.skyblocker.skyblock.item.tooltip;

import de.hysky.skyblocker.utils.render.gui.AbstractContainerMatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Extend this class and add it to {@link TooltipManager#adders} to add additional text to tooltips.
 */
public abstract class TooltipAdder extends AbstractContainerMatcher {
	/**
	 * The priority of this adder. Lower priority means it will be applied first.
	 * @apiNote Consider taking this value on your class' constructor and setting it from {@link TooltipManager#adders} to make it easy to read and maintain.
	 */
	public final int priority;

	protected TooltipAdder(@Language("RegExp") String titlePattern, int priority) {
		super(titlePattern);
		this.priority = priority;
	}

	protected TooltipAdder(Pattern titlePattern, int priority) {
		super(titlePattern);
		this.priority = priority;
	}

	/**
	 * Creates a TooltipAdder that will be applied to all screens.
	 */
	protected TooltipAdder(int priority) {
		super();
		this.priority = priority;
	}

	/**
	 * @implNote The first element of the lines list holds the item's display name,
	 * as it's a list of all lines that will be displayed in the tooltip.
	 */
	public abstract void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines);
}
