package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.FrustumUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BlockStateManagers;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class CatPicture {

	private static final Vec3d RENDER_POSITION = new Vec3d(6, 72, -92);
	private static final Box CULLING_BOX = new Box(RENDER_POSITION.x, RENDER_POSITION.y, RENDER_POSITION.z, RENDER_POSITION.x + 1, RENDER_POSITION.y + 1, RENDER_POSITION.z + 1 / 16d);
	private static final Identifier TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "textures/cat.png");

	@Init
	public static void init() {
		WorldRenderEvents.AFTER_ENTITIES.register(CatPicture::render);
	}

	private static void render(WorldRenderContext context) {
		if (!SkyblockerConfigManager.get().misc.cat ||
				Utils.getLocation() != Location.HUB ||
				!FrustumUtils.isVisible(CULLING_BOX)
		) return;
		BlockState blockState = BlockStateManagers.getStateForItemFrame(false, true);
		BlockStateModel blockStateModel = MinecraftClient.getInstance().getBlockRenderManager().getModel(blockState);
		VertexConsumerProvider vertexConsumerProvider = context.consumers();
		MatrixStack matrixStack = context.matrixStack();
		if (matrixStack == null || vertexConsumerProvider == null) return;

		matrixStack.push();
		Vec3d pos = context.camera().getPos();
		matrixStack.translate(-pos.x + RENDER_POSITION.x + 1, -pos.y + RENDER_POSITION.y, -pos.z + RENDER_POSITION.z + 1);
		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
		// Render item frame
		MatrixStack.Entry peek = matrixStack.peek();
		BlockModelRenderer.render(
				peek,
				vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolidZOffsetForward(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)),
				blockStateModel,
				1.0F,
				1.0F,
				1.0F,
				15,
				OverlayTexture.DEFAULT_UV
		);
		// Render kitty
		matrixStack.translate(1, 1, 0);
		matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
		Matrix4f matrix4f = peek.getPositionMatrix();
		VertexConsumer cat = vertexConsumerProvider.getBuffer(RenderLayer.getText(TEXTURE));
		float z = 1F - 1 / 16f - 1 / 2048f;
		cat.vertex(matrix4f, 0.0F, 1, z).color(-1).texture(0.0F, 1.0F).light(15);
		cat.vertex(matrix4f, 1, 1, z).color(-1).texture(1.0F, 1.0F).light(15);
		cat.vertex(matrix4f, 1, 0.0F, z).color(-1).texture(1.0F, 0.0F).light(15);
		cat.vertex(matrix4f, 0.0F, 0.0F, z).color(-1).texture(0.0F, 0.0F).light(15);
		matrixStack.pop();
	}
}
