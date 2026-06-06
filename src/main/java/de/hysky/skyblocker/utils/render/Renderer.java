package de.hysky.skyblocker.utils.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

import org.joml.Matrix4fStack;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * This class automatically handles batching, buffering, and drawing of objects within the world.
 *
 * <p>Mostly modeled off {@link net.minecraft.client.gui.render.GuiRenderer}.
 */
public class Renderer {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final StagedVertexBuffer VERTEX_BUFFER = new StagedVertexBuffer(() -> "Skyblocker Renderer Vertex Buffer", RenderType.SMALL_BUFFER_SIZE);
	private static final List<Draw> DRAWS = new ArrayList<>();
	private static @Nullable RenderPipeline previousPipeline = null;
	private static @Nullable TextureSetup previousTextureSetup = null;
	private static float previousAlphaMultiplier = 1f;
	private static int previousInstanceCount = 1;
	private static @Nullable UniformBinding previousUniform = null;
	private static StagedVertexBuffer.@Nullable Draw previousDraw = null;

	public static VertexConsumer getBuffer(RenderPipeline pipeline) {
		return getBuffer(pipeline, TextureSetup.noTexture(), 1f, 1, null);
	}

	public static VertexConsumer getBuffer(RenderPipeline pipeline, TextureSetup textureSetup) {
		return getBuffer(pipeline, textureSetup, 1f, 1, null);
	}

	public static VertexConsumer getBuffer(RenderPipeline pipeline, TextureSetup textureSetup, float alphaMultiplier) {
		return getBuffer(pipeline, textureSetup, alphaMultiplier, 1, null);
	}

	public static VertexConsumer getBuffer(RenderPipeline pipeline, TextureSetup textureSetup, float alphaMultiplier, int instanceCount, @Nullable UniformBinding uniform) {
		if (previousDraw == null || pipeline != previousPipeline || !textureSetup.equals(previousTextureSetup) || alphaMultiplier != previousAlphaMultiplier || instanceCount != previousInstanceCount || !uniform.equals(previousUniform)) {
			previousDraw = VERTEX_BUFFER.appendDraw(pipeline.getVertexFormatBinding(0), pipeline.getPrimitiveTopology());
			DRAWS.add(new Draw(previousDraw, pipeline, textureSetup, alphaMultiplier, instanceCount, uniform));
		}

		return VERTEX_BUFFER.getVertexBuilder(Objects.requireNonNull(previousDraw));
	}

	protected static void prepare() {
		previousDraw = null;
		previousPipeline = null;
		previousTextureSetup = null;
		previousAlphaMultiplier = 1f;
		previousInstanceCount = 1;
		previousUniform = null;
	}

	protected static void executeDraws() {
		ProfilerFiller profiler = Profiler.get();

		// Upload vertex buffer
		profiler.push("skyblockerRendererUpload");
		VERTEX_BUFFER.upload();

		// Dispatch draws
		profiler.popPush("skyblockerRendererDraw");
		dispatchDraws();

		// Clean up state
		profiler.popPush("skyblockerRendererEndFrame");
		VERTEX_BUFFER.endDraw();
		VERTEX_BUFFER.endFrame();
		DRAWS.clear();

		profiler.pop();
	}

	private static void dispatchDraws() {
		applyViewOffsetZLayering();

		for (Draw draw : DRAWS) {
			draw(draw);
		}

		unapplyViewOffsetZLayering();
	}

	private static void draw(Draw draw) {
		RenderTarget mainRenderTarget = CLIENT.gameRenderer.mainRenderTarget();
		StagedVertexBuffer.ExecuteInfo executeInfo = VERTEX_BUFFER.getExecuteInfo(draw.draw);

		if (executeInfo == null) {
			return;
		}

		try (RenderPass renderPass = RenderSystem.getDevice()
				.createCommandEncoder()
				.createRenderPass(
						() -> "Skyblocker Level Rendering",
						mainRenderTarget.getColorTextureView(), 
						Optional.empty(),
						mainRenderTarget.useDepth ? mainRenderTarget.getDepthTextureView() : null,
						OptionalDouble.empty()
						)) {
			renderPass.setPipeline(draw.pipeline);

			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setUniform("DynamicTransforms", setupDynamicTransforms(draw.alphaMultiplier));

			if (draw.uniform != null) {
				renderPass.setUniform(draw.uniform.name, draw.uniform.buffer);
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

			renderPass.setVertexBuffer(0, executeInfo.vertexBuffer().slice());
			renderPass.setIndexBuffer(executeInfo.indexBuffer(), executeInfo.indexType());

			renderPass.drawIndexed(executeInfo.indexCount(), draw.instanceCount, executeInfo.firstIndex(), executeInfo.baseVertex(), 0);
		}
	}

	private static GpuBufferSlice setupDynamicTransforms(float alphaMultiplier) {
		return RenderSystem.getDynamicUniforms()
				.writeTransform(RenderSystem.getModelViewMatrixCopy(), new Vector4f(1f, 1f, 1f, alphaMultiplier));
	}

	private static void applyViewOffsetZLayering() {
		Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		RenderSystem.getProjectionType().applyLayeringTransform(modelViewStack, 1f);
	}

	private static void unapplyViewOffsetZLayering() {
		RenderSystem.getModelViewStack().popMatrix();
	}

	public static void close() {
		VERTEX_BUFFER.close();
	}

	private record Draw(StagedVertexBuffer.Draw draw, RenderPipeline pipeline, TextureSetup textureSetup, float alphaMultiplier, int instanceCount, @Nullable UniformBinding uniform) {}

	public record UniformBinding(String name, GpuBuffer buffer) {}
}
