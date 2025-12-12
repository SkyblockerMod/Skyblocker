package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.gui.components.PlayerTabOverlay;

@Mixin(PlayerTabOverlay.class)
public class PlayerListHudMixin {

	@Inject(method = "renderPingIcon", at = @At("HEAD"), cancellable = true)
	private void skyblocker$hideLatencyIcon(CallbackInfo ci) {
		if (Utils.isOnSkyblock()) ci.cancel();
	}
}
