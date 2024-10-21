package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NewYearCakeAdder extends SimpleSlotTextAdder {
	@Override
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		if (stack.getSkyblockId().equals("NEW_YEAR_CAKE")) {
			return SlotText.bottomLeftList(Text.literal(String.valueOf(ItemUtils.getCustomData(stack).getInt("new_years_cake"))).withColor(0xCBA6F7));
		}
		return List.of();
	}
}
