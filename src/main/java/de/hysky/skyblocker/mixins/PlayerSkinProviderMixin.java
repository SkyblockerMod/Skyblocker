package de.hysky.skyblocker.mixins;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import de.hysky.skyblocker.utils.Utils;

@Mixin(targets = "net.minecraft.client.texture.PlayerSkinProvider$1")
public class PlayerSkinProviderMixin {

	@WrapWithCondition(method = "method_54647", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
	private static boolean skyblocker$dontLogInvalidSignatureWarnings(Logger logger, String message, Object profileId) {
		return !Utils.isOnHypixel();
	}
}
