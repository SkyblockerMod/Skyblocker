package de.hysky.skyblocker.skyblock.item.wikilookup;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.datafixers.util.Either;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

public class VisitorLookup implements WikiLookup {
	public static final VisitorLookup INSTANCE = new VisitorLookup();

	private VisitorLookup() {}

	@Override
	public void open(@NotNull ItemStack itemStack, @NotNull PlayerEntity player, boolean useOfficial) {
		String itemName = REPLACING_FUNCTION.apply(itemStack.getName().getString());
		WikiLookupManager.openWikiLinkName(itemName, player, useOfficial);
	}

	@Override
	public boolean canSearch(@Nullable String title, @NotNull Either<Slot, ItemStack> either) {
		Optional<Slot> optional = either.left();
		if (optional.isEmpty()) return false;
		Slot slot = optional.get();
		if (slot.id <= 9 || slot.id >= 44) return false;
		if (slot.getStack().isOf(Items.BLACK_STAINED_GLASS_PANE)) return false;
		return StringUtils.isNotEmpty(title) && title.matches("^Visitor's Logbook$");
	}
}
