package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.List;

import org.jspecify.annotations.Nullable;

public class PotionLevelAdder extends SimpleSlotTextAdder {
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"potion_level",
			"skyblocker.config.uiAndVisuals.slotText.potionLevel");

	public PotionLevelAdder() {
		super(CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		NbtCompound customData = ItemUtils.getCustomData(stack);
		String title = stack.getName().getString();
		if (customData.contains("potion_level") && !title.contains("Healer") && !title.contains("Class Passives")) {
			int level = customData.getInt("potion_level", 0);
			return SlotText.bottomRightList(Text.literal(String.valueOf(level)).withColor(SlotText.CREAM));
		}
		return List.of();
	}
}
