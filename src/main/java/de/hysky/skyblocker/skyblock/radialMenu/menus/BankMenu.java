package de.hysky.skyblocker.skyblock.radialMenu.menus;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BankMenu extends RegexMenu {
	public BankMenu() {
		super(".*bank.*", "bank");
	}

	/// Excludes the Banker Broadjaw Garden visitor
	@Override
	public boolean titleMatches(String title) {
		return super.titleMatches(title) && !title.contains("Banker Broadjaw");
	}

	@Override
	public boolean itemMatches(int slotId, ItemStack stack) {
		Item item = stack.getItem();
		return !(item.equals(Items.STAINED_GLASS_PANE.black()) || item.equals(Items.GOLD_BLOCK) || item.equals(Items.REDSTONE_TORCH) || item.equals(Items.FILLED_MAP));
	}
}
