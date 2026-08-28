package de.hysky.skyblocker.utils.render.primitive;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4fStack;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;

import de.hysky.skyblocker.utils.render.InstancingParameters;

public abstract class PrimitiveFeatureRenderer<Submit extends SubmitNode> implements FeatureRenderer<Submit> {
	private PrimitiveFeatureRenderer.@Nullable Group currentGroup;
	private final List<PrimitiveFeatureRenderer.Group> groups = new ArrayList<>();

	protected abstract void buildGroup(FeatureFrameContext context, List<Submit> submits);

	protected final VertexConsumer getVertexBuilder(RenderPipeline pipeline) {
		return this.currentGroup().getVertexBuilder(pipeline, TextureSetup.noTexture(), InstancingParameters.NONE);
	}

	protected final VertexConsumer getVertexBuilder(RenderPipeline pipeline, TextureSetup textureSetup) {
		return this.currentGroup().getVertexBuilder(pipeline, textureSetup, InstancingParameters.NONE);
	}

	protected final VertexConsumer getVertexBuilder(RenderPipeline pipeline, InstancingParameters instancing) {
		return this.currentGroup().getVertexBuilder(pipeline, TextureSetup.noTexture(), instancing);
	}

	private PrimitiveFeatureRenderer.Group currentGroup() {
		return Objects.requireNonNull(this.currentGroup, "Not preparing group");
	}

	@Override
	public final void prepareGroup(FeatureFrameContext context, List<Submit> submits, boolean strictlyOrdered) {
		this.currentGroup = new PrimitiveFeatureRenderer.Group(context.stagedVertexBuffer(), !strictlyOrdered);
		this.buildGroup(context, submits);
		this.groups.add(this.currentGroup);
		this.currentGroup = null;
	}

	@Override
	public final void executeGroup(FeatureFrameContext context, int groupIndex, List<Submit> submits, boolean strictlyOrdered) {
		PrimitiveFeatureRenderer.Group group = this.groups.get(groupIndex);

		applyViewOffsetZLayering();

		RenderTarget mainTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy());

		try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
				() -> "Skyblocker Primitive Feature Renderer",
				mainTarget.getColorTextureView(),
				Optional.empty(),
				mainTarget.useDepth ? mainTarget.getDepthTextureView() : null,
						OptionalDouble.empty()
				)) {
			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setUniform("DynamicTransforms", dynamicTransforms);

			for (int i = 0; i < group.draws.size(); i++) {
				PreparedPrimitiveDraw primitiveDraw = group.drawPrimitives.get(i);
				StagedVertexBuffer.ExecuteInfo info = context.stagedVertexBuffer().getExecuteInfo(group.draws.get(i));

				if (info != null) {
					executeDraw(renderPass, primitiveDraw, info);
				}
			}
		}

		unapplyViewOffsetZLayering();
	}

	private static void executeDraw(RenderPass renderPass, PreparedPrimitiveDraw draw, StagedVertexBuffer.ExecuteInfo info) {
		renderPass.setPipeline(draw.pipeline());

		InstancingParameters instancing = draw.instancing();

		if (instancing.name() != null && instancing.buffer() != null) {
			renderPass.setUniform(instancing.name(), instancing.buffer());
		}

		if (draw.textureSetup.texure0() != null) {
			// Sampler0 is used for normal texture inputs in shaders
			renderPass.bindTexture("Sampler0", draw.textureSetup.texure0(), draw.textureSetup.sampler0());
		}

		if (draw.textureSetup.texure1() != null) {
			// Sampler1 is used for alternate texture inputs in shaders
			renderPass.bindTexture("Sampler1", draw.textureSetup.texure1(), draw.textureSetup.sampler1());
		}

		if (draw.textureSetup.texure2() != null) {
			// Sampler2 is used for lightmap texture inputs in shaders
			renderPass.bindTexture("Sampler2", draw.textureSetup.texure2(), draw.textureSetup.sampler2());
		}

		renderPass.setVertexBuffer(0, info.vertexBuffer().slice());
		renderPass.setIndexBuffer(info.indexBuffer(), info.indexType());

		renderPass.drawIndexed(info.indexCount(), draw.instancing().count(), info.firstIndex(), info.baseVertex(), 0);
	}

	private static void applyViewOffsetZLayering() {
		Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		RenderSystem.getProjectionType().applyLayeringTransform(modelViewStack, 1f);
	}

	private static void unapplyViewOffsetZLayering() {
		RenderSystem.getModelViewStack().popMatrix();
	}

	@Override
	public final void finishExecute(FeatureFrameContext context) {
		this.groups.clear();
	}

	private static class Group {
		private final StagedVertexBuffer stagedBuffer;
		private final boolean canReorder;
		private final List<StagedVertexBuffer.Draw> draws = new ArrayList<>();
		private final List<PreparedPrimitiveDraw> drawPrimitives = new ArrayList<>();
		private @Nullable RenderPipeline lastPipeline;
		private @Nullable TextureSetup lastTextureSetup;
		private @Nullable InstancingParameters lastInstancing;
		private StagedVertexBuffer.@Nullable Draw lastDraw;

		private Group(StagedVertexBuffer stagedBuffer, boolean canReorder) {
			this.stagedBuffer = stagedBuffer;
			this.canReorder = canReorder;
		}

		public VertexConsumer getVertexBuilder(RenderPipeline pipeline, TextureSetup textureSetup, InstancingParameters instancing) {
			if (this.lastDraw == null || pipeline != this.lastPipeline || textureSetup != this.lastTextureSetup || this.lastInstancing != instancing || !canConsolidateConsecutiveGeometry(pipeline)) {
				this.lastDraw = this.getOrAddDraw(pipeline, textureSetup, instancing);
				this.lastPipeline = pipeline;
				this.lastTextureSetup = textureSetup;
				this.lastInstancing = instancing;
			}

			return this.stagedBuffer.getVertexBuilder(this.lastDraw);
		}

		private StagedVertexBuffer.Draw getOrAddDraw(RenderPipeline pipeline, TextureSetup textureSetup, InstancingParameters instancing) {
			PreparedPrimitiveDraw preparedPrimitiveDraw = new PreparedPrimitiveDraw(pipeline, textureSetup, instancing);
			int existingIndex = this.canReorder && canConsolidateConsecutiveGeometry(pipeline) ? this.drawPrimitives.indexOf(preparedPrimitiveDraw) : -1;

			if (existingIndex != -1) {
				return (StagedVertexBuffer.Draw) this.draws.get(existingIndex);
			}

			StagedVertexBuffer.Draw draw = this.stagedBuffer.appendDraw(pipeline.getVertexFormatBinding(0), pipeline.getPrimitiveTopology());
			this.draws.add(draw);
			this.drawPrimitives.add(preparedPrimitiveDraw);

			return draw;
		}

		private static boolean canConsolidateConsecutiveGeometry(RenderPipeline pipeline) {
			return !pipeline.getPrimitiveTopology().connectedPrimitives;
		}
	}

	private record PreparedPrimitiveDraw(RenderPipeline pipeline, TextureSetup textureSetup, InstancingParameters instancing) {}
}
