package de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.dungeon.puzzle.DungeonPuzzle;
import de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard.Cell.SwitchCell;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeverBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Waterboard extends DungeonPuzzle {
    private static final Logger LOGGER = LoggerFactory.getLogger(Waterboard.class);
    public static final Waterboard INSTANCE = new Waterboard();
    private static final Object2IntMap<Block> SWITCH_BLOCKS = Object2IntMaps.unmodifiable(new Object2IntOpenHashMap<>(Map.of(
            Blocks.COAL_BLOCK, 1,
            Blocks.GOLD_BLOCK, 2,
            Blocks.QUARTZ_BLOCK, 3,
            Blocks.DIAMOND_BLOCK, 4,
            Blocks.EMERALD_BLOCK, 5,
            Blocks.TERRACOTTA, 6
    )));
    private static final BlockPos[] SWITCH_POSITIONS = new BlockPos[]{
            new BlockPos(20, 61, 10),
            new BlockPos(20, 61, 15),
            new BlockPos(20, 61, 20),
            new BlockPos(10, 61, 20),
            new BlockPos(10, 61, 15),
            new BlockPos(10, 61, 10)
    };
    public static final BlockPos WATER_LEVER = new BlockPos(15, 60, 5);
    private static final float[] LIME_COLOR_COMPONENTS = ColorUtils.getFloatComponents(DyeColor.LIME);

    private CompletableFuture<Void> solve;
    private final Cell[][] cells = new Cell[19][19];
    private final Switch[] switches = new Switch[]{new Switch(0), new Switch(1), new Switch(2), new Switch(3), new Switch(4), new Switch(5)};
    private int doors = 0;
    private final Result[] results = new Result[64];
    private int currentCombination;
    private final IntList bestCombinations = new IntArrayList();
    private final Waypoint[] waypoints = new Waypoint[7];
    /**
     * Used to check the water lever state since the block state does not update immediately after the lever is toggled.
     */
    private boolean bestCombinationsUpdated;

    private Waterboard() {
        super("waterboard", "water-puzzle");
    }

    @Init
    public static void init() {
        UseBlockCallback.EVENT.register(INSTANCE::onUseBlock);
        if (Debug.debugEnabled()) {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("dungeons").then(literal("puzzle").then(literal(INSTANCE.puzzleName)
                    .then(literal("printBoard").executes(context -> {
                        context.getSource().sendFeedback(Constants.PREFIX.get().append(boardToString(INSTANCE.cells)));
                        return Command.SINGLE_SUCCESS;
                    })).then(literal("printDoors").executes(context -> {
                        context.getSource().sendFeedback(Constants.PREFIX.get().append(Integer.toBinaryString(INSTANCE.doors)));
                        return Command.SINGLE_SUCCESS;
                    })).then(literal("printSimulationResults").then(argument("combination", IntegerArgumentType.integer(0, 63)).executes(context -> {
                        context.getSource().sendFeedback(Constants.PREFIX.get().append(INSTANCE.results[IntegerArgumentType.getInteger(context, "combination")].toString()));
                        return Command.SINGLE_SUCCESS;
                    }))).then(literal("printCurrentCombination").executes(context -> {
                        context.getSource().sendFeedback(Constants.PREFIX.get().append(Integer.toBinaryString(INSTANCE.currentCombination)));
                        return Command.SINGLE_SUCCESS;
                    })).then(literal("printBestCombination").executes(context -> {
                        context.getSource().sendFeedback(Constants.PREFIX.get().append(INSTANCE.bestCombinations.toString()));
                        return Command.SINGLE_SUCCESS;
                    }))
            )))));
        }
    }

    private static String boardToString(Cell[][] cells) {
        StringBuilder sb = new StringBuilder();
        for (Cell[] row : cells) {
            sb.append("\n");
            for (Cell cell : row) {
                switch (cell) {
                    case SwitchCell switchCell -> sb.append(switchCell.id);
                    case Cell c when c.type == Cell.Type.BLOCK -> sb.append('#');
                    case Cell c when c.type == Cell.Type.EMPTY -> sb.append('.');

                    case null, default -> sb.append('?');
                }
            }
        }
        return sb.toString();
    }

    @Override
    public void tick(MinecraftClient client) {
        if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveWaterboard || client.world == null || !DungeonManager.isCurrentRoomMatched() || solve != null && !solve.isDone()) {
            return;
        }
        Room room = DungeonManager.getCurrentRoom();
        solve = CompletableFuture.runAsync(() -> {
            Changed changed = updateBoard(client.world, room);
            if (changed == Changed.NONE) {
                return;
            }
            if (results[0] == null) {
                updateSwitches();
                simulateCombinations();
                clearSwitches();
            }
            if (bestCombinations.isEmpty() || changed.doorChanged()) {
                findBestCombinations();
                bestCombinationsUpdated = true;
            }
        }).exceptionally(e -> {
            LOGGER.error("[Skyblocker Waterboard] Encountered an unknown exception while solving waterboard.", e);
            return null;
        });
        if (waypoints[0] == null) {
            for (int i = 0; i < 6; i++) {
                waypoints[i] = new Waypoint(room.relativeToActual(SWITCH_POSITIONS[i]), Waypoint.Type.HIGHLIGHT, LIME_COLOR_COMPONENTS);
            }
            waypoints[6] = new Waypoint(room.relativeToActual(WATER_LEVER), Waypoint.Type.HIGHLIGHT, LIME_COLOR_COMPONENTS);
            waypoints[6].setFound();
        }
    }

    private Changed updateBoard(World world, Room room) {
        // Parse the waterboard.
        BlockPos.Mutable pos = new BlockPos.Mutable(24, 78, 26);
        Changed changed = Changed.NONE;
        for (int row = 0; row < cells.length; pos.move(cells[row].length, -1, 0), row++) {
            for (int col = 0; col < cells[row].length; pos.move(Direction.WEST), col++) {
                Cell cell = parseBlock(world, room, pos);
                if (!cell.equals(cells[row][col])) {
                    cells[row][col] = cell;
                    changed = changed.onCellChanged();
                }
            }
        }

        // Parse door states.
        pos.set(15, 57, 15);
        int prevDoors = doors;
        doors = 0;
        for (int i = 0; i < 5; pos.move(Direction.SOUTH), i++) {
            doors |= world.getBlockState(room.relativeToActual(pos)).isAir() ? 1 << i : 0;
        }
        if (doors != prevDoors) {
            changed = changed.onDoorChanged();
        }

        // Parse current combination of switches based on the levers.
        currentCombination = 0;
        for (int i = 0; i < 6; i++) {
            currentCombination |= getSwitchState(world, room, i);
        }

        return changed;
    }

    private Cell parseBlock(World world, Room room, BlockPos.Mutable pos) {
        // Check if the block is a switch.
        BlockState state = world.getBlockState(room.relativeToActual(pos));
        int switch_ = SWITCH_BLOCKS.getInt(state.getBlock());
        if (switch_-- > 0) {
            return new SwitchCell(switch_);
        }
        // Check if the block is an opened switch by checking the block behind it.
        int switchBehind = SWITCH_BLOCKS.getInt(world.getBlockState(room.relativeToActual(pos.move(Direction.SOUTH))).getBlock());
        pos.move(Direction.NORTH);
        if (switchBehind-- > 0) {
            return SwitchCell.ofOpened(switchBehind);
        }

        // Check if the block is empty otherwise the block is a wall.
        return state.isAir() || state.isOf(Blocks.WATER) ? Cell.EMPTY : Cell.BLOCK;
    }

    private static int getSwitchState(World world, Room room, int i) {
        BlockState state = world.getBlockState(room.relativeToActual(SWITCH_POSITIONS[i]));
        return state.contains(LeverBlock.POWERED) && state.get(LeverBlock.POWERED) ? 1 << i : 0;
    }

    private void updateSwitches() {
        clearSwitches();
        for (Cell[] row : cells) {
            for (Cell cell : row) {
                if (cell instanceof SwitchCell switchCell) {
                    switches[switchCell.id].add(switchCell);
                }
            }
        }
    }

    private void simulateCombinations() {
        for (int combination = 0; combination < (1 << 6); combination++) {
            for (int switchIndex = 0; switchIndex < 6; switchIndex++) {
                if ((combination & (1 << switchIndex)) != 0) {
                    switches[switchIndex].toggle();
                }
            }
            results[combination] = simulateCombination();
            for (int switchIndex = 0; switchIndex < 6; switchIndex++) {
                if ((combination & (1 << switchIndex)) != 0) {
                    switches[switchIndex].toggle();
                }
            }
        }
    }

    private Result simulateCombination() {
        List<Vector2i> waters = new ArrayList<>();
        waters.add(new Vector2i(9, 0));
        Result result = new Result();
        while (!waters.isEmpty()) {
            List<Vector2i> newWaters = new ArrayList<>();
            for (Iterator<Vector2i> watersIt = waters.iterator(); watersIt.hasNext(); ) {
                Vector2i water = watersIt.next();
                // Check if the water has reached a door.
                if (water.y == 18) {
                    switch (water.x) {
                        case 0 -> result.reachedDoors |= 1 << 4;
                        case 4 -> result.reachedDoors |= 1 << 3;
                        case 9 -> result.reachedDoors |= 1 << 2;
                        case 14 -> result.reachedDoors |= 1 << 1;
                        case 18 -> result.reachedDoors |= 1;
                    }
                    watersIt.remove();
                    continue;
                }
                // Check if the water can flow down.
                if (water.y < 18 && cells[water.y + 1][water.x].isOpen()) {
                    result.putPath(water, 0);
                    water.add(0, 1);
                    continue;
                }

                // Get the offset to the first block on the left and the right that can flow down.
                int leftFlowDownOffset = findFlowDown(water, false);
                int rightFlowDownOffset = findFlowDown(water, true);
                // Check if left down is in range and is closer than right down.
                // Note 1: The yarn name "getFlowSpeed" is incorrect as it actually returns the maximum distance that water will check for a hole to flow towards.
                // Note 2: Skyblock's maximum offset is 5 instead of 4 for some reason.
                if (-leftFlowDownOffset <= ((WaterFluid) Fluids.WATER).getMaxFlowDistance(null) + 1 && -leftFlowDownOffset < rightFlowDownOffset) {
                    result.putPath(water, leftFlowDownOffset);
                    water.add(leftFlowDownOffset, 1);
                    continue;
                }
                // Check if right down is in range and closer than left down.
                if (rightFlowDownOffset <= ((WaterFluid) Fluids.WATER).getMaxFlowDistance(null) + 1 && rightFlowDownOffset < -leftFlowDownOffset) {
                    result.putPath(water, rightFlowDownOffset);
                    water.add(rightFlowDownOffset, 1);
                    continue;
                }

                // Else flow to both sides if in range.
                if (leftFlowDownOffset > Integer.MIN_VALUE + 1) {
                    result.putPath(water, leftFlowDownOffset);
                    newWaters.add(new Vector2i(water).add(leftFlowDownOffset, 1));
                }
                if (rightFlowDownOffset < Integer.MAX_VALUE) {
                    result.putPath(water, rightFlowDownOffset);
                    newWaters.add(new Vector2i(water).add(rightFlowDownOffset, 1));
                }
                watersIt.remove();
            }
            waters.addAll(newWaters);
        }
        return result;
    }

    /**
     * Finds the first block on the left that can flow down.
     */
    private int findFlowDown(Vector2i water, boolean direction) {
        for (int i = 0; water.x + i >= 0 && water.x + i < 19 && i > -8 && i < 8 && cells[water.y][water.x + i].isOpen(); i += direction ? 1 : -1) {
            if (cells[water.y + 1][water.x + i].isOpen()) {
                return i;
            }
        }
        return direction ? Integer.MAX_VALUE : Integer.MIN_VALUE + 1;
    }

    private void findBestCombinations() {
        bestCombinations.clear();
        for (int combination = 0, bestScore = 0; combination < (1 << 6); combination++) {
            int newScore = Integer.bitCount(results[combination].reachedDoors ^ doors);
            if (newScore >= bestScore) {
                if (newScore > bestScore) {
                    bestCombinations.clear();
                    bestScore = newScore;
                }
                bestCombinations.add(combination);
            }
        }
    }

    @Override
    public void render(WorldRenderContext context) {
        if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveWaterboard || !DungeonManager.isCurrentRoomMatched()) return;
        Room room = DungeonManager.getCurrentRoom();

        // Render the best combination.
        @SuppressWarnings("resource")
        BlockState state = context.world().getBlockState(room.relativeToActual(WATER_LEVER));
        // bestCombinationsUpdated is needed because bestCombinations does not update immediately after the lever is turned off.
        if (waypoints[0] != null && bestCombinationsUpdated && state.contains(LeverBlock.POWERED) && !state.get(LeverBlock.POWERED)) {
            bestCombinations.intStream().mapToObj(bestCombination -> currentCombination ^ bestCombination).min(Comparator.comparingInt(Integer::bitCount)).ifPresent(bestDifference -> {
                for (int i = 0; i < 6; i++) {
                    if ((bestDifference & 1 << i) != 0) {
                        waypoints[i].render(context);
                    }
                }
                if (bestDifference == 0 && !waypoints[6].shouldRender()) {
                    waypoints[6].setMissing();
                }
            });
        }
        if (waypoints[6] != null && waypoints[6].shouldRender()) {
            waypoints[6].render(context);
        }

        // Render the current path of the water.
        BlockPos.Mutable pos = new BlockPos.Mutable(15, 79, 26);
        RenderHelper.renderLinesFromPoints(context, new Vec3d[]{Vec3d.ofCenter(room.relativeToActual(pos)), Vec3d.ofCenter(room.relativeToActual(pos.move(Direction.DOWN)))}, LIME_COLOR_COMPONENTS, 1f, 5f, true);
        Result currentResult = results[currentCombination];
        if (currentResult != null) {
            for (Map.Entry<Vector2ic, Integer> entry : currentResult.path.entries()) {
                Vec3d start = Vec3d.ofCenter(room.relativeToActual(pos.set(24 - entry.getKey().x(), 78 - entry.getKey().y(), 26)));
                Vec3d middle = Vec3d.ofCenter(room.relativeToActual(pos.move(Direction.WEST, entry.getValue())));
                Vec3d end = Vec3d.ofCenter(room.relativeToActual(pos.move(Direction.DOWN)));
                RenderHelper.renderLinesFromPoints(context, new Vec3d[]{start, middle}, LIME_COLOR_COMPONENTS, 1f, 5f, true);
                RenderHelper.renderLinesFromPoints(context, new Vec3d[]{middle, end}, LIME_COLOR_COMPONENTS, 1f, 5f, true);
            }
        }
    }

    private ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult blockHitResult) {
        BlockState state = world.getBlockState(blockHitResult.getBlockPos());
        if (SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveWaterboard && blockHitResult.getType() == HitResult.Type.BLOCK && waypoints[6] != null && DungeonManager.isCurrentRoomMatched() && blockHitResult.getBlockPos().equals(DungeonManager.getCurrentRoom().relativeToActual(WATER_LEVER)) && state.contains(LeverBlock.POWERED)) {
            if (!state.get(LeverBlock.POWERED)) {
                bestCombinationsUpdated = false;
                Scheduler.INSTANCE.schedule(() -> waypoints[6].setMissing(), 50);
            }
            waypoints[6].setFound();
        }
        return ActionResult.PASS;
    }

    @Override
    public void reset() {
        super.reset();
        solve = null;
        for (Cell[] row : cells) {
            Arrays.fill(row, null);
        }
        clearSwitches();
        doors = 0;
        Arrays.fill(results, null);
        currentCombination = 0;
        bestCombinations.clear();
        Arrays.fill(waypoints, null);
    }

    public void clearSwitches() {
        for (Switch switch_ : switches) {
            switch_.clear();
        }
    }

    private enum Changed {
        NONE, CELL, DOOR, BOTH;

        private boolean cellChanged() {
            return this == CELL || this == BOTH;
        }

        private boolean doorChanged() {
            return this == DOOR || this == BOTH;
        }

        private Changed onCellChanged() {
            return switch (this) {
                case NONE, CELL -> Changed.CELL;
                case DOOR, BOTH -> Changed.BOTH;
            };
        }

        private Changed onDoorChanged() {
            return switch (this) {
                case NONE, DOOR -> Changed.DOOR;
                case CELL, BOTH -> Changed.BOTH;
            };
        }
    }

    public static class Result {
        private int reachedDoors;
        private final Multimap<Vector2ic, Integer> path = MultimapBuilder.hashKeys().arrayListValues().build();

        public boolean putPath(Vector2i water, int offset) {
            return path.put(new Vector2i(water), offset);
        }

        @Override
        public String toString() {
            return "Result[reachedDoors=" + Integer.toBinaryString(reachedDoors) + ", path=" + path + ']';
        }
    }
}
