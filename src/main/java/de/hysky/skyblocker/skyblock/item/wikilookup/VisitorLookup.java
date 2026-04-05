package de.hysky.skyblocker.skyblock.item.wikilookup;

import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import com.mojang.datafixers.util.Either;

public class VisitorLookup implements WikiLookup {
	public static final VisitorLookup INSTANCE = new VisitorLookup();

	private VisitorLookup() {}

	@Override
	public void open(ItemStack itemStack, Player player, boolean useOfficial) {
		String itemName = REPLACING_FUNCTION.apply(itemStack.getHoverName().getString());
		WikiLookupManager.openWikiLinkName(itemName, player, useOfficial);
	}

	@Override
	public boolean canSearch(@Nullable String title, Either<Slot, ItemStack> either) {
		Optional<Slot> optional = either.left();
		if (optional.isEmpty()) return false;
		Slot slot = optional.get();
		if (slot.index <= 9 || slot.index >= 44) return false;
		if (slot.getItem().is(Items.BLACK_STAINED_GLASS_PANE)) return false;
		return StringUtils.isNotEmpty(title) && title.matches("^Visitor's Logbook$");
	}
}
