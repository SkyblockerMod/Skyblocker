package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.tictactoe.BoardIndex;
import de.hysky.skyblocker.utils.tictactoe.TicTacToeUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;

/**
 * Thanks to Danker for a reference implementation!
 */
public class TicTacToe extends DungeonPuzzle {
	private static final Logger LOGGER = LoggerFactory.getLogger(TicTacToe.class);
	private static final float[] GREEN_COLOR_COMPONENTS = { 0.0F, 1.0F, 0.0F };
	@SuppressWarnings("unused")
	private static final TicTacToe INSTANCE = new TicTacToe();
	private static @Nullable AABB nextBestMoveToMake = null;

	private TicTacToe() {
		super("tic-tac-toe", "tic-tac-toe-1");
	}

	@Init
	public static void init() {
	}

	@Override
	public void tick(Minecraft client) {
		if (!shouldSolve()) {
			return;
		}

		nextBestMoveToMake = null;

		if (client.level == null || client.player == null || !Utils.isInDungeons()) return;

		//Search within 21 blocks for item frames that contain maps
		AABB searchBox = new AABB(client.player.getX() - 21, client.player.getY() - 21, client.player.getZ() - 21, client.player.getX() + 21, client.player.getY() + 21, client.player.getZ() + 21);
		List<ItemFrame> itemFramesThatHoldMaps = client.level.getEntitiesOfClass(ItemFrame.class, searchBox, ItemFrame::hasFramedMap);

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

				double nextX = 8;
				double nextY = 72 - bestMove.row();
				double nextZ = 17 - bestMove.column();

				//noinspection DataFlowIssue - same as above, room is not null and matched
				BlockPos nextPos = DungeonManager.getCurrentRoom().relativeToActual(BlockPos.containing(nextX, nextY, nextZ));
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
