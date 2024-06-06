package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.PositionedText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RomanNumerals;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EnchantmentLevelAdder extends SlotTextAdder {
	public EnchantmentLevelAdder() {
		super();
	}

	@Override
	public @NotNull List<PositionedText> getText(Slot slot) {
		final ItemStack itemStack = slot.getStack();
		if (!itemStack.isOf(Items.ENCHANTED_BOOK)) return List.of();
		String name = itemStack.getName().getString();
		if (name.equals("Enchanted Book")) {
			NbtCompound nbt = ItemUtils.getCustomData(itemStack);
			if (nbt.isEmpty() || !nbt.contains("enchantments", NbtElement.COMPOUND_TYPE)) return List.of();
			NbtCompound enchantments = nbt.getCompound("enchantments");
			if (enchantments.getSize() != 1) return List.of(); //Only makes sense to display the level when there's one enchant.
			int level = enchantments.getInt(enchantments.getKeys().iterator().next());
			return List.of(PositionedText.BOTTOM_LEFT(Text.literal(String.valueOf(level)).formatted(Formatting.GREEN)));
		} else { //In bazaar, the books have the enchantment level in the name
			int level = getEnchantLevelFromString(name);
			if (level == 0) return List.of();
			return List.of(PositionedText.BOTTOM_LEFT(Text.literal(String.valueOf(level)).formatted(Formatting.GREEN)));
		}
	}

	private static int getEnchantLevelFromString(String str) {
		String romanNumeral = str.substring(str.lastIndexOf(' ') + 1); //+1 because we don't need the space itself
		return RomanNumerals.romanToDecimal(romanNumeral); //Temporary line. The method will be moved out later.
	}
}
