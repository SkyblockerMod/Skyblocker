package de.hysky.skyblocker.skyblock.item.wikilookup;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.common.collect.Iterables;
import com.mojang.datafixers.util.Either;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;

public class EnchantmentBookItemLookup implements WikiLookup {
	public static final EnchantmentBookItemLookup INSTANCE = new EnchantmentBookItemLookup();

	private EnchantmentBookItemLookup() {}

	@Override
	public void open(@NotNull ItemStack itemStack, @NotNull PlayerEntity player, boolean useOfficial) {
		NbtCompound nbt = ItemUtils.getCustomData(itemStack);
		NbtCompound enchantments = nbt.getCompoundOrEmpty("enchantments");
		String firstEnchantment = Iterables.getFirst(enchantments.getKeys(), null)
				.replace("ultimate_", "") // Stripped out ultimate prefix
				.replace("_", " ").trim();
		String enchantment = REPLACING_FUNCTION.apply(WordUtils.capitalizeFully(firstEnchantment + " enchantment"));
		WikiLookupManager.openWikiLinkName(enchantment, player, useOfficial);
	}

	@Override
	public boolean canSearch(@Nullable String title, @NotNull Either<Slot, ItemStack> either) {
		AtomicBoolean canSearch = new AtomicBoolean();
		Function<ItemStack, Boolean> function = itemStack -> {
			NbtCompound nbt = ItemUtils.getCustomData(itemStack);
			if (itemStack.isOf(Items.ENCHANTED_BOOK) && nbt.contains("enchantments")) {
				NbtCompound enchantments = nbt.getCompoundOrEmpty("enchantments");
				// Only an enchantment book that contains one enchantment
				return enchantments.getKeys().size() == 1;
			}
			return false;
		};
		either.ifLeft(slot -> canSearch.set(function.apply(slot.getStack())));
		either.ifRight(itemStack -> canSearch.set(function.apply(itemStack)));
		return canSearch.get();
	}
}
