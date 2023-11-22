package de.hysky.skyblocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;

import de.hysky.skyblocker.utils.Utils;

@Mixin(targets = "net.minecraft.client.texture.PlayerSkinProvider$1")
public class PlayerSkinProviderMixin {

	//TODO This may not be needed anymore, will need to check
	/*@ModifyReturnValue(method = "method_52867", at = @At("RETURN"))
	private static MinecraftProfileTextures skyblocker$fixTexturesThatHadAnInvalidSignature(MinecraftProfileTextures texture, @Local MinecraftSessionService sessionService, @Local GameProfile profile) {
		if (Utils.isOnHypixel() && texture == MinecraftProfileTextures.EMPTY) {
			try {
				return MinecraftProfileTextures.fromMap(sessionService.getTextures(profile, false), false);
			} catch (Throwable t) {
				return MinecraftProfileTextures.EMPTY;
			}
		}

		return texture;
	}*/
}
