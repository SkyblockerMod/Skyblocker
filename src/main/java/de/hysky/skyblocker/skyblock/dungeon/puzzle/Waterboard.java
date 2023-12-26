package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import com.mojang.brigadier.Command;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.utils.Constants;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Waterboard extends DungeonPuzzle {
    public static final Waterboard INSTANCE = new Waterboard();
    private static final Object2IntMap<Block> SWITCHES = Object2IntMaps.unmodifiable(new Object2IntOpenHashMap<>(Map.of(
            Blocks.COAL_BLOCK, 1,
            Blocks.GOLD_BLOCK, 2,
            Blocks.QUARTZ_BLOCK, 3,
            Blocks.DIAMOND_BLOCK, 4,
            Blocks.EMERALD_BLOCK, 5,
            Blocks.TERRACOTTA, 6
    )));

    private CompletableFuture<Void> solve;
    private final Cell[][] cells = new Cell[18][19];
    private final List<List<Cell>> switches = List.of(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    private final boolean[] doors = new boolean[5];

    private Waterboard() {
        super("waterboard", "water-puzzle");
        if (Debug.debugEnabled()) {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("dungeons").then(literal("puzzle").then(literal(puzzleName)
                    .then(literal("printBoard").executes(context -> {
                        context.getSource().sendFeedback(Constants.PREFIX.get().append(boardToString(cells)));
                        return Command.SINGLE_SUCCESS;
                    })).then(literal("printDoors").executes(context -> {
                        context.getSource().sendFeedback(Constants.PREFIX.get().append(Arrays.toString(INSTANCE.doors)));
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
                } else switch (cell.type()) {
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
            if (client.world == null || !DungeonManager.isCurrentRoomMatched()) return;

            Room room = DungeonManager.getCurrentRoom();
            BlockPos.Mutable pos = new BlockPos.Mutable(24, 78, 26);
            for (int row = 0; row < cells.length; row++, pos.move(cells[row].length, -1, 0)) {
                for (int col = 0; col < cells[row].length; col++, pos.move(-1, 0, 0)) {
                    // Check if the block is a switch.
                    BlockState state = client.world.getBlockState(room.relativeToActual(pos));
                    int switch_ = SWITCHES.getInt(state.getBlock());
                    if (switch_-- > 0) {
                        cells[row][col] = new SwitchCell(switch_);
                        switches.get(switch_).add(cells[row][col]);
                        continue;
                    }
                    // Check if the block is an opened switch by checking the block behind it.
                    int switchBehind = SWITCHES.getInt(client.world.getBlockState(room.relativeToActual(pos.move(0, 0, 1))).getBlock());
                    pos.move(0, 0, -1);
                    if (switchBehind-- > 0) {
                        cells[row][col] = SwitchCell.ofOpened(switchBehind);
                        switches.get(switchBehind).add(cells[row][col]);
                        continue;
                    }

                    // Check if the block is empty.
                    if (state.isAir() || state.isOf(Blocks.WATER)) {
                        cells[row][col] = Cell.EMPTY;
                        continue;
                    }
                    // Otherwise the block is a wall.
                    cells[row][col] = Cell.BLOCK;
                }
            }
            pos.set(15, 57, 15);
            for (int i = 0; i < 5; i++, pos.move(0, 0, 1)) {
                doors[i] = client.world.getBlockState(room.relativeToActual(pos)).isAir();
            }
        });
    }

    @Override
    public void render(WorldRenderContext context) {
    }

    @Override
    public void reset() {
        super.reset();
        solve = null;
        for (Cell[] row : cells) {
            Arrays.fill(row, null);
        }
        for (List<Cell> switch_ : switches) {
            switch_.clear();
        }
        Arrays.fill(doors, false);
    }

    private static class Cell {
        private static final Cell BLOCK = new Cell(CellType.BLOCK);
        private static final Cell EMPTY = new Cell(CellType.EMPTY);
        private final CellType type;

        private Cell(CellType type) {
            this.type = type;
        }

        public CellType type() {
            return type;
        }

        public boolean isOpen() {
            return type == CellType.EMPTY;
        }
    }

    private static class SwitchCell extends Cell {
        private final int id;
        private boolean open;

        private SwitchCell(int id) {
            super(CellType.SWITCH);
            this.id = id;
        }

        private static SwitchCell ofOpened(int id) {
            SwitchCell switchCell = new SwitchCell(id);
            switchCell.open = true;
            return switchCell;
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        public void toggle() {
            open = !open;
        }
    }

    private enum CellType {
        BLOCK,
        EMPTY,
        SWITCH
    }
}
