package de.hysky.skyblocker.mixins;

import java.awt.Color;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.HeadTextures;
import de.hysky.skyblocker.skyblock.item.PlayerHeadHashCache;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerScreen;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.PlayerSkinTextureDownloader;
import net.minecraft.util.math.ColorHelper;

@Mixin(PlayerSkinTextureDownloader.class)
public class PlayerSkinTextureDownloaderMixin {
	@Unique
	private static final Set<String> STRIP_DE_FACTO_TRANSPARENT_PIXELS = Set.of(
			PlayerHeadHashCache.getSkinHashFromBase64(HeadTextures.TITANIUM_NECKLACE),
			PlayerHeadHashCache.getSkinHashFromBase64(HeadTextures.TITANIUM_CLOAK),
			PlayerHeadHashCache.getSkinHashFromBase64(HeadTextures.TITANIUM_BELT));
	@Unique
	private static final float BRIGHTNESS_THRESHOLD = 0.1f;

	@Inject(method = "remapTexture", at = @At("HEAD"))
	private static void skyblocker$determineSkinSource(NativeImage image, String uri, CallbackInfoReturnable<NativeImage> cir, @Share("isSkyblockSkinTexture") LocalBooleanRef isSkyblockSkinTexture) {
		if (SkyblockerConfigManager.get().uiAndVisuals.dontStripSkinAlphaValues && (Utils.isOnSkyblock() || MinecraftClient.getInstance().currentScreen instanceof ProfileViewerScreen)) {
			String skinTextureHash = PlayerHeadHashCache.getSkinHashFromUrl(uri);
			int skinHash = skinTextureHash.hashCode();
			isSkyblockSkinTexture.set(PlayerHeadHashCache.contains(skinHash));

			//Hypixel had the grand idea of using black pixels in place of actual transparent pixels on the titanium equipment so here we go!
			if (STRIP_DE_FACTO_TRANSPARENT_PIXELS.contains(skinTextureHash)) {
				stripDeFactoTransparentPixels(image);
			}
		}
	}

	@WrapWithCondition(method = "remapTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/PlayerSkinTextureDownloader;stripAlpha(Lnet/minecraft/client/texture/NativeImage;IIII)V"))
	private static boolean skyblocker$dontStripAlphaValues(NativeImage image, int x1, int y1, int x2, int y2, @Share("isSkyblockSkinTexture") LocalBooleanRef isSkyblockSkinTexture) {
		return !isSkyblockSkinTexture.get();
	}

	@Unique
	private static void stripDeFactoTransparentPixels(NativeImage image) {
		int height = image.getHeight();
		int width = image.getWidth();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int color = image.getColorArgb(x, y);
				float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);

				//Work around "fake" transparent pixels - Thanks Hypixel I totally appreciate this!
				if (hsb[2] <= BRIGHTNESS_THRESHOLD) image.setColorArgb(x, y, ColorHelper.withAlpha(0x00, color & 0x00FFFFFF));
			}
		}
	}
}
