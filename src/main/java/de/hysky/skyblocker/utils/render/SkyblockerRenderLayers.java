package de.hysky.skyblocker.utils.render;

import java.util.OptionalDouble;
import java.util.function.DoubleFunction;
import java.util.function.Function;

import org.lwjgl.opengl.GL11;

import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayer.MultiPhase;
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.RenderPhase.DepthTest;
import net.minecraft.client.render.RenderPhase.LineWidth;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;

public class SkyblockerRenderLayers {
	public static final DepthTest OUTLINE_ALWAYS = new DepthTest("outline_always", GL11.GL_ALWAYS);
	private static final Double2ObjectMap<MultiPhase> LINES_LAYERS = new Double2ObjectOpenHashMap<>();
	private static final Double2ObjectMap<MultiPhase> LINES_THROUGH_WALLS_LAYERS = new Double2ObjectOpenHashMap<>();
	private static final Object2ObjectMap<Identifier, MultiPhase> TEXTURE_LAYERS = new Object2ObjectOpenHashMap<>();
	private static final Object2ObjectMap<Identifier, MultiPhase> TEXTURE_THROUGH_WALLS_LAYERS = new Object2ObjectOpenHashMap<>();

	public static final MultiPhase FILLED = RenderLayer.of("filled", VertexFormats.POSITION_COLOR, DrawMode.TRIANGLE_STRIP, RenderLayer.CUTOUT_BUFFER_SIZE, false, true, MultiPhaseParameters.builder()
			.program(RenderPhase.POSITION_COLOR_PROGRAM)
			.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
			.transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
			.depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
			.build(false));

	public static final MultiPhase FILLED_THROUGH_WALLS = RenderLayer.of("filled_through_walls", VertexFormats.POSITION_COLOR, DrawMode.TRIANGLE_STRIP, RenderLayer.CUTOUT_BUFFER_SIZE, false, true, MultiPhaseParameters.builder()
			.program(RenderPhase.POSITION_COLOR_PROGRAM)
			.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
			.transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
			.depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
			.build(false));

	private static final DoubleFunction<MultiPhase> LINES = lineWidth -> RenderLayer.of("lines", VertexFormats.LINES, DrawMode.LINES, RenderLayer.DEFAULT_BUFFER_SIZE, false, false, MultiPhaseParameters.builder()
			.program(RenderPhase.LINES_PROGRAM)
			.lineWidth(new LineWidth(OptionalDouble.of(lineWidth)))
			.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
			.transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
			.writeMaskState(RenderPhase.ALL_MASK)
			.cull(RenderPhase.DISABLE_CULLING)
			.depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
			.build(false));

	private static final DoubleFunction<MultiPhase> LINES_THROUGH_WALLS = lineWidth -> RenderLayer.of("lines_through_walls", VertexFormats.LINES, DrawMode.LINES, RenderLayer.DEFAULT_BUFFER_SIZE, false, false, MultiPhaseParameters.builder()
			.program(RenderPhase.LINES_PROGRAM)
			.lineWidth(new LineWidth(OptionalDouble.of(lineWidth)))
			.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
			.transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
			.writeMaskState(RenderPhase.ALL_MASK)
			.cull(RenderPhase.DISABLE_CULLING)
			.depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
			.build(false));

	public static final MultiPhase QUAD = RenderLayer.of("quad", VertexFormats.POSITION_COLOR, DrawMode.QUADS, RenderLayer.DEFAULT_BUFFER_SIZE, false, true, MultiPhaseParameters.builder()
			.program(RenderPhase.POSITION_COLOR_PROGRAM)
			.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
			.transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
			.cull(RenderPhase.DISABLE_CULLING)
			.depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
			.build(false));

	public static final MultiPhase QUAD_THROUGH_WALLS = RenderLayer.of("quad_through_walls", VertexFormats.POSITION_COLOR, DrawMode.QUADS, RenderLayer.DEFAULT_BUFFER_SIZE, false, true, MultiPhaseParameters.builder()
			.program(RenderPhase.POSITION_COLOR_PROGRAM)
			.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
			.transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
			.cull(RenderPhase.DISABLE_CULLING)
			.depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
			.build(false));

	private static final Function<Identifier, MultiPhase> TEXTURE = texture -> RenderLayer.of("texture", VertexFormats.POSITION_TEXTURE_COLOR, DrawMode.QUADS, RenderLayer.DEFAULT_BUFFER_SIZE, false, true, MultiPhaseParameters.builder()
			.program(RenderPhase.POSITION_TEXTURE_COLOR_PROGRAM)
			.texture(new RenderPhase.Texture(texture, TriState.FALSE, false))
			.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
			.transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
			.cull(RenderPhase.DISABLE_CULLING)
			.depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
			.build(false));

	private static final Function<Identifier, MultiPhase> TEXTURE_THROUGH_WALLS = texture -> RenderLayer.of("texture_through_walls", VertexFormats.POSITION_TEXTURE_COLOR, DrawMode.QUADS, RenderLayer.DEFAULT_BUFFER_SIZE, false, true, MultiPhaseParameters.builder()
			.program(RenderPhase.POSITION_TEXTURE_COLOR_PROGRAM)
			.texture(new RenderPhase.Texture(texture, TriState.FALSE, false))
			.layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
			.transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
			.cull(RenderPhase.DISABLE_CULLING)
			.depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
			.build(false));

	public static MultiPhase getLines(double lineWidth) {
		return LINES_LAYERS.computeIfAbsent(lineWidth, LINES);
	}

	public static MultiPhase getLinesThroughWalls(double lineWidth) {
		return LINES_THROUGH_WALLS_LAYERS.computeIfAbsent(lineWidth, LINES_THROUGH_WALLS);
	}

	public static MultiPhase getTexture(Identifier texture) {
		return TEXTURE_LAYERS.computeIfAbsent(texture, TEXTURE);
	}

	public static MultiPhase getTextureThroughWalls(Identifier texture) {
		return TEXTURE_THROUGH_WALLS_LAYERS.computeIfAbsent(texture, TEXTURE_THROUGH_WALLS);
	}
}
