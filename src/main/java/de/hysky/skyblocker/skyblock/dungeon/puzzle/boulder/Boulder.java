package de.hysky.skyblocker.skyblock.dungeon.puzzle.boulder;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Gatherers;

import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.puzzle.DungeonPuzzle;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;

public class Boulder extends DungeonPuzzle {
	@SuppressWarnings("unused")
	private static final Boulder INSTANCE = new Boulder();
	private static final float[] RED_COLOR_COMPONENTS = ColorUtils.getFloatComponents(DyeColor.RED);
	private static final float[] ORANGE_COLOR_COMPONENTS = ColorUtils.getFloatComponents(DyeColor.ORANGE);
	protected static final int BASE_Y = 65;
	@SuppressWarnings("unused")
	public static final BlockPos CHEST_POS = new BlockPos(15, BASE_Y, 29); // Kept for documentation purposes
	public static final BlockPos START = new BlockPos(25, BASE_Y, 25);
	@SuppressWarnings("unused")
	public static final BlockPos END = new BlockPos(5, BASE_Y, 8); // Kept for documentation purposes
	static Vec3 @Nullable [] linePoints;
	static @Nullable AABB boundingBox;

	private Boulder() {
		super("boulder", "boxes-room");
	}

	@Init
	public static void init() {}

	@Override
	public void tick(Minecraft client) {

		if (!shouldSolve() || !SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveBoulder || client.level == null || !DungeonManager.isCurrentRoomMatched()) {
			return;
		}

		Room room = DungeonManager.getCurrentRoom();
		if (room == null) {
			return;
		}

		// Create a BoulderBoard representing the puzzle's grid
		BoulderBoard board = new BoulderBoard(7, 7);

		// Populate the BoulderBoard grid with BoulderObjects based on block types in the room
		for (int row = 0; row < board.getRows(); row++) {
			for (int col = 0; col < board.getCols(); col++) {
				board.placeObject(row, col,
						getBlockType(client.level, room, gridToRelative(row, col))
				);
			}
		}

		CompletableFuture.supplyAsync(() -> {
			// Generate initial game states for the A* solver
			List<BoulderSolver.GameState> initialStates = getInitialStates(board);

			// Solve the puzzle using the A* algorithm
			return BoulderSolver.aStarSolve(initialStates);
		}, SkyblockerMod.VIRTUAL_THREAD_EXECUTOR).thenAcceptAsync(solution -> {
			if (solution == null) {
				// If no solution is found, display a title message and reset the puzzle
				Title title = new Title("skyblocker.dungeons.puzzle.boulder.noSolution", ChatFormatting.GREEN);
				TitleContainer.addTitleAndPlaySound(title, 15);
				reset();
				return;
			}

			linePoints = new Vec3[solution.size()];
			int index = 0;
			// Convert solution coordinates to Vec3d points for rendering
			for (int[] coord : solution) {
				int x = coord[0];
				int y = coord[1];
				// Convert relative coordinates to actual coordinates
				linePoints[index++] = Vec3.atCenterOf(room.relativeToActual(gridToRelative(x, y).relative(Direction.Axis.Y, -1)));
			}

			if (linePoints == null || linePoints.length == 0) return;
			// Check for buttons along the path of the solution
			Arrays.stream(linePoints)
					.gather(Gatherers.windowSliding(2))
					.map(pair -> checkForButtonBlocksOnLine(client.level, pair.getFirst(), pair.getLast()))
					.filter(Objects::nonNull)
					.findFirst()
					.map(button -> RenderHelper.getBlockBoundingBox(client.level, button))
					.ifPresentOrElse(buttonBox -> boundingBox = buttonBox, this::reset); // If no button is found along the path the puzzle is solved; reset the puzzle
		}, client);
	}

	static BlockPos gridToRelative(int row, int col) {
		return START.mutable().move(-1, 0, -1).move(-3 * col, 0, -3 * row);
	}

	@VisibleForTesting
	static List<BoulderSolver.GameState> getInitialStates(BoulderBoard board) {
		char[][] boardArray = board.getBoardCharArray();
		return List.of(
				new BoulderSolver.GameState(boardArray, board.getRows() - 1, 0, 0),
				new BoulderSolver.GameState(boardArray, board.getRows() - 1, 1, 0),
				new BoulderSolver.GameState(boardArray, board.getRows() - 1, 2, 0),
				new BoulderSolver.GameState(boardArray, board.getRows() - 1, 3, 0),
				new BoulderSolver.GameState(boardArray, board.getRows() - 1, 4, 0),
				new BoulderSolver.GameState(boardArray, board.getRows() - 1, 5, 0),
				new BoulderSolver.GameState(boardArray, board.getRows() - 1, 6, 0)
		);
	}

	/**
	 * Retrieves the type of block at the specified position in the world.
	 * If the block is Birch or Jungle plank, it will return 'B'; otherwise, it will return '.'.
	 *
	 * @param world The client world.
	 * @param pos   The position of the block.
	 * @return The type of block at the specified position.
	 */
	public static char getBlockType(ClientLevel world, Room room, BlockPos pos) {
		Block block = world.getBlockState(room.relativeToActual(pos)).getBlock();
		return (block == Blocks.BIRCH_PLANKS || block == Blocks.JUNGLE_PLANKS) ? 'B' : '.';
	}

	/**
	 * Checks for blocks along the line between two points in the world.
	 * Returns the position of a block if it found a button on the line, if any.
	 *
	 * @param world   The client world.
	 * @param point1  The starting point of the line.
	 * @param point2  The ending point of the line.
	 * @return The position of the block found on the line, or null if no block is found.
	 */
	private static @Nullable BlockPos checkForButtonBlocksOnLine(ClientLevel world, Vec3 point1, Vec3 point2) {
		double x1 = point1.x();
		double y1 = point1.y() + 1;
		double z1 = point1.z();

		double x2 = point2.x();
		double y2 = point2.y() + 1;
		double z2 = point2.z();

		int steps = (int) Math.max(Math.abs(x2 - x1), Math.max(Math.abs(y2 - y1), Math.abs(z2 - z1)));

		double xStep = (x2 - x1) / steps;
		double yStep = (y2 - y1) / steps;
		double zStep = (z2 - z1) / steps;


		for (int step = 0; step <= steps; step++) {
			double currentX = x1 + step * xStep;
			double currentY = y1 + step * yStep;
			double currentZ = z1 + step * zStep;

			BlockPos blockPos = BlockPos.containing(currentX, currentY, currentZ);
			Block block = world.getBlockState(blockPos).getBlock();

			if (block == Blocks.STONE_BUTTON) {
				return blockPos;
			}

		}
		return null;
	}

	@Override
	public void extractRendering(PrimitiveCollector collector) {
		if (!shouldSolve() || !SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveBoulder || !DungeonManager.isCurrentRoomMatched())
			return;
		float alpha = 1.0f;
		float lineWidth = 5.0f;

		if (linePoints != null && linePoints.length > 0) {
			for (int i = 0; i < linePoints.length - 1; i++) {
				Vec3 startPoint = linePoints[i];
				Vec3 endPoint = linePoints[i + 1];
				collector.submitLinesFromPoints(new Vec3[]{startPoint, endPoint}, ORANGE_COLOR_COMPONENTS, alpha, lineWidth, true);
			}
			if (boundingBox != null) {
				collector.submitFilledBox(boundingBox, RED_COLOR_COMPONENTS, 0.5f, false);
				collector.submitOutlinedBox(boundingBox, RED_COLOR_COMPONENTS, 5f, false);
			}
		}
	}

	@Override
	public void reset() {
		super.reset();
		linePoints = null;
		boundingBox = null;
	}
}
