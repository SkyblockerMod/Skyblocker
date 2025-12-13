package de.hysky.skyblocker.utils.render.primitive;

import net.minecraft.client.renderer.state.CameraRenderState;

/**
 * Interface to represent a class that renders simple primitives in the world. Implementations
 * of this interface must be stateless.
 */
public interface PrimitiveRenderer<S> {
	void submitPrimitives(S state, CameraRenderState cameraState);
}
