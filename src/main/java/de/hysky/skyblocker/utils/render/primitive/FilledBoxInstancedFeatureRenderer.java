package de.hysky.skyblocker.utils.render.primitive;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import de.hysky.skyblocker.utils.render.BoxDataUniform;
import de.hysky.skyblocker.utils.render.InstancingParameters;
import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import de.hysky.skyblocker.utils.render.state.FilledBoxRenderState;

public class FilledBoxInstancedFeatureRenderer extends PrimitiveFeatureRenderer<FilledBoxInstancedFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("Skyblocker Filled Box Instanced");
	private static final AABB UNIT_BOX = new AABB(BlockPos.ZERO);
	private final BoxDataUniform boxData = new BoxDataUniform();

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		try (Arena arena = Arena.ofConfined()) {
			for (Submit submit : submits) {
				int boxes = submit.states().size();
				MemorySegment uniformSegment = arena.allocate(this.boxData.calculateRequiredSize(boxes));
				this.boxData.writeToBuffer(submit.states(), submit.camera(), uniformSegment);

				GpuDevice device = RenderSystem.getDevice();
				GpuBufferSlice tbo = device.createCommandEncoder()
						.transientMemory()
						.uploadGpu(uniformSegment.asByteBuffer(), device.getDeviceInfo().limits().minUniformOffsetAlignment(), GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER);

				RenderPipeline pipeline = submit.throughWalls() ? SkyblockerRenderPipelines.FILLED_THROUGH_WALLS_INSTANCED : SkyblockerRenderPipelines.FILLED_INSTANCED;
				VertexConsumer builder = this.getVertexBuilder(pipeline, new InstancingParameters(boxes, "BoxData", tbo));
				buildUnitBox((float) UNIT_BOX.minX, (float) UNIT_BOX.minY, (float) UNIT_BOX.minZ, (float) UNIT_BOX.maxX, (float) UNIT_BOX.maxY, (float) UNIT_BOX.maxZ, builder);
			}
		}
	}

	private static void buildUnitBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, VertexConsumer builder) {
		// Front face
		builder.addVertex(minX, minY, minZ);
		builder.addVertex(maxX, minY, minZ);
		builder.addVertex(maxX, maxY, minZ);
		builder.addVertex(minX, maxY, minZ);

		// Back face
		builder.addVertex(maxX, minY, maxZ);
		builder.addVertex(minX, minY, maxZ);
		builder.addVertex(minX, maxY, maxZ);
		builder.addVertex(maxX, maxY, maxZ);

		// Left face
		builder.addVertex(minX, minY, maxZ);
		builder.addVertex(minX, minY, minZ);
		builder.addVertex(minX, maxY, minZ);
		builder.addVertex(minX, maxY, maxZ);

		// Right face
		builder.addVertex(maxX, minY, minZ);
		builder.addVertex(maxX, minY, maxZ);
		builder.addVertex(maxX, maxY, maxZ);
		builder.addVertex(maxX, maxY, minZ);

		// Top face
		builder.addVertex(minX, maxY, minZ);
		builder.addVertex(maxX, maxY, minZ);
		builder.addVertex(maxX, maxY, maxZ);
		builder.addVertex(minX, maxY, maxZ);

		// Bottom face
		builder.addVertex(minX, minY, maxZ);
		builder.addVertex(maxX, minY, maxZ);
		builder.addVertex(maxX, minY, minZ);
		builder.addVertex(minX, minY, minZ);
	}

	public record Submit(List<FilledBoxRenderState> states, CameraRenderState camera, boolean throughWalls) implements SubmitNode {
		@Override
		public FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
