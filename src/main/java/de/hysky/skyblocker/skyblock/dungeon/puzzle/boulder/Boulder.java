package de.hysky.skyblocker.skyblock.dungeon.puzzle.boulder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.puzzle.DungeonPuzzle;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.title.Title;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

public class Boulder extends DungeonPuzzle {
    @SuppressWarnings("unused")
	private static final Boulder INSTANCE = new Boulder();
    private static final float[] RED_COLOR_COMPONENTS = ColorUtils.getFloatComponents(DyeColor.RED);
    private static final float[] ORANGE_COLOR_COMPONENTS = ColorUtils.getFloatComponents(DyeColor.ORANGE);
    private static final int BASE_Y = 65;
    static Vec3d[] linePoints;
    static Box boundingBox;

    private Boulder() {
        super("boulder", "boxes-room");
    }

    @Init
    public static void init() {
    }

    @Override
    public void tick(MinecraftClient client) {

        if (!shouldSolve() || !SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveBoulder || client.world == null || !DungeonManager.isCurrentRoomMatched()) {
            return;
        }

        Room room = DungeonManager.getCurrentRoom();

        BlockPos chestPos = new BlockPos(15, BASE_Y, 29);
        BlockPos start = new BlockPos(25, BASE_Y, 25);
        BlockPos end = new BlockPos(5, BASE_Y, 8);
        // Create a target BoulderObject for the puzzle
        BoulderObject target = new BoulderObject(chestPos.getX(), chestPos.getX(), chestPos.getZ(), "T");
        // Create a BoulderBoard representing the puzzle's grid
        BoulderBoard board = new BoulderBoard(8, 7, target);

        // Populate the BoulderBoard grid with BoulderObjects based on block types in the room
        int column = 1;
        for (int z = start.getZ(); z > end.getZ(); z--) {
            int row = 0;
            for (int x = start.getX(); x > end.getX(); x--) {
                if (Math.abs(start.getX() - x) % 3 == 1 && Math.abs(start.getZ() - z) % 3 == 1) {
                    String blockType = getBlockType(client.world, x, BASE_Y, z);
                    board.placeObject(column, row, new BoulderObject(x, BASE_Y, z, blockType));
                    row++;
                }
            }
            if (row == board.getWidth()) {
                column++;
            }
        }

        // Generate initial game states for the A* solver
        char[][] boardArray = board.getBoardCharArray();
        List<BoulderSolver.GameState> initialStates = Arrays.asList(
                new BoulderSolver.GameState(boardArray, board.getHeight() - 1, 0),
                new BoulderSolver.GameState(boardArray, board.getHeight() - 1, 1),
                new BoulderSolver.GameState(boardArray, board.getHeight() - 1, 2),
                new BoulderSolver.GameState(boardArray, board.getHeight() - 1, 3),
                new BoulderSolver.GameState(boardArray, board.getHeight() - 1, 4),
                new BoulderSolver.GameState(boardArray, board.getHeight() - 1, 5),
                new BoulderSolver.GameState(boardArray, board.getHeight() - 1, 6)
        );

        // Solve the puzzle using the A* algorithm
        List<int[]> solution = BoulderSolver.aStarSolve(initialStates);

        if (solution != null) {
            linePoints = new Vec3d[solution.size()];
            int index = 0;
            // Convert solution coordinates to Vec3d points for rendering
            for (int[] coord : solution) {
                int x = coord[0];
                int y = coord[1];
                // Convert relative coordinates to actual coordinates
                linePoints[index++] = Vec3d.ofCenter(room.relativeToActual(board.getObject3DPosition(x, y)));
            }

            BlockPos button = null;
            if (linePoints != null && linePoints.length > 0) {
                // Check for buttons along the path of the solution
                for (int i = 0; i < linePoints.length - 1; i++) {
                    Vec3d point1 = linePoints[i];
                    Vec3d point2 = linePoints[i + 1];
                    button = checkForButtonBlocksOnLine(client.world, point1, point2);
                    if (button != null) {
                        // If a button is found, calculate its bounding box
                        boundingBox = RenderHelper.getBlockBoundingBox(client.world, button);
                        break;
                    }
                }
                if (button == null){
                    // If no button is found along the path the puzzle is solved; reset the puzzle
                    reset();
                }
            }
        } else {
            // If no solution is found, display a title message and reset the puzzle
            Title title = new Title("skyblocker.dungeons.puzzle.boulder.noSolution", Formatting.GREEN);
            RenderHelper.displayInTitleContainerAndPlaySound(title, 15);
            reset();
        }
    }

    /**
     * Retrieves the type of block at the specified position in the world.
     * If the block is Birch or Jungle plank, it will return "B"; otherwise, it will return ".".
     *
     * @param world The client world.
     * @param x     The x-coordinate of the block.
     * @param y     The y-coordinate of the block.
     * @param z     The z-coordinate of the block.
     * @return The type of block at the specified position.
     */
    public static String getBlockType(ClientWorld world, int x, int y, int z) {
        Block block = world.getBlockState(DungeonManager.getCurrentRoom().relativeToActual(new BlockPos(x, y, z))).getBlock();
        return (block == Blocks.BIRCH_PLANKS || block == Blocks.JUNGLE_PLANKS) ? "B" : ".";
    }

    /**
     * Checks for blocks along the line between two points in the world.
     * Returns the position of a block if it found a button on the line, if any.
     *
     * @param world   The client world.
     * @param point1  The starting point of the line.
     * @param point2  The ending point of the line.
     * @return The position of the block found on the line, or null if no block is found.
     */
    private static BlockPos checkForButtonBlocksOnLine(ClientWorld world, Vec3d point1, Vec3d point2) {
        double x1 = point1.getX();
        double y1 = point1.getY() + 1;
        double z1 = point1.getZ();

        double x2 = point2.getX();
        double y2 = point2.getY() + 1;
        double z2 = point2.getZ();

        int steps = (int) Math.max(Math.abs(x2 - x1), Math.max(Math.abs(y2 - y1), Math.abs(z2 - z1)));

        double xStep = (x2 - x1) / steps;
        double yStep = (y2 - y1) / steps;
        double zStep = (z2 - z1) / steps;


        for (int step = 0; step <= steps; step++) {
            double currentX = x1 + step * xStep;
            double currentY = y1 + step * yStep;
            double currentZ = z1 + step * zStep;

            BlockPos blockPos = BlockPos.ofFloored(currentX, currentY, currentZ);
            Block block = world.getBlockState(blockPos).getBlock();

            if (block == Blocks.STONE_BUTTON) {
                return blockPos;
            }

        }
        return null;
    }

    @Override
    public void render(WorldRenderContext context) {
        if (!shouldSolve() || !SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveBoulder || !DungeonManager.isCurrentRoomMatched())
            return;
        float alpha = 1.0f;
        float lineWidth = 5.0f;

        if (linePoints != null && linePoints.length > 0) {
            for (int i = 0; i < linePoints.length - 1; i++) {
                Vec3d startPoint = linePoints[i];
                Vec3d endPoint = linePoints[i + 1];
                RenderHelper.renderLinesFromPoints(context, new Vec3d[]{startPoint, endPoint}, ORANGE_COLOR_COMPONENTS, alpha, lineWidth, true);
            }
            if (boundingBox != null) {
                RenderHelper.renderOutline(context, boundingBox, RED_COLOR_COMPONENTS, 5, false);
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
        linePoints = null;
        boundingBox = null;
    }
}
