package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jspecify.annotations.Nullable;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SkyblockLevelAdder extends SimpleSlotTextAdder {
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"skyblock_level",
			"skyblocker.config.uiAndVisuals.slotText.skyblockLevel");
	public SkyblockLevelAdder() {
		super("^SkyBlock Menu", CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		if (slotId != 22) return List.of();
		@SuppressWarnings("deprecation")
		List<Component> lore = ItemUtils.getLore(stack);
		if (lore.isEmpty()) return List.of();
		List<Component> siblings = lore.getFirst().getSiblings();
		if (siblings.size() < 3) return List.of();
		String levelText = siblings.get(2).getString(); //The 3rd child is the level text itself
		if (!NumberUtils.isDigits(levelText)) return List.of();
		return SlotText.bottomLeftList(Component.literal(levelText).withColor(SlotText.CREAM));
	}
}
