package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PotionLevelAdder extends SimpleSlotTextAdder {
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"potion_level",
			"skyblocker.config.uiAndVisuals.slotText.potionLevel");

	public PotionLevelAdder() {
		super(CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		CompoundTag customData = ItemUtils.getCustomData(stack);
		String title = stack.getHoverName().getString();
		if (customData.contains("potion_level") && !title.contains("Healer") && !title.contains("Class Passives")) {
			int level = customData.getIntOr("potion_level", 0);
			return SlotText.bottomRightList(Component.literal(String.valueOf(level)).withColor(SlotText.CREAM));
		}
		return List.of();
	}
}
