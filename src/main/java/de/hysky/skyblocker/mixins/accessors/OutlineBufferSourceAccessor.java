package de.hysky.skyblocker.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;

@Mixin(OutlineBufferSource.class)
public interface OutlineBufferSourceAccessor {

	@Accessor
	@Mutable
	void setOutlineBufferSource(MultiBufferSource.BufferSource immediate);
}
