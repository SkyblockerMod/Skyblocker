package de.hysky.skyblocker.skyblock.dungeon.puzzle.boulder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.WorldEvents;
import de.hysky.skyblocker.skyblock.dungeon.puzzle.DungeonPuzzle;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;

public class Boulder extends DungeonPuzzle {
	//A box slides for a moment after a push and chunks load in late, so a board that reads as unsolvable
	//must persist for this many ticks before the solver actually declares the puzzle locked
	private static final int NO_SOLUTION_GRACE_TICKS = 10;
	protected static final int BASE_Y = 65;
	private static final float[] RED_COLOR_COMPONENTS = ColorUtils.getFloatComponents(DyeColor.RED);
	private static final float[] ORANGE_COLOR_COMPONENTS = ColorUtils.getFloatComponents(DyeColor.ORANGE);
	private static final float[] YELLOW_COLOR_COMPONENTS = ColorUtils.getFloatComponents(DyeColor.YELLOW);
	@SuppressWarnings("unused")
	private static final Boulder INSTANCE = new Boulder();
	//The fastest solution's player route; alternates render in yellow but only their unique segments so
	//routes that share parts don't draw the same line twice
	static final List<Vec3[]> SOLUTION_LINES = new ArrayList<>();
	static final List<Vec3[]> ALTERNATE_SEGMENTS = new ArrayList<>();
	static @Nullable AABB boundingBox;
	//The button the current plan wants pressed next, clicks on the other buttons get blocked
	static @Nullable BlockPos nextButton;
	//Buttons already pressed, by anyone. The plan's next button is the first unpressed one along the route,
	//without this the scan would keep finding the first button after it was pressed
	private static final LongSet pressedButtons = new LongOpenHashSet();
	private static @Nullable Integer lastBoardKey;
	private static int noSolutionTicks;

	private Boulder() {
		super("boulder", "boxes-room");
	}

	@Init
	public static void init() {
		UseBlockCallback.EVENT.register(Boulder::onButtonUse);
		AttackBlockCallback.EVENT.register(Boulder::onButtonAttack);
		WorldEvents.BLOCK_STATE_UPDATE.register(Boulder::onBlockUpdate);
	}

	//Breaking a box button would fail the puzzle, there is never a legitimate reason to attack one
	private static InteractionResult onButtonAttack(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
		if (!isPuzzleButton(pos)) return InteractionResult.PASS;

		Utils.sendBlockedClickMessage("skyblocker.dungeons.blockers.breakBlocked");
		return InteractionResult.FAIL;
	}

	private static InteractionResult onButtonUse(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
		BlockPos pos = hitResult.getBlockPos();
		if (!isPuzzleButton(pos)) return InteractionResult.PASS;
		if (pos.equals(nextButton)) {
			//Count our own press immediately so the next button unlocks before any block update arrives
			pressedButtons.add(pos.asLong());
			return InteractionResult.PASS;
		}

		Utils.sendBlockedClickMessage("skyblocker.dungeons.blockers.wrongButton");
		return InteractionResult.FAIL;
	}

	//A stone button flipping to its powered state is a press, by any player whose presses reach us,
	//which keeps the plan in sync even when someone else presses the buttons
	private static void onBlockUpdate(BlockPos pos, @Nullable BlockState oldState, BlockState newState) {
		if (!isInPuzzleRoom()) return;
		if (newState.getBlock() == Blocks.STONE_BUTTON && newState.getValue(BlockStateProperties.POWERED)) {
			pressedButtons.add(pos.asLong());
		}
	}

	private static boolean isPuzzleButton(BlockPos pos) {
		if (Utils.isBlockerBypassed() || !shouldBlockClicks()) return false;
		Level world = Minecraft.getInstance().level;
		return world != null && world.getBlockState(pos).getBlock() == Blocks.STONE_BUTTON;
	}

	private static boolean shouldBlockClicks() {
		return isInPuzzleRoom() && SkyblockerConfigManager.get().dungeons.puzzleSolvers.blockIncorrectClicks;
	}

	private static boolean isInPuzzleRoom() {
		if (!INSTANCE.shouldSolve() || !SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveBoulder) {
			return false;
		}
		//shouldSolve stays armed long after you left or failed the puzzle, never touch clicks outside the actual room
		Room room = DungeonManager.getCurrentRoom();
		return room != null && room.isMatched() && "boxes-room".equals(room.getName());
	}

	@Override
	public void tick(Minecraft client) {

		if (!isInPuzzleRoom() || client.level == null) {
			return;
		}

		Room room = DungeonManager.getCurrentRoom();
		if (room == null) {
			return;
		}

		@SuppressWarnings("unused") // Kept for documentation purposes
		BlockPos chestPos = new BlockPos(15, BASE_Y, 29);
		BlockPos start = new BlockPos(25, BASE_Y, 25);
		BlockPos end = new BlockPos(5, BASE_Y, 8);
		// Create a BoulderBoard representing the puzzle's grid
		BoulderBoard board = new BoulderBoard(7, 7);

		// Populate the BoulderBoard grid with BoulderObjects based on block types in the room
		int column = 0;
		for (int z = start.getZ(); z > end.getZ(); z--) {
			int row = 0;
			for (int x = start.getX(); x > end.getX(); x--) {
				if (Math.abs(start.getX() - x) % 3 == 1 && Math.abs(start.getZ() - z) % 3 == 1) {
					String blockType = getBlockType(client.level, room, x, BASE_Y, z);
					board.placeObject(column, row, new BoulderObject(x, BASE_Y, z, blockType));
					row++;
				}
			}
			if (row == board.getWidth()) {
				column++;
			}
		}

		//The board only changes when a box moves or a chunk loads in, so the search only reruns on those
		//moments; the next button is still re-derived every tick so a fresh press unlocks the next one at once
		int boardKey = Arrays.deepHashCode(board.getBoardCharArray());
		if (!Objects.equals(lastBoardKey, boardKey)) {
			lastBoardKey = boardKey;
			noSolutionTicks = 0;
			solveBoard(client.level, room, board);
		}

		if (SOLUTION_LINES.isEmpty()) {
			if (++noSolutionTicks >= NO_SOLUTION_GRACE_TICKS) {
				// If no solution is found, display a title message and reset the puzzle
				Title title = new Title("skyblocker.dungeons.puzzle.boulder.noSolution", ChatFormatting.GREEN);
				TitleContainer.addTitleAndPlaySound(title, 15);
				reset();
			}
			return;
		}

		//The button to press next is the first unpressed one along the fastest route, that is the one to follow
		nextButton = findButtonAlongPath(client.level, SOLUTION_LINES.getFirst());
		if (nextButton == null) {
			// No unpressed button is found along the path, the puzzle is solved
			reset();
			return;
		}
		boundingBox = RenderHelper.getBlockBoundingBox(client.level, nextButton);
	}

	//Recomputes the solution lines from the given board, the next button is derived from the fastest route every tick
	private void solveBoard(ClientLevel level, Room room, BoulderBoard board) {
		SOLUTION_LINES.clear();
		ALTERNATE_SEGMENTS.clear();
		boundingBox = null;
		nextButton = null;

		List<List<int[]>> solutions = BoulderSolver.aStarSolve(getInitialStates(board));
		if (solutions.isEmpty()) {
			return;
		}

		//Segments of the routes already drawn, packed cell pairs so alternates only show their unique parts
		LongSet drawnSegments = new LongOpenHashSet();
		for (int i = 0; i < solutions.size(); i++) {
			List<int[]> solution = solutions.get(i);

			if (i == 0) {
				SOLUTION_LINES.add(toLinePoints(room, board, solution));
				markSegments(solution, drawnSegments);
			} else {
				collectUniqueSegments(room, board, solution, drawnSegments);
			}
		}
	}

	private static void markSegments(List<int[]> solution, LongSet drawnSegments) {
		for (int i = 0; i < solution.size() - 1; i++) {
			drawnSegments.add(segmentKey(solution.get(i), solution.get(i + 1)));
		}
	}

	//Appends the route's segments that no earlier solution already covers
	private static void collectUniqueSegments(Room room, BoulderBoard board, List<int[]> solution, LongSet drawnSegments) {
		for (int i = 0; i < solution.size() - 1; i++) {
			long key = segmentKey(solution.get(i), solution.get(i + 1));
			if (drawnSegments.add(key)) {
				ALTERNATE_SEGMENTS.add(new Vec3[]{cellCenter(room, board, solution.get(i)), cellCenter(room, board, solution.get(i + 1))});
			}
		}
	}

	//Grid coords are 0..6, so a normalized cell pair packs into a long
	private static long segmentKey(int[] a, int[] b) {
		return (long) Math.min(a[0], b[0]) << 9 | Math.min(a[1], b[1]) << 6 | Math.max(a[0], b[0]) << 3 | Math.max(a[1], b[1]);
	}

	private static Vec3 cellCenter(Room room, BoulderBoard board, int[] cell) {
		return Vec3.atCenterOf(room.relativeToActual(board.getObject3DPosition(cell[0], cell[1])));
	}

	private static Vec3[] toLinePoints(Room room, BoulderBoard board, List<int[]> solution) {
		Vec3[] points = new Vec3[solution.size()];
		for (int i = 0; i < points.length; i++) {
			points[i] = cellCenter(room, board, solution.get(i));
		}
		return points;
	}

	private static @Nullable BlockPos findButtonAlongPath(ClientLevel level, Vec3[] points) {
		for (int i = 0; i < points.length - 1; i++) {
			BlockPos button = checkForButtonBlocksOnLine(level, points[i], points[i + 1]);
			if (button != null) {
				return button;
			}
		}
		return null;
	}

	private static List<BoulderSolver.GameState> getInitialStates(BoulderBoard board) {
		char[][] boardArray = board.getBoardCharArray();
		return List.of(
				new BoulderSolver.GameState(boardArray, board.getHeight() - 1, 0, 0),
				new BoulderSolver.GameState(boardArray, board.getHeight() - 1, 1, 0),
				new BoulderSolver.GameState(boardArray, board.getHeight() - 1, 2, 0),
				new BoulderSolver.GameState(boardArray, board.getHeight() - 1, 3, 0),
				new BoulderSolver.GameState(boardArray, board.getHeight() - 1, 4, 0),
				new BoulderSolver.GameState(boardArray, board.getHeight() - 1, 5, 0),
				new BoulderSolver.GameState(boardArray, board.getHeight() - 1, 6, 0)
		);
	}

	/**
	 * Retrieves the type of block at the specified position in the world.
	 * If the block is Birch or Jungle plank, it will return "B"; otherwise, it will return ".".
	 *
	 * @param world The client world.
	 * @param room  The room the puzzle takes place in.
	 * @param x     The x-coordinate of the block.
	 * @param y     The y-coordinate of the block.
	 * @param z     The z-coordinate of the block.
	 * @return The type of block found at the given position in the world.
	 */
	public static String getBlockType(ClientLevel world, Room room, int x, int y, int z) {
		Block block = world.getBlockState(room.relativeToActual(new BlockPos(x, y, z))).getBlock();
		return (block == Blocks.BIRCH_PLANKS || block == Blocks.JUNGLE_PLANKS) ? "B" : ".";
	}

	/**
	 * Checks for blocks along the line between two points in the world.
	 * Returns the position of a block if it found a button on the line, if any.
	 * Buttons that were already pressed are skipped so the plan always points at the next one.
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

			if (block == Blocks.STONE_BUTTON && !pressedButtons.contains(blockPos.asLong())) {
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

		if (!SOLUTION_LINES.isEmpty()) {
			Vec3[] points = SOLUTION_LINES.getFirst();
			for (int j = 0; j < points.length - 1; j++) {
				collector.submitLinesFromPoints(new Vec3[]{points[j], points[j + 1]}, ORANGE_COLOR_COMPONENTS, alpha, lineWidth, true);
			}
			for (Vec3[] segment : ALTERNATE_SEGMENTS) {
				collector.submitLinesFromPoints(segment, YELLOW_COLOR_COMPONENTS, alpha, lineWidth, true);
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
		SOLUTION_LINES.clear();
		ALTERNATE_SEGMENTS.clear();
		boundingBox = null;
		nextButton = null;
		pressedButtons.clear();
		lastBoardKey = null;
		noSolutionTicks = 0;
	}
}
