package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.FogRenderer;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FogRenderer.class)
public class FogRendererMixin {

    /**
     * Moves fog farther away from the player when in the crimson isles.
     * This sets it to be the same distance as what you would see in the overworld (every other skyblock island)
     */
	@WrapOperation(method = "applyFog", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/fog/FogData;renderDistanceStart:F", opcode = Opcodes.PUTFIELD))
	private void applyFogModifyDistance(FogData fogData, float renderDistanceStart, Operation<Void> operation, @Local(argsOnly = true) boolean thick, @Local CameraSubmersionType cameraSubmersionType) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().crimsonIsle.extendNetherFog && cameraSubmersionType == CameraSubmersionType.ATMOSPHERIC && thick) {
			operation.call(fogData, 0f);
		} else {
			operation.call(fogData, renderDistanceStart);
		}
	}
}
