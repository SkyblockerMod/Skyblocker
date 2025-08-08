package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.render.fog.FogRenderer;

import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public class FogRendererMixin {

    /**
     * Moves fog farther away from the player when in the crimson isles.
     * This sets it to be the same distance as what you would see in the overworld (every other skyblock island)
     */
	@Inject(method = "applyFog(Lnet/minecraft/client/render/Camera;IZLnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;", at = @At("HEAD"))
	private void applyFogModifyDistance(CallbackInfoReturnable<Vector4f> ci, @Local(argsOnly = true) LocalBooleanRef thickFog) {
		if (Utils.isOnSkyblock() && Utils.isInCrimson() && SkyblockerConfigManager.get().crimsonIsle.extendNetherFog && thickFog.get()) {
			thickFog.set(false);
		}
	}
}
