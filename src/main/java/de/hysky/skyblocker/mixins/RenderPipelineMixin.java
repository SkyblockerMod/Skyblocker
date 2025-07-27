package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;

import de.hysky.skyblocker.utils.render.GlowRenderer;
import net.minecraft.client.gl.RenderPipelines;

@Mixin(RenderPipeline.class)
public class RenderPipelineMixin {

	@ModifyReturnValue(method = "getDepthTestFunction", at = @At("RETURN"))
	private DepthTestFunction skyblocker$modifyGlowDepthTest(DepthTestFunction original) {
		return ((Object) this == RenderPipelines.OUTLINE_CULL || (Object) this == RenderPipelines.OUTLINE_NO_CULL) && GlowRenderer.isRenderingGlow() ? DepthTestFunction.LEQUAL_DEPTH_TEST : original;
	}
}
