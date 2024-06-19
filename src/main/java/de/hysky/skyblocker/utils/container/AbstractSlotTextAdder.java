package de.hysky.skyblocker.utils.container;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface AbstractSlotTextAdder extends AbstractContainerMatcher {

	/**
	 * This method will be called for each rendered slot. Consider using a switch statement on {@link Slot#id} if you wish to add different text to different slots.
	 *
	 * @return A list of positioned text to be rendered. Return {@link List#of()} if no text should be rendered.
	 * @implNote By minecraft's design, scaled text inexplicably moves around.
	 *           It's also not anti-aliased, so it looks horribly jagged and unreadable when scaled down too much.
	 *           So, limit your text to 3 characters (or roughly less than 20 width) if you want it to not look horrible.
	 */
	@NotNull
	List<SlotText> getText(Slot slot);
}
