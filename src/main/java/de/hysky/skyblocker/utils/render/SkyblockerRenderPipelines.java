package de.hysky.skyblocker.utils.render;

import java.util.Optional;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;

import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.oit.OitPipelineSet;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.compatibility.IrisCompatibility;

public class SkyblockerRenderPipelines {
	// Snippets
	private static final RenderPipeline.Snippet FILLED_BOX_SNIPPET = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withVertexShader(SkyblockerMod.id("core/filled_box"))
			.withBindGroupLayout(SkyblockerBindGroupLayouts.BOX_DATA)
			.withVertexBinding(0, DefaultVertexFormat.POSITION)
			.withCull(false)
			.buildSnippet();
	private static final RenderPipeline.Snippet OIT_FILLED_BOX_SNIPPET = RenderPipeline.builder(RenderPipelines.OIT_DEBUG_FILLED_SNIPPET)
			.withVertexShader(SkyblockerMod.id("core/filled_box"))
			.withBindGroupLayout(SkyblockerBindGroupLayouts.BOX_DATA)
			.withVertexBinding(0, DefaultVertexFormat.POSITION)
			.withCull(false)
			.buildSnippet();
	private static final RenderPipeline.Snippet OUTLINED_BOX_SNIPPET = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
			.withVertexShader(SkyblockerMod.id("core/outlined_box"))
			.withBindGroupLayout(SkyblockerBindGroupLayouts.OUTLINED_BOX_DATA)
			.withVertexBinding(0, SkyblockerVertexFormats.POSITION_NORMAL)
			.withColorTargetState(ColorTargetState.DEFAULT)
			.buildSnippet();
	private static final RenderPipeline.Snippet OIT_OUTLINED_BOX_SNIPPET = RenderPipeline.builder(RenderPipelines.OIT_LINES_SNIPPET)
			.withVertexShader(SkyblockerMod.id("core/outlined_box"))
			.withBindGroupLayout(SkyblockerBindGroupLayouts.OUTLINED_BOX_DATA)
			.withVertexBinding(0, SkyblockerVertexFormats.POSITION_NORMAL)
			.buildSnippet();
	/// From {@link RenderPipelines#GUI_TEXTURED_SNIPPET}
	private static final RenderPipeline.Snippet TEXTURE_SNIPPET = RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
			.withBindGroupLayout(BindGroupLayouts.PROJECTION)
			.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
			.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
			.withVertexShader("core/position_tex_color")
			.withFragmentShader("core/position_tex_color")
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withCull(false)
			.buildSnippet();
	private static final RenderPipeline.Snippet OIT_TEXTURE_SNIPPET = RenderPipeline.builder()
			.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
			.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
			.withVertexShader("core/position_tex_color")
			.withFragmentShader("core/position_tex_color")
			.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withCull(false)
			.buildSnippet();

	// Instancing
	public static final RenderPipeline FILLED_INSTANCED = RenderPipelines.register(RenderPipeline.builder(FILLED_BOX_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/debug_filled_box_instanced"))
			.build());
	public static final RenderPipeline FILLED_THROUGH_WALLS_INSTANCED = RenderPipelines.register(RenderPipeline.builder(FILLED_BOX_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/debug_filled_box_through_walls_instanced"))
			.withDepthStencilState(Optional.empty())
			.build());
	public static final OitPipelineSet OIT_FILLED_INSTANCED = RenderPipelines.register(OitPipelineSet.builder("debug_filled_box_instanced", RenderPipeline.builder(OIT_FILLED_BOX_SNIPPET))
			.build());
	/** Similar to {@link RenderPipelines#DEBUG_FILLED_BOX} */
	public static final RenderPipeline FILLED_THROUGH_WALLS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/debug_filled_box_through_walls"))
			.withDepthStencilState(Optional.empty())
			.build());
	public static final RenderPipeline OUTLINED_BOX_INSTANCED = RenderPipelines.register(RenderPipeline.builder(OUTLINED_BOX_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/outlined_box_instanced"))
			.build());
	public static final RenderPipeline OUTLINED_BOX_THROUGH_WALLS_INSTANCED = RenderPipelines.register(RenderPipeline.builder(OUTLINED_BOX_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/outlined_box_through_walls_instanced"))
			.withDepthStencilState(Optional.empty())
			.build());
	public static final OitPipelineSet OIT_OUTLINED_BOX_INSTANCED = RenderPipelines.register(OitPipelineSet.builder("outlined_box_instanced", RenderPipeline.builder(OIT_OUTLINED_BOX_SNIPPET))
			.build());

	// Normal
	/** Similar to {@link RenderPipelines#LINES} */
	public static final RenderPipeline LINES_THROUGH_WALLS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/lines_through_walls"))
			.withColorTargetState(ColorTargetState.DEFAULT)
			.withDepthStencilState(Optional.empty())
			.build());
	/** Similar to {@link RenderPipelines#DEBUG_QUADS}  */
	public static final RenderPipeline QUADS_THROUGH_WALLS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/debug_quads_through_walls"))
			.withDepthStencilState(Optional.empty())
			.withCull(false)
			.build());
	/** Similar to {@link RenderPipelines#GUI_TEXTURED} */
	public static final RenderPipeline TEXTURE = RenderPipelines.register(RenderPipeline.builder(TEXTURE_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/texture"))
			.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
			.build());
	public static final RenderPipeline TEXTURE_THROUGH_WALLS = RenderPipelines.register(RenderPipeline.builder(TEXTURE_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/texture_through_walls"))
			.withDepthStencilState(Optional.empty())
			.build());
	public static final OitPipelineSet OIT_TEXTURE = RenderPipelines.register(OitPipelineSet.builder("texture", RenderPipeline.builder(OIT_TEXTURE_SNIPPET))
			.build());
	public static final RenderPipeline CYLINDER = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/cylinder"))
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP)
			.withCull(false)
			.build());
	public static final OitPipelineSet OIT_CYLINDER = RenderPipelines.register(OitPipelineSet.builder("cylinder", RenderPipeline.builder(RenderPipelines.OIT_DEBUG_FILLED_SNIPPET)
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP)
			.withCull(false)
			).build());
	public static final RenderPipeline CIRCLE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/circle"))
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLE_FAN)
			.withCull(false)
			.build());
	public static final OitPipelineSet OIT_CIRCLE = RenderPipelines.register(OitPipelineSet.builder("circle", RenderPipeline.builder(RenderPipelines.OIT_DEBUG_FILLED_SNIPPET)
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLE_FAN)
			.withCull(false)
			).build());
	public static final RenderPipeline CIRCLE_LINES = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/circle_lines"))
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withCull(false)
			.build());
	public static final OitPipelineSet OIT_CIRCLE_LINES = RenderPipelines.register(OitPipelineSet.builder("circle_lines", RenderPipeline.builder(RenderPipelines.OIT_DEBUG_FILLED_SNIPPET)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withCull(false)
			).build());
	public static final RenderPipeline BLURRED_RECTANGLE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/blurred_rectangle"))
			.withVertexShader("core/position_color")
			.withFragmentShader(SkyblockerMod.id("core/box_blur"))
			.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
			.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.build());

	/**
	 * Ensures that pipelines are pre-compiled instead of compiled on demand. Also used for excluding some pipelines from batching.
	 */
	@Init
	public static void init() {
		IrisCompatibility.assignPipelines();
	}
}
