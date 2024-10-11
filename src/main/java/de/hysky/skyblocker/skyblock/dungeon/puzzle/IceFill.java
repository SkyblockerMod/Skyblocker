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
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.*;
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
    private CompletableFuture<Void> solve;
    private final boolean[][][] iceFillBoards = {new boolean[3][3], new boolean[5][5], new boolean[7][7]};
    private final List<List<Vector2ic>> iceFillPaths = List.of(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

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

    private static String boardToString(boolean[][] iceFillBoard) {
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
    public void tick(MinecraftClient client) {
        if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveIceFill || client.world == null || !DungeonManager.isCurrentRoomMatched() || solve != null && !solve.isDone()) {
            return;
        }
        Room room = DungeonManager.getCurrentRoom();

        solve = CompletableFuture.runAsync(() -> {
            BlockPos.Mutable pos = new BlockPos.Mutable();
            for (int i = 0; i < 3; i++) {
                if (updateBoard(client.world, room, iceFillBoards[i], pos.set(BOARD_ORIGINS[i]))) {
                    solve(iceFillBoards[i], iceFillPaths.get(i));
                }
            }
        });
    }

    private boolean updateBoard(World world, Room room, boolean[][] iceFillBoard, BlockPos.Mutable pos) {
        boolean boardChanged = false;
        for (int row = 0; row < iceFillBoard.length; pos.move(iceFillBoard[row].length, 0, -1), row++) {
            for (int col = 0; col < iceFillBoard[row].length; pos.move(Direction.WEST), col++) {
                BlockPos actualPos = room.relativeToActual(pos);
                boolean isBlock = !world.getBlockState(actualPos).isAir();
                if (iceFillBoard[row][col] != isBlock) {
                    iceFillBoard[row][col] = isBlock;
                    boardChanged = true;
                }
            }
        }
        return boardChanged;
    }

    void solve(boolean[][] iceFillBoard, List<Vector2ic> iceFillPath) {
        Vector2ic start = new Vector2i(iceFillBoard.length - 1, iceFillBoard[0].length / 2);
        int count = iceFillBoard.length * iceFillBoard[0].length - Arrays.stream(iceFillBoard).mapToInt(Booleans::countTrue).sum();

        List<Vector2ic> newPath = solveDfs(iceFillBoard, count - 1, new ArrayList<>(List.of(start)), new HashSet<>(List.of(start)));
        if (newPath != null) {
            iceFillPath.clear();
            iceFillPath.addAll(newPath);
        }
    }

    private List<Vector2ic> solveDfs(boolean[][] iceFillBoard, int count, List<Vector2ic> path, Set<Vector2ic> visited) {
        Vector2ic pos = path.get(path.size() - 1);
        if (count == 0) {
            if (pos.x() == 0 && pos.y() == iceFillBoard[0].length / 2) {
                return path;
            } else {
                return null;
            }
        }

        Vector2ic[] newPosArray = {pos.add(1, 0, new Vector2i()), pos.add(-1, 0, new Vector2i()), pos.add(0, 1, new Vector2i()), pos.add(0, -1, new Vector2i())};
        for (Vector2ic newPos : newPosArray) {
            if (newPos.x() >= 0 && newPos.x() < iceFillBoard.length && newPos.y() >= 0 && newPos.y() < iceFillBoard[0].length && !iceFillBoard[newPos.x()][newPos.y()] && !visited.contains(newPos)) {
                path.add(newPos);
                visited.add(newPos);
                List<Vector2ic> newPath = solveDfs(iceFillBoard, count - 1, path, visited);
                if (newPath != null) {
                    return newPath;
                }
                path.remove(path.size() - 1);
                visited.remove(newPos);
            }
        }

        return null;
    }

    @Override
    public void render(WorldRenderContext context) {
        if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveIceFill || !DungeonManager.isCurrentRoomMatched()) {
            return;
        }
        Room room = DungeonManager.getCurrentRoom();
        for (int i = 0; i < 3; i++) {
            renderPath(context, room, iceFillPaths.get(i), BOARD_ORIGINS[i]);
        }
    }

    private void renderPath(WorldRenderContext context, Room room, List<Vector2ic> iceFillPath, BlockPos originPos) {
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int i = 0; i < iceFillPath.size() - 1; i++) {
            Vec3d start = Vec3d.ofCenter(room.relativeToActual(pos.set(originPos).move(-iceFillPath.get(i).y(), 0, -iceFillPath.get(i).x())));
            Vec3d end = Vec3d.ofCenter(room.relativeToActual(pos.set(originPos).move(-iceFillPath.get(i + 1).y(), 0, -iceFillPath.get(i + 1).x())));
            RenderHelper.renderLinesFromPoints(context, new Vec3d[]{start, end}, RED_COLOR_COMPONENTS, 1f, 5f, true);
        }
    }
}
