package de.hysky.skyblocker.utils.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class SkyblockerVertexFormats {
	public static final VertexFormat POSITION_NORMAL = VertexFormat.builder()
			.add("Position", VertexFormatElement.POSITION)
			.add("Normal", VertexFormatElement.NORMAL)
			.padding(1)
			.build();
}
