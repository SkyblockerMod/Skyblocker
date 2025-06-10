package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.skyblock.InventorySearch;
import de.hysky.skyblocker.skyblock.ItemPickupWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
	@Inject(method = "setStackInSlot", at = @At("HEAD"))
	private void onSetStackInSlot(int slot, int revision, ItemStack stack, CallbackInfo ci) {
		if (InventorySearch.isSearching()) {
			InventorySearch.refreshSlot(slot);
		}
		ItemPickupWidget.getInstance().onItemPickup(slot, stack);
	}
}
