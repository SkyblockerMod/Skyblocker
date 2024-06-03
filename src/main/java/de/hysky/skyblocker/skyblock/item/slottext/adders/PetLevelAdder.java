package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.PositionedText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PetLevelAdder extends SlotTextAdder {
	public PetLevelAdder() {
		super();
	}

	@Override
	public @NotNull List<PositionedText> getText(Slot slot) {
		ItemStack itemStack = slot.getStack();
		if (!itemStack.isOf(Items.PLAYER_HEAD)) return List.of();
		String level = CatacombsLevelAdder.getBracketedLevelFromName(itemStack);
		if (!NumberUtils.isDigits(level)) return List.of();
		return List.of(PositionedText.TOP_LEFT(Text.literal(level).formatted(Formatting.GOLD)));
	}
}
