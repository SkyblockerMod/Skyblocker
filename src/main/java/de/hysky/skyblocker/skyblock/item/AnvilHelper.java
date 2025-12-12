package de.hysky.skyblocker.skyblock.item;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AnvilHelper extends SimpleContainerSolver {
	public AnvilHelper() {
		super("Anvil");
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		ItemStack stack1 = slots.get(29);
		ItemStack stack2 = slots.get(33);
		if (!stack1.isOf(Items.ENCHANTED_BOOK) || !stack2.isOf(Items.ENCHANTED_BOOK)) return List.of();
		if (!haveUnequalEnchantments(stack1, stack2)) return List.of();
		return List.of(ColorHighlight.red(13));
	}

	@Nullable
	private NbtCompound getEnchantments(ItemStack stack) {
		NbtCompound nbt = ItemUtils.getCustomData(stack);
		if (nbt.isEmpty() || !nbt.contains("enchantments")) return null;
		return nbt.getCompoundOrEmpty("enchantments");
	}

	private boolean haveUnequalEnchantments(ItemStack stack1, ItemStack stack2) {
		NbtCompound enchantments1 = getEnchantments(stack1);
		NbtCompound enchantments2 = getEnchantments(stack2);
		if (enchantments1 == null || enchantments2 == null) return false;
		for (String enchantmentId : enchantments2.getKeys()) {
			int level1 = enchantments1.getInt(enchantmentId, 0);
			int level2 = enchantments2.getInt(enchantmentId, 0);
			if (level1 != level2) return true;
		}
		return false;
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.enableAnvilHelper;
	}
}
