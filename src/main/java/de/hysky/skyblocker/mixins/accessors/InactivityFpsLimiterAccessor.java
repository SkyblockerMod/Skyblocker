package de.hysky.skyblocker.mixins.accessors;

import com.mojang.blaze3d.platform.FramerateLimitTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FramerateLimitTracker.class)
public interface InactivityFpsLimiterAccessor {

	@Accessor("latestInputTime")
	long getLatestInputTime();
}
