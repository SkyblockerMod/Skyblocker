package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PrehistoricEggAdder extends SlotTextAdder {
	@Override
	public @NotNull List<SlotText> getText(Slot slot) {
		final ItemStack stack = slot.getStack();
		if (!stack.isOf(Items.PLAYER_HEAD) || !StringUtils.equals(stack.getSkyblockId(), "PREHISTORIC_EGG")) return List.of();
		NbtCompound nbt = ItemUtils.getCustomData(stack);
		if (!nbt.contains("blocks_walked", NbtElement.INT_TYPE)) return List.of();
		int walked = nbt.getInt("blocks_walked");

		String walkedstr;
		if (walked < 1000) walkedstr = String.valueOf(walked);
		else if (walked < 10000) walkedstr = String.format("%.1fk", walked/1000.0f);
		else walkedstr = walked / 1000 + "k";

		return List.of(SlotText.bottomLeft(Text.literal(walkedstr).formatted(Formatting.GOLD)));
	}
}
