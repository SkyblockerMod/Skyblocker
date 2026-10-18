package de.hysky.skyblocker.utils.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

public class SkyblockerVertexFormats {
	public static final VertexFormat POSITION_NORMAL = VertexFormat.builder(0)
			.addAttribute(DefaultVertexFormat.POSITION_SEMANTIC_NAME, DefaultVertexFormat.POSITION_FORMAT)
			.addAttribute(DefaultVertexFormat.NORMAL_SEMANTIC_NAME, DefaultVertexFormat.NORMAL_FORMAT)
			.build();
}
