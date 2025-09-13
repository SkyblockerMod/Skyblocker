package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.option.InactivityFpsLimiter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InactivityFpsLimiter.class)
public interface InactivityFpsLimiterAccessor {

	@Accessor("lastInputTime")
	long getLastInputTime();
}
