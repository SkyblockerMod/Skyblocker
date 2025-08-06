package de.hysky.skyblocker.utils.render;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderSystem.ShapeIndexBuffer;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import com.mojang.blaze3d.vertex.VertexFormat.IndexType;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.MappableRingBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.BuiltBuffer.DrawParameters;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.BufferAllocator;

/**
 * This class automatically handles batching, buffering, and drawing of objects within the world.
 *
 * Mostly modelled off the {@link net.minecraft.client.gui.render.GuiRenderer}.
 */
public class Renderer {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final List<RenderPipeline> EXCLUDED_FROM_BATCHING = new ArrayList<>();
	private static final BufferAllocator GENERAL_ALLOCATOR = new BufferAllocator(RenderLayer.CUTOUT_BUFFER_SIZE);
	private static final float DEFAULT_LINE_WIDTH = 0f;
	private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
	private static final Int2ObjectMap<BufferAllocator> ALLOCATORS = new Int2ObjectArrayMap<>(5);
	private static final Int2ObjectMap<BatchedDraw> BATCHED_DRAWS = new Int2ObjectArrayMap<>(5);
	private static final Map<VertexFormat, MappableRingBuffer> VERTEX_BUFFERS = new Object2ObjectOpenHashMap<>();
	private static final List<PreparedDraw> PREPARED_DRAWS = new ArrayList<>();
	private static final List<Draw> DRAWS = new ArrayList<>();
	private static BatchedDraw lastUnbatchedDraw = null;

	protected static BufferBuilder getBuffer(RenderPipeline pipeline) {
		return getBuffer(pipeline, null, DEFAULT_LINE_WIDTH);
	}

	protected static BufferBuilder getBuffer(RenderPipeline pipeline, GpuTextureView textureView) {
		return getBuffer(pipeline, Objects.requireNonNull(textureView, "textureView must not be null"), DEFAULT_LINE_WIDTH);
	}

	protected static BufferBuilder getBuffer(RenderPipeline pipeline, float lineWidth) {
		return getBuffer(pipeline, null, lineWidth);
	}

	/**
	 * Returns the appropriate {@code BufferBuilder} that should be used with the given pipeline, texture view, and line width.
	 */
	private static BufferBuilder getBuffer(RenderPipeline pipeline, @Nullable GpuTextureView textureView, float lineWidth) {
		if (!EXCLUDED_FROM_BATCHING.contains(pipeline)) {
			return setupBatched(pipeline, textureView, lineWidth);
		} else {
			return setupUnbatched(pipeline, textureView, lineWidth);
		}
	}

	private static BufferBuilder setupBatched(RenderPipeline pipeline, @Nullable GpuTextureView textureView, float lineWidth) {
		int hash = hash(pipeline, textureView, lineWidth);
		BatchedDraw draw = BATCHED_DRAWS.get(hash);

		if (draw == null) {
			BufferAllocator allocator = ALLOCATORS.computeIfAbsent(hash, _hash -> new BufferAllocator(RenderLayer.CUTOUT_BUFFER_SIZE));
			BufferBuilder bufferBuilder = new BufferBuilder(allocator, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
			BATCHED_DRAWS.put(hash, new BatchedDraw(bufferBuilder, pipeline, textureView, lineWidth));

			return bufferBuilder;
		} else {
			return draw.bufferBuilder();
		}
	}

	private static BufferBuilder setupUnbatched(RenderPipeline pipeline, @Nullable GpuTextureView textureView, float lineWidth) {
		if (lastUnbatchedDraw != null) {
			prepareBatchedDraw(lastUnbatchedDraw);
		}

		BufferBuilder bufferBuilder = new BufferBuilder(GENERAL_ALLOCATOR, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
		lastUnbatchedDraw = new BatchedDraw(bufferBuilder, pipeline, textureView, lineWidth);

		return bufferBuilder;
	}

	/**
	 * Calculates the hash of the given inputs which serves as the keys to our maps where we store stuff for the batched draws.
	 * This is much faster than using an object-based key as we do not need to create any objects to find the instances we want.
	 */
	private static int hash(RenderPipeline pipeline, @Nullable GpuTextureView textureView, float lineWidth) {
		//This manually calculates the hash, avoiding Objects#hash to not incur the array allocation each time
		int hash = 1;
		hash = 31 * hash + pipeline.hashCode();
		hash = 31 * hash + Objects.hashCode(textureView);
		hash = 31 * hash + Float.hashCode(lineWidth);

		return hash;
	}

	/**
	 * Allows for excluding the {@code pipeline} from the batching system. This may be needed when working with triangle fans
	 * or contiguous triangle strips.
	 */
	protected static void excludePipelineFromBatching(RenderPipeline pipeline) {
		EXCLUDED_FROM_BATCHING.add(pipeline);
	}

	private static void endBatches() {
		for (Int2ObjectMap.Entry<BatchedDraw> entry : Int2ObjectMaps.fastIterable(BATCHED_DRAWS)) {
			prepareBatchedDraw(entry.getValue());
		}

		if (lastUnbatchedDraw != null) {
			prepareBatchedDraw(lastUnbatchedDraw);
			lastUnbatchedDraw = null;
		}
	}

	private static void prepareBatchedDraw(BatchedDraw draw) {
		PREPARED_DRAWS.add(new PreparedDraw(draw.bufferBuilder().end(), draw.pipeline(), draw.textureView(), draw.lineWidth()));
	}

	protected static void executeDraws() {
		//End all of the batches and prepare the draws
		endBatches();

		//Setup the draws
		setupDraws();

		//Execute the draws
		for (Draw draw : DRAWS) {
			draw(draw);
		}

		//Rotate the buffers - ensures that we're likely to be using buffers that the GPU isn't (prevents synchronization/stalls)
		for (MappableRingBuffer buffer : VERTEX_BUFFERS.values()) {
			buffer.rotate();
		}

		//Clear the draws from this frame
		BATCHED_DRAWS.clear();
		PREPARED_DRAWS.clear();
		DRAWS.clear();
	}

	private static void setupDraws() {
		setupVertexBuffers();
		Object2IntMap<VertexFormat> vertexBufferPositions = new Object2IntOpenHashMap<>();

		for (PreparedDraw prepared : PREPARED_DRAWS) {
			BuiltBuffer builtBuffer = prepared.builtBuffer();
			DrawParameters drawParameters = builtBuffer.getDrawParameters();
			VertexFormat format = drawParameters.format();

			MappableRingBuffer vertices = VERTEX_BUFFERS.get(format);
			ByteBuffer vertexData = builtBuffer.getBuffer();
			int vertexBufferPosition = vertexBufferPositions.getInt(format);
			int remainingVertexBytes = vertexData.remaining();

			//Copy vertex data into the shared vertex buffer
			copyDataInto(vertices, vertexData, vertexBufferPosition, remainingVertexBytes);
			//Update vertex buffer position
			vertexBufferPositions.put(format, vertexBufferPosition + remainingVertexBytes);

			DRAWS.add(new Draw(
					builtBuffer,
					vertices.getBlocking(),
					vertexBufferPosition / format.getVertexSize(),
					drawParameters.indexCount(),
					prepared.pipeline(),
					prepared.textureView(),
					prepared.lineWidth())
					);
		}
	}

	/**
	 * Maps the {@code target} buffer and copies the {@code source} data into it.
	 */
	private static void copyDataInto(MappableRingBuffer target, ByteBuffer source, int position, int remainingBytes) {
		CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

		try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(target.getBlocking().slice(position, remainingBytes), false, true)) {
			MemoryUtil.memCopy(source, mappedView.data());
		}
	}

	/**
	 * Resizes/allocates the necessary vertex buffers.
	 */
	private static void setupVertexBuffers() {
		Object2IntMap<VertexFormat> vertexBufferSizes = collectVertexBufferSizes();

		for (Object2IntMap.Entry<VertexFormat> entry : Object2IntMaps.fastIterable(vertexBufferSizes)) {
			VertexFormat format = entry.getKey();
			int vertexBufferSize = entry.getIntValue();
			MappableRingBuffer vertexBuffer = VERTEX_BUFFERS.get(format);

			VERTEX_BUFFERS.put(format, initOrResizeBuffer(vertexBuffer, "Skyblocker vertex buffer for: " + format, vertexBufferSize, GpuBuffer.USAGE_VERTEX));
		}
	}

	private static MappableRingBuffer initOrResizeBuffer(MappableRingBuffer buffer, String name, int neededSize, int usageType) {
		if (buffer == null || buffer.size() < neededSize) {
			if (buffer != null) {
				buffer.close();
			}

			return new MappableRingBuffer(() -> name, GpuBuffer.USAGE_MAP_WRITE | usageType, neededSize);
		}

		return buffer;
	}

	/**
	 * Collect the required buffer size for each vertex format in use.
	 */
	private static Object2IntMap<VertexFormat> collectVertexBufferSizes() {
		//If we ever need to create our own shared index buffers then we can turn this into an Object2LongMap and pack
		//both the vertex & index buffer sizes into a single long (since they're two ints)
		Object2IntMap<VertexFormat> vertexSizes = new Object2IntOpenHashMap<>();

		for (PreparedDraw prepared : PREPARED_DRAWS) {
			DrawParameters drawParameters = prepared.builtBuffer().getDrawParameters();
			VertexFormat format = drawParameters.format();

			vertexSizes.put(format, vertexSizes.getOrDefault(format, 0) + drawParameters.vertexCount() * format.getVertexSize());
		}

		return vertexSizes;
	}

	private static void draw(Draw draw) {
		GpuBuffer indices;
		IndexType indexType;

		if (draw.pipeline().getVertexFormatMode() == DrawMode.QUADS) {
			//The quads we're rendering are translucent so they need to be sorted for our index buffer
			draw.builtBuffer().sortQuads(GENERAL_ALLOCATOR, RenderSystem.getProjectionType().getVertexSorter());
			indices = draw.pipeline().getVertexFormat().uploadImmediateIndexBuffer(draw.builtBuffer().getSortedBuffer());
			indexType = draw.builtBuffer().getDrawParameters().indexType();
		} else {
			//Use general shape index buffer for other draw modes
			ShapeIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(draw.pipeline().getVertexFormatMode());
			indices = shapeIndexBuffer.getIndexBuffer(draw.indexCount());
			indexType = shapeIndexBuffer.getIndexType();
		}

		draw(draw, indices, indexType);
	}

	private static void draw(Draw draw, GpuBuffer indices, IndexType indexType) {
		applyViewOffsetZLayering();
		GpuBufferSlice dynamicTransforms = setupDynamicTransforms(draw.lineWidth);

		try (RenderPass renderPass = RenderSystem.getDevice()
				.createCommandEncoder()
				.createRenderPass(() -> "skyblocker world rendering", getMainColorTexture(), OptionalInt.empty(), getMainDepthTexture(), OptionalDouble.empty())) {
			renderPass.setPipeline(draw.pipeline);

			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setUniform("DynamicTransforms", dynamicTransforms);

			//Bind texture if applicable
			if (draw.textureView != null) {
				//Sampler0 is used for texture inputs in vertices
				renderPass.bindSampler("Sampler0", draw.textureView);
			}

			renderPass.setVertexBuffer(0, draw.vertices);
			renderPass.setIndexBuffer(indices, indexType);

			renderPass.drawIndexed(draw.baseVertex, 0, draw.indexCount, 1);
		}

		draw.builtBuffer().close();
		unapplyViewOffsetZLayering();
	}

	private static GpuBufferSlice setupDynamicTransforms(float lineWidth) {
		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
				.write(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, RenderSystem.getModelOffset(), RenderSystem.getTextureMatrix(), lineWidth);

		return dynamicTransforms;
	}

	private static GpuTextureView getMainColorTexture() {
		return CLIENT.getFramebuffer().getColorAttachmentView();
	}

	private static GpuTextureView getMainDepthTexture() {
		return CLIENT.getFramebuffer().getDepthAttachmentView();
	}

	private static void applyViewOffsetZLayering() {
		Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		RenderSystem.getProjectionType().apply(modelViewStack, 1f);
	}

	private static void unapplyViewOffsetZLayering() {
		RenderSystem.getModelViewStack().popMatrix();
	}

	public static void close() {
		GENERAL_ALLOCATOR.close();

		for (BufferAllocator allocator : ALLOCATORS.values()) {
			allocator.close();
		}

		for (MappableRingBuffer vertexBuffer : VERTEX_BUFFERS.values()) {
			vertexBuffer.close();
		}
	}

	private record Draw(BuiltBuffer builtBuffer, GpuBuffer vertices, int baseVertex, int indexCount, RenderPipeline pipeline, @Nullable GpuTextureView textureView, float lineWidth) {}

	private record PreparedDraw(BuiltBuffer builtBuffer, RenderPipeline pipeline, @Nullable GpuTextureView textureView, float lineWidth) {}

	private record BatchedDraw(BufferBuilder bufferBuilder, RenderPipeline pipeline, @Nullable GpuTextureView textureView, float lineWidth) {}
}
