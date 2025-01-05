package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PrehistoricEggAdder extends SimpleSlotTextAdder {
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"prehistoric_egg",
			"skyblocker.config.uiAndVisuals.slotText.prehistoricEgg",
			"skyblocker.config.uiAndVisuals.slotText.prehistoricEgg.@Tooltip");

	public PrehistoricEggAdder() {
		super(CONFIG_INFORMATION);
	}

	@Override
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		if (!stack.isOf(Items.PLAYER_HEAD) || !stack.getSkyblockId().equals("PREHISTORIC_EGG")) return List.of();
		NbtCompound nbt = ItemUtils.getCustomData(stack);
		if (!nbt.contains("blocks_walked", NbtElement.INT_TYPE)) return List.of();
		int walked = nbt.getInt("blocks_walked");

		String walkedStr;
		if (walked < 1000) walkedStr = String.valueOf(walked);
		else if (walked < 10000) walkedStr = String.format("%.1fk", walked/1000.0f);
		else walkedStr = walked / 1000 + "k";

		return SlotText.bottomLeftList(Text.literal(walkedStr).withColor(0xFFDDC1));
	}
}
