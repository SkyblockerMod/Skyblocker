package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.tictactoe.TicTacToeUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Thanks to Danker for a reference implementation!
 */
public class TicTacToe extends DungeonPuzzle {
	private static final Logger LOGGER = LoggerFactory.getLogger(TicTacToe.class);
	private static final float[] RED_COLOR_COMPONENTS = { 1.0F, 0.0F, 0.0F };
	@SuppressWarnings("unused")
	private static final TicTacToe INSTANCE = new TicTacToe();
	private static Box nextBestMoveToMake = null;

	private TicTacToe() {
		super("tic-tac-toe", "tic-tac-toe-1");
	}

	public static void init() {
	}

	@Override
	public void tick(MinecraftClient client) {
		if (!shouldSolve()) {
			return;
		}

		nextBestMoveToMake = null;

		if (client.world == null || client.player == null || !Utils.isInDungeons()) return;

		//Search within 21 blocks for item frames that contain maps
		Box searchBox = new Box(client.player.getX() - 21, client.player.getY() - 21, client.player.getZ() - 21, client.player.getX() + 21, client.player.getY() + 21, client.player.getZ() + 21);
		List<ItemFrameEntity> itemFramesThatHoldMaps = client.world.getEntitiesByClass(ItemFrameEntity.class, searchBox, ItemFrameEntity::containsMap);

		try {
			//Only attempt to solve if the puzzle wasn't just completed and if its the player's turn
			if (itemFramesThatHoldMaps.size() != 9 && itemFramesThatHoldMaps.size() % 2 == 1) {
				char[][] board = new char[3][3];

				for (ItemFrameEntity itemFrame : itemFramesThatHoldMaps) {
					MapState mapState = client.world.getMapState(FilledMapItem.getMapName(itemFrame.getMapId().getAsInt()));

					if (mapState == null) continue;

					//Surely if we pass shouldSolve then the room should be matched right
					BlockPos relative = DungeonManager.getCurrentRoom().actualToRelative(itemFrame.getBlockPos());

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
					int middleColor = mapState.colors[8256] & 255;

					if (middleColor == 114) {
						board[row][column] = 'X';
					} else if (middleColor == 33) {
						board[row][column] = 'O';
					}
				}

				int bestMove = TicTacToeUtils.getBestMove(board) - 1;

				double nextX = 8;
				double nextY = 72 - (double) (bestMove / 3);
				double nextZ = 17 - (bestMove % 3);

				BlockPos nextPos = DungeonManager.getCurrentRoom().relativeToActual(BlockPos.ofFloored(nextX, nextY, nextZ));
				nextBestMoveToMake = new Box(nextPos);
			}
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Tic Tac Toe] Encountered an exception while determining a tic tac toe solution!", e);
		}
	}

	@Override
	public void render(WorldRenderContext context) {
		try {
			if (SkyblockerConfigManager.get().locations.dungeons.solveTicTacToe && nextBestMoveToMake != null) {
				RenderHelper.renderOutline(context, nextBestMoveToMake, RED_COLOR_COMPONENTS, 5, false);
			}
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Tic Tac Toe] Encountered an exception while rendering the tic tac toe solution!", e);
		}
	}
}
