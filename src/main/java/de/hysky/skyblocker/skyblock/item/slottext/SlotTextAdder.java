package de.hysky.skyblocker.skyblock.item.slottext;

import de.hysky.skyblocker.skyblock.ChestValue;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Extend this class and add it to {@link SlotTextManager#adders} to add text to any arbitrary slot.
 */
public abstract class SlotTextAdder {
	/**
	 * The title of the screen must match this pattern for this adder to be applied. Null means it will be applied to all screens.
	 * @implNote Don't end your regex with a {@code $} as {@link ChestValue} appends text to the end of the title,
	 * so the regex will stop matching if the player uses it.
	 */
	public final Pattern titlePattern;

	protected SlotTextAdder(String titlePattern) {
		this(Pattern.compile(titlePattern));
	}

	protected SlotTextAdder(Pattern titlePattern) {
		this.titlePattern = titlePattern;
	}

	/**
	 * Creates a SlotTextRenderer that will be applied to all screens.
	 */
	protected SlotTextAdder() {
		this.titlePattern = null;
	}

	/**
	 * This method will be called for each rendered slot. Consider using a switch statement on {@link Slot#id} if you wish to add different text to different slots.
	 *
	 * @return The text to be rendered. Return null if no text should be rendered.
	 * @implNote By minecraft's design, scaled text inexplicably moves around.
	 * So, limit your text to 3 characters (or roughly less than 20 width) if you want it to not look horrible.
	 */
	@Nullable
	public abstract Text getText(Slot slot);
}
