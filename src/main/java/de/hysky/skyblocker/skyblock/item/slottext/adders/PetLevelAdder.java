package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;

public class PetLevelAdder extends SlotTextAdder {
	public PetLevelAdder() {
		super();
	}

	@Override
	public @Nullable Text getText(Slot slot) {
		ItemStack itemStack = slot.getStack();
		if (!itemStack.isOf(Items.PLAYER_HEAD)) return null;
		String level = CatacombsLevelAdder.getBracketedLevelFromName(itemStack);
		if (!NumberUtils.isDigits(level)) return null;
		return Text.literal(level).formatted(Formatting.GOLD);
	}
}
