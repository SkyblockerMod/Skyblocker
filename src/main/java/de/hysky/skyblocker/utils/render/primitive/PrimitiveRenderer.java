package de.hysky.skyblocker.utils.render.primitive;

import de.hysky.skyblocker.utils.render.state.CameraRenderState;

/**
 * Interface to represent a class that renders simple primitives in the world. Implementations
 * of this interface must be stateless.
 */
public interface PrimitiveRenderer<T> {
	void submitPrimitives(T state, CameraRenderState cameraState);
}
