package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RomanNumerals;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnchantmentLevelAdder extends SimpleSlotTextAdder {
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"enchantment_level",
			"skyblocker.config.uiAndVisuals.slotText.enchantmentLevel");

	public EnchantmentLevelAdder() {
		super(CONFIG_INFORMATION);
	}

	@Override
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		if (!stack.isOf(Items.ENCHANTED_BOOK)) return List.of();
		String name = stack.getName().getString();
		if (name.equals("Enchanted Book")) {
			NbtCompound nbt = ItemUtils.getCustomData(stack);
			if (nbt.isEmpty() || !nbt.contains("enchantments", NbtElement.COMPOUND_TYPE)) return List.of();
			NbtCompound enchantments = nbt.getCompound("enchantments");
			if (enchantments.getSize() != 1) return List.of(); //Only makes sense to display the level when there's one enchant.
			int level = enchantments.getInt(enchantments.getKeys().iterator().next());
			return SlotText.bottomLeftList(Text.literal(String.valueOf(level)).withColor(0xFFDDC1));
		} else { //In bazaar, the books have the enchantment level in the name
			int level = getEnchantLevelFromString(name);
			if (level == 0) return List.of();
			return SlotText.bottomLeftList(Text.literal(String.valueOf(level)).withColor(0xFFDDC1));
		}
	}

	private static int getEnchantLevelFromString(String str) {
		String romanNumeral = str.substring(str.lastIndexOf(' ') + 1); //+1 because we don't need the space itself
		return RomanNumerals.romanToDecimal(romanNumeral); //Temporary line. The method will be moved out later.
	}
}
