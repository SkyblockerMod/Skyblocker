package de.hysky.skyblocker.utils.container;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextState;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SlotTextAdder extends ContainerMatcher {

	/**
	 * This method will be called for each rendered slot. Consider using a switch statement on {@code slotId} if you wish to limit the text to specific slots.
	 *
	 * @return A list of positioned text to be rendered. Return {@link List#of()} if no text should be rendered.
	 * @implNote By minecraft's design, scaled text inexplicably moves around.
	 *           It's also not anti-aliased, so it looks horribly jagged and unreadable when scaled down too much.
	 *           So, limit your text to 3 characters (or roughly less than 20 width) if you want it to not look horrible.
	 */
	@NotNull
	List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId);

	@Override
	default boolean isEnabled() {
		return SkyblockerConfigManager.get().general.itemInfoDisplay.slotText != SlotTextState.DISABLED;
	}
}
