package de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.primitives.Booleans;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.dungeon.puzzle.DungeonPuzzle;
import de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard.Cell.SwitchCell;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.render.RenderHelper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
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
    private static final Object2IntMap<Block> SWITCHES = Object2IntMaps.unmodifiable(new Object2IntOpenHashMap<>(Map.of(
            Blocks.COAL_BLOCK, 1,
            Blocks.GOLD_BLOCK, 2,
            Blocks.QUARTZ_BLOCK, 3,
            Blocks.DIAMOND_BLOCK, 4,
            Blocks.EMERALD_BLOCK, 5,
            Blocks.TERRACOTTA, 6
    )));
    private static final float[] LIME_COLOR_COMPONENTS = DyeColor.LIME.getColorComponents();

    private CompletableFuture<Void> solve;
    private final Cell[][] cells = new Cell[19][19];
    private final Switch[] switches = new Switch[]{new Switch(0), new Switch(1), new Switch(2), new Switch(3), new Switch(4), new Switch(5)};
    private final boolean[] doors = new boolean[5];
    private final Result[] results = new Result[64];
    private int currentCombination;
    private int bestCombination = -1;

    private Waterboard() {
        super("waterboard", "water-puzzle");
        UseBlockCallback.EVENT.register(this::onUseBlock);
        if (Debug.debugEnabled()) {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("dungeons").then(literal("puzzle").then(literal(puzzleName)
                    .then(literal("printBoard").executes(context -> {
                        context.getSource().sendFeedback(Constants.PREFIX.get().append(boardToString(cells)));
                        return Command.SINGLE_SUCCESS;
                    })).then(literal("printDoors").executes(context -> {
                        context.getSource().sendFeedback(Constants.PREFIX.get().append(Arrays.toString(INSTANCE.doors)));
                        return Command.SINGLE_SUCCESS;
                    })).then(literal("printSimulationResults").then(argument("combination", IntegerArgumentType.integer(0, 63)).executes(context -> {
                        context.getSource().sendFeedback(Constants.PREFIX.get().append(results[IntegerArgumentType.getInteger(context, "combination")].toString()));
                        return Command.SINGLE_SUCCESS;
                    }))).then(literal("printCurrentCombination").executes(context -> {
                        context.getSource().sendFeedback(Constants.PREFIX.get().append(Integer.toBinaryString(INSTANCE.currentCombination)));
                        return Command.SINGLE_SUCCESS;
                    })).then(literal("printBestCombination").executes(context -> {
                        context.getSource().sendFeedback(Constants.PREFIX.get().append(Integer.toBinaryString(INSTANCE.bestCombination)));
                        return Command.SINGLE_SUCCESS;
                    }))
            )))));
        }
    }

    public static void init() {
    }

    private static String boardToString(Cell[][] cells) {
        StringBuilder sb = new StringBuilder();
        for (Cell[] row : cells) {
            sb.append("\n");
            for (Cell cell : row) {
                if (cell == null) {
                    sb.append('?');
                } else if (cell instanceof SwitchCell switchCell) {
                    sb.append(switchCell.id);
                } else switch (cell.type) {
                    case BLOCK -> sb.append('#');
                    case EMPTY -> sb.append('.');
                }
            }
        }
        return sb.toString();
    }

    @Override
    public void tick(MinecraftClient client) {
        if (solve != null && !solve.isDone()) {
            return;
        }
        solve = CompletableFuture.runAsync(() -> {
            Changed changed = updateBoard(client.world);
            if (changed == Changed.NONE) {
                return;
            }
            if (results[0] == null) {
                updateSwitches();
                simulateCombinations();
                clearSwitches();
            }
            if (bestCombination < 0 || changed.doorChanged()) {
                bestCombination = findBestCombination();
            }
        }).exceptionally(e -> {
            LOGGER.error("[Skyblocker Waterboard] Encountered an unknown exception while solving waterboard.", e);
            return null;
        });
    }

    private Changed updateBoard(@Nullable World world) {
        if (world == null || !DungeonManager.isCurrentRoomMatched()) return Changed.NONE;

        Room room = DungeonManager.getCurrentRoom();
        BlockPos.Mutable pos = new BlockPos.Mutable(24, 78, 26);
        Changed changed = Changed.NONE;
        boolean[] changedSwitches = new boolean[6];
        for (int row = 0; row < cells.length; pos.move(cells[row].length, -1, 0), row++) {
            for (int col = 0; col < cells[row].length; pos.move(-1, 0, 0), col++) {
                Cell cell = parseBlock(world, room, pos);
                if (!cell.equals(cells[row][col])) {
                    if (cells[row][col] instanceof SwitchCell switchCell) {
                        changedSwitches[switchCell.id] = true;
                    }
                    cells[row][col] = cell;
                    changed = changed.onCellChanged();
                }
            }
        }
        pos.set(15, 57, 15);
        for (int i = 0; i < 5; i++, pos.move(0, 0, 1)) {
            boolean door = world.getBlockState(room.relativeToActual(pos)).isAir();
            if (doors[i] != door) {
                doors[i] = door;
                changed = changed.onDoorChanged();
            }
        }
        for (int switch_ = 0; switch_ < 6; switch_++) {
            if (changedSwitches[switch_]) {
                currentCombination ^= 1 << switch_;
            }
        }
        return changed;
    }

    private Cell parseBlock(World world, Room room, BlockPos.Mutable pos) {
        // Check if the block is a switch.
        BlockState state = world.getBlockState(room.relativeToActual(pos));
        int switch_ = SWITCHES.getInt(state.getBlock());
        if (switch_-- > 0) {
            return new SwitchCell(switch_);
        }
        // Check if the block is an opened switch by checking the block behind it.
        int switchBehind = SWITCHES.getInt(world.getBlockState(room.relativeToActual(pos.move(0, 0, 1))).getBlock());
        pos.move(0, 0, -1);
        if (switchBehind-- > 0) {
            return SwitchCell.ofOpened(switchBehind);
        }

        // Check if the block is empty otherwise the block is a wall.
        return state.isAir() || state.isOf(Blocks.WATER) ? Cell.EMPTY : Cell.BLOCK;
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
                        case 0 -> result.reachedDoors[4] = true;
                        case 4 -> result.reachedDoors[3] = true;
                        case 9 -> result.reachedDoors[2] = true;
                        case 14 -> result.reachedDoors[1] = true;
                        case 18 -> result.reachedDoors[0] = true;
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
                if (-leftFlowDownOffset <= ((WaterFluid) Fluids.WATER).getFlowSpeed(null) && -leftFlowDownOffset < rightFlowDownOffset) {
                    result.putPath(water, leftFlowDownOffset);
                    water.add(leftFlowDownOffset, 1);
                    continue;
                }
                // Check if right down is in range and closer than left down.
                if (rightFlowDownOffset <= ((WaterFluid) Fluids.WATER).getFlowSpeed(null) && rightFlowDownOffset < -leftFlowDownOffset) {
                    result.putPath(water, rightFlowDownOffset);
                    water.add(rightFlowDownOffset, 1);
                    continue;
                }

                // Else flow to both sides.
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
        for (int i = 0; water.x + i >= 0 && water.x + i < 19 && cells[water.y][water.x + i].isOpen(); i += direction ? 1 : -1) {
            if (cells[water.y + 1][water.x + i].isOpen()) {
                return i;
            }
        }
        return direction ? Integer.MAX_VALUE : Integer.MIN_VALUE + 1;
    }

    private int findBestCombination() {
        int bestCombination = -1;
        int bestScore = 0;
        boolean[] newDoors = new boolean[5];
        for (int combination = 0; combination < (1 << 6); combination++) {
            for (int i = 0; i < 5; i++) {
                newDoors[i] = results[combination].reachedDoors[i] ^ doors[i];
            }
            int newScore = Booleans.countTrue(newDoors);
            if (newScore > bestScore) {
                bestCombination = combination;
            }
        }
        return bestCombination;
    }

    @Override
    public void render(WorldRenderContext context) {
        if (!DungeonManager.isCurrentRoomMatched()) return;
        Room room = DungeonManager.getCurrentRoom();

        // Render the current path of the water.
        if (currentCombination > 0) {
            Result currentResult = results[currentCombination];
            if (currentResult != null) {
                for (Map.Entry<Vector2ic, Integer> entry : currentResult.path.entries()) {
                    BlockPos.Mutable pos = new BlockPos.Mutable(24 - entry.getKey().x(), 78 - entry.getKey().y(), 26);
                    BlockPos start = room.relativeToActual(pos);
                    BlockPos middle = room.relativeToActual(pos.move(Direction.WEST, entry.getValue()));
                    BlockPos end = room.relativeToActual(pos.move(Direction.DOWN));
                    RenderHelper.renderLinesFromPoints(context, new Vec3d[]{Vec3d.ofCenter(start), Vec3d.ofCenter(middle), Vec3d.ofCenter(end)}, LIME_COLOR_COMPONENTS, 1f, 5f, true);
                }
            }
        }
    }

    private ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, HitResult result) {
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
        Arrays.fill(doors, false);
        Arrays.fill(results, null);
        currentCombination = 0;
        bestCombination = -1;
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

    public record Result(boolean[] reachedDoors, Multimap<Vector2ic, Integer> path) {
        public Result() {
            this(new boolean[5], MultimapBuilder.hashKeys().arrayListValues().build());
        }

        public boolean putPath(Vector2i water, int offset) {
            return path.put(new Vector2i(water), offset);
        }

        @Override
        public String toString() {
            return "Result[reachedDoors=" + Arrays.toString(reachedDoors) + ", path=" + path + ']';
        }
    }
}
