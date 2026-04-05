package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.ItemUtils;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class PrehistoricEggAdder extends SimpleSlotTextAdder {
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"prehistoric_egg",
			"skyblocker.config.uiAndVisuals.slotText.prehistoricEgg",
			"skyblocker.config.uiAndVisuals.slotText.prehistoricEgg.@Tooltip");

	public PrehistoricEggAdder() {
		super(CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		if (!stack.is(Items.PLAYER_HEAD) || !stack.getSkyblockId().equals("PREHISTORIC_EGG")) return List.of();
		CompoundTag nbt = ItemUtils.getCustomData(stack);
		if (!nbt.contains("blocks_walked")) return List.of();
		int walked = nbt.getIntOr("blocks_walked", 0);

		String walkedStr;
		if (walked < 1000) walkedStr = String.valueOf(walked);
		else if (walked < 10000) walkedStr = String.format("%.1fk", walked/1000.0f);
		else walkedStr = walked / 1000 + "k";

		return SlotText.bottomLeftList(Component.literal(walkedStr).withColor(SlotText.CREAM));
	}
}
