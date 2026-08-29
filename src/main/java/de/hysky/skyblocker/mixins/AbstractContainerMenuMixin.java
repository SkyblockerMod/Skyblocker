package de.hysky.skyblocker.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;

import de.hysky.skyblocker.skyblock.InventorySearch;
import de.hysky.skyblocker.skyblock.ItemPickupWidget;
import de.hysky.skyblocker.skyblock.dungeon.partyfinder.PartyFinderScreen;
import de.hysky.skyblocker.skyblock.storageoverlay.StorageOverlayScreen;
import de.hysky.skyblocker.utils.container.ContainerSolverManager;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
	@Shadow
	public abstract void broadcastChanges();

	@Inject(method = "setItem", at = @At("HEAD"))
	private void beforeSetStackInSlot(int slot, int revision, ItemStack stack, CallbackInfo ci) {
		ItemPickupWidget.getInstance().onItemPickup(slot, stack);
	}

	@Inject(method = "setItem", at = @At("RETURN"))
	private void onSetStackInSlot(int slot, int revision, ItemStack stack, CallbackInfo ci) {
		ContainerSolverManager.markHighlightsDirty();
		if (InventorySearch.isSearching()) {
			InventorySearch.refreshSlot(slot);
		}

		// instanceof check to prevent changing behavior from old ChestMenuMixin
		if ((Object) this instanceof ChestMenu) {
			switch (Minecraft.getInstance().screen) {
				case PartyFinderScreen screen -> screen.markDirty();
				case StorageOverlayScreen screen -> screen.refreshSearch();
				case null, default -> {}
			}
			broadcastChanges();
		}
	}

	@Inject(method = "initializeContents", at = @At("RETURN"))
	public void initializeContents(int stateId, List<ItemStack> items, ItemStack carried, CallbackInfo ci) {
		ContainerSolverManager.markHighlightsDirty();

		// instanceof check to prevent changing behavior from old ChestMenuMixin
		if ((Object) this instanceof ChestMenu) {
			switch (Minecraft.getInstance().screen) {
				case PartyFinderScreen screen -> screen.markDirty();
				case StorageOverlayScreen screen -> screen.refreshSearch();
				case null, default -> {}
			}
			broadcastChanges();
		}
	}
}
