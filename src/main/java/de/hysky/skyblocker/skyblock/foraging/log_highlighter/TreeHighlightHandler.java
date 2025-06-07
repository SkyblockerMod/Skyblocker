package de.hysky.skyblocker.skyblock.foraging.log_highlighter;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;

public class TreeHighlightHandler {
	private static final MinecraftClient client = MinecraftClient.getInstance();
	private static final Set<BlockPos> highlightedLogs = new HashSet<>();
	private static long lastHighlightUpdate = 0;
	private static final int MAX_HIGHLIGHT_BLOCKS = 200;

	public static void render(MatrixStack matrices, WorldRenderer worldRenderer, Vec3d cameraPos) {
		if (!isHoldingTreecapOrJungleAxe()) return;
		if (!SkyblockerConfigManager.get().foraging.park.highlightConnectedTree) return;
		if (client.crosshairTarget instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
			if (Utils.getLocation() != Location.THE_PARK) return;

			BlockPos start = hit.getBlockPos();
			long now = System.currentTimeMillis();

			if (now - lastHighlightUpdate > 100) {
				highlightedLogs.clear();
				floodFill(start, 0);
				lastHighlightUpdate = now;
			}

			Box boundingBox = getBoundingBox(highlightedLogs);
			if (boundingBox != null) {
				Box offsetBox = boundingBox.offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);

				RenderHelper.init();
				int color = SkyblockerConfigManager.get().foraging.park.highlightColor;

// Extract RGBA components
				float alpha = ((color >> 24) & 0xFF) / 255.0f;
				float red   = ((color >> 16) & 0xFF) / 255.0f;
				float green = ((color >> 8) & 0xFF) / 255.0f;
				float blue  = (color & 0xFF) / 255.0f;

				renderOutline(matrices, client.getBufferBuilders().getEntityVertexConsumers(), offsetBox, red, green, blue, alpha);
			}
		}
	}

	private static boolean isHoldingTreecapOrJungleAxe() {
		if (client.player == null) return false;

		var heldItem = client.player.getMainHandStack();
		if (heldItem.isEmpty()) return false;

		String id = heldItem.getSkyblockId();
		if(!id.isEmpty()) {
			return id.equals("TREECAPITATOR_AXE") || id.equals("JUNGLE_AXE");
		}
		// Option 2: Fallback: check display name
		String displayName = heldItem.getItemName().getString();
		return displayName.contains("treecapitator") || displayName.contains("jungle axe");
	}


	private static void floodFill(BlockPos pos, int depth) {
		if (highlightedLogs.size() >= MAX_HIGHLIGHT_BLOCKS || depth > 64) return;

		BlockState state = client.world.getBlockState(pos);
		if (!isLogBlock(state.getBlock())) return;
		if (!highlightedLogs.add(pos)) return;

		for (Direction dir : Direction.values()) {
			floodFill(pos.offset(dir), depth + 1);
		}
	}

	private static boolean isLogBlock(Block block) {
		return block == Blocks.OAK_LOG ||
				block == Blocks.BIRCH_LOG ||
				block == Blocks.SPRUCE_LOG ||
				block == Blocks.JUNGLE_LOG ||
				block == Blocks.ACACIA_LOG ||
				block == Blocks.DARK_OAK_LOG;
	}

	private static Box getBoundingBox(Set<BlockPos> positions) {
		if (positions.isEmpty()) return null;

		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

		for (BlockPos pos : positions) {
			if (pos.getX() < minX) minX = pos.getX();
			if (pos.getY() < minY) minY = pos.getY();
			if (pos.getZ() < minZ) minZ = pos.getZ();

			if (pos.getX() > maxX) maxX = pos.getX();
			if (pos.getY() > maxY) maxY = pos.getY();
			if (pos.getZ() > maxZ) maxZ = pos.getZ();
		}

		// +1 on max coords because Box is exclusive on the max edge
		return new Box(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
	}

	/**
	 * Renders a colored outline around the given box.
	 * Adapted from how Minecraft renders bounding box edges.
	 */
	private static void renderOutline(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, Box box, float r, float g, float b, float a) {
		// Get the vertices of the box
		double minX = box.minX;
		double minY = box.minY;
		double minZ = box.minZ;
		double maxX = box.maxX;
		double maxY = box.maxY;
		double maxZ = box.maxZ;

		// Begin drawing lines
		VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getLines());

		// Use current matrices to get the pose stack
		MatrixStack.Entry entry = matrices.peek();
		// 12 edges of a box, each with 2 vertices:
		// bottom rectangle
		buffer.vertex(entry.getPositionMatrix(), (float) minX, (float) minY, (float) minZ).color(r, g, b, a);
		buffer.vertex(entry.getPositionMatrix(), (float) maxX, (float) minY, (float) minZ).color(r, g, b, a);

		buffer.vertex(entry.getPositionMatrix(), (float) maxX, (float) minY, (float) minZ).color(r, g, b, a);
		buffer.vertex(entry.getPositionMatrix(), (float) maxX, (float) minY, (float) maxZ).color(r, g, b, a);

		buffer.vertex(entry.getPositionMatrix(), (float) maxX, (float) minY, (float) maxZ).color(r, g, b, a);
		buffer.vertex(entry.getPositionMatrix(), (float) minX, (float) minY, (float) maxZ).color(r, g, b, a);

		buffer.vertex(entry.getPositionMatrix(), (float) minX, (float) minY, (float) maxZ).color(r, g, b, a);
		buffer.vertex(entry.getPositionMatrix(), (float) minX, (float) minY, (float) minZ).color(r, g, b, a);

		// top rectangle
		buffer.vertex(entry.getPositionMatrix(), (float) minX, (float) maxY, (float) minZ).color(r, g, b, a);
		buffer.vertex(entry.getPositionMatrix(), (float) maxX, (float) maxY, (float) minZ).color(r, g, b, a);

		buffer.vertex(entry.getPositionMatrix(), (float) maxX, (float) maxY, (float) minZ).color(r, g, b, a);
		buffer.vertex(entry.getPositionMatrix(), (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, a);

		buffer.vertex(entry.getPositionMatrix(), (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, a);
		buffer.vertex(entry.getPositionMatrix(), (float) minX, (float) maxY, (float) maxZ).color(r, g, b, a);

		buffer.vertex(entry.getPositionMatrix(), (float) minX, (float) maxY, (float) maxZ).color(r, g, b, a);
		buffer.vertex(entry.getPositionMatrix(), (float) minX, (float) maxY, (float) minZ).color(r, g, b, a);

		// vertical edges
		buffer.vertex(entry.getPositionMatrix(), (float) minX, (float) minY, (float) minZ).color(r, g, b, a);
		buffer.vertex(entry.getPositionMatrix(), (float) minX, (float) maxY, (float) minZ).color(r, g, b, a);

		buffer.vertex(entry.getPositionMatrix(), (float) maxX, (float) minY, (float) minZ).color(r, g, b, a);
		buffer.vertex(entry.getPositionMatrix(), (float) maxX, (float) maxY, (float) minZ).color(r, g, b, a);

		buffer.vertex(entry.getPositionMatrix(), (float) maxX, (float) minY, (float) maxZ).color(r, g, b, a);
		buffer.vertex(entry.getPositionMatrix(), (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, a);

		buffer.vertex(entry.getPositionMatrix(), (float) minX, (float) minY, (float) maxZ).color(r, g, b, a);
		buffer.vertex(entry.getPositionMatrix(), (float) minX, (float) maxY, (float) maxZ).color(r, g, b, a);
	}
}
