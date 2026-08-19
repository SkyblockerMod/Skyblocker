package de.hysky.skyblocker.utils.render;

import java.lang.foreign.MemorySegment;

import com.mojang.blaze3d.GpuFormat;

import net.minecraft.client.renderer.state.level.CameraRenderState;

import de.hysky.skyblocker.utils.render.state.OutlinedConnectedRenderState;

public class OutlinedConnectedDataUniform extends AbstractUniformTexelBuffer<OutlinedConnectedRenderState> {
	private static final int TEXELS_PER_INSTANCE = 2;
	private static final int BYTES_PER_BOX = GpuFormat.RGBA32_FLOAT.blockSize() * TEXELS_PER_INSTANCE;

	public OutlinedConnectedDataUniform() {
		super(BYTES_PER_BOX);
	}

	@Override
	protected void writeToBuffer(int index, OutlinedConnectedRenderState state, CameraRenderState camera, MemorySegment buffer) {
		// TODO!
	}
}
