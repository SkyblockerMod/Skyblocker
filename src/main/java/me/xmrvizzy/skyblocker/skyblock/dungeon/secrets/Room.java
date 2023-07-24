package me.xmrvizzy.skyblocker.skyblock.dungeon.secrets;

import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.ints.IntSortedSets;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Room {
    public static final String UNKNOWN_NAME = "Unknown";
    private final Type type;
    private final Set<Vector2ic> segments;
    private final Shape shape;
    private HashMap<String, int[]> roomsData;
    private List<MutableTriple<Direction, Vector2ic, List<String>>> possibleRooms = new ArrayList<>();
    private Set<BlockPos> checkedBlocks = new HashSet<>();
    private CompletableFuture<Void> findRoom;
    private String name;
    private Direction direction;
    private Vector2ic corner;

    public Room(Type type, Vector2ic... physicalPositions) {
        this.type = type;
        segments = Set.of(physicalPositions);
        IntSortedSet segmentsX = IntSortedSets.unmodifiable(new IntRBTreeSet(segments.stream().mapToInt(Vector2ic::x).toArray()));
        IntSortedSet segmentsY = IntSortedSets.unmodifiable(new IntRBTreeSet(segments.stream().mapToInt(Vector2ic::y).toArray()));
        shape = getShape(segmentsX, segmentsY);
        roomsData = DungeonSecrets.ROOMS_DATA.get("catacombs").get(shape.shape);
        List<String> possibleDirectionRooms = new ArrayList<>(roomsData.keySet());
        for (Direction direction : getPossibleDirections(segmentsX, segmentsY)) {
            possibleRooms.add(MutableTriple.of(direction, DungeonMapUtils.getPhysicalCornerPos(direction, segmentsX, segmentsY), possibleDirectionRooms));
        }
        DungeonSecrets.LOGGER.info("Created {}", this); // TODO change to debug
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Room{type=" + type + ", shape=" + shape + ", name='" + name + "', direction=" + direction + ", corner=" + corner + ", segments=" + Arrays.toString(segments.toArray()) + "}";
    }

    private Shape getShape(IntSortedSet segmentsX, IntSortedSet segmentsY) {
        int segmentsSize = segments.size();
        if (segmentsSize == 1) {
            return Shape.ONE_BY_ONE;
        }
        if (segmentsSize == 2) {
            return Shape.ONE_BY_TWO;
        }
        if (segmentsSize == 3) {
            if (segmentsX.size() == 2 && segmentsY.size() == 2) {
                return Shape.L_SHAPE;
            }
            return Shape.ONE_BY_THREE;
        }
        if (segmentsSize == 4) {
            if (segmentsX.size() == 2 && segmentsY.size() == 2) {
                return Shape.TWO_BY_TWO;
            }
            return Shape.ONE_BY_FOUR;
        }
        throw new IllegalArgumentException("There are no matching room shapes with this set of physical positions: " + Arrays.toString(segments.toArray()));
    }

    private Direction[] getPossibleDirections(IntSortedSet segmentsX, IntSortedSet segmentsY) {
        return switch (shape) {
            case ONE_BY_ONE, TWO_BY_TWO -> Direction.values();
            case ONE_BY_TWO, ONE_BY_THREE, ONE_BY_FOUR -> {
                if (segmentsX.size() > 1 && segmentsY.size() == 1) {
                    yield new Direction[]{Direction.NW, Direction.SE};
                } else if (segmentsX.size() == 1 && segmentsY.size() > 1) {
                    yield new Direction[]{Direction.NE, Direction.SW};
                }
                throw new IllegalStateException("Shape " + shape.shape + " does not match segments: " + Arrays.toString(segments.toArray()));
            }
            case L_SHAPE -> {
                if (!segments.contains(new Vector2i(segmentsX.firstInt(), segmentsY.firstInt()))) {
                    yield new Direction[]{Direction.SW};
                } else if (!segments.contains(new Vector2i(segmentsX.firstInt(), segmentsY.lastInt()))) {
                    yield new Direction[]{Direction.SE};
                } else if (!segments.contains(new Vector2i(segmentsX.lastInt(), segmentsY.firstInt()))) {
                    yield new Direction[]{Direction.NW};
                } else if (!segments.contains(new Vector2i(segmentsX.lastInt(), segmentsY.lastInt()))) {
                    yield new Direction[]{Direction.NE};
                }
                throw new IllegalArgumentException("Shape " + shape.shape + " does not match segments: " + Arrays.toString(segments.toArray()));
            }
        };
    }

    public void update() {
        // Logical AND has higher precedence than logical OR
        if (name != null || !DungeonSecrets.isRoomsLoaded() || findRoom != null && !findRoom.isDone()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        if (player == null || world == null) {
            return;
        }
        findRoom = CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            for (BlockPos pos : BlockPos.iterate(player.getBlockPos().add(-5, -5, -5), player.getBlockPos().add(5, 5, 5))) {
                if (checkedBlocks.add(pos) && checkBlock(world, pos)) {
                    reset();
                    break;
                }
            }
            long endTime = System.currentTimeMillis();
            DungeonSecrets.LOGGER.info("[Skyblocker] Processed room in {} ms", endTime - startTime); // TODO change to debug
        });
    }

    private boolean checkBlock(ClientWorld world, BlockPos pos) {
        byte id = DungeonSecrets.NUMERIC_ID.getByte(Registries.BLOCK.getId(world.getBlockState(pos).getBlock()).toString());
        if (id == 0) {
            return false;
        }
        for (MutableTriple<Direction, Vector2ic, List<String>> directionRooms : possibleRooms) {
            Direction direction = directionRooms.getLeft();
            BlockPos relative = DungeonMapUtils.actualToRelative(directionRooms.getMiddle(), direction, pos);
            int block = posIdToInt(relative, id);
            List<String> possibleDirectionRooms = new ArrayList<>();
            for (String room : directionRooms.getRight()) {
                if (Arrays.binarySearch(roomsData.get(room), block) >= 0) {
                    possibleDirectionRooms.add(room);
                }
            }
            directionRooms.setRight(possibleDirectionRooms);
        }

        int matchingRoomsSize = possibleRooms.stream().map(Triple::getRight).mapToInt(Collection::size).sum();
        if (matchingRoomsSize == 0) {
            DungeonSecrets.LOGGER.warn("[Skyblocker] No dungeon room matches after checking {} block(s)", checkedBlocks.size());
            name = UNKNOWN_NAME;
            return true;
        } else if (matchingRoomsSize == 1) {
            for (Triple<Direction, Vector2ic, List<String>> directionRooms : possibleRooms) {
                if (directionRooms.getRight().size() == 1) {
                    name = directionRooms.getRight().get(0);
                    direction = directionRooms.getLeft();
                    corner = directionRooms.getMiddle();
                }
            }
            DungeonSecrets.LOGGER.info("[Skyblocker] Room {} matched after checking {} block(s)", name, checkedBlocks.size()); // TODO change to debug
            return true;
        } else {
            DungeonSecrets.LOGGER.info("[Skyblocker] {} rooms remaining after checking {} block(s)", matchingRoomsSize, checkedBlocks.size()); // TODO change to debug
            return false;
        }
    }

    private int posIdToInt(BlockPos pos, byte id) {
        return pos.getX() << 24 | pos.getY() << 16 | pos.getZ() << 8 | id;
    }

    /**
     * Resets fields after room matching completes, where either a room is found or none matched.
     * These fields are no longer needed and are discarded to save memory.
     */
    private void reset() {
        roomsData = null;
        possibleRooms = null;
        checkedBlocks = null;
    }

    public enum Type {
        ENTRANCE(MapColor.DARK_GREEN.getRenderColorByte(MapColor.Brightness.HIGH)),
        ROOM(MapColor.ORANGE.getRenderColorByte(MapColor.Brightness.LOWEST)),
        PUZZLE(MapColor.MAGENTA.getRenderColorByte(MapColor.Brightness.HIGH)),
        TRAP(MapColor.ORANGE.getRenderColorByte(MapColor.Brightness.HIGH)),
        MINIBOSS(MapColor.YELLOW.getRenderColorByte(MapColor.Brightness.HIGH)),
        FAIRY(MapColor.PINK.getRenderColorByte(MapColor.Brightness.HIGH)),
        BLOOD(MapColor.BRIGHT_RED.getRenderColorByte(MapColor.Brightness.HIGH)),
        UNKNOWN(MapColor.GRAY.getRenderColorByte(MapColor.Brightness.NORMAL));
        final byte color;

        Type(byte color) {
            this.color = color;
        }
    }

    public enum Shape {
        ONE_BY_ONE("1x1"),
        ONE_BY_TWO("1x2"),
        ONE_BY_THREE("1x3"),
        ONE_BY_FOUR("1x4"),
        L_SHAPE("L-shape"),
        TWO_BY_TWO("2x2");
        final String shape;

        Shape(String shape) {
            this.shape = shape;
        }

        @Override
        public String toString() {
            return shape;
        }
    }

    public enum Direction {
        NW, NE, SW, SE
    }
}
