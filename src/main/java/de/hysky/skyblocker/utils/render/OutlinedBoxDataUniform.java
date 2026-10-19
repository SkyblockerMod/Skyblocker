package de.hysky.skyblocker.utils.render;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import com.mojang.blaze3d.GpuFormat;

import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;

import de.hysky.skyblocker.utils.render.state.OutlinedBoxRenderState;

public class OutlinedBoxDataUniform extends AbstractUniformTexelBuffer<OutlinedBoxRenderState> {
	private static final int TEXELS_PER_INSTANCE = 2;
	private static final int BYTES_PER_BOX = GpuFormat.RGBA32_FLOAT.blockSize() * TEXELS_PER_INSTANCE;

	public OutlinedBoxDataUniform() {
		super(BYTES_PER_BOX);
	}

	@Override
	protected void writeToBuffer(int index, OutlinedBoxRenderState state, CameraRenderState camera, MemorySegment buffer) {
		long offset = index * BYTES_PER_BOX;
		int colour = toNativeRgba(ARGB.colorFromFloat(state.alpha(), state.colourComponents()[0], state.colourComponents()[1], state.colourComponents()[2]));

		// The coordinates must be offset by the camera here since double precision is required.

		// Update first Texel
		buffer.set(ValueLayout.JAVA_FLOAT, offset + 0L, (float) (state.minX() - camera.pos.x));
		buffer.set(ValueLayout.JAVA_FLOAT, offset + 4L, (float) (state.minY() - camera.pos.y));
		buffer.set(ValueLayout.JAVA_FLOAT, offset + 8L, (float) (state.minZ() - camera.pos.z));
		buffer.set(ValueLayout.JAVA_FLOAT, offset + 12L, (float) (state.maxX() - camera.pos.x));

		// Update second Texel
		buffer.set(ValueLayout.JAVA_FLOAT, offset + 16L, (float) (state.maxY() - camera.pos.y));
		buffer.set(ValueLayout.JAVA_FLOAT, offset + 20L, (float) (state.maxZ() - camera.pos.z));
		buffer.set(ValueLayout.JAVA_INT,   offset + 24L, colour);
		buffer.set(ValueLayout.JAVA_FLOAT, offset + 28L, state.lineWidth());
	}
}
