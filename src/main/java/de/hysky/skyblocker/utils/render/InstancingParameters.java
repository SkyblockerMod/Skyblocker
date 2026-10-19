package de.hysky.skyblocker.utils.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import org.jspecify.annotations.Nullable;

public record InstancingParameters(int count, @Nullable String name, @Nullable GpuBufferSlice buffer) {
	public static final InstancingParameters NONE = new InstancingParameters(1, null, null);
}
