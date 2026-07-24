package de.hysky.skyblocker.utils.render.primitive;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import de.hysky.skyblocker.utils.render.MatrixHelper;
import de.hysky.skyblocker.utils.render.state.BlockHologramRenderState;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.AltModelBlockRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;

public final class BlockHologramRenderer implements PrimitiveRenderer<BlockHologramRenderState> {
	private static final Minecraft MINECRAFT = Minecraft.getInstance();
	private final AltModelBlockRenderer altModelBlockRenderer;

	protected BlockHologramRenderer(AltModelBlockRenderer altModelBlockRenderer) {
		this.altModelBlockRenderer = altModelBlockRenderer;
	}

	@Override
	public void submitPrimitives(BlockHologramRenderState state, CameraRenderState cameraState) {
		Matrix4f positionMatrix = new Matrix4f()
				.translate((float) (state.pos().getX() - cameraState.pos.x()), (float) (state.pos().getY() - cameraState.pos.y()), (float) (state.pos().getZ() - cameraState.pos.z()));
		PoseStack pose = MatrixHelper.toStack(positionMatrix);

		@SuppressWarnings("deprecation")
		GpuTextureView blocksAtlasTexture = MINECRAFT.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).getTextureView();
		GpuSampler sampler = RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.NEAREST, true);
		VertexConsumer buffer = de.hysky.skyblocker.utils.render.Renderer.getBuffer(RenderPipelines.TRANSLUCENT_BLOCK, TextureSetup.singleTextureWithLightmap(blocksAtlasTexture, sampler), state.alpha());
		QuadEmitter quadEmitter = Renderer.get().quadEmitter(quad -> {
			quad.buffer(OverlayTexture.NO_OVERLAY, pose.last(), buffer);
		});
		BlockStateModel model = MINECRAFT.getModelManager().getBlockStateModelSet().get(state.state());
		long blockSeed = state.state().getSeed(state.pos());

		this.altModelBlockRenderer.tesselateBlock(quadEmitter, 0, 0, 0, MINECRAFT.level, state.pos(), state.state(), model, blockSeed);
	}
}
