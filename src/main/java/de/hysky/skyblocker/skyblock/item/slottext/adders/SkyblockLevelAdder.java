package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SkyblockLevelAdder extends SimpleSlotTextAdder {
	public SkyblockLevelAdder() {
		super("^SkyBlock Menu");
	}

	@Override
	public @NotNull List<SlotText> getText(@NotNull ItemStack itemStack, int slotId) {
		if (slotId != 22) return List.of();
		List<Text> lore = ItemUtils.getLore(itemStack);
		if (lore.isEmpty()) return List.of();
		List<Text> siblings = lore.getFirst().getSiblings();
		if (siblings.size() < 3) return List.of();
		String levelText = siblings.get(2).getString(); //The 3rd child is the level text itself
		if (!NumberUtils.isDigits(levelText)) return List.of();
		return List.of(SlotText.bottomLeft(Text.literal(levelText).withColor(0xFFDDC1)));
	}
}
