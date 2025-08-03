package de.hysky.skyblocker.mixins;

import net.minecraft.entity.projectile.FishingBobberEntity;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import de.hysky.skyblocker.utils.Utils;

@Mixin(FishingBobberEntity.class)
public class FishingBobberEntityMixin {

	@WrapWithCondition(method = "onSpawnPacket", at = @At(
			value = "INVOKE",
			target = "org/slf4j/Logger.error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
			remap = false))
	private static boolean skyblocker$dontLogInvalidOwner(Logger logger, String message, Object entityId, Object entityData) {
		return !Utils.isOnHypixel();
	}
}
