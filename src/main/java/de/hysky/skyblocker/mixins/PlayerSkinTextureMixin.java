package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.PlayerHeadHashCache;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.PlayerSkinTexture;

@Mixin(PlayerSkinTexture.class)
public class PlayerSkinTextureMixin {
	@Shadow
	@Final
	private String url;

	@Unique
	private boolean isSkyblockSkinTexture;

	@Inject(method = "remapTexture", at = @At("HEAD"))
	private void skyblocker$determineSkinSource(CallbackInfoReturnable<NativeImage> cir) {
		if (Utils.isOnSkyblock()) {
			int skinHash = PlayerHeadHashCache.getSkinHash(this.url).hashCode();
			this.isSkyblockSkinTexture = PlayerHeadHashCache.contains(skinHash);
		}
	}

	@WrapWithCondition(method = "remapTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/PlayerSkinTexture;stripAlpha(Lnet/minecraft/client/texture/NativeImage;IIII)V"))
	private boolean skyblocker$dontStripAlphaValues(NativeImage image, int x1, int y1, int x2, int y2) {
		return !(SkyblockerConfigManager.get().general.dontStripSkinAlphaValues && this.isSkyblockSkinTexture);
	}
}
