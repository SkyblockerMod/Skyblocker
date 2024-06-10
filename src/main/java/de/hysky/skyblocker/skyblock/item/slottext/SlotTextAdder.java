package de.hysky.skyblocker.skyblock.item.slottext;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.gui.AbstractContainerMatcher;
import net.minecraft.screen.slot.Slot;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Extend this class and add it to {@link SlotTextManager#adders} to add text to any arbitrary slot.
 */
public abstract class SlotTextAdder extends AbstractContainerMatcher {
	/**
	 * Utility constructor that will compile the given string into a pattern.
	 *
	 * @see #SlotTextAdder(Pattern)
	 */
	protected SlotTextAdder(@NotNull @Language("RegExp") String titlePattern) {
		super(titlePattern);
	}

	/**
	 * Creates a SlotTextAdder that will be applied to screens with titles that match the given pattern.
	 *
	 * @param titlePattern The pattern to match the screen title against.
	 */
	protected SlotTextAdder(@NotNull Pattern titlePattern) {
		super(titlePattern);
	}

	/**
	 * Creates a SlotTextAdder that will be applied to all screens.
	 */
	protected SlotTextAdder() {
		super();
	}

	/**
	 * This method will be called for each rendered slot. Consider using a switch statement on {@link Slot#id} if you wish to add different text to different slots.
	 *
	 * @return A list of positioned text to be rendered. Return {@link List#of()} if no text should be rendered.
	 * @implNote By minecraft's design, scaled text inexplicably moves around.
	 * So, limit your text to 3 characters (or roughly less than 20 width) if you want it to not look horrible.
	 */
	public abstract @NotNull List<SlotText> getText(Slot slot);

	/**
	 * Override this method to add conditions to enable or disable this adder.
	 * @return Whether this adder is enabled.
	 * @implNote The slot text adders only work while in skyblock, so no need to check for that again.
	 */
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().general.itemInfoDisplay.slotText;
	}
}
