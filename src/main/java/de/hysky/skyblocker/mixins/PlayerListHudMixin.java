package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.gui.hud.PlayerListHud;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

	@Inject(method = "renderLatencyIcon", at = @At("HEAD"), cancellable = true)
	private void hideLatencyIcon(CallbackInfo ci) {
		if (Utils.isOnSkyblock()) ci.cancel();
	}
}
