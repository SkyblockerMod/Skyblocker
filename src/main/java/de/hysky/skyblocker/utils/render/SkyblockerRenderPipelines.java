package de.hysky.skyblocker.utils.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.compatibility.IrisCompatibility;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class SkyblockerRenderPipelines {
	/** Similar to {@link RenderPipelines#DEBUG_FILLED_BOX} */
	public static final RenderPipeline FILLED_THROUGH_WALLS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
			.withLocation(Identifier.of(SkyblockerMod.NAMESPACE, "pipeline/debug_filled_box_through_walls"))
			.withVertexFormat(VertexFormats.POSITION_COLOR, DrawMode.TRIANGLE_STRIP)
			.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
			.build());
	/** Similar to {@link RenderPipelines#LINES} */
	public static final RenderPipeline LINES_THROUGH_WALLS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET)
			.withLocation(Identifier.of(SkyblockerMod.NAMESPACE, "pipeline/lines_through_walls"))
			.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
			.build());
	/** Similar to {@link RenderPipelines#DEBUG_QUADS}  */
	public static final RenderPipeline QUADS_THROUGH_WALLS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
			.withLocation(Identifier.of(SkyblockerMod.NAMESPACE, "pipeline/debug_quads_through_walls"))
			.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
			.withCull(false)
			.build());
	/** Similar to {@link RenderPipelines#GUI_TEXTURED} */
	public static final RenderPipeline TEXTURE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.POSITION_TEX_COLOR_SNIPPET)
			.withLocation(Identifier.of(SkyblockerMod.NAMESPACE, "pipeline/texture"))
			.withCull(false)
			.build());
	public static final RenderPipeline TEXTURE_THROUGH_WALLS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.POSITION_TEX_COLOR_SNIPPET)
			.withLocation(Identifier.of(SkyblockerMod.NAMESPACE, "pipeline/texture_through_walls"))
			.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
			.withCull(false)
			.build());
	public static final RenderPipeline CYLINDER = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
			.withLocation(Identifier.of(SkyblockerMod.NAMESPACE, "pipeline/cylinder"))
			.withVertexFormat(VertexFormats.POSITION_COLOR, DrawMode.TRIANGLE_STRIP)
			.withCull(false)
			.build());
	public static final RenderPipeline CIRCLE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
			.withLocation(Identifier.of(SkyblockerMod.NAMESPACE, "pipeline/circle"))
			.withVertexFormat(VertexFormats.POSITION_COLOR, DrawMode.TRIANGLE_FAN)
			.withCull(false)
			.build());
	public static final RenderPipeline CIRCLE_LINES = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
			.withLocation(Identifier.of(SkyblockerMod.NAMESPACE, "pipeline/circle_lines"))
			.withVertexFormat(VertexFormats.POSITION_COLOR, DrawMode.QUADS)
			.withCull(false)
			.build());

	/**
	 * Ensures that pipelines are pre-compiled instead of compiled on demand. Also used for excluding some pipelines from batching.
	 */
	@Init
	public static void init() {
		Renderer.excludePipelineFromBatching(CYLINDER);
		Renderer.excludePipelineFromBatching(CIRCLE);
		IrisCompatibility.assignPipelines();
	}
}
