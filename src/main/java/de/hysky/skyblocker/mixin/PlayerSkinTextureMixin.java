package de.hysky.skyblocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import dev.cbyrne.betterinject.annotations.Inject;
import net.minecraft.client.texture.PlayerSkinTexture;

@Mixin(PlayerSkinTexture.class)
public class PlayerSkinTextureMixin {

	@Inject(method = "stripAlpha", at = @At("HEAD"), cancellable = true)
	private static void skyblocker$dontStripAlphaValues(CallbackInfo ci) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().general.dontStripSkinAlphaValues) ci.cancel();
	}
}
