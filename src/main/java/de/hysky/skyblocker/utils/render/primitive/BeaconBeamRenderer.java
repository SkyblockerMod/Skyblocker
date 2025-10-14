package de.hysky.skyblocker.utils.render.primitive;

import org.joml.Matrix4f;

import de.hysky.skyblocker.mixins.accessors.BeaconBlockEntityRendererInvoker;
import de.hysky.skyblocker.utils.render.MatrixHelper;
import de.hysky.skyblocker.utils.render.state.BeaconBeamRenderState;
import de.hysky.skyblocker.utils.render.state.CameraRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public final class BeaconBeamRenderer implements PrimitiveRenderer<BeaconBeamRenderState> {
	protected static final BeaconBeamRenderer INSTANCE = new BeaconBeamRenderer();

	private BeaconBeamRenderer() {}

	@Override
	public void submitPrimitives(BeaconBeamRenderState state, CameraRenderState cameraState) {
		Matrix4f positionMatrix = new Matrix4f()
				.translate((float) (state.pos.getX() - cameraState.pos.getX()), (float) (state.pos.getY() - cameraState.pos.getY()), (float) (state.pos.getZ() - cameraState.pos.getZ()));
		MatrixStack matrices = MatrixHelper.toStack(positionMatrix);
		VertexConsumerProvider.Immediate consumers = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

		BeaconBlockEntityRendererInvoker.renderBeam(matrices, consumers, state.tickProgress, state.scale, state.worldTime, 0, PrimitiveCollectorImpl.MAX_OVERWORLD_BUILD_HEIGHT, state.colour);
		consumers.draw();
	}
}
