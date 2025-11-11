package de.hysky.skyblocker.mixins;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;

import de.hysky.skyblocker.mixins.accessors.BufferManagerInvoker;
import net.minecraft.client.gl.BufferManager;
import net.minecraft.client.gl.GlCommandEncoder;

@Mixin(GlCommandEncoder.class)
public class GlCommandEncoderMixin {

	@WrapWithCondition(method = "writeToBuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/BufferManager;setBufferSubData(IILjava/nio/ByteBuffer;I)V"))
	private static boolean aaronMod$replaceBufferData(BufferManager manager, int buffer, int offset, ByteBuffer data, int usage, @Local(argsOnly = true) GpuBufferSlice gpuBufferSlice) {
		if (offset == 0 && gpuBufferSlice.length() == gpuBufferSlice.buffer().size()) {
			((BufferManagerInvoker) manager).invokeSetBufferData(buffer, data, gpuBufferSlice.buffer().usage());

			return false;
		}

		return true;
	}
}
