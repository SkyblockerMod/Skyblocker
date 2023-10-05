package de.hysky.skyblocker.mixin;

import de.hysky.skyblocker.utils.Utils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;

@Mixin(value = YggdrasilMinecraftSessionService.class, remap = false)
public class YggdrasilMinecraftSessionServiceMixin {

	@WrapOperation(method = "getSecurePropertyValue", remap = false, at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
	private void skyblocker$dontLogMissingSignaturesOrTamperedProperties(Logger logger, String message, Object property, Operation<Void> operation) {
		if (!Utils.isOnHypixel()) operation.call(logger, message, property);
	}
}
