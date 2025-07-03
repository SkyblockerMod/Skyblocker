package de.hysky.skyblocker.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import dev.isxander.yacl3.config.v2.impl.ConfigClassHandlerImpl;

@Mixin(ConfigClassHandlerImpl.class)
public interface ConfigClassHandlerImplAccessor {

	@Accessor
	void setInstance(Object instance);
}
