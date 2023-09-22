package me.xmrvizzy.skyblocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;

import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.texture.PlayerSkinProvider.Textures;

@Mixin(targets = "net.minecraft.client.texture.PlayerSkinProvider$1")
public class PlayerSkinProviderMixin {

	@ModifyReturnValue(method = "method_52867", at = @At("RETURN"))
	private static Textures skyblocker$fixTexturesThatHadAnInvalidSignature(Textures texture, @Local MinecraftSessionService sessionService, @Local GameProfile profile) {
		if (Utils.isOnHypixel() && texture == Textures.MISSING) {
			try {
				return Textures.fromMap(sessionService.getTextures(profile, false), false);
			} catch (Throwable t) {
				return Textures.MISSING;
			}
		}

		return texture;
	}
}
