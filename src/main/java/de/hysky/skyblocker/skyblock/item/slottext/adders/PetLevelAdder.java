package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PetLevelAdder extends SlotTextAdder {
	public PetLevelAdder() {
		super();
	}

	@Override
	public @NotNull List<SlotText> getText(Slot slot) {
		ItemStack itemStack = slot.getStack();
		if (!itemStack.isOf(Items.PLAYER_HEAD)) return List.of();
		String level = CatacombsLevelAdder.getBracketedLevelFromName(itemStack);
		if (!NumberUtils.isDigits(level) || "100".equals(level) || "200".equals(level)) return List.of();
		if (!ItemUtils.getItemId(itemStack).equals("PET")) return List.of();
		return List.of(SlotText.topLeft(Text.literal(level).withColor(0xFFDDC1)));
	}
}
