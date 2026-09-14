package de.hysky.skyblocker.utils.render.primitive;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.AltModelBlockRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;

import de.hysky.skyblocker.utils.render.MatrixHelper;
import de.hysky.skyblocker.utils.render.state.BlockHologramRenderState;

public class BlockHologramFeatureRenderer extends PrimitiveFeatureRenderer<BlockHologramFeatureRenderer.Submit> {
	public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("Skyblocker Block Hologram");

	@Override
	protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
		@SuppressWarnings("deprecation")
		GpuTextureView blocksAtlasTexture = context.textureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).getTextureView();
		GpuSampler sampler = RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.NEAREST, true);
		AltModelBlockRenderer altModelBlockRenderer = Renderer.get().altModelBlockRenderer(context.options().ambientOcclusion, false, context.blockColors());

		for (Submit submit : submits) {
			for (BlockHologramRenderState state : submit.states()) {
				Matrix4f positionMatrix = new Matrix4f()
						.translate((float) (state.pos().getX() - submit.camera().pos.x()), (float) (state.pos().getY() - submit.camera().pos.y()), (float) (state.pos().getZ() - submit.camera().pos.z()));
				PoseStack pose = MatrixHelper.toStack(positionMatrix);

				VertexConsumer builder = this.getVertexBuilder(RenderPipelines.TRANSLUCENT_BLOCK, TextureSetup.singleTextureWithLightmap(blocksAtlasTexture, sampler));
				QuadEmitter quadEmitter = Renderer.get().quadEmitter(quad -> {
					int colour = ARGB.color(state.alpha(), CommonColors.WHITE);

					quad.color(colour, colour, colour, colour);
					quad.buffer(OverlayTexture.NO_OVERLAY, pose.last(), builder);
				});
				BlockStateModel model = context.blockStateModelSet().get(state.state());
				long blockSeed = state.state().getSeed(state.pos());

				altModelBlockRenderer.tesselateBlock(quadEmitter, 0, 0, 0, Minecraft.getInstance().level, state.pos(), state.state(), model, blockSeed);
			}
		}
	}

	public record Submit(List<BlockHologramRenderState> states, CameraRenderState camera) implements SubmitNode {
		@Override
		public FeatureRendererType<? extends SubmitNode> featureType() {
			return TYPE;
		}
	}
}
