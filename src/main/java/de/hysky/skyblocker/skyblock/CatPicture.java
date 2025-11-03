package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.render.state.EmptyRenderState;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.model.BlockStateManagers;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class CatPicture {
	private static final Vec3d RENDER_POSITION = new Vec3d(6, 72, -92);
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
	private static void render(EmptyRenderState state, WorldRenderState worldState, OrderedRenderCommandQueue commandQueue) {
		// Vanilla does this in the ItemFrameEntityRenderer
		BlockState blockState = BlockStateManagers.getStateForItemFrame(false, true);
		BlockStateModel blockStateModel = MinecraftClient.getInstance().getBlockRenderManager().getModel(blockState);

		MatrixStack matrices = new MatrixStack();
		matrices.push();
		matrices.translate(-worldState.cameraRenderState.pos.x + RENDER_POSITION.x + 1, -worldState.cameraRenderState.pos.y + RENDER_POSITION.y, -worldState.cameraRenderState.pos.z + RENDER_POSITION.z + 1);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));

		// Render Item Frame
		commandQueue.submitBlockStateModel(
				matrices,
				RenderLayer.getEntitySolidZOffsetForward(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE),
				blockStateModel,
				1f,
				1f,
				1f,
				LightmapTextureManager.MAX_LIGHT_COORDINATE,
				OverlayTexture.DEFAULT_UV,
				EntityRenderState.NO_OUTLINE
		);

		// Render Kitty
		matrices.translate(1, 1, 0);
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));

		commandQueue.submitCustom(matrices, RenderLayer.getText(TEXTURE), (matricesEntry, buffer) -> {
			float z = 1F - 1 / 16f - 1 / 2048f;
			buffer.vertex(matricesEntry, 0.0F, 1, z).color(Colors.WHITE).texture(0.0F, 1.0F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
			buffer.vertex(matricesEntry, 1, 1, z).color(Colors.WHITE).texture(1.0F, 1.0F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
			buffer.vertex(matricesEntry, 1, 0.0F, z).color(Colors.WHITE).texture(1.0F, 0.0F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
			buffer.vertex(matricesEntry, 0.0F, 0.0F, z).color(Colors.WHITE).texture(0.0F, 0.0F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
		});

		matrices.pop();
	}
}
