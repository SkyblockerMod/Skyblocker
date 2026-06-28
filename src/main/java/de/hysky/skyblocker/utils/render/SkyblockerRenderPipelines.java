package de.hysky.skyblocker.utils.render;

import java.util.Optional;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.compatibility.IrisCompatibility;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;

public class SkyblockerRenderPipelines {
	public static final RenderPipeline FILLED_INSTANCED = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/debug_filled_box_instanced"))
			.withVertexShader(SkyblockerMod.id("core/filled_box"))
			.withBindGroupLayout(SkyblockerBindGroupLayouts.BOX_DATA)
			.withVertexBinding(0, DefaultVertexFormat.POSITION)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withCull(false)
			.build());
	public static final RenderPipeline FILLED_THROUGH_WALLS_INSTANCED = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/debug_filled_box_through_walls_instanced"))
			.withVertexShader(SkyblockerMod.id("core/filled_box"))
			.withBindGroupLayout(SkyblockerBindGroupLayouts.BOX_DATA)
			.withVertexBinding(0, DefaultVertexFormat.POSITION)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withDepthStencilState(Optional.empty())
			.build());
	/** Similar to {@link RenderPipelines#DEBUG_FILLED_BOX} */
	public static final RenderPipeline FILLED_THROUGH_WALLS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/debug_filled_box_through_walls"))
			.withDepthStencilState(Optional.empty())
			.build());
	public static final RenderPipeline OUTLINED_BOX_INSTANCED = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/outlined_box_instanced"))
			.withVertexShader(SkyblockerMod.id("core/outlined_box"))
			.withBindGroupLayout(SkyblockerBindGroupLayouts.OUTLINED_BOX_DATA)
			.withVertexBinding(0, SkyblockerVertexFormats.POSITION_NORMAL)
			.withPrimitiveTopology(PrimitiveTopology.LINES)
			.build());
	public static final RenderPipeline OUTLINED_BOX_THROUGH_WALLS_INSTANCED = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/outlined_box_through_walls_instanced"))
			.withVertexShader(SkyblockerMod.id("core/outlined_box"))
			.withBindGroupLayout(SkyblockerBindGroupLayouts.OUTLINED_BOX_DATA)
			.withVertexBinding(0, SkyblockerVertexFormats.POSITION_NORMAL)
			.withPrimitiveTopology(PrimitiveTopology.LINES)
			.withDepthStencilState(Optional.empty())
			.build());
	/** Similar to {@link RenderPipelines#LINES} */
	public static final RenderPipeline LINES_THROUGH_WALLS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/lines_through_walls"))
			.withDepthStencilState(Optional.empty())
			.build());
	/** Similar to {@link RenderPipelines#DEBUG_QUADS}  */
	public static final RenderPipeline QUADS_THROUGH_WALLS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/debug_quads_through_walls"))
			.withDepthStencilState(Optional.empty())
			.withCull(false)
			.build());
	/** Similar to {@link RenderPipelines#GUI_TEXTURED} */
	public static final RenderPipeline TEXTURE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/texture"))
			.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
			.withCull(false)
			.build());
	public static final RenderPipeline TEXTURE_THROUGH_WALLS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/texture_through_walls"))
			.withDepthStencilState(Optional.empty())
			.withCull(false)
			.build());
	public static final RenderPipeline CYLINDER = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/cylinder"))
			.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP)
			.withCull(false)
			.build());
	public static final RenderPipeline CIRCLE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/circle"))
			.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLE_FAN)
			.withCull(false)
			.build());
	public static final RenderPipeline CIRCLE_LINES = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/circle_lines"))
			.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withCull(false)
			.build());
	public static final RenderPipeline BLURRED_RECTANGLE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
			.withLocation(SkyblockerMod.id("pipeline/blurred_rectangle"))
			.withVertexShader("core/position_color")
			.withFragmentShader(SkyblockerMod.id("core/box_blur"))
			.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
			.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.build());
	public static final RenderPipeline OUTLINE_DEPTH_CULL = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.OUTLINE_SNIPPET)
			.withLocation(SkyblockerMod.id("outline_depth_cull"))
			.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true))
			.build());
	public static final RenderPipeline OUTLINE_DEPTH_NO_CULL = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.OUTLINE_SNIPPET)
			.withLocation(SkyblockerMod.id("outline_depth_no_cull"))
			.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true))
			.withCull(false)
			.build());

	/**
	 * Ensures that pipelines are pre-compiled instead of compiled on demand. Also used for excluding some pipelines from batching.
	 */
	@Init
	public static void init() {
		IrisCompatibility.assignPipelines();
	}
}
