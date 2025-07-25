package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.utils.render.RenderHelper;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MasteryTestHelper {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final DecimalFormat FORMATTER = new DecimalFormat("0.00");
	/**
	 * How long it takes for a block to turn red
	 */
	private static final int BLOCK_LIFE_TIME = 6550;

	private static final List<BlockPos> blockOrder = new ArrayList<>();
	private static final Object2LongOpenHashMap<BlockPos> endTimes = new Object2LongOpenHashMap<>();

	protected static void reset() {
		blockOrder.clear();
		endTimes.clear();
	}

	protected static void onBlockUpdate(BlockPos pos, BlockState state) {
		if (CLIENT == null || CLIENT.player == null) {
			return;
		}
		if (state.isOf(Blocks.LIME_WOOL)) {
			blockOrder.add(pos);
			//add lifetime of a block to the time to get time when block expires
			// work out how long it will take between the player firing and arrow hitting the block and to subtract from time
			long travelTime = (long) (CLIENT.player.getPos().distanceTo(pos.toCenterPos()) * 1000 / 60); //an arrow speed is about 60 blocks a second from a full draw
			endTimes.put(pos, System.currentTimeMillis() + BLOCK_LIFE_TIME - DojoManager.ping - travelTime);
		}
		if (state.isAir()) {
			blockOrder.remove(pos);
			endTimes.removeLong(pos);
		}
	}

	protected static void render(WorldRenderContext context) {
		//render connecting lines
		if (!blockOrder.isEmpty()) {
			RenderHelper.renderLineFromCursor(context, blockOrder.getFirst().toCenterPos(), Color.LIGHT_GRAY.getColorComponents(new float[]{0, 0, 0}), 1f, 2);
		}
		if (blockOrder.size() >= 2) {
			RenderHelper.renderLinesFromPoints(context, new Vec3d[]{blockOrder.get(0).toCenterPos(), blockOrder.get(1).toCenterPos()}, Color.LIGHT_GRAY.getColorComponents(new float[]{0, 0, 0}), 1, 2, false);
		}

		//render times
		long currentTime = System.currentTimeMillis();
		for (BlockPos pos : blockOrder) {
			long blockEndTime = endTimes.getLong(pos);
			float secondsTime = Math.max((blockEndTime - currentTime) / 1000f, 0);
			RenderHelper.renderText(context, Text.literal(FORMATTER.format(secondsTime)), pos.add(0, 1, 0).toCenterPos(), 3, true);
		}
	}
}
