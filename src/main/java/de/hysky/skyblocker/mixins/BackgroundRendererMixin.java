package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.CrimsonIsleConfig;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;

import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {

    /**
     * Moves fog farther away from the player when in the crimson isles.
     * This sets it to be the same distance as what you would see in the overworld (every other skyblock island)
     */
    @ModifyReturnValue(method = "applyFog", at = @At("RETURN"))
    private static Fog applyFogModifyDistance(Fog original, @Local(argsOnly = true) Camera camera, @Local(argsOnly = true) BackgroundRenderer.FogType fogType, @Local(argsOnly = true) Vector4f color, @Local(argsOnly = true, ordinal = 0) float viewDistance, @Local(argsOnly = true) boolean thickFog) {
        final CameraSubmersionType cameraSubmersionType = camera.getSubmersionType();
        CrimsonIsleConfig config = SkyblockerConfigManager.get().crimsonIsle;

        if (Utils.isOnSkyblock() && config.extendNetherFog && cameraSubmersionType == CameraSubmersionType.NONE && thickFog) {
            float start;
            if (fogType == BackgroundRenderer.FogType.FOG_SKY) {
                start = 0.0f;
            } else {
                start = viewDistance - Math.clamp(viewDistance / 10.0f, 4.0f, 64.0f);
            }

            return new Fog(start, original.end(), original.shape(), original.red(), original.green(), original.blue(), original.alpha());
        }

        return original;
    }
}
