package de.hysky.skyblocker.skyblock.item;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class AnvilHelper extends SimpleContainerSolver {
	public AnvilHelper() {
		super("Anvil");
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		ItemStack stack1 = slots.get(29);
		ItemStack stack2 = slots.get(33);
		if (!stack1.is(Items.ENCHANTED_BOOK) || !stack2.is(Items.ENCHANTED_BOOK)) return List.of();
		if (!haveUnequalEnchantments(stack1, stack2)) return List.of();
		return List.of(ColorHighlight.red(13));
	}

	private @Nullable CompoundTag getEnchantments(ItemStack stack) {
		CompoundTag nbt = ItemUtils.getCustomData(stack);
		if (nbt.isEmpty() || !nbt.contains("enchantments")) return null;
		return nbt.getCompoundOrEmpty("enchantments");
	}

	private boolean haveUnequalEnchantments(ItemStack stack1, ItemStack stack2) {
		CompoundTag enchantments1 = getEnchantments(stack1);
		CompoundTag enchantments2 = getEnchantments(stack2);
		if (enchantments1 == null || enchantments2 == null) return false;
		for (String enchantmentId : enchantments2.keySet()) {
			int level1 = enchantments1.getIntOr(enchantmentId, 0);
			int level2 = enchantments2.getIntOr(enchantmentId, 0);
			if (level1 != level2) return true;
		}
		return false;
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.enableAnvilHelper;
	}
}
