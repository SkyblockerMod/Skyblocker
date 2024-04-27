package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {

    @ModifyArg(method = "renderFireOverlay", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BufferBuilder;vertex(Lorg/joml/Matrix4f;FFF)Lnet/minecraft/client/render/VertexConsumer;"))
    private static float configureFlameHeight(float y) {
        return y - (0.5f - ((float) SkyblockerConfigManager.get().general.flameOverlay.flameHeight / 200.0f));
    }

    @ModifyArg(method = "renderFireOverlay", index = 3, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumer;color(FFFF)Lnet/minecraft/client/render/VertexConsumer;"))
    private static float configureFlameOpacity(float opacity) {
        return opacity - (0.8f - ((float) SkyblockerConfigManager.get().general.flameOverlay.flameOpacity / 125.0f));
    }

}
