package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.skyblock.item.ItemStackUpdateDurability;
import de.hysky.skyblocker.skyblock.InventorySearch;
import de.hysky.skyblocker.skyblock.ItemPickupWidget;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
	@Shadow
	@Final
	public NonNullList<Slot> slots;

	@Shadow
	private ItemStack carried;

	///  When the server creates or updates the entire menu
	@Inject(method = "initializeContents", at = @At("TAIL"))
	private void skyblocker$initializeContents(int stateId, List<ItemStack> list, ItemStack itemStack, CallbackInfo ci) {
		for (int j = 0; j < list.size(); j++) {
			((ItemStackUpdateDurability) (Object) this.slots.get(j).getItem()).skyblocker$getAndCacheDurability();
			if (InventorySearch.isSearching()) {
				InventorySearch.refreshSlot(j);
			}
		}
		((ItemStackUpdateDurability) (Object) this.carried).skyblocker$getAndCacheDurability();
	}

	///  When the player clicks
	@Inject(method = "doClick", at = @At("TAIL"))
	private void skyblocker$doClick(int i, int j, ClickType clickType, Player player, CallbackInfo ci) {
		// I'm way too lazy to figure how to only update the slots that were moved, soo...
		for (int k = 0; k < this.slots.size(); k++) {
			((ItemStackUpdateDurability) (Object) this.slots.get(k).getItem()).skyblocker$getAndCacheDurability();
			if (InventorySearch.isSearching()) {
				InventorySearch.refreshSlot(k);
			}
		}
		((ItemStackUpdateDurability) (Object) this.carried).skyblocker$getAndCacheDurability();
	}

	/// When the server updates a single item
	@Inject(method = "setItem", at = @At("HEAD"))
	private void onSetStackInSlot(int slot, int revision, ItemStack stack, CallbackInfo ci) {
		if (InventorySearch.isSearching()) {
			InventorySearch.refreshSlot(slot);
		}
		ItemPickupWidget.getInstance().onItemPickup(slot, stack);
		((ItemStackUpdateDurability) (Object) stack).skyblocker$getAndCacheDurability();
	}
}
