package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.tictactoe.TicTacToeUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Thanks to Danker for a reference implementation!
 */
public class TicTacToe extends DungeonPuzzle {
	private static final Logger LOGGER = LoggerFactory.getLogger(TicTacToe.class);
	private static final float[] RED_COLOR_COMPONENTS = {1.0F, 0.0F, 0.0F};
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
			//Only attempt to solve if its the player's turn
			if (itemFramesThatHoldMaps.size() != 9 && itemFramesThatHoldMaps.size() % 2 == 1) {
				char[][] board = new char[3][3];
				BlockPos leftmostRow = null;
				int sign = 1;
				char facing = 'X';

				for (ItemFrameEntity itemFrame : itemFramesThatHoldMaps) {
					MapState mapState = client.world.getMapState(FilledMapItem.getMapName(itemFrame.getMapId().getAsInt()));

					if (mapState == null) continue;

					int column = 0, row;
					sign = 1;

					//Find position of the item frame relative to where it is on the tic tac toe board
					if (itemFrame.getHorizontalFacing() == Direction.SOUTH || itemFrame.getHorizontalFacing() == Direction.WEST) sign = -1;
					BlockPos itemFramePos = BlockPos.ofFloored(itemFrame.getX(), itemFrame.getY(), itemFrame.getZ());

					for (int i = 2; i >= 0; i--) {
						int realI = i * sign;
						BlockPos blockPos = itemFramePos;

						if (itemFrame.getX() % 0.5 == 0) {
							blockPos = itemFramePos.add(realI, 0, 0);
						} else if (itemFrame.getZ() % 0.5 == 0) {
							blockPos = itemFramePos.add(0, 0, realI);
							facing = 'Z';
						}

						Block block = client.world.getBlockState(blockPos).getBlock();
						if (block == Blocks.AIR || block == Blocks.STONE_BUTTON) {
							leftmostRow = blockPos;
							column = i;

							break;
						}
					}

					//Determine the row of the item frame
					if (itemFrame.getY() == 72.5) {
						row = 0;
					} else if (itemFrame.getY() == 71.5) {
						row = 1;
					} else if (itemFrame.getY() == 70.5) {
						row = 2;
					} else {
						continue;
					}


					//Get the color of the middle pixel of the map which determines whether its X or O
					int middleColor = mapState.colors[8256] & 255;

					if (middleColor == 114) {
						board[row][column] = 'X';
					} else if (middleColor == 33) {
						board[row][column] = 'O';
					}

					int bestMove = TicTacToeUtils.getBestMove(board) - 1;

					if (leftmostRow != null) {
						double drawX = facing == 'X' ? leftmostRow.getX() - sign * (bestMove % 3) : leftmostRow.getX();
						double drawY = 72 - (double) (bestMove / 3);
						double drawZ = facing == 'Z' ? leftmostRow.getZ() - sign * (bestMove % 3) : leftmostRow.getZ();

						nextBestMoveToMake = new Box(drawX, drawY, drawZ, drawX + 1, drawY + 1, drawZ + 1);
					}
				}
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
