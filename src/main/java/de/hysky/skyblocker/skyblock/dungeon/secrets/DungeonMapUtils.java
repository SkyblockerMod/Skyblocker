package de.hysky.skyblocker.skyblock.dungeon.secrets;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.item.map.MapState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.RoundingMode;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.*;

public class DungeonMapUtils {
    public static final byte BLACK_COLOR = MapColor.BLACK.getRenderColorByte(MapColor.Brightness.LOWEST);
    public static final byte WHITE_COLOR = MapColor.WHITE.getRenderColorByte(MapColor.Brightness.HIGH);

    public static byte getColor(MapState map, @Nullable Vector2ic pos) {
        return pos == null ? -1 : getColor(map, pos.x(), pos.y());
    }

    public static byte getColor(MapState map, int x, int z) {
        if (x < 0 || z < 0 || x >= 128 || z >= 128) {
            return -1;
        }
        return map.colors[x + (z << 7)];
    }

    public static boolean isEntranceColor(MapState map, int x, int z) {
        return getColor(map, x, z) == Room.Type.ENTRANCE.color;
    }

    public static boolean isEntranceColor(MapState map, @Nullable Vector2ic pos) {
        return getColor(map, pos) == Room.Type.ENTRANCE.color;
    }

    @Nullable
    private static Vector2i getMapPlayerPos(MapState map) {
        for (MapDecoration decoration : map.getDecorations()) {
            if (decoration.type().value().equals(MapDecorationTypes.FRAME.value())) {
                return new Vector2i((decoration.x() >> 1) + 64, (decoration.z() >> 1) + 64);
            }
        }
        return null;
    }

    @Nullable
    public static ObjectIntPair<Vector2ic> getMapEntrancePosAndRoomSize(@NotNull MapState map) {
        Vector2ic mapPos = getMapPlayerPos(map);
        if (mapPos == null) {
            return null;
        }
        Queue<Vector2ic> posToCheck = new ArrayDeque<>();
        Set<Vector2ic> checked = new HashSet<>();
        posToCheck.add(mapPos);
        checked.add(mapPos);
        while ((mapPos = posToCheck.poll()) != null) {
            if (isEntranceColor(map, mapPos)) {
                ObjectIntPair<Vector2ic> mapEntranceAndRoomSizePos = getMapEntrancePosAndRoomSizeAt(map, mapPos);
                if (mapEntranceAndRoomSizePos.rightInt() > 0) {
                    return mapEntranceAndRoomSizePos;
                }
            }
            Vector2ic pos = new Vector2i(mapPos).sub(10, 0);
            if (checked.add(pos)) {
                posToCheck.add(pos);
            }
            pos = new Vector2i(mapPos).sub(0, 10);
            if (checked.add(pos)) {
                posToCheck.add(pos);
            }
            pos = new Vector2i(mapPos).add(10, 0);
            if (checked.add(pos)) {
                posToCheck.add(pos);
            }
            pos = new Vector2i(mapPos).add(0, 10);
            if (checked.add(pos)) {
                posToCheck.add(pos);
            }
        }
        return null;
    }

    private static ObjectIntPair<Vector2ic> getMapEntrancePosAndRoomSizeAt(MapState map, Vector2ic mapPosImmutable) {
        Vector2i mapPos = new Vector2i(mapPosImmutable);
        // noinspection StatementWithEmptyBody
        while (isEntranceColor(map, mapPos.sub(1, 0))) {
        }
        mapPos.add(1, 0);
        //noinspection StatementWithEmptyBody
        while (isEntranceColor(map, mapPos.sub(0, 1))) {
        }
        return ObjectIntPair.of(mapPos.add(0, 1), getMapRoomSize(map, mapPos));
    }

    public static int getMapRoomSize(MapState map, Vector2ic mapEntrancePos) {
        int i = -1;
        //noinspection StatementWithEmptyBody
        while (isEntranceColor(map, mapEntrancePos.x() + ++i, mapEntrancePos.y())) {
        }
        return i > 5 ? i : 0;
    }

    /**
     * Gets the map position of the top left corner of the room the player is in.
     *
     * @param map            the map
     * @param mapEntrancePos the map position of the top left corner of the entrance
     * @param mapRoomSize    the size of a room on the map
     * @return the map position of the top left corner of the room the player is in
     * @implNote {@code mapPos} is shifted by 2 so room borders are evenly split.
     * {@code mapPos} is then shifted by {@code offset} to align the top left most room at (0, 0)
     * so subtracting the modulo will give the top left corner of the room shifted by {@code offset}.
     * Finally, {@code mapPos} is shifted back by {@code offset} to its intended position.
     */
    @Nullable
    public static Vector2ic getMapRoomPos(MapState map, Vector2ic mapEntrancePos, int mapRoomSize) {
        int mapRoomSizeWithGap = mapRoomSize + 4;
        Vector2i mapPos = getMapPlayerPos(map);
        if (mapPos == null) {
            return null;
        }
        Vector2ic offset = new Vector2i(mapEntrancePos.x() % mapRoomSizeWithGap, mapEntrancePos.y() % mapRoomSizeWithGap);
        return mapPos.add(2, 2).sub(offset).sub(Math.floorMod(mapPos.x(), mapRoomSizeWithGap), Math.floorMod(mapPos.y(), mapRoomSizeWithGap)).add(offset);
    }

    /**
     * Gets the map position of the top left corner of the room corresponding to the physical position of the northwest corner of a room.
     *
     * @param physicalEntrancePos the physical position of the northwest corner of the entrance room
     * @param mapEntrancePos      the map position of the top left corner of the entrance room
     * @param mapRoomSize         the size of a room on the map
     * @param physicalPos         the physical position of the northwest corner of the room
     * @return the map position of the top left corner of the room corresponding to the physical position of the northwest corner of a room
     */
    public static Vector2ic getMapPosFromPhysical(Vector2ic physicalEntrancePos, Vector2ic mapEntrancePos, int mapRoomSize, Vector2ic physicalPos) {
        return new Vector2i(physicalPos).sub(physicalEntrancePos).div(32).mul(mapRoomSize + 4).add(mapEntrancePos);
    }

    /**
     * @see #getPhysicalRoomPos(double, double)
     */
    @NotNull
    public static Vector2ic getPhysicalRoomPos(@NotNull Vec3d pos) {
        return getPhysicalRoomPos(pos.getX(), pos.getZ());
    }

    /**
     * @see #getPhysicalRoomPos(double, double)
     */
    @NotNull
    public static Vector2ic getPhysicalRoomPos(@NotNull Vec3i pos) {
        return getPhysicalRoomPos(pos.getX(), pos.getZ());
    }

    /**
     * Gets the physical position of the northwest corner of the room the given coordinate is in. Hypixel Skyblock Dungeons are aligned to a 32 by 32 blocks grid, allowing corners to be calculated through math.
     *
     * @param x the x position of the coordinate to calculate
     * @param z the z position of the coordinate to calculate
     * @return the physical position of the northwest corner of the room the player is in
     * @implNote {@code physicalPos} is shifted by 0.5 so room borders are evenly split.
     * {@code physicalPos} is further shifted by 8 because Hypixel offset dungeons by 8 blocks in Skyblock 0.12.3.
     * Subtracting the modulo gives the northwest corner of the room shifted by 8. Finally, {@code physicalPos} is shifted back by 8 to its intended position.
     */
    @NotNull
    public static Vector2ic getPhysicalRoomPos(double x, double z) {
        Vector2i physicalPos = new Vector2i(x + 8.5, z + 8.5, RoundingMode.TRUNCATE);
        return physicalPos.sub(Math.floorMod(physicalPos.x(), 32), Math.floorMod(physicalPos.y(), 32)).sub(8, 8);
    }

    public static Vector2ic[] getPhysicalPosFromMap(Vector2ic mapEntrancePos, int mapRoomSize, Vector2ic physicalEntrancePos, Vector2ic... mapPositions) {
        for (int i = 0; i < mapPositions.length; i++) {
            mapPositions[i] = getPhysicalPosFromMap(mapEntrancePos, mapRoomSize, physicalEntrancePos, mapPositions[i]);
        }
        return mapPositions;
    }

    /**
     * Gets the physical position of the northwest corner of the room corresponding to the map position of the top left corner of a room.
     *
     * @param mapEntrancePos      the map position of the top left corner of the entrance room
     * @param mapRoomSize         the size of a room on the map
     * @param physicalEntrancePos the physical position of the northwest corner of the entrance room
     * @param mapPos              the map position of the top left corner of the room
     * @return the physical position of the northwest corner of the room corresponding to the map position of the top left corner of a room
     */
    public static Vector2ic getPhysicalPosFromMap(Vector2ic mapEntrancePos, int mapRoomSize, Vector2ic physicalEntrancePos, Vector2ic mapPos) {
        return new Vector2i(mapPos).sub(mapEntrancePos).div(mapRoomSize + 4).mul(32).add(physicalEntrancePos);
    }

    public static Vector2ic getPhysicalCornerPos(Room.Direction direction, IntSortedSet segmentsX, IntSortedSet segmentsY) {
        return switch (direction) {
            case NW -> new Vector2i(segmentsX.firstInt(), segmentsY.firstInt());
            case NE -> new Vector2i(segmentsX.lastInt() + 30, segmentsY.firstInt());
            case SW -> new Vector2i(segmentsX.firstInt(), segmentsY.lastInt() + 30);
            case SE -> new Vector2i(segmentsX.lastInt() + 30, segmentsY.lastInt() + 30);
        };
    }

    public static BlockPos actualToRelative(Room.Direction direction, Vector2ic physicalCornerPos, BlockPos pos) {
        return switch (direction) {
            case NW -> new BlockPos(pos.getX() - physicalCornerPos.x(), pos.getY(), pos.getZ() - physicalCornerPos.y());
            case NE -> new BlockPos(pos.getZ() - physicalCornerPos.y(), pos.getY(), -pos.getX() + physicalCornerPos.x());
            case SW -> new BlockPos(-pos.getZ() + physicalCornerPos.y(), pos.getY(), pos.getX() - physicalCornerPos.x());
            case SE -> new BlockPos(-pos.getX() + physicalCornerPos.x(), pos.getY(), -pos.getZ() + physicalCornerPos.y());
        };
    }

    public static BlockPos relativeToActual(Room.Direction direction, Vector2ic physicalCornerPos, JsonObject posJson) {
        return relativeToActual(direction, physicalCornerPos, new BlockPos(posJson.get("x").getAsInt(), posJson.get("y").getAsInt(), posJson.get("z").getAsInt()));
    }

    public static BlockPos relativeToActual(Room.Direction direction, Vector2ic physicalCornerPos, BlockPos pos) {
        return switch (direction) {
            case NW -> new BlockPos(pos.getX() + physicalCornerPos.x(), pos.getY(), pos.getZ() + physicalCornerPos.y());
            case NE -> new BlockPos(-pos.getZ() + physicalCornerPos.x(), pos.getY(), pos.getX() + physicalCornerPos.y());
            case SW -> new BlockPos(pos.getZ() + physicalCornerPos.x(), pos.getY(), -pos.getX() + physicalCornerPos.y());
            case SE -> new BlockPos(-pos.getX() + physicalCornerPos.x(), pos.getY(), -pos.getZ() + physicalCornerPos.y());
        };
    }

    public static Room.Type getRoomType(MapState map, Vector2ic mapPos) {
        return switch (getColor(map, mapPos)) {
            case 30 -> Room.Type.ENTRANCE;
            case 63 -> Room.Type.ROOM;
            case 66 -> Room.Type.PUZZLE;
            case 62 -> Room.Type.TRAP;
            case 74 -> Room.Type.MINIBOSS;
            case 82 -> Room.Type.FAIRY;
            case 18 -> Room.Type.BLOOD;
            case 85 -> Room.Type.UNKNOWN;
            default -> null;
        };
    }

    public static Vector2ic[] getRoomSegments(MapState map, Vector2ic mapPos, int mapRoomSize, byte color) {
        Set<Vector2ic> segments = new HashSet<>();
        Queue<Vector2ic> queue = new ArrayDeque<>();
        segments.add(mapPos);
        queue.add(mapPos);
        while (!queue.isEmpty()) {
            Vector2ic curMapPos = queue.poll();
            Vector2i newMapPos = new Vector2i();
            if (getColor(map, newMapPos.set(curMapPos).sub(1, 0)) == color && !segments.contains(newMapPos.sub(mapRoomSize + 3, 0))) {
                segments.add(newMapPos);
                queue.add(newMapPos);
                newMapPos = new Vector2i();
            }
            if (getColor(map, newMapPos.set(curMapPos).sub(0, 1)) == color && !segments.contains(newMapPos.sub(0, mapRoomSize + 3))) {
                segments.add(newMapPos);
                queue.add(newMapPos);
                newMapPos = new Vector2i();
            }
            if (getColor(map, newMapPos.set(curMapPos).add(mapRoomSize, 0)) == color && !segments.contains(newMapPos.add(4, 0))) {
                segments.add(newMapPos);
                queue.add(newMapPos);
                newMapPos = new Vector2i();
            }
            if (getColor(map, newMapPos.set(curMapPos).add(0, mapRoomSize)) == color && !segments.contains(newMapPos.add(0, 4))) {
                segments.add(newMapPos);
                queue.add(newMapPos);
            }
        }
        DungeonManager.LOGGER.debug("[Skyblocker] Found dungeon room segments: {}", Arrays.toString(segments.toArray()));
        return segments.toArray(Vector2ic[]::new);
    }

    public static BlockPos getWitherBloodDoorPos(World world, Collection<Vector2ic> physicalPositions) {
        BlockPos.Mutable doorPos = new BlockPos.Mutable();
        for (Vector2ic pos : physicalPositions) {
            if (hasWitherOrBloodDoor(world, pos, doorPos)) {
                return doorPos;
            }
        }
        return null;
    }

    private static boolean hasWitherOrBloodDoor(World world, Vector2ic pos, BlockPos.Mutable doorPos) {
        return isWitherOrBloodDoor(world, doorPos.set(pos.x() + 1, 72, pos.y() + 17)) ||
                isWitherOrBloodDoor(world, doorPos.set(pos.x() + 17, 72, pos.y() + 1)) ||
                isWitherOrBloodDoor(world, doorPos.set(pos.x() + 17, 72, pos.y() + 33)) ||
                isWitherOrBloodDoor(world, doorPos.set(pos.x() + 33, 72, pos.y() + 17));
    }

    private static boolean isWitherOrBloodDoor(World world, BlockPos.Mutable pos) {
        return world.getStatesInBox(Box.enclosing(pos, pos.move(-3, -3, -3))).allMatch(state -> state.isOf(Blocks.COAL_BLOCK) || state.isOf(Blocks.RED_TERRACOTTA));
    }
}
