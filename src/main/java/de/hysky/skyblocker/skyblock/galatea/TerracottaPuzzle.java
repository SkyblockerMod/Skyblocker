package de.hysky.skyblocker.skyblock.galatea;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;

public class TerracottaPuzzle {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final BlockPos WALL_TOP_LEFT = new BlockPos(-633, 66, 85);
	private static final BlockPos WALL_BOTTOM_RIGHT = new BlockPos(-640, 65, 85);
	private static final BlockPos FLOOR_TOP_LEFT = new BlockPos(-633, 59, 76);
	private static final BlockPos FLOOR_BOTTOM_LEFT = new BlockPos(-633, 59, 75);
	/**
	 * Mappings of the direction of the terracotta blocks on the wall to the ones on the floor.
	 */
	private static final Map<Direction, Direction> DIRECTION_MAPPINGS = Map.ofEntries(
			Map.entry(Direction.NORTH, Direction.WEST),
			Map.entry(Direction.EAST, Direction.SOUTH),
			Map.entry(Direction.SOUTH, Direction.EAST),
			Map.entry(Direction.WEST, Direction.NORTH)
			);

	@Init
	public static void init() {
		WorldRenderExtractionCallback.EVENT.register(TerracottaPuzzle::extractRendering);
	}

	/**
	 * Calculates the direction of each terracotta block on the wall.
	 */
	private static List<Direction> solve() {
		if (CLIENT.level == null) return List.of();
		//Two row layout where the top indices are even and the bottom indices are odd
		//For example, index 0 is the top left block, 1 is the bottom left, and so on...
		List<Direction> solutions = new ArrayList<>();

		for (int x = WALL_TOP_LEFT.getX(); x >= WALL_BOTTOM_RIGHT.getX(); x--) {
			for (int y = WALL_TOP_LEFT.getY(); y >= WALL_BOTTOM_RIGHT.getY(); y--) {
				//The Z is same for top & bottom
				BlockPos pos = new BlockPos(x, y, WALL_TOP_LEFT.getZ());
				BlockState state = CLIENT.level.getBlockState(pos);

				//This should be the case but we'll be extra safe
				if (!state.is(Blocks.ORANGE_GLAZED_TERRACOTTA) || !state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) continue;

				Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

				solutions.add(DIRECTION_MAPPINGS.get(facing));
			}
		}

		return solutions;
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (!SkyblockerConfigManager.get().foraging.galatea.solveForestTemplePuzzle || !Utils.isInGalatea() || Utils.getArea() != Area.Galatea.FOREST_TEMPLE || CLIENT.level == null) return;

		List<Direction> solutions = solve();

		for (int i = 0; i < solutions.size(); i++) {
			Direction correctDirection = solutions.get(i);
			BlockPos pos = getFloorBlock(i);
			BlockState state = CLIENT.level.getBlockState(pos);

			//This should be the case but we'll be extra safe
			if (!state.is(Blocks.ORANGE_GLAZED_TERRACOTTA) || !state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) continue;

			Direction floorDirection = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

			//Simulate rotating either way to find the most efficient path
			int clockwiseRotations = 0;
			Direction lastClockwiseRotation = floorDirection;
			int counterclockwiseRotations = 0;
			Direction lastCounterclockwiseRotation = floorDirection;

			//How many rotations are needed to solve this block
			int rotationsNeeded = 0;

			for (int j = 0; j < 4; j++) {
				if (floorDirection == correctDirection) break;

				clockwiseRotations++;
				lastClockwiseRotation = lastClockwiseRotation.getClockWise();

				if (lastClockwiseRotation == correctDirection) {
					rotationsNeeded = clockwiseRotations;
					break;
				}

				counterclockwiseRotations++;
				lastCounterclockwiseRotation = lastCounterclockwiseRotation.getCounterClockWise();

				if (lastCounterclockwiseRotation == correctDirection) {
					//Invert counterclockwise rotations since you perform them by left clicking (I imagine most will rotate the blocks with right click)
					rotationsNeeded = -counterclockwiseRotations;
					break;
				}
			}

			//If the block needs to be rotated
			if (clockwiseRotations != 0) {
				collector.submitText(Component.literal(String.valueOf(rotationsNeeded)).withStyle(ChatFormatting.DARK_BLUE), Vec3.atCenterOf(pos).add(0, 1, 0), false);
			}
		}
	}

	private static BlockPos getFloorBlock(int index) {
		//Find how much the index is offset from the start of the row
		int rowIndex = index / 2;

		//If the index is even we're on the top row, odd if bottom row
		return index % 2 == 0 ? FLOOR_TOP_LEFT.offset(-rowIndex, 0, 0) : FLOOR_BOTTOM_LEFT.offset(-rowIndex, 0, 0);
	}
}
