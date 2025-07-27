package de.hysky.skyblocker.mixins.accessors;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gl.BufferManager;

@Mixin(BufferManager.class)
public interface BufferManagerInvoker {

	@Invoker
	void invokeSetBufferData(int buffer, ByteBuffer data, int usage);
}
