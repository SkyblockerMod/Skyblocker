package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ScreenEffectRenderer.class)
public class InGameOverlayRendererMixin {

	@ModifyArg(method = "renderFire", index = 2, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;addVertex(Lorg/joml/Matrix4fc;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
	private static float configureFlameHeight(float y) {
		return y - (0.5f - ((float) SkyblockerConfigManager.get().uiAndVisuals.flameOverlay.flameHeight / 200.0f));
	}

	@ModifyArg(method = "renderFire", index = 3, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setColor(FFFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
	private static float configureFlameOpacity(float opacity) {
		return opacity - (0.8f - ((float) SkyblockerConfigManager.get().uiAndVisuals.flameOverlay.flameOpacity / 125.0f));
	}

}
