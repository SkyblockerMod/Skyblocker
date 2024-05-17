package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaminaTestHelper {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final int WALL_THRESHOLD_VALUE = 13;
    private static final float[] INCOMING_COLOR = new float[]{0f, 1f, 0f, 0f};
    private static final float[] OUTGOING_COLOR = new float[]{1f, 0.64f, 0f, 0f};

    private static final List<BlockPos> wallHoles = new ArrayList<>();
    private static final List<BlockPos> lastHoles = new ArrayList<>();
    private static final Map<BlockPos, holeDirection> holeDirections = new HashMap<>();
    private static BlockPos middleBase;

    private enum holeDirection {
        POSITIVE_X,
        POSITIVE_Z,
        NEGATIVE_X,
        NEGATIVE_Z,
        NEW,
        UNCHANGED;
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
            wallHoles.addAll(findAirInBox(wall));
        }
        // get direction for the holes
        Map<BlockPos, holeDirection> lastHoleDirections = new HashMap<>(holeDirections);
        holeDirections.clear();
        for (BlockPos hole : wallHoles) {
            holeDirection holeDirection = getWholeDirection(hole);
            if (holeDirection == StaminaTestHelper.holeDirection.UNCHANGED) {
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
        Map<Integer, List<BlockPos>> possibleWallsX = new HashMap<>();
        Map<Integer, List<BlockPos>> possibleWallsZ = new HashMap<>();
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
            maxPos = new BlockPos(maxPos.getX(), maxPos.getY() + 5, maxPos.getZ());

            wallBoxes.add(Box.enclosing(minPos, maxPos));
        }

        return wallBoxes;
    }

    private static List<BlockPos> findAirInBox(Box box) {
        List<BlockPos> air = new ArrayList<>();
        if (CLIENT == null || CLIENT.player == null || CLIENT.world == null) {
            return air;
        }
        for (int x = (int) box.minX; x < box.maxX; x++) {
            for (int y = (int) box.minY; y < box.maxY; y++) {
                for (int z = (int) box.minZ; z < box.maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = CLIENT.world.getBlockState(pos);
                    if (state.isAir()) {
                        air.add(pos);
                    }
                }
            }
        }
        return air;
    }

    private static List<Box> combineAir(List<BlockPos> airLocations) {
        //todo
        List<Box> holes = new ArrayList<>();
        //check if air is conected to existing whole and if so
        for (BlockPos airLocation : airLocations) {
            holes.add(Box.enclosing(airLocation, airLocation));
        }
        return holes;
    }

    private static holeDirection getWholeDirection(BlockPos hole) {
        //the value has not changed since last time
        if (lastHoles.contains(hole)) {
            return holeDirection.UNCHANGED;
        }
        //check each direction to work out which way the whole is going
        BlockPos posX = hole.add(1, 0, 0);
        if (lastHoles.contains(posX)) {
            return holeDirection.POSITIVE_X;
        }
        BlockPos negX = hole.add(-1, 0, 0);
        if (lastHoles.contains(negX)) {
            System.out.println("positiveX");
            return holeDirection.NEGATIVE_X;
        }
        BlockPos posZ = hole.add(0, 0, 1);
        if (lastHoles.contains(posZ)) {
            return holeDirection.POSITIVE_Z;
        }
        BlockPos negZ = hole.add(0, 0, -1);
        if (lastHoles.contains(negZ)) {
            return holeDirection.NEGATIVE_Z;
        }
        // if pos can not be found mark as new
        return holeDirection.NEW;

    }

    protected static void render(WorldRenderContext context) {
        if (wallHoles.isEmpty() || CLIENT == null || CLIENT.player == null) {
            return;
        }
        BlockPos playerPos = CLIENT.player.getBlockPos();
        for (BlockPos hole : wallHoles) {
            float[] color = isHoleIncoming(hole, holeDirections.get(hole), playerPos) ? INCOMING_COLOR : OUTGOING_COLOR;
            RenderHelper.renderFilled(context, hole, color, 0.3f, true);
        }
    }

    private static boolean isHoleIncoming(BlockPos holePos, holeDirection holeDirection, BlockPos playerPos) {
        return switch (holeDirection) {
            case POSITIVE_X -> playerPos.getX() < holePos.getX();
            case POSITIVE_Z -> playerPos.getZ() < holePos.getZ();
            case NEGATIVE_X -> playerPos.getX() > holePos.getX();
            case NEGATIVE_Z -> playerPos.getZ() > holePos.getZ();

            default -> true;
        };
    }
}
