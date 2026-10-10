package de.hysky.skyblocker.utils.render;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.UniformType;

public class SkyblockerBindGroupLayouts {
	public static final BindGroupLayout BOX_DATA = BindGroupLayout.builder()
			.withUniform("BoxData", UniformType.TEXEL_BUFFER, GpuFormat.RGBA32_FLOAT)
			.build();
	public static final BindGroupLayout OUTLINED_BOX_DATA = BindGroupLayout.builder()
			.withUniform("OutlinedBoxData", UniformType.TEXEL_BUFFER, GpuFormat.RGBA32_FLOAT)
			.build();
}
