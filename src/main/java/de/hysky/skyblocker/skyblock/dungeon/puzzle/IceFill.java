package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import com.google.common.primitives.Booleans;
import com.mojang.brigadier.Command;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
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
    private static final float[] RED_COLOR_COMPONENTS = DyeColor.RED.getColorComponents();
    private static final BlockPos BOARD_1_ORIGIN = new BlockPos(16, 70, 9);
    private static final BlockPos BOARD_2_ORIGIN = new BlockPos(17, 71, 16);
    private static final BlockPos BOARD_3_ORIGIN = new BlockPos(18, 72, 25);
    private CompletableFuture<Void> solve;
    private final boolean[][] iceFillBoard1 = new boolean[3][3];
    private final boolean[][] iceFillBoard2 = new boolean[5][5];
    private final boolean[][] iceFillBoard3 = new boolean[7][7];
    private final List<Vector2ic> iceFillPath1 = new ArrayList<>();
    private final List<Vector2ic> iceFillPath2 = new ArrayList<>();
    private final List<Vector2ic> iceFillPath3 = new ArrayList<>();

    private IceFill() {
        super("ice-fill", "ice-path");
    }

    public static void init() {
        if (Debug.debugEnabled()) {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("dungeons").then(literal("puzzle").then(literal(INSTANCE.puzzleName)
                    .then(literal("printBoard1").executes(context -> {
                        context.getSource().sendFeedback(Constants.PREFIX.get().append(boardToString(INSTANCE.iceFillBoard1)));
                        return Command.SINGLE_SUCCESS;
                    })).then(literal("printBoard2").executes(context -> {
                        context.getSource().sendFeedback(Constants.PREFIX.get().append(boardToString(INSTANCE.iceFillBoard2)));
                        return Command.SINGLE_SUCCESS;
                    })).then(literal("printBoard3").executes(context -> {
                        context.getSource().sendFeedback(Constants.PREFIX.get().append(boardToString(INSTANCE.iceFillBoard3)));
                        return Command.SINGLE_SUCCESS;
                    })).then(literal("printPath1").executes(context -> {
                        context.getSource().sendFeedback(Constants.PREFIX.get().append(INSTANCE.iceFillPath1.toString()));
                        return Command.SINGLE_SUCCESS;
                    })).then(literal("printPath2").executes(context -> {
                        context.getSource().sendFeedback(Constants.PREFIX.get().append(INSTANCE.iceFillPath2.toString()));
                        return Command.SINGLE_SUCCESS;
                    })).then(literal("printPath3").executes(context -> {
                        context.getSource().sendFeedback(Constants.PREFIX.get().append(INSTANCE.iceFillPath3.toString()));
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
        if (!SkyblockerConfigManager.get().locations.dungeons.solveIceFill || client.world == null || !DungeonManager.isCurrentRoomMatched() || solve != null && !solve.isDone()) {
            return;
        }
        Room room = DungeonManager.getCurrentRoom();

        solve = CompletableFuture.runAsync(() -> {
            BlockPos.Mutable pos = new BlockPos.Mutable();
            boolean board1Changed = updateBoard(client.world, room, iceFillBoard1, pos.set(BOARD_1_ORIGIN));
            boolean board2Changed = updateBoard(client.world, room, iceFillBoard2, pos.set(BOARD_2_ORIGIN));
            boolean board3Changed = updateBoard(client.world, room, iceFillBoard3, pos.set(BOARD_3_ORIGIN));

            if (board1Changed) {
                solve(iceFillBoard1, iceFillPath1);
            }
            if (board2Changed) {
                solve(iceFillBoard2, iceFillPath2);
            }
            if (board3Changed) {
                solve(iceFillBoard3, iceFillPath3);
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

        List<Vector2ic> newPath = solveDfs(iceFillBoard, count - 1, new ArrayList<>(List.of(start)));
        if (newPath != null) {
            iceFillPath.clear();
            iceFillPath.addAll(newPath);
        }
    }

    private List<Vector2ic> solveDfs(boolean[][] iceFillBoard, int count, List<Vector2ic> path) {
        Vector2ic pos = path.get(path.size() - 1);
        if (pos.x() == 0 && pos.y() == iceFillBoard[0].length / 2 && count == 0) {
            return path;
        }

        Vector2ic newPos = pos.add(1, 0, new Vector2i());
        if (newPos.x() < iceFillBoard.length && !iceFillBoard[newPos.x()][newPos.y()] && !path.contains(newPos)) {
            path.add(newPos);
            List<Vector2ic> newPath = solveDfs(iceFillBoard, count - 1, path);
            if (newPath != null) {
                return newPath;
            } else {
                path.remove(path.size() - 1);
            }
        }

        newPos = pos.add(-1, 0, new Vector2i());
        if (newPos.x() >= 0 && !iceFillBoard[newPos.x()][newPos.y()] && !path.contains(newPos)) {
            path.add(newPos);
            List<Vector2ic> newPath = solveDfs(iceFillBoard, count - 1, path);
            if (newPath != null) {
                return newPath;
            } else {
                path.remove(path.size() - 1);
            }
        }

        newPos = pos.add(0, 1, new Vector2i());
        if (newPos.y() < iceFillBoard[0].length && !iceFillBoard[newPos.x()][newPos.y()] && !path.contains(newPos)) {
            path.add(newPos);
            List<Vector2ic> newPath = solveDfs(iceFillBoard, count - 1, path);
            if (newPath != null) {
                return newPath;
            } else {
                path.remove(path.size() - 1);
            }
        }

        newPos = pos.add(0, -1, new Vector2i());
        if (newPos.y() >= 0 && !iceFillBoard[newPos.x()][newPos.y()] && !path.contains(newPos)) {
            path.add(newPos);
            List<Vector2ic> newPath = solveDfs(iceFillBoard, count - 1, path);
            if (newPath != null) {
                return newPath;
            } else {
                path.remove(path.size() - 1);
            }
        }

        return null;
    }

    /*
    void solve(boolean[][] iceFillBoard, List<Vector2ic> iceFillPath) {
        Vector2ic start = new Vector2i(iceFillBoard.length - 1, iceFillBoard[0].length / 2);
        int count = iceFillBoard.length * iceFillBoard[0].length - Arrays.stream(iceFillBoard).mapToInt(Booleans::countTrue).sum();

        Vector2ic[] newPath = solveDfs(iceFillBoard, count - 1, new Vector2ic[]{start});
        if (newPath != null) {
            iceFillPath.clear();
            iceFillPath.addAll(Arrays.asList(newPath));
        }
    }

    private Vector2ic[] solveDfs(boolean[][] iceFillBoard, int count, Vector2ic[] path) {
        Vector2ic pos = path[path.length - 1];
        if (pos.x() == 0 && pos.y() == iceFillBoard[0].length / 2 && count == 0) {
            return path;
        }

        Vector2ic newPos = pos.add(1, 0, new Vector2i());
        if (newPos.x() < iceFillBoard.length && !iceFillBoard[newPos.x()][newPos.y()] && !ArrayUtils.contains(path, newPos)) {
            Vector2ic[] newPath = Arrays.copyOf(path, path.length + 1);
            newPath[path.length] = newPos;
            newPath = solveDfs(iceFillBoard, count - 1, newPath);
            if (newPath != null) {
                return newPath;
            }
        }

        newPos = pos.add(-1, 0, new Vector2i());
        if (newPos.x() >= 0 && !iceFillBoard[newPos.x()][newPos.y()] && !ArrayUtils.contains(path, newPos)) {
            Vector2ic[] newPath = Arrays.copyOf(path, path.length + 1);
            newPath[path.length] = newPos;
            newPath = solveDfs(iceFillBoard, count - 1, newPath);
            if (newPath != null) {
                return newPath;
            }
        }

        newPos = pos.add(0, 1, new Vector2i());
        if (newPos.y() < iceFillBoard[0].length && !iceFillBoard[newPos.x()][newPos.y()] && !ArrayUtils.contains(path, newPos)) {
            Vector2ic[] newPath = Arrays.copyOf(path, path.length + 1);
            newPath[path.length] = newPos;
            newPath = solveDfs(iceFillBoard, count - 1, newPath);
            if (newPath != null) {
                return newPath;
            }
        }

        newPos = pos.add(0, -1, new Vector2i());
        if (newPos.y() >= 0 && !iceFillBoard[newPos.x()][newPos.y()] && !ArrayUtils.contains(path, newPos)) {
            Vector2ic[] newPath = Arrays.copyOf(path, path.length + 1);
            newPath[path.length] = newPos;
            newPath = solveDfs(iceFillBoard, count - 1, newPath);
            if (newPath != null) {
                return newPath;
            }
        }

        return null;
    }
     */

    @Override
    public void render(WorldRenderContext context) {
        if (!SkyblockerConfigManager.get().locations.dungeons.solveIceFill || !DungeonManager.isCurrentRoomMatched()) {
            return;
        }
        Room room = DungeonManager.getCurrentRoom();
        renderPath(context, room, iceFillPath1, BOARD_1_ORIGIN);
        renderPath(context, room, iceFillPath2, BOARD_2_ORIGIN);
        renderPath(context, room, iceFillPath3, BOARD_3_ORIGIN);
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
