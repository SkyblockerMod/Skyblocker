package de.hysky.skyblocker.mixins.accessors;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DirectStateAccess.class)
public interface BufferManagerInvoker {

	@Invoker
	void invokeBufferData(int buffer, ByteBuffer data, int usage);
}
