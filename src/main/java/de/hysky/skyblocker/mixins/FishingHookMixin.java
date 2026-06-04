package de.hysky.skyblocker.mixins;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.world.entity.projectile.FishingHook;

@Mixin(FishingHook.class)
public class FishingHookMixin {

	@WrapWithCondition(method = "recreateFromPacket", at = @At(
			value = "INVOKE",
			target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
	private static boolean skyblocker$dontLogInvalidOwner(Logger logger, String message, Object entityId, Object entityData) {
		return !Utils.isOnHypixel();
	}
}
