package de.hysky.skyblocker.mixins.accessors;

import java.nio.ByteBuffer;

import com.mojang.renderpearl.backend.opengl.DirectStateAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DirectStateAccess.class)
public interface DirectStateAccessInvoker {

	@Invoker
	void invokeBufferData(int buffer, ByteBuffer data, int usage);
}
