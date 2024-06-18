package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SkyblockLevelAdder extends SlotTextAdder {
	public SkyblockLevelAdder() {
		super("^SkyBlock Menu");
	}

	@Override
	public @NotNull List<SlotText> getText(Slot slot) {
		if (slot.getIndex() != 22) return List.of();
		List<Text> lore = ItemUtils.getLore(slot.getStack());
		if (lore.isEmpty()) return List.of();
		List<Text> siblings = lore.getFirst().getSiblings();
		if (siblings.size() < 3) return List.of();
		String levelText = siblings.get(2).getString(); //The 3rd child is the level text itself
		if (!NumberUtils.isDigits(levelText)) return List.of();
		return List.of(SlotText.bottomLeft(Text.literal(levelText).withColor(0xFFDDC1)));
	}
}
