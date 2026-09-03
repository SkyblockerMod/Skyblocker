package de.hysky.skyblocker.utils.render.primitive;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import de.hysky.skyblocker.utils.render.InstancingParameters;
import de.hysky.skyblocker.utils.render.OutlinedConnectedDataUniform;
import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.OutlinedConnectedRenderState;

public class OutlinedConnectedInstancedFeatureRenderer  extends PrimitiveFeatureRenderer<OutlinedConnectedInstancedFeatureRenderer.Submit> {
	public static final FeatureRendererType<OutlinedConnectedInstancedFeatureRenderer.Submit> TYPE = FeatureRendererType.create("Skyblocker Outlined Connected Instanced");
	private static final AABB UNIT_BOX = new AABB(BlockPos.ZERO);
	private final OutlinedConnectedDataUniform outlinedBoxData = new OutlinedConnectedDataUniform();

	@Override
	protected void buildGroup(FeatureFrameContext context, List<OutlinedConnectedInstancedFeatureRenderer.Submit> submits) {
		try (Arena arena = Arena.ofConfined()) {
			for (Submit submit : submits) {
				int boxes = submit.states().size();
				MemorySegment uniformSegment = arena.allocate(this.outlinedBoxData.calculateRequiredSize(boxes));
				this.outlinedBoxData.writeToBuffer(submit.states(), submit.camera(), uniformSegment);

				GpuBufferSlice tbo = RenderSystem.getDevice().createCommandEncoder()
						.transientMemory()
						.uploadGpu(uniformSegment.asByteBuffer(), 1L, GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER);

				RenderPipeline pipeline = submit.throughWalls() ? SkyblockerRenderPipelines.OUTLINED_BOX_THROUGH_WALLS_INSTANCED : SkyblockerRenderPipelines.OUTLINED_BOX_INSTANCED;
				VertexConsumer builder = this.getVertexBuilder(pipeline, new InstancingParameters(boxes, "OutlinedBoxData", tbo));
				buildUnitBox((float) UNIT_BOX.minX, (float) UNIT_BOX.minY, (float) UNIT_BOX.minZ, (float) UNIT_BOX.maxX, (float) UNIT_BOX.maxY, (float) UNIT_BOX.maxZ, builder);
			}
		}
	}
	private static void buildUnitBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, VertexConsumer builder) {
		// TODO: Iterate over the list of block sides and add vertexes for each one to build the overall shape's outline
	}

	public record Submit(List<OutlinedConnectedRenderState> states, CameraRenderState camera, boolean throughWalls) implements SubmitNode {
		@Override
		public FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
