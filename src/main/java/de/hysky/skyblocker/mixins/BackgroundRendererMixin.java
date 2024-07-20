package de.hysky.skyblocker.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.CrimsonIsleConfig;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FogShape;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {

    /**
     * Moves fog farther away from the player when in the crimson isles.
     * This sets it to be the same distance as what you would see in the overworld (every other skyblock island)
     */
    @Inject(method = "applyFog", at = @At("RETURN"))
    private static void applyFogModifyDistance(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo info) {
        final CameraSubmersionType cameraSubmersionType = camera.getSubmersionType();
        CrimsonIsleConfig config = SkyblockerConfigManager.get().crimsonIsle;

        if (Utils.isOnSkyblock()
                && config.extendNetherFog
                && cameraSubmersionType == CameraSubmersionType.NONE && thickFog) {

            float start;
            if (fogType == BackgroundRenderer.FogType.FOG_SKY) {
                start = 0.0f;
            } else {
                start = viewDistance - MathHelper.clamp(viewDistance / 10.0f, 4.0f, 64.0f);
            }

            RenderSystem.setShaderFogStart(start);
            RenderSystem.setShaderFogEnd(viewDistance);
            RenderSystem.setShaderFogShape(FogShape.CYLINDER);
        }
    }
}
