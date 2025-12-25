package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.skyblock.item.ItemStackUpdateDurability;
import de.hysky.skyblocker.skyblock.InventorySearch;
import de.hysky.skyblocker.skyblock.ItemPickupWidget;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
	@Inject(method = "setItem", at = @At("HEAD"))
	private void onSetStackInSlot(int slot, int revision, ItemStack stack, CallbackInfo ci) {
		if (InventorySearch.isSearching()) {
			InventorySearch.refreshSlot(slot);
		}
		ItemPickupWidget.getInstance().onItemPickup(slot, stack);
		((ItemStackUpdateDurability) (Object) stack).skyblocker$getAndCacheDurability();
	}
}
