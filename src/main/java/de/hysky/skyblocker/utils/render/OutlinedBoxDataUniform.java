package de.hysky.skyblocker.utils.render;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.List;

import de.hysky.skyblocker.utils.render.state.OutlinedBoxRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;

public class OutlinedBoxDataUniform extends AbstractUniformTexelBuffer<OutlinedBoxRenderState> {
	private static final int TEXELS_PER_INSTANCE = 2;
	private static final int BYTES_PER_BOX = (Float.BYTES * 4) * TEXELS_PER_INSTANCE;

	public OutlinedBoxDataUniform() {
		super(BYTES_PER_BOX);
	}

	@Override
	protected void updateBuffer(List<OutlinedBoxRenderState> states, CameraRenderState cameraRenderState, MemorySegment buffer) {
		for (int i = 0; i < states.size(); i++) {
			long offset = i * BYTES_PER_BOX;
			OutlinedBoxRenderState state = states.get(i);
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
			buffer.set(ValueLayout.JAVA_FLOAT, offset + 28L, state.lineWidth);
		}
	}

}
