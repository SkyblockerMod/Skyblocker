package de.hysky.skyblocker.utils.render;

import java.lang.foreign.MemorySegment;
import java.nio.ByteOrder;
import java.util.List;

import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;

public abstract class AbstractUniformTexelBuffer<T> {
	private static final boolean IS_LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
	private final int instanceBytes;

	/// @param instanceBytes the amount of bytes used for each instance of an object
	protected AbstractUniformTexelBuffer(int instanceBytes) {
		this.instanceBytes = instanceBytes;
	}

	public final int calculateRequiredSize(int count) {
		return count * this.instanceBytes;
	}

	public final void writeToBuffer(List<T> states, CameraRenderState camera, MemorySegment buffer) {
		for (int i = 0; i < states.size(); i++) {
			this.writeToBuffer(i, states.get(i), camera, buffer);
		}
	}

	protected abstract void writeToBuffer(int index, T state, CameraRenderState camera, MemorySegment buffer);

	/// Converts the {@code colour} to the native RGBA format.
	protected static final int toNativeRgba(int colour) {
		int abgr = ARGB.toABGR(colour);
		return IS_LITTLE_ENDIAN ? abgr : Integer.reverseBytes(abgr);
	}
}
