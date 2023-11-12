package de.hysky.skyblocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;

import de.hysky.skyblocker.utils.render.RenderHelper;
import dev.cbyrne.betterinject.annotations.Inject;
import net.minecraft.client.render.WorldRenderer;

/**
 * Injects after Fabric's After Translucent world render event
 */
@Mixin(value = WorldRenderer.class, priority = 2000)
public class AfterTranslucentMixin {
	
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleManager;renderParticles(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/client/render/Camera;F)V"))
	private void onRenderParticles(@Share("renderedParticles") LocalBooleanRef renderedParticles) {
		renderedParticles.set(true);
	}
	
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V"))
	private void beforeClouds(@Share("renderedParticles") LocalBooleanRef renderedParticles) {
		if (renderedParticles.get()) {
			renderedParticles.set(false);
			RenderHelper.drawGlobalObjectsAfterTranslucent();
		}
	}
}
