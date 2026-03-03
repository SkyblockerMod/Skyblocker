package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Implementation of an {@link RecipeBookMenu} that does not do anything.
 */
public class NoopRecipeScreenHandler extends RecipeBookMenu {
	protected NoopRecipeScreenHandler(int syncId) {
		super(MenuType.GENERIC_9x6, syncId);
	}

	@Override
	public PostPlaceAction handlePlacement(boolean craftAll, boolean creative, RecipeHolder<?> recipe, ServerLevel world, Inventory inventory) {
		return PostPlaceAction.NOTHING;
	}

	@Override
	public void fillCraftSlotsStackedContents(StackedItemContents finder) {}

	@Override
	public RecipeBookType getRecipeBookType() {
		return RecipeBookType.CRAFTING;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return false;
	}
}
