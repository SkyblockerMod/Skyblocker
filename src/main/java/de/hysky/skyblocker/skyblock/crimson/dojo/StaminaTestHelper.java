package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.utils.render.RenderHelper;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaminaTestHelper {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final int WALL_THRESHOLD_VALUE = 13;
    private static final int WALL_HEIGHT = 5;
    private static final float[] INCOMING_COLOR = new float[]{0f, 1f, 0f, 0f};
    private static final float[] OUTGOING_COLOR = new float[]{1f, 0.64f, 0f, 0f};

    private static final List<Box> wallHoles = new ArrayList<>();
    private static final List<Box> lastHoles = new ArrayList<>();
    private static final Map<Box, HoleDirection> holeDirections = new HashMap<>();
    private static BlockPos middleBase;

    private enum HoleDirection {
        POSITIVE_X,
        POSITIVE_Z,
        NEGATIVE_X,
        NEGATIVE_Z,
        NEW,
        UNCHANGED
    }

    protected static void reset() {
        wallHoles.clear();
        lastHoles.clear();
        holeDirections.clear();
        middleBase = null;
    }

    protected static void update() {

        //search the world around the player for walls 30 x 10 x 30 area centered on player

        List<BlockPos> currentBottomWallLocations = findWallBlocks();
        if (currentBottomWallLocations == null) { //stop here if the center pos has not been found
            return;
        }
        //find walls
        List<Box> walls = findWalls(currentBottomWallLocations);

        //find air then holes and add whole to list
        lastHoles.clear();
        lastHoles.addAll(wallHoles);
        wallHoles.clear();
        for (Box wall : walls) {
            wallHoles.addAll(findHolesInBox(wall));
        }
        // get direction for the holes
        Map<Box, HoleDirection> lastHoleDirections = new HashMap<>(holeDirections);
        holeDirections.clear();
        for (Box hole : wallHoles) {
            HoleDirection holeDirection = getWholeDirection(hole);
            if (holeDirection == HoleDirection.UNCHANGED) {
                holeDirections.put(hole, lastHoleDirections.get(hole));
                continue;
            }
            holeDirections.put(hole, holeDirection);
        }
    }

    /**
     * Locates the center of the game and once this is found scans the bottom of room for blocks that make up the walls
     *
     * @return list of blocks that make up the bottom of the walls
     */
    private static List<BlockPos> findWallBlocks() {
        if (CLIENT == null || CLIENT.player == null || CLIENT.world == null) {
            return null;
        }
        BlockPos playerPos = CLIENT.player.getBlockPos();
        //find the center first before starting to look for walls
        if (middleBase == null) {
            for (int x = playerPos.getX() - 10; x < playerPos.getX() + 10; x++) {
                for (int y = playerPos.getY() - 5; y < playerPos.getY(); y++) {
                    for (int z = playerPos.getZ() - 10; z < playerPos.getZ() + 10; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState state = CLIENT.world.getBlockState(pos);
                        if (state.isOf(Blocks.CHISELED_STONE_BRICKS)) {
                            middleBase = pos;
                            return null;
                        }
                    }
                }
            }
            return null;
        }
        List<BlockPos> currentBottomWallLocations = new ArrayList<>();
        for (int x = middleBase.getX() - 15; x < middleBase.getX() + 15; x++) {
            for (int z = middleBase.getZ() - 15; z < middleBase.getZ() + 15; z++) {
                BlockPos pos = new BlockPos(x, middleBase.getY() + 1, z);
                BlockState state = CLIENT.world.getBlockState(pos);
                //find the bottom of walls
                if (!state.isAir()) {
                    currentBottomWallLocations.add(pos);
                }
            }
        }
        return currentBottomWallLocations;
    }

    private static List<Box> findWalls(List<BlockPos> currentBottomWallLocations) {
        Int2ObjectOpenHashMap<List<BlockPos>> possibleWallsX = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<List<BlockPos>> possibleWallsZ = new Int2ObjectOpenHashMap<>();
        for (BlockPos block : currentBottomWallLocations) {
            //add to the x walls
            int x = block.getX();
            if (!possibleWallsX.containsKey(x)) {
                possibleWallsX.put(x, new ArrayList<>());

            }
            possibleWallsX.get(x).add(block);
            //add to the z walls
            int z = block.getZ();
            if (!possibleWallsZ.containsKey(z)) {
                possibleWallsZ.put(z, new ArrayList<>());
            }
            possibleWallsZ.get(z).add(block);
        }

        //extract only the lines that are long enough to be a wall and not from walls overlapping
        List<List<BlockPos>> walls = new ArrayList<>();
        for (List<BlockPos> line : possibleWallsX.values()) {
            if (line.size() >= WALL_THRESHOLD_VALUE) {
                walls.add(line);
            }
        }
        for (List<BlockPos> line : possibleWallsZ.values()) {
            if (line.size() >= WALL_THRESHOLD_VALUE) {
                walls.add(line);
            }
        }

        //final find the maximum values for each wall to output a box for them
        List<Box> wallBoxes = new ArrayList<>();
        for (List<BlockPos> wall : walls) {
            BlockPos minPos = wall.getFirst();
            BlockPos maxPos = wall.getFirst();
            for (BlockPos pos : wall) {
                if (pos.getX() < minPos.getX()) {
                    minPos = new BlockPos(pos.getX(), minPos.getY(), minPos.getZ());
                }
                if (pos.getZ() < minPos.getZ()) {
                    minPos = new BlockPos(minPos.getX(), minPos.getY(), pos.getZ());
                }

                if (pos.getX() > maxPos.getX()) {
                    maxPos = new BlockPos(pos.getX(), maxPos.getY(), maxPos.getZ());
                }
                if (pos.getZ() > maxPos.getZ()) {
                    maxPos = new BlockPos(maxPos.getX(), maxPos.getY(), pos.getZ());
                }
            }
            //expand wall to top
            maxPos = new BlockPos(maxPos.getX(), maxPos.getY() + WALL_HEIGHT, maxPos.getZ());

            wallBoxes.add(Box.enclosing(minPos, maxPos));
        }

        return wallBoxes;
    }

    private static List<Box> findHolesInBox(Box box) {
        List<Box> holes = new ArrayList<>();
        if (CLIENT == null || CLIENT.player == null || CLIENT.world == null) {
            return holes;
        }
        //get the direction vector
        Vec3i wallDirection = box.getLengthX() == 1 ? new Vec3i(0, 0, 1) : new Vec3i(1, 0, 0);
        //find the corners of boxes (only need 3)
        List<BlockPos> topLeft = new ArrayList<>();
        List<BlockPos> topRight = new ArrayList<>();
        List<BlockPos> bottomLeft = new ArrayList<>();
        for (int z = (int) box.minZ; z < box.maxZ; z++) {
            for (int x = (int) box.minX; x < box.maxX; x++) {
                for (int y = (int) box.minY; y < box.maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = CLIENT.world.getBlockState(pos);
                    if (!state.isAir()) {
                        //do not check non-air
                        continue;
                    }
                    boolean top = y == box.maxY - 1 || !CLIENT.world.getBlockState(pos.add(0, 1, 0)).isAir();
                    boolean bottom = !CLIENT.world.getBlockState(pos.add(0, -1, 0)).isAir();
                    boolean left = !CLIENT.world.getBlockState(pos.add(wallDirection)).isAir();
                    boolean right = !CLIENT.world.getBlockState(pos.subtract(wallDirection)).isAir();
                    if (top) {
                        if (left) {
                            topLeft.add(pos);
                        }
                        if (right) {
                            topRight.add(pos);
                        }
                    }
                    if (bottom && left) {
                        bottomLeft.add(pos);
                    }

                }
            }
        }
        // gets box around top of hole then expands to the bottom of hole
        for (int i = 0; i < topLeft.size(); i++) {
            if (topRight.size() <= i || bottomLeft.size() <= i) {
                //if corners can not be found end looking
                break;
            }
            Box hole = Box.enclosing(topLeft.get(i), topRight.get(i));
            hole = hole.stretch(0, bottomLeft.get(i).getY() - topLeft.get(i).getY(), 0);
            holes.add(hole);
        }
        return holes;
    }

    private static HoleDirection getWholeDirection(Box hole) {
        //the value has not changed since last time
        if (lastHoles.contains(hole)) {
            return HoleDirection.UNCHANGED;
        }
        //check each direction to work out which way the whole is going
        Box posX = hole.offset(1, 0, 0);
        if (lastHoles.contains(posX)) {
            return HoleDirection.POSITIVE_X;
        }
        Box negX = hole.offset(-1, 0, 0);
        if (lastHoles.contains(negX)) {
            return HoleDirection.NEGATIVE_X;
        }
        Box posZ = hole.offset(0, 0, 1);
        if (lastHoles.contains(posZ)) {
            return HoleDirection.POSITIVE_Z;
        }
        Box negZ = hole.offset(0, 0, -1);
        if (lastHoles.contains(negZ)) {
            return HoleDirection.NEGATIVE_Z;
        }
        // if pos can not be found mark as new
        return HoleDirection.NEW;

    }

    protected static void render(WorldRenderContext context) {
        if (wallHoles.isEmpty() || CLIENT == null || CLIENT.player == null) {
            return;
        }
        BlockPos playerPos = CLIENT.player.getBlockPos();
        for (Box hole : wallHoles) {
            float[] color = isHoleIncoming(hole, holeDirections.get(hole), playerPos) ? INCOMING_COLOR : OUTGOING_COLOR;
            RenderHelper.renderFilled(context, hole, color, 0.3f, false);
        }
    }

    private static boolean isHoleIncoming(Box holePos, HoleDirection holeDirection, BlockPos playerPos) {
        return switch (holeDirection) {
            case POSITIVE_X -> playerPos.getX() < holePos.minX;
            case POSITIVE_Z -> playerPos.getZ() < holePos.minZ;
            case NEGATIVE_X -> playerPos.getX() > holePos.maxX;
            case NEGATIVE_Z -> playerPos.getZ() > holePos.maxZ;

            default -> true;
        };
    }
}
