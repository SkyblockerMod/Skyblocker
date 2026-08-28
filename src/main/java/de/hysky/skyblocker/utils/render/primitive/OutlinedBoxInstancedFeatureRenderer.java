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
import de.hysky.skyblocker.utils.render.OutlinedBoxDataUniform;
import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.OutlinedBoxRenderState;

public class OutlinedBoxInstancedFeatureRenderer extends PrimitiveFeatureRenderer<OutlinedBoxInstancedFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("Skyblocker Outlined Box Instanced");
	private static final AABB UNIT_BOX = new AABB(BlockPos.ZERO);
	private final OutlinedBoxDataUniform outlinedBoxData = new OutlinedBoxDataUniform();

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
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
		builder.addVertex(minX, minY, minZ).setNormal(1.0f, 0.0f, 0.0f);
		builder.addVertex(maxX, minY, minZ).setNormal(1.0f, 0.0f, 0.0f);
		builder.addVertex(minX, minY, minZ).setNormal(0.0f, 1.0f, 0.0f);
		builder.addVertex(minX, maxY, minZ).setNormal(0.0f, 1.0f, 0.0f);
		builder.addVertex(minX, minY, minZ).setNormal(0.0f, 0.0f, 1.0f);
		builder.addVertex(minX, minY, maxZ).setNormal(0.0f, 0.0f, 1.0f);
		builder.addVertex(maxX, minY, minZ).setNormal(0.0f, 1.0f, 0.0f);
		builder.addVertex(maxX, maxY, minZ).setNormal(0.0f, 1.0f, 0.0f);
		builder.addVertex(maxX, maxY, minZ).setNormal(-1.0f, 0.0f, 0.0f);
		builder.addVertex(minX, maxY, minZ).setNormal(-1.0f, 0.0f, 0.0f);
		builder.addVertex(minX, maxY, minZ).setNormal(0.0f, 0.0f, 1.0f);
		builder.addVertex(minX, maxY, maxZ).setNormal(0.0f, 0.0f, 1.0f);
		builder.addVertex(minX, maxY, maxZ).setNormal(0.0f, -1.0f, 0.0f);
		builder.addVertex(minX, minY, maxZ).setNormal(0.0f, -1.0f, 0.0f);
		builder.addVertex(minX, minY, maxZ).setNormal(1.0f, 0.0f, 0.0f);
		builder.addVertex(maxX, minY, maxZ).setNormal(1.0f, 0.0f, 0.0f);
		builder.addVertex(maxX, minY, maxZ).setNormal(0.0f, 0.0f, -1.0f);
		builder.addVertex(maxX, minY, minZ).setNormal(0.0f, 0.0f, -1.0f);
		builder.addVertex(minX, maxY, maxZ).setNormal(1.0f, 0.0f, 0.0f);
		builder.addVertex(maxX, maxY, maxZ).setNormal(1.0f, 0.0f, 0.0f);
		builder.addVertex(maxX, minY, maxZ).setNormal(0.0f, 1.0f, 0.0f);
		builder.addVertex(maxX, maxY, maxZ).setNormal(0.0f, 1.0f, 0.0f);
		builder.addVertex(maxX, maxY, minZ).setNormal(0.0f, 0.0f, 1.0f);
		builder.addVertex(maxX, maxY, maxZ).setNormal(0.0f, 0.0f, 1.0f);
	}

	public record Submit(List<OutlinedBoxRenderState> states, CameraRenderState camera, boolean throughWalls) implements SubmitNode {
		@Override
		public FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
