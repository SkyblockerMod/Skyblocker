package de.hysky.skyblocker.utils.render.primitive;

import org.joml.Matrix4f;

import de.hysky.skyblocker.utils.render.MatrixHelper;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.Renderer;
import de.hysky.skyblocker.utils.render.state.BlockHologramRenderState;
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;

public final class BlockHologramRenderer implements PrimitiveRenderer<BlockHologramRenderState> {
	protected static final BlockHologramRenderer INSTANCE = new BlockHologramRenderer();
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final boolean SODIUM_LOADED = FabricLoader.getInstance().isModLoaded("sodium");

	private BlockHologramRenderer() {}

	@Override
	public void submitPrimitives(BlockHologramRenderState state, CameraRenderState cameraState) {
		Matrix4f positionMatrix = new Matrix4f()
				.translate((float) (state.pos.getX() - cameraState.pos.getX()), (float) (state.pos.getY() - cameraState.pos.getY()), (float) (state.pos.getZ() - cameraState.pos.getZ()));
		MatrixStack matrices = MatrixHelper.toStack(positionMatrix);
		BlockStateModel model = CLIENT.getBlockRenderManager().getModel(state.state);

		VertexConsumerProvider consumers = SODIUM_LOADED ? CLIENT.getBufferBuilders().getEntityVertexConsumers() : _layer -> Renderer.getBuffer(RenderPipelines.TRANSLUCENT, RenderHelper.singleTexture(BlockRenderLayer.TRANSLUCENT.getTextureView()), true);
		CLIENT.getBlockRenderManager().getModelRenderer().render(CLIENT.world, model, state.state, state.pos, matrices, RenderLayerHelper.movingDelegate(consumers), true, state.state.getRenderingSeed(state.pos), 0);
	}
}
