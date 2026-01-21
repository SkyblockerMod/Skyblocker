package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import com.google.common.primitives.Booleans;
import com.mojang.brigadier.Command;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class IceFill extends DungeonPuzzle {
	public static final IceFill INSTANCE = new IceFill();
	private static final float[] RED_COLOR_COMPONENTS = ColorUtils.getFloatComponents(DyeColor.RED);
	private static final BlockPos[] BOARD_ORIGINS = {
			new BlockPos(16, 70, 9),
			new BlockPos(17, 71, 16),
			new BlockPos(18, 72, 25)
	};
	private @Nullable CompletableFuture<Void> solve;
	private final boolean[][][] iceFillBoards = {new boolean[3][3], new boolean[5][5], new boolean[7][7]};
	private final List<List<Vector2ic>> iceFillPaths = new ArrayList<>(List.of(List.of(), List.of(), List.of()));

	private IceFill() {
		super("ice-fill", "ice-path");
	}

	@Init
	public static void init() {
		if (Debug.debugEnabled()) {
			ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("dungeons").then(literal("puzzle").then(literal(INSTANCE.puzzleName)
					.then(literal("printBoard1").executes(context -> {
						context.getSource().sendFeedback(Constants.PREFIX.get().append(boardToString(INSTANCE.iceFillBoards[0])));
						return Command.SINGLE_SUCCESS;
					})).then(literal("printBoard2").executes(context -> {
						context.getSource().sendFeedback(Constants.PREFIX.get().append(boardToString(INSTANCE.iceFillBoards[1])));
						return Command.SINGLE_SUCCESS;
					})).then(literal("printBoard3").executes(context -> {
						context.getSource().sendFeedback(Constants.PREFIX.get().append(boardToString(INSTANCE.iceFillBoards[2])));
						return Command.SINGLE_SUCCESS;
					})).then(literal("printPath1").executes(context -> {
						context.getSource().sendFeedback(Constants.PREFIX.get().append(INSTANCE.iceFillPaths.getFirst().toString()));
						return Command.SINGLE_SUCCESS;
					})).then(literal("printPath2").executes(context -> {
						context.getSource().sendFeedback(Constants.PREFIX.get().append(INSTANCE.iceFillPaths.get(1).toString()));
						return Command.SINGLE_SUCCESS;
					})).then(literal("printPath3").executes(context -> {
						context.getSource().sendFeedback(Constants.PREFIX.get().append(INSTANCE.iceFillPaths.get(2).toString()));
						return Command.SINGLE_SUCCESS;
					}))
			)))));
		}
	}

	public static String boardToString(boolean[][] iceFillBoard) {
		StringBuilder sb = new StringBuilder();
		for (boolean[] row : iceFillBoard) {
			sb.append("\n");
			for (boolean cell : row) {
				sb.append(cell ? '#' : '.');
			}
		}
		return sb.toString();
	}

	@Override
	public void tick(Minecraft client) {
		if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveIceFill || client.level == null || !DungeonManager.isCurrentRoomMatched() || solve != null && !solve.isDone()) {
			return;
		}
		Room room = DungeonManager.getCurrentRoom();
		if (room == null) return;

		solve = CompletableFuture.runAsync(() -> {
			BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
			for (int i = 0; i < 3; i++) {
				if (updateBoard(client.level, room, iceFillBoards[i], pos.set(BOARD_ORIGINS[i]))) {
					iceFillPaths.set(i, solve(iceFillBoards[i]));
				}
			}
		});
	}

	private boolean updateBoard(Level world, Room room, boolean[][] iceFillBoard, BlockPos.MutableBlockPos pos) {
		boolean boardChanged = false;
		for (int row = 0; row < iceFillBoard.length; pos.move(iceFillBoard[row].length, 0, -1), row++) {
			for (int col = 0; col < iceFillBoard[row].length; pos.move(Direction.WEST), col++) {
				BlockPos actualPos = room.relativeToActual(pos);
				// Don't solve the board if the block below is air
				if (world.getBlockState(actualPos.below()).isAir()) return false;

				boolean isBlock = !world.getBlockState(actualPos).isAir();
				if (iceFillBoard[row][col] != isBlock) {
					iceFillBoard[row][col] = isBlock;
					boardChanged = true;
				}
			}
		}
		return boardChanged;
	}

	List<Vector2ic> solve(boolean[][] iceFillBoard) {
		Vector2ic start = new Vector2i(iceFillBoard.length - 1, iceFillBoard[0].length / 2);
		int count = iceFillBoard.length * iceFillBoard[0].length - Arrays.stream(iceFillBoard).mapToInt(Booleans::countTrue).sum();

		boolean[][] visited = new boolean[iceFillBoard.length][iceFillBoard[0].length];
		visited[start.x()][start.y()] = true;

		List<Vector2ic> newPath = solveDfs(iceFillBoard, count - 1, new ArrayList<>(List.of(start)), visited);
		return newPath != null ? newPath : List.of();
	}

	private @Nullable List<Vector2ic> solveDfs(boolean[][] iceFillBoard, int count, List<Vector2ic> path, boolean[][] visited) {
		Vector2ic pos = path.getLast();
		if (count == 0) {
			if (pos.x() == 0 && pos.y() == iceFillBoard[0].length / 2) {
				return path;
			} else {
				return null;
			}
		}

		Vector2ic[] newPosArray = {pos.add(1, 0, new Vector2i()), pos.add(-1, 0, new Vector2i()), pos.add(0, 1, new Vector2i()), pos.add(0, -1, new Vector2i())};
		for (Vector2ic newPos : newPosArray) {
			if (newPos.x() >= 0 && newPos.x() < iceFillBoard.length && newPos.y() >= 0 && newPos.y() < iceFillBoard[0].length && !iceFillBoard[newPos.x()][newPos.y()] && !visited[newPos.x()][newPos.y()]) {
				path.add(newPos);
				visited[newPos.x()][newPos.y()] = true;
				List<Vector2ic> newPath = solveDfs(iceFillBoard, count - 1, path, visited);
				if (newPath != null) {
					return newPath;
				}
				path.removeLast();
				visited[newPos.x()][newPos.y()] = false;
			}
		}

		return null;
	}

	@Override
	public void extractRendering(PrimitiveCollector collector) {
		if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveIceFill || !DungeonManager.isCurrentRoomMatched()) {
			return;
		}
		Room room = DungeonManager.getCurrentRoom();
		if (room == null) return;

		for (int i = 0; i < 3; i++) {
			extractPath(collector, room, iceFillPaths.get(i), BOARD_ORIGINS[i]);
		}
	}

	private void extractPath(PrimitiveCollector collector, Room room, List<Vector2ic> iceFillPath, BlockPos originPos) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int i = 0; i < iceFillPath.size() - 1; i++) {
			Vec3 start = Vec3.atCenterOf(room.relativeToActual(pos.set(originPos).move(-iceFillPath.get(i).y(), 0, -iceFillPath.get(i).x())));
			Vec3 end = Vec3.atCenterOf(room.relativeToActual(pos.set(originPos).move(-iceFillPath.get(i + 1).y(), 0, -iceFillPath.get(i + 1).x())));
			collector.submitLinesFromPoints(new Vec3[]{start, end}, RED_COLOR_COMPONENTS, 1f, 5f, true);
		}
	}
}
