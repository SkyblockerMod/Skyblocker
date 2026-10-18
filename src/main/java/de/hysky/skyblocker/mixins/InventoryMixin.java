package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.player.Inventory;

import de.hysky.skyblocker.skyblock.crimson.kuudra.ArrowPoisonWarning;

@Mixin(Inventory.class)
public class InventoryMixin {
	@Shadow
	public int selected;

	@Inject(method = "setSelectedSlot", at = @At("TAIL"))
	private void skyblocker$onHotbarScroll(CallbackInfo ci) {
		ArrowPoisonWarning.tryWarn(selected);
	}
}
