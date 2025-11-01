package de.hysky.skyblocker.utils.render.primitive;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.render.state.CameraRenderState;

/**
 * Interface to represent a class that renders simple primitives in the world. Implementations
 * of this interface must be stateless.
 */
public interface PrimitiveRenderer<T> {
	RenderStateDataKey<Float> CAMERA_YAW = RenderStateDataKey.create(() -> "Skyblocker camera yaw");
	RenderStateDataKey<Float> CAMERA_PITCH = RenderStateDataKey.create(() -> "Skyblocker camera pitch");

	void submitPrimitives(T state, CameraRenderState cameraState);
}
