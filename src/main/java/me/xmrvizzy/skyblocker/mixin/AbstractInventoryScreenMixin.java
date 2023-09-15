package me.xmrvizzy.skyblocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;

@Mixin(AbstractInventoryScreen.class)
public class AbstractInventoryScreenMixin {

	@Inject(method = "drawStatusEffects", at = @At("HEAD"), cancellable = true)
	private void skyblocker$dontDrawStatusEffects(CallbackInfo ci) {
		if (Utils.isOnSkyblock() && SkyblockerConfig.get().general.hideStatusEffectOverlay) ci.cancel();
	}
}
