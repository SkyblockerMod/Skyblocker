package de.hysky.skyblocker.mixins.accessors;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.opengl.DirectStateAccess;

@Mixin(DirectStateAccess.class)
public interface DirectStateAccessInvoker {

	@Invoker
	void invokeBufferData(int buffer, ByteBuffer data, int usage);
}
