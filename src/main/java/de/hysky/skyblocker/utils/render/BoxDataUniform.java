package de.hysky.skyblocker.utils.render;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.util.List;

import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;

import de.hysky.skyblocker.utils.render.state.FilledBoxRenderState;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;

public class BoxDataUniform implements AutoCloseable {
	private static final boolean IS_LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
	private static final int BUFFER_USAGE = GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER;
	private static final int TEXELS_PER_INSTANCE = 2;
	private static final int BYTES_PER_BOX = (Float.BYTES * 4) * TEXELS_PER_INSTANCE;
	private MappableRingBuffer buffer = this.createBuffer(1);

	public GpuBuffer update(List<FilledBoxRenderState> states, CameraRenderState cameraRenderState) {
		this.prepareBuffer(states.size() * BYTES_PER_BOX);
		GpuBuffer texelBuffer = this.buffer.currentBuffer();

		try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(texelBuffer, false, true)) {
			long address = MemoryUtil.memAddress(mappedView.data());
			MemorySegment buffer = MemorySegment.ofAddress(address).reinterpret(this.buffer.size());

			// The memory segment is practically treated as a native float[states.size()][8]
			for (int i = 0; i < states.size(); i++) {
				long offset = i * BYTES_PER_BOX;
				FilledBoxRenderState state = states.get(i);

				int colour = toNativeRgba(ARGB.colorFromFloat(state.alpha, state.colourComponents[0], state.colourComponents[1], state.colourComponents[2]));

				// The coordinates must be offset by the camera here since double precision is required.

				// Update first Texel
				buffer.set(ValueLayout.JAVA_FLOAT, offset + 0L, (float) (state.minX - cameraRenderState.pos.x));
				buffer.set(ValueLayout.JAVA_FLOAT, offset + 4L, (float) (state.minY - cameraRenderState.pos.y));
				buffer.set(ValueLayout.JAVA_FLOAT, offset + 8L, (float) (state.minZ - cameraRenderState.pos.z));
				buffer.set(ValueLayout.JAVA_FLOAT, offset + 12L, (float) (state.maxX - cameraRenderState.pos.x));

				// Update second Texel
				buffer.set(ValueLayout.JAVA_FLOAT, offset + 16L, (float) (state.maxY - cameraRenderState.pos.y));
				buffer.set(ValueLayout.JAVA_FLOAT, offset + 20L, (float) (state.maxZ - cameraRenderState.pos.z));
				buffer.set(ValueLayout.JAVA_INT,   offset + 24L, colour);
				buffer.set(ValueLayout.JAVA_FLOAT, offset + 28L, 0f);
			}
		}

		return texelBuffer;
	}

	/// Converts the {@code colour} to the native RGBA format.
	protected static int toNativeRgba(int colour) {
		int abgr = ARGB.toABGR(colour);
		return IS_LITTLE_ENDIAN ? abgr : Integer.reverseBytes(abgr);
	}

	/// Ensures that the buffer is appropriately sized and rotates it if applicable.
	private void prepareBuffer(int requiredSize) {
		// If the buffer is smaller than the needed byte size then recreate it, otherwise rotate it
		if (this.buffer.size() < requiredSize) {
			this.buffer.close();
			this.buffer = this.createBuffer(requiredSize);
		} else {
			this.buffer.rotate();
		}
	}

	/// Allocates the buffer for the given number of boxes
	private MappableRingBuffer createBuffer(int size) {
		return new MappableRingBuffer(() -> "Box Data", BUFFER_USAGE, size);
	}

	@Override
	public void close() {
		this.buffer.close();
	}
}
