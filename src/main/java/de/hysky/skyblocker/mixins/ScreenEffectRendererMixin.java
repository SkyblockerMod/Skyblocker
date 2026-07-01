package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.util.ARGB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

	@ModifyArg(method = "buildFireQuad", index = 4, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;buildSpriteQuad(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;FFFFFI)V"))
	private static float configureFlameHeight1(float y) {
		return configureFlameHeightInternal(y);
	}

	@ModifyArg(method = "buildFireQuad", index = 6, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;buildSpriteQuad(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;FFFFFI)V"))
	private static float configureFlameHeight2(float y) {
		return configureFlameHeightInternal(y);
	}

	@Unique
	private static float configureFlameHeightInternal(float y) {
		return y - (0.5f - ((float) SkyblockerConfigManager.get().uiAndVisuals.flameOverlay.flameHeight / 200.0f));
	}

	@ModifyArg(method = "buildFireQuad", index = 8, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;buildSpriteQuad(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;FFFFFI)V"))
	private static int configureFlameOpacity(int colour) {
		float opacity = ARGB.alphaFloat(colour);
		float modifiedOpacity = opacity - (0.8f - ((float) SkyblockerConfigManager.get().uiAndVisuals.flameOverlay.flameOpacity / 125.0f));

		return ARGB.color(modifiedOpacity, colour);
	}
}
