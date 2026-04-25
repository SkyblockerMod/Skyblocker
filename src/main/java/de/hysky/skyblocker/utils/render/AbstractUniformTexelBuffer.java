package de.hysky.skyblocker.utils.render;

import java.lang.foreign.MemorySegment;
import java.nio.ByteOrder;
import java.util.List;

import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;

public abstract class AbstractUniformTexelBuffer<T> implements AutoCloseable {
	protected static final boolean IS_LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
	private final int instanceBytes;
	private final @GpuBuffer.Usage int usage = GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER;
	private MappableRingBuffer buffer = this.createBuffer(1);

	/// @param instanceBytes the amount of bytes used for each instance of an object
	protected AbstractUniformTexelBuffer(int instanceBytes) {
		this.instanceBytes = instanceBytes;
	}

	public final GpuBuffer update(List<T> states, CameraRenderState cameraRenderState) {
		this.prepareBuffer(states.size() * this.instanceBytes);
		GpuBuffer texelBuffer = this.buffer.currentBuffer();

		try (GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(texelBuffer, false, true)) {
			long address = MemoryUtil.memAddress(mappedView.data());
			MemorySegment buffer = MemorySegment.ofAddress(address).reinterpret(this.buffer.size());

			this.updateBuffer(states, cameraRenderState, buffer);
		}

		return texelBuffer;
	}

	protected abstract void updateBuffer(List<T> states, CameraRenderState cameraRenderState, MemorySegment buffer);

	/// Converts the {@code colour} to the native RGBA format.
	protected static final int toNativeRgba(int colour) {
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
		return new MappableRingBuffer(() -> "Box Data", this.usage, size);
	}

	@Override
	public final void close() {
		this.buffer.close();
	}
}
