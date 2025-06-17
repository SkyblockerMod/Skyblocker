package de.hysky.skyblocker.mixins;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;

import net.minecraft.client.gl.GlResourceManager;

@Mixin(GlResourceManager.class)
public class GlResourceManagerMixin {

	@WrapWithCondition(method = "writeToBuffer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_glBufferSubData(IILjava/nio/ByteBuffer;)V", ordinal = 0))
	private boolean replaceBufferData(int target, int offset, ByteBuffer data, @Local(argsOnly = true) GpuBuffer gpuBuffer) {
		GlStateManager._glBufferData(target, data, GlConst.toGl(gpuBuffer.usage()));

		return false;
	}
}
