package de.hysky.skyblocker.utils.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;

public class SkullRenderer {
	public static void submitSkull(PrimitiveCollector collector, BlockPos pos, FlexibleItemStack stack, int alpha) {
		collector.submitVanilla(new SkullPreviewState(pos, stack, alpha), SkullRenderer::renderSkullPreview);
	}

	private static void renderSkullPreview(
			SkullPreviewState state,
			LevelRenderState levelState,
			SubmitNodeCollector submitNodeCollector
	) {
		ItemStack stack = state.skull().getStack();
		if (stack == null || stack.isEmpty()) return;

		ResolvableProfile profile = stack.get(DataComponents.PROFILE);
		if (profile == null) return;

		PlayerSkinRenderCache.RenderInfo skinRenderInfo = Minecraft.getInstance()
				.playerSkinRenderCache()
				.getOrDefault(profile);

		Identifier texture = skinRenderInfo.playerSkin().body().texturePath();
		RenderType renderType = RenderTypes.entityTranslucent(texture);
		if (renderType == null) return;

		PoseStack matrices = new PoseStack();
		matrices.pushPose();

		BlockPos pos = state.pos();

		// Camera offset
		matrices.translate(
				pos.getX() - levelState.cameraRenderState.pos.x,
				pos.getY() - levelState.cameraRenderState.pos.y,
				pos.getZ() - levelState.cameraRenderState.pos.z
		);

		matrices.mulPose(SkullBlockRenderer.TRANSFORMATIONS.freeTransformations(0));
		matrices.scale(1.0f, -1.0f, 1.0f);

		submitNodeCollector.submitCustomGeometry(matrices, renderType, (matricesEntry, buffer) -> {
			float ox = -0.5f;
			float oy = 0.02f;
			float oz = -0.5f;

			renderHeadLayer(
					buffer, matricesEntry, state.argb(),
					0.25f + ox, 0.0f + oy, 0.25f + oz,
					0.75f + ox, 0.5f + oy, 0.75f + oz,
					8.0f, 0.0f
			);

			renderHeadLayer(
					buffer, matricesEntry, state.argb(),
					0.21875f + ox, -0.03125f + oy, 0.21875f + oz,
					0.78125f + ox, 0.53125f + oy, 0.78125f + oz,
					40.0f, 32.0f
			);
		});


		matrices.popPose();
	}

	private static void renderHeadLayer(
			com.mojang.blaze3d.vertex.VertexConsumer buffer,
			com.mojang.blaze3d.vertex.PoseStack.Pose matricesEntry,
			int argb,
			float x0,
			float y0,
			float z0,
			float x1,
			float y1,
			float z1,
			float frontU,
			float leftU
	) {
		float inv = 1.0f / 64.0f;
		addQuad(buffer, matricesEntry, x0, y0, z0, x1, y1, z0, frontU, 8.0f, frontU + 8.0f, 16.0f, argb); // front
		addQuad(buffer, matricesEntry, x1, y0, z1, x0, y1, z1, frontU + 16.0f, 8.0f, frontU + 24.0f, 16.0f, argb); // back
		addQuad(buffer, matricesEntry, x0, y0, z1, x0, y1, z0, leftU, 8.0f, leftU + 8.0f, 16.0f, argb); // left
		addQuad(buffer, matricesEntry, x1, y0, z0, x1, y1, z1, leftU + 16.0f, 8.0f, leftU + 24.0f, 16.0f, argb); // right
		addTopQuad(buffer, matricesEntry, x0, y1, z0, x1, z1, frontU, 0.0f, frontU + 8.0f, 8.0f, argb, inv); // top
		addTopQuad(buffer, matricesEntry, x0, y0, z1, x1, z0, frontU + 8.0f, 0.0f, frontU + 16.0f, 8.0f, argb, inv); // bottom
	}

	private static void addQuad(
			VertexConsumer buffer,
			PoseStack.Pose pose,
			float x0, float y0, float z0,
			float x1, float y1, float z1,
			float minU, float minV,
			float maxU, float maxV,
			int argb
	) {
		float inv = 1.0f / 64.0f;

		float nx = 0.0f;
		float ny = 0.0f;
		float nz = 1.0f;

		vertex(buffer, pose, x0, y0, z0, minU * inv, maxV * inv, argb, nx, ny, nz);
		vertex(buffer, pose, x0, y1, z0, minU * inv, minV * inv, argb, nx, ny, nz);
		vertex(buffer, pose, x1, y1, z1, maxU * inv, minV * inv, argb, nx, ny, nz);
		vertex(buffer, pose, x1, y0, z1, maxU * inv, maxV * inv, argb, nx, ny, nz);
	}

	private static void addTopQuad(
			VertexConsumer buffer,
			PoseStack.Pose pose,
			float x0,
			float y,
			float z0,
			float x1,
			float z1,
			float minU,
			float minV,
			float maxU,
			float maxV,
			int argb,
			float inv
	) {
		vertex(buffer, pose, x0, y, z0, minU * inv, minV * inv, argb, 0.0f, 1.0f, 0.0f);
		vertex(buffer, pose, x0, y, z1, minU * inv, maxV * inv, argb, 0.0f, 1.0f, 0.0f);
		vertex(buffer, pose, x1, y, z1, maxU * inv, maxV * inv, argb, 0.0f, 1.0f, 0.0f);
		vertex(buffer, pose, x1, y, z0, maxU * inv, minV * inv, argb, 0.0f, 1.0f, 0.0f);
	}

	// Helper method to add vertex
	private static void vertex(
			VertexConsumer buffer,
			PoseStack.Pose pose,
			float x, float y, float z,
			float u, float v,
			int argb,
			float nx, float ny, float nz
	) {
		buffer.addVertex(pose, x, y, z)
				.setColor(argb)
				.setUv(u, v)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(LightCoordsUtil.FULL_BRIGHT)
				.setNormal(pose, nx, ny, nz);
	}

	public record SkullPreviewState(BlockPos pos, FlexibleItemStack skull, int argb) {}
}
