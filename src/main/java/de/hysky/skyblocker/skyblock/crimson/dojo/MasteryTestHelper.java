package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MasteryTestHelper {
	private static final Minecraft CLIENT = Minecraft.getInstance();
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
		if (state.is(Blocks.LIME_WOOL)) {
			blockOrder.add(pos);
			//add lifetime of a block to the time to get time when block expires
			// work out how long it will take between the player firing and arrow hitting the block and to subtract from time
			long travelTime = (long) (CLIENT.player.position().distanceTo(pos.getCenter()) * 1000 / 60); //an arrow speed is about 60 blocks a second from a full draw
			endTimes.put(pos, System.currentTimeMillis() + BLOCK_LIFE_TIME - DojoManager.ping - travelTime);
		}
		if (state.isAir()) {
			blockOrder.remove(pos);
			endTimes.removeLong(pos);
		}
	}

	protected static void extractRendering(PrimitiveCollector collector) {
		//render connecting lines
		if (!blockOrder.isEmpty()) {
			collector.submitLineFromCursor(blockOrder.getFirst().getCenter(), Color.LIGHT_GRAY.getColorComponents(new float[]{0, 0, 0}), 1f, 2);
		}
		if (blockOrder.size() >= 2) {
			collector.submitLinesFromPoints(new Vec3[]{blockOrder.get(0).getCenter(), blockOrder.get(1).getCenter()}, Color.LIGHT_GRAY.getColorComponents(new float[]{0, 0, 0}), 1, 2, false);
		}

		//render times
		long currentTime = System.currentTimeMillis();
		for (BlockPos pos : blockOrder) {
			long blockEndTime = endTimes.getLong(pos);
			float secondsTime = Math.max((blockEndTime - currentTime) / 1000f, 0);
			collector.submitText(Component.literal(FORMATTER.format(secondsTime)), pos.offset(0, 1, 0).getCenter(), 3, true);
		}
	}
}
