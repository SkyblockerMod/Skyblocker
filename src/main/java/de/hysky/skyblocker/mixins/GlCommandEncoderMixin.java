package de.hysky.skyblocker.mixins;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import de.hysky.skyblocker.mixins.accessors.DirectStateAccessInvoker;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlCommandEncoder")
public class GlCommandEncoderMixin {

	@WrapWithCondition(method = "writeToBuffer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/DirectStateAccess;bufferSubData(IJLjava/nio/ByteBuffer;I)V"))
	private static boolean skyblocker$replaceBufferData(DirectStateAccess manager, int buffer, long offset, ByteBuffer data, int usage, @Local(name = "slice") GpuBufferSlice slice) {
		if (offset == 0 && slice.length() == slice.buffer().size()) {
			((DirectStateAccessInvoker) manager).invokeBufferData(buffer, data, slice.buffer().usage());

			return false;
		}

		return true;
	}
}
