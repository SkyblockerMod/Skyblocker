package de.hysky.skyblocker.skyblock.item.wikilookup;

import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.text.WordUtils;
import org.jspecify.annotations.Nullable;

import com.google.common.collect.Iterables;
import com.mojang.datafixers.util.Either;
import de.hysky.skyblocker.utils.ItemUtils;

public class EnchantmentBookItemLookup implements WikiLookup {
	private static final Predicate<ItemStack> ENCHANTMENT_BOOK_FILTER = itemStack -> {
		CompoundTag nbt = ItemUtils.getCustomData(itemStack);
		if (itemStack.is(Items.ENCHANTED_BOOK) && nbt.contains("enchantments")) {
			CompoundTag enchantments = nbt.getCompoundOrEmpty("enchantments");
			// Only an enchantment book that contains one enchantment
			return enchantments.keySet().size() == 1;
		}
		return false;
	};
	public static final EnchantmentBookItemLookup INSTANCE = new EnchantmentBookItemLookup();

	private EnchantmentBookItemLookup() {}

	@Override
	public void open(ItemStack itemStack, Player player, boolean useOfficial) {
		CompoundTag nbt = ItemUtils.getCustomData(itemStack);
		CompoundTag enchantments = nbt.getCompoundOrEmpty("enchantments");
		String firstEnchantment = Iterables.getFirst(enchantments.keySet(), null)
				.replace("ultimate_", "") // Stripped out ultimate prefix
				.replace("_", " ").trim();
		String enchantment = REPLACING_FUNCTION.apply(WordUtils.capitalizeFully(firstEnchantment + " enchantment"));
		WikiLookupManager.openWikiLinkName(enchantment, player, useOfficial);
	}

	@Override
	public boolean canSearch(@Nullable String title, Either<Slot, ItemStack> either) {
		ItemStack itemStack = WikiLookupManager.mapEitherToItemStack(either);
		return ENCHANTMENT_BOOK_FILTER.test(itemStack);
	}
}
