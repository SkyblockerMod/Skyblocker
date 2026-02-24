package de.hysky.skyblocker.skyblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.render.state.EmptyRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BlockStateDefinitions;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;

public class CatPicture {
	private static final Vec3 RENDER_POSITION = new Vec3(-3, 79, 3);
	//private static final Box CULLING_BOX = new Box(RENDER_POSITION.x, RENDER_POSITION.y, RENDER_POSITION.z, RENDER_POSITION.x + 1, RENDER_POSITION.y + 1, RENDER_POSITION.z + 1/16d);
	private static final Identifier TEXTURE = SkyblockerMod.id("textures/cat.png");

	@Init
	public static void init() {
		WorldRenderExtractionCallback.EVENT.register(CatPicture::extractRendering);
	}

	private static void extractRendering(PrimitiveCollector collector) {
		// TODO Bring back culling eventually, maybe just include more context in the collector
		if (SkyblockerConfigManager.get().misc.cat && Utils.getLocation() == Location.HUB) {
			collector.submitVanilla(EmptyRenderState.INSTANCE, CatPicture::render);
		}
	}

	@SuppressWarnings("deprecation")
	private static void render(EmptyRenderState state, LevelRenderState worldState, SubmitNodeCollector commandQueue) {
		// Vanilla does this in the ItemFrameEntityRenderer
		BlockState blockState = BlockStateDefinitions.getItemFrameFakeState(false, true);
		BlockStateModel blockStateModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState);

		PoseStack matrices = new PoseStack();
		matrices.pushPose();
		matrices.translate(-worldState.cameraRenderState.pos.x + RENDER_POSITION.x + 1, -worldState.cameraRenderState.pos.y + RENDER_POSITION.y, -worldState.cameraRenderState.pos.z + RENDER_POSITION.z + 1);
		matrices.mulPose(Axis.YP.rotationDegrees(180));

		// Render Item Frame
		commandQueue.submitBlockModel(
				matrices,
				RenderType.entitySolidZOffsetForward(TextureAtlas.LOCATION_BLOCKS),
				blockStateModel,
				1f,
				1f,
				1f,
				LightTexture.FULL_BRIGHT,
				OverlayTexture.NO_OVERLAY,
				EntityRenderState.NO_OUTLINE
		);

		// Render Kitty
		matrices.translate(1, 1, 0);
		matrices.mulPose(Axis.ZP.rotationDegrees(180.0F));

		commandQueue.submitCustomGeometry(matrices, RenderType.text(TEXTURE), (matricesEntry, buffer) -> {
			float z = 1F - 1 / 16f - 1 / 2048f;
			buffer.addVertex(matricesEntry, 0.0F, 1, z).setColor(CommonColors.WHITE).setUv(0.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT);
			buffer.addVertex(matricesEntry, 1, 1, z).setColor(CommonColors.WHITE).setUv(1.0F, 1.0F).setLight(LightTexture.FULL_BRIGHT);
			buffer.addVertex(matricesEntry, 1, 0.0F, z).setColor(CommonColors.WHITE).setUv(1.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT);
			buffer.addVertex(matricesEntry, 0.0F, 0.0F, z).setColor(CommonColors.WHITE).setUv(0.0F, 0.0F).setLight(LightTexture.FULL_BRIGHT);
		});

		matrices.popPose();
	}
}
