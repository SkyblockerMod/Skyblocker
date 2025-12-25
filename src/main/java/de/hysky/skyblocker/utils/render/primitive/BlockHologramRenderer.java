package de.hysky.skyblocker.utils.render.primitive;

import org.joml.Matrix4f;
import com.mojang.blaze3d.vertex.PoseStack;
import de.hysky.skyblocker.utils.render.MatrixHelper;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.Renderer;
import de.hysky.skyblocker.utils.render.state.BlockHologramRenderState;
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.state.CameraRenderState;

public final class BlockHologramRenderer implements PrimitiveRenderer<BlockHologramRenderState> {
	protected static final BlockHologramRenderer INSTANCE = new BlockHologramRenderer();
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final boolean SODIUM_LOADED = FabricLoader.getInstance().isModLoaded("sodium");

	private BlockHologramRenderer() {}

	@Override
	public void submitPrimitives(BlockHologramRenderState state, CameraRenderState cameraState) {
		Matrix4f positionMatrix = new Matrix4f()
				.translate((float) (state.pos.getX() - cameraState.pos.x()), (float) (state.pos.getY() - cameraState.pos.y()), (float) (state.pos.getZ() - cameraState.pos.z()));
		PoseStack matrices = MatrixHelper.toStack(positionMatrix);
		BlockStateModel model = CLIENT.getBlockRenderer().getBlockModel(state.state);

		MultiBufferSource consumers = SODIUM_LOADED ? CLIENT.renderBuffers().bufferSource() : _layer -> Renderer.getBuffer(RenderPipelines.TRANSLUCENT, RenderHelper.singleTexture(ChunkSectionLayer.TRANSLUCENT.textureView()), true);
		CLIENT.getBlockRenderer().getModelRenderer().render(CLIENT.level, model, state.state, state.pos, matrices, RenderLayerHelper.movingDelegate(consumers), true, state.state.getSeed(state.pos), 0);
	}
}
