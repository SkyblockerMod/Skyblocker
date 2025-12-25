package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.RomanNumerals;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
		if (!stack.is(Items.ENCHANTED_BOOK)) return List.of();
		String name = stack.getHoverName().getString();
		if (name.equals("Enchanted Book")) {
			CompoundTag enchantments = EnchantmentAbbreviationAdder.getEnchantments(stack);
			if (enchantments == null) return List.of();
			String enchantmentId = enchantments.keySet().iterator().next();
			int level = enchantments.getIntOr(enchantmentId, 0);
			return List.of(SlotText.bottomLeft(Component.literal(String.valueOf(level)).withColor(SlotText.CREAM)));
		} else { //In bazaar, the books have the enchantment level in the name
			int level = getEnchantLevelFromString(name);
			if (level == 0) return List.of();
			return SlotText.bottomLeftList(Component.literal(String.valueOf(level)).withColor(SlotText.CREAM));
		}
	}

	private static int getEnchantLevelFromString(String str) {
		return RomanNumerals.romanToDecimal(str.substring(str.lastIndexOf(' ') + 1)); //+1 because we don't need the space itself
	}
}
