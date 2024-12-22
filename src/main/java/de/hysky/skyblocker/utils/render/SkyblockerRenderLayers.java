package de.hysky.skyblocker.utils.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayer.MultiPhase;
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.RenderPhase.DepthTest;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;

public class SkyblockerRenderLayers {
	public static final DepthTest OUTLINE_ALWAYS = new DepthTest("outline_always", GL11.GL_ALWAYS);

	public static final MultiPhase FILLED = RenderLayer.of("filled", VertexFormats.POSITION_COLOR, DrawMode.TRIANGLE_STRIP, RenderLayer.CUTOUT_BUFFER_SIZE, false, true, MultiPhaseParameters.builder()
			.program(RenderPhase.POSITION_COLOR_PROGRAM)
			.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
			.transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
			.depthTest(DepthTest.LEQUAL_DEPTH_TEST)
			.build(false));

	public static final MultiPhase FILLED_THROUGH_WALLS = RenderLayer.of("filled_through_walls", VertexFormats.POSITION_COLOR, DrawMode.TRIANGLE_STRIP, RenderLayer.CUTOUT_BUFFER_SIZE, false, true, MultiPhaseParameters.builder()
			.program(RenderPhase.POSITION_COLOR_PROGRAM)
			.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
			.transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
			.depthTest(DepthTest.ALWAYS_DEPTH_TEST)
			.build(false));
}
