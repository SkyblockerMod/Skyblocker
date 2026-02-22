package de.hysky.skyblocker.utils.render.primitive;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import de.hysky.skyblocker.utils.render.MatrixHelper;
import de.hysky.skyblocker.utils.render.Renderer;
import de.hysky.skyblocker.utils.render.state.BlockHologramRenderState;
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;

public final class BlockHologramRenderer implements PrimitiveRenderer<BlockHologramRenderState> {
	protected static final BlockHologramRenderer INSTANCE = new BlockHologramRenderer();
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private BlockHologramRenderer() {}

	@Override
	public void submitPrimitives(BlockHologramRenderState state, CameraRenderState cameraState) {
		Matrix4f positionMatrix = new Matrix4f()
				.translate((float) (state.pos.getX() - cameraState.pos.x()), (float) (state.pos.getY() - cameraState.pos.y()), (float) (state.pos.getZ() - cameraState.pos.z()));
		PoseStack matrices = MatrixHelper.toStack(positionMatrix);
		BlockStateModel model = CLIENT.getBlockRenderer().getBlockModel(state.state);

		@SuppressWarnings("deprecation")
		MultiBufferSource bufferSource = _type -> Renderer.getBuffer(RenderPipelines.TRANSLUCENT_MOVING_BLOCK, TextureSetup.singleTextureWithLightmap(CLIENT.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).getTextureView(), RenderTypes.MOVING_BLOCK_SAMPLER.get()), state.alpha);
		CLIENT.getBlockRenderer().getModelRenderer().render(CLIENT.level, model, state.state, state.pos, matrices, RenderLayerHelper.movingDelegate(bufferSource), true, state.state.getSeed(state.pos), 0);
	}
}
