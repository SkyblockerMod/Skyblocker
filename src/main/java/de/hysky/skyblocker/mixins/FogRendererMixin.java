package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

@Mixin(FogRenderer.class)
public class FogRendererMixin {

	/**
	 * Moves fog farther away from the player when in the crimson isles.
	 * This sets it to be the same distance as what you would see in the overworld (every other skyblock island)
	 */
	@Inject(method = "setupFog(Lnet/minecraft/client/Camera;ILnet/minecraft/client/DeltaTracker;FLnet/minecraft/client/multiplayer/ClientLevel;)Lorg/joml/Vector4f;", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/fog/FogData;renderDistanceEnd:F", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private void applyFogModifyDistance(CallbackInfoReturnable<Vector4f> ci, @Local FogData fogData) {
		if (Utils.isOnSkyblock() && Utils.isInCrimson() && SkyblockerConfigManager.get().crimsonIsle.extendNetherFog) {
			fogData.environmentalStart = Float.MAX_VALUE;
			fogData.environmentalEnd = Float.MAX_VALUE;
			fogData.renderDistanceStart = Float.MAX_VALUE;
			fogData.renderDistanceEnd = Float.MAX_VALUE;
		}
	}
}
