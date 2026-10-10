package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.tictactoe.BoardIndex;
import de.hysky.skyblocker.utils.tictactoe.TicTacToeUtils;

/**
 * Thanks to Danker for a reference implementation!
 */
public class TicTacToe extends DungeonPuzzle {
	private static final Logger LOGGER = LoggerFactory.getLogger(TicTacToe.class);
	private static final float[] GREEN_COLOR_COMPONENTS = { 0.0f, 1.0f, 0.0f };
	private static final TicTacToe INSTANCE = new TicTacToe();
	private static @Nullable AABB nextBestMoveToMake = null;
	private static int bestMoveRow = -1;
	private static int bestMoveColumn = -1;
	private static int placedMoves = -1;
	private static int lastLoggedFrameCount = -1;

	private TicTacToe() {
		super("tic-tac-toe", "tic-tac-toe-1");
	}

	@Init
	public static void init() {
		AttackEntityCallback.EVENT.register((_, _, _, entity, _) -> onFrameInteract(entity));
		UseEntityCallback.EVENT.register((_, _, _, entity, _) -> onFrameInteract(entity));
		UseBlockCallback.EVENT.register((_, _, _, blockHitResult) -> onBoardBlockInteract(blockHitResult.getBlockPos()));
	}

	private static boolean shouldBlock() {
		if (!INSTANCE.shouldSolve() || !SkyblockerConfigManager.get().dungeons.puzzleSolvers.blockIncorrectClicks) {
			return false;
		}
		//shouldSolve stays armed long after you left or failed the puzzle, never touch clicks outside the actual room
		Room room = DungeonManager.getCurrentRoom();
		return room != null && room.isMatched() && "tic-tac-toe-1".equals(room.getName());
	}

	private static InteractionResult onFrameInteract(Entity entity) {
		if (!shouldBlock() || !(entity instanceof ItemFrame frame)) return InteractionResult.PASS;
		Room room = DungeonManager.getCurrentRoom();
		//room direction is null until matching finishes, actualToRelative NPEs on it. this used to crash the whole game
		if (room == null || room.getDirection() == null) return InteractionResult.PASS;

		//same row/column math the board scan uses, the blocker always agrees with the green box
		BlockPos relative = room.actualToRelative(frame.blockPosition());
		int row = 72 - relative.getY();
		int column = 17 - relative.getZ();
		if (row < 0 || row > 2 || column < 0 || column > 2) return InteractionResult.PASS;

		return verdict(row, column, "Frame click at relative " + relative.toShortString());
	}

	private static InteractionResult onBoardBlockInteract(BlockPos clickedPos) {
		if (!shouldBlock()) return InteractionResult.PASS;
		Room room = DungeonManager.getCurrentRoom();
		//room direction is null until matching finishes, actualToRelative NPEs on it. this used to crash the whole game
		if (room == null || room.getDirection() == null) return InteractionResult.PASS;

		//empty cells are buttons, placed moves are item frames: the board's input is block clicks. yes, really
		BlockPos relative = room.actualToRelative(clickedPos);
		int row = 72 - relative.getY();
		int column = 17 - relative.getZ();
		if (relative.getX() < 7 || relative.getX() > 9 || row < 0 || row > 2 || column < 0 || column > 2) return InteractionResult.PASS;

		return verdict(row, column, "Block click at relative " + relative.toShortString());
	}

	private static InteractionResult verdict(int row, int column, String description) {
		boolean blocked;
		String message = null;
		if (placedMoves < 0 || placedMoves >= 9) {
			blocked = false;
		} else if (bestMoveRow != -1) {
			//your turn, only the best move survives
			blocked = row != bestMoveRow || column != bestMoveColumn;
			message = "skyblocker.dungeons.blockers.wrongBox";
		} else {
			//AI's turn, any click places your move early and that is how free puzzles get failed
			blocked = true;
			message = "skyblocker.dungeons.blockers.notYourTurn";
		}
		if (Debug.debugEnabled()) LOGGER.info("[Skyblocker Tic Tac Toe] {} maps to cell ({}, {}), best move is ({}, {}): {}", description, row, column, bestMoveRow, bestMoveColumn, blocked ? "blocked" : "allowed");
		if (blocked) Utils.sendBlockedClickMessage(message);
		return blocked ? InteractionResult.FAIL : InteractionResult.PASS;
	}

	@Override
	public void tick(Minecraft client) {
		if (!shouldSolve()) {
			return;
		}

		nextBestMoveToMake = null;
		bestMoveRow = -1;
		bestMoveColumn = -1;
		placedMoves = -1;

		if (client.level == null || client.player == null || !Utils.isInDungeons()) return;

		//Search within 21 blocks for item frames that contain maps
		AABB searchBox = new AABB(client.player.getX() - 21, client.player.getY() - 21, client.player.getZ() - 21, client.player.getX() + 21, client.player.getY() + 21, client.player.getZ() + 21);
		List<ItemFrame> itemFramesThatHoldMaps = client.level.getEntitiesOfClass(ItemFrame.class, searchBox, ItemFrame::hasFramedMap);
		placedMoves = itemFramesThatHoldMaps.size();

		if (Debug.debugEnabled() && itemFramesThatHoldMaps.size() != lastLoggedFrameCount) {
			lastLoggedFrameCount = itemFramesThatHoldMaps.size();
			LOGGER.info("[Skyblocker Tic Tac Toe] Item frames with maps in range: {}, shouldSolve: {}", itemFramesThatHoldMaps.size(), INSTANCE.shouldSolve());
		}

		try {
			//Only attempt to solve if the puzzle wasn't just completed and if its the player's turn
			//The low bit will always be set to 1 on odd numbers
			if (itemFramesThatHoldMaps.size() != 9 && (itemFramesThatHoldMaps.size() & 1) == 1) {
				char[][] board = new char[3][3];

				for (ItemFrame itemFrame : itemFramesThatHoldMaps) {
					MapItemSavedData mapState = client.level.getMapData(itemFrame.getFramedMapId(itemFrame.getItem()));

					if (mapState == null) continue;

					//noinspection DataFlowIssue - the room must not be null and must be matched
					BlockPos relative = DungeonManager.getCurrentRoom().actualToRelative(itemFrame.blockPosition());

					//Determine the row -- 72 = top, 71 = middle, 70 = bottom
					int y = relative.getY();
					int row = switch (y) {
						case 72 -> 0;
						case 71 -> 1;
						case 70 -> 2;

						default -> -1;
					};

					//Determine the column - 17 = first, 16 = second, 15 = third
					int z = relative.getZ();
					int column = switch (z) {
						case 17 -> 0;
						case 16 -> 1;
						case 15 -> 2;

						default -> -1;
					};

					if (row == -1 || column == -1) continue;

					//Get the color of the middle pixel of the map which determines whether its X or O
					int middleColor = mapState.colors[8256] & 0xFF;

					if (middleColor == 114) {
						board[row][column] = 'X';
					} else if (middleColor == 33) {
						board[row][column] = 'O';
					}
				}

				BoardIndex bestMove = TicTacToeUtils.getBestMove(board);
				bestMoveRow = bestMove.row();
				bestMoveColumn = bestMove.column();

				double nextY = 72 - bestMoveRow;
				double nextZ = 17 - bestMoveColumn;

				//noinspection DataFlowIssue - same as above, room is not null and matched
				BlockPos nextPos = DungeonManager.getCurrentRoom().relativeToActual(BlockPos.containing(8, nextY, nextZ));
				nextBestMoveToMake = RenderHelper.getBlockBoundingBox(client.level, nextPos);
			}
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Tic Tac Toe] Encountered an exception while determining a tic tac toe solution!", e);
		}
	}

	@Override
	public void extractRendering(PrimitiveCollector collector) {
		try {
			if (SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveTicTacToe && nextBestMoveToMake != null) {
				collector.submitFilledBox(nextBestMoveToMake, GREEN_COLOR_COMPONENTS, 0.5f, false);
				collector.submitOutlinedBox(nextBestMoveToMake, GREEN_COLOR_COMPONENTS, 5f, false);
			}
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Tic Tac Toe] Encountered an exception while rendering the tic tac toe solution!", e);
		}
	}
}
