package de.hysky.skyblocker.skyblock.dungeon.puzzle;

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
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;

import static de.hysky.skyblocker.skyblock.dungeon.puzzle.IceFill.boardToString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Puzzle solver for the Silverfish "Ice Path" puzzle
 */
public class IcePath extends DungeonPuzzle {
	public static final IcePath INSTANCE = new IcePath();
	private static final float[] RED_COLOR_COMPONENTS = ColorUtils.getFloatComponents(DyeColor.RED);
	final boolean[][] silverfishBoard = new boolean[17][17];
	@Nullable Vector2ic silverfishPos;
	final List<Vector2ic> silverfishPath = new ArrayList<>();

	private IcePath() {
		super("silverfish", "ice-silverfish-room");
	}

	@Init
	public static void init() {
		if (Debug.debugEnabled()) {
			ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("dungeons").then(literal("puzzle").then(literal(INSTANCE.puzzleName)
					.then(literal("printBoard").executes(context -> {
						context.getSource().sendFeedback(Constants.PREFIX.get().append(boardToString(INSTANCE.silverfishBoard)));
						return Command.SINGLE_SUCCESS;
					})).then(literal("printPath").executes(context -> {
						context.getSource().sendFeedback(Constants.PREFIX.get().append(INSTANCE.silverfishPath.toString()));
						return Command.SINGLE_SUCCESS;
					}))
			)))));
		}
	}

	@Override
	public void tick(Minecraft client) {
		if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveSilverfish || client.level == null || !DungeonManager.isCurrentRoomMatched()) {
			return;
		}
		Room room = DungeonManager.getCurrentRoom();
		if (room == null) return;

		boolean boardChanged = false;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(23, 67, 24);
		for (int row = 0; row < silverfishBoard.length; pos.move(silverfishBoard[row].length, 0, -1), row++) {
			for (int col = 0; col < silverfishBoard[row].length; pos.move(Direction.WEST), col++) {
				boolean isBlock = !client.level.getBlockState(room.relativeToActual(pos)).isAir();
				if (silverfishBoard[row][col] != isBlock) {
					silverfishBoard[row][col] = isBlock;
					boardChanged = true;
				}
			}
		}

		List<Silverfish> entities = client.level.getEntitiesOfClass(Silverfish.class, AABB.ofSize(Vec3.atCenterOf(room.relativeToActual(new BlockPos(15, 66, 16))), 16, 16, 16), silverfishEntity -> true);
		if (entities.isEmpty()) {
			return;
		}
		BlockPos newSilverfishBlockPos = room.actualToRelative(entities.getFirst().blockPosition());
		Vector2ic newSilverfishPos = new Vector2i(24 - newSilverfishBlockPos.getZ(), 23 - newSilverfishBlockPos.getX());
		if (newSilverfishPos.x() < 0 || newSilverfishPos.x() >= 17 || newSilverfishPos.y() < 0 || newSilverfishPos.y() >= 17) {
			return;
		}
		boolean silverfishChanged = !newSilverfishPos.equals(silverfishPos);
		if (silverfishChanged) {
			silverfishPos = newSilverfishPos;
		}
		if (silverfishChanged || boardChanged) {
			solve();
		}
	}

	void solve() {
		if (silverfishPos == null) {
			return;
		}
		Set<Vector2ic> visited = new HashSet<>();
		Queue<List<Vector2ic>> queue = new ArrayDeque<>();
		queue.add(List.of(silverfishPos));
		visited.add(silverfishPos);
		while (!queue.isEmpty()) {
			List<Vector2ic> path = queue.poll();
			Vector2ic pos = path.get(path.size() - 1);
			if (pos.x() == 0 && pos.y() >= 7 && pos.y() <= 9) {
				silverfishPath.clear();
				silverfishPath.addAll(path);
				return;
			}

			Vector2i posMutable = new Vector2i(pos);
			while (posMutable.x() < 17 && !silverfishBoard[posMutable.x()][posMutable.y()]) {
				posMutable.add(1, 0);
			}
			posMutable.add(-1, 0);
			addQueue(visited, queue, path, posMutable);

			posMutable = new Vector2i(pos);
			while (posMutable.x() >= 0 && !silverfishBoard[posMutable.x()][posMutable.y()]) {
				posMutable.add(-1, 0);
			}
			posMutable.add(1, 0);
			addQueue(visited, queue, path, posMutable);

			posMutable = new Vector2i(pos);
			while (posMutable.y() < 17 && !silverfishBoard[posMutable.x()][posMutable.y()]) {
				posMutable.add(0, 1);
			}
			posMutable.add(0, -1);
			addQueue(visited, queue, path, posMutable);

			posMutable = new Vector2i(pos);
			while (posMutable.y() >= 0 && !silverfishBoard[posMutable.x()][posMutable.y()]) {
				posMutable.add(0, -1);
			}
			posMutable.add(0, 1);
			addQueue(visited, queue, path, posMutable);
		}
	}

	private void addQueue(Set<Vector2ic> visited, Queue<List<Vector2ic>> queue, List<Vector2ic> path, Vector2ic newPos) {
		if (!visited.contains(newPos)) {
			List<Vector2ic> newPath = new ArrayList<>(path);
			newPath.add(newPos);
			queue.add(newPath);
			visited.add(newPos);
		}
	}

	@Override
	public void extractRendering(PrimitiveCollector collector) {
		if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveSilverfish || !DungeonManager.isCurrentRoomMatched() || silverfishPath.isEmpty()) {
			return;
		}
		Room room = DungeonManager.getCurrentRoom();
		if (room == null) return;

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int i = 0; i < silverfishPath.size() - 1; i++) {
			Vec3 start = Vec3.atCenterOf(room.relativeToActual(pos.set(23 - silverfishPath.get(i).y(), 67, 24 - silverfishPath.get(i).x())));
			Vec3 end = Vec3.atCenterOf(room.relativeToActual(pos.set(23 - silverfishPath.get(i + 1).y(), 67, 24 - silverfishPath.get(i + 1).x())));
			collector.submitLinesFromPoints(new Vec3[]{start, end}, RED_COLOR_COMPONENTS, 1f, 5f, true);
		}
	}

	@Override
	public void reset() {
		super.reset();
		for (boolean[] silverfishBoardRow : silverfishBoard) {
			Arrays.fill(silverfishBoardRow, false);
		}
		silverfishPos = null;
	}
}
