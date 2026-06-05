package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;

import de.hysky.skyblocker.utils.render.GlowRenderer;
import net.minecraft.client.renderer.RenderPipelines;

@Mixin(RenderPipeline.class)
public class RenderPipelineMixin {

	@ModifyReturnValue(method = "getDepthStencilState", at = @At("RETURN"))
	private DepthStencilState skyblocker$modifyGlowDepthTest(DepthStencilState original) {
		return ((Object) this == RenderPipelines.OUTLINE_CULL || (Object) this == RenderPipelines.OUTLINE_NO_CULL) && GlowRenderer.isRenderingGlow() ? DepthStencilState.DEFAULT : original;
	}
}
