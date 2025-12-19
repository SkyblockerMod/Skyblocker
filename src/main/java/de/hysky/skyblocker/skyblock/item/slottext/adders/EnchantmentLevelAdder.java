package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.RomanNumerals;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnchantmentLevelAdder extends SimpleSlotTextAdder {
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"enchantment_level",
			"skyblocker.config.uiAndVisuals.slotText.enchantmentLevel"
	);

	public EnchantmentLevelAdder() {
		super(CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		if (!stack.isOf(Items.ENCHANTED_BOOK)) return List.of();
		String name = stack.getName().getString();
		if (name.equals("Enchanted Book")) {
			NbtCompound enchantments = EnchantmentAbbreviationAdder.getEnchantments(stack);
			if (enchantments == null) return List.of();
			String enchantmentId = enchantments.getKeys().iterator().next();
			int level = enchantments.getInt(enchantmentId, 0);
			return List.of(SlotText.bottomLeft(Text.literal(String.valueOf(level)).withColor(SlotText.CREAM)));
		} else { //In bazaar, the books have the enchantment level in the name
			int level = getEnchantLevelFromString(name);
			if (level == 0) return List.of();
			return SlotText.bottomLeftList(Text.literal(String.valueOf(level)).withColor(SlotText.CREAM));
		}
	}

	private static int getEnchantLevelFromString(String str) {
		return RomanNumerals.romanToDecimal(str.substring(str.lastIndexOf(' ') + 1)); //+1 because we don't need the space itself
	}
}
