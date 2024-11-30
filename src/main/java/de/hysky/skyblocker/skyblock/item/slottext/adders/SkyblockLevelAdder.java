package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SkyblockLevelAdder extends SimpleSlotTextAdder {
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"skyblock_level",
			"skyblocker.config.uiAndVisuals.slotText.skyblockLevel");
	public SkyblockLevelAdder() {
		super("^SkyBlock Menu", CONFIG_INFORMATION);
	}

	@Override
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		if (slotId != 22) return List.of();
		List<Text> lore = ItemUtils.getLore(stack);
		if (lore.isEmpty()) return List.of();
		List<Text> siblings = lore.getFirst().getSiblings();
		if (siblings.size() < 3) return List.of();
		String levelText = siblings.get(2).getString(); //The 3rd child is the level text itself
		if (!NumberUtils.isDigits(levelText)) return List.of();
		return SlotText.bottomLeftList(Text.literal(levelText).withColor(0xFFDDC1));
	}
}
