package me.xmrvizzy.skyblocker.skyblock.dungeon.secrets;

import net.minecraft.block.MapColor;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.RoundingMode;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class DungeonMapUtils {
    public static final byte BLACK_COLOR = MapColor.BLACK.getRenderColorByte(MapColor.Brightness.LOWEST);
    public static final byte WHITE_COLOR = MapColor.WHITE.getRenderColorByte(MapColor.Brightness.HIGH);

    @Nullable
    private static Vector2i getMapPlayerPos(MapState map) {
        for (MapIcon icon : map.getIcons()) {
            if (icon.getType() == MapIcon.Type.FRAME) {
                return new Vector2i((icon.getX() >> 1) + 64, (icon.getZ() >> 1) + 64);
            }
        }
        return null;
    }

    @Nullable
    public static Vector2ic getMapEntrancePos(MapState map) {
        Vector2i mapPos = getMapPlayerPos(map);
        if (!isEntranceColor(getColor(map, mapPos))) {
            return null;
        }
        // noinspection StatementWithEmptyBody, DataFlowIssue
        while (isEntranceColor(getColor(map, mapPos.sub(1, 0)))) {
        }
        //noinspection StatementWithEmptyBody
        while (isEntranceColor(getColor(map, mapPos.sub(0, 1)))) {
        }
        return mapPos;
    }

    public static int getMapRoomWidth(MapState map, Vector2ic entrancePos) {
        int i = 0;
        //noinspection StatementWithEmptyBody
        while (isEntranceColor(getColor(map, entrancePos.x() + i++, entrancePos.y()))) {
        }
        return i;
    }

    /**
     * Gets the map position of the top left corner of the room the player is in.
     *
     * @param map          the map
     * @param entrancePos  the map position of the top left corner of the entrance
     * @param mapRoomWidth the width of a room on the map
     * @return the map position of the top left corner of the room the player is in
     * @implNote {@code mapPos} is shifted by 2 so room borders are evenly split.
     * {@code mapPos} is then shifted by {@code offset} to align the top left most room at (0, 0)
     * so subtracting the modulo will give the top left corner of the room shifted by {@code offset}.
     * Finally, {@code mapPos} is shifted back by {@code offset} to its intended position.
     */
    @Nullable
    public static Vector2ic getMapRoomPos(MapState map, Vector2ic entrancePos, int mapRoomWidth) {
        int mapRoomWidthWithGap = mapRoomWidth + 4;
        Vector2i mapPos = getMapPlayerPos(map);
        if (mapPos == null) {
            return null;
        }
        Vector2ic offset = new Vector2i(entrancePos.x() % mapRoomWidthWithGap, entrancePos.y() % mapRoomWidthWithGap);
        return mapPos.add(2, 2).sub(offset).sub(mapPos.x() % mapRoomWidthWithGap, mapPos.y() % mapRoomWidthWithGap).add(offset);
    }

    /**
     * Gets the map position of the top left corner of the room corresponding to the physical position of the northwest corner of a room.
     *
     * @param physicalEntrancePos the physical position of the northwest corner of the entrance room
     * @param mapEntrancePos      the map position of the top left corner of the entrance room
     * @param mapRoomWidth        the width of a room on the map
     * @param physicalPos         the physical position of the northwest corner of the room
     * @return the map position of the top left corner of the room corresponding to the physical position of the northwest corner of a room
     */
    public static Vector2ic getMapPosFromPhysical(Vector2ic physicalEntrancePos, Vector2ic mapEntrancePos, int mapRoomWidth, Vector2ic physicalPos) {
        return new Vector2i(physicalPos).sub(physicalEntrancePos).div(32).mul(mapRoomWidth + 4).add(mapEntrancePos);
    }

    @Nullable
    public static Vector2ic getPhysicalEntrancePos(MapState map, @NotNull Vec3d playerPos) {
        if (isEntranceColor(getColor(map, getMapPlayerPos(map)))) {
            return getPhysicalRoomPos(playerPos);
        }
        return null;
    }

    /**
     * Gets the physical position of the northwest corner of the room the player is in. Hypixel Skyblock Dungeons are aligned to a 32 by 32 blocks grid, allowing corners to be calculated through math.
     *
     * @param playerPos the position of the player
     * @return the physical position of the northwest corner of the room the player is in
     * @implNote {@code physicalPos} is shifted by 0.5 so room borders are evenly split.
     * {@code physicalPos} is further shifted by 8 because Hypixel offset dungeons by 8 blocks in Skyblock 0.12.3.
     * Subtracting the modulo gives the northwest corner of the room shifted by 8. Finally, {@code physicalPos} is shifted back by 8 to its intended position.
     */
    @NotNull
    public static Vector2ic getPhysicalRoomPos(@NotNull Vec3d playerPos) {
        Vector2i physicalPos = new Vector2i(playerPos.getX() + 8.5, playerPos.getZ() + 8.5, RoundingMode.TRUNCATE);
        return physicalPos.sub(MathHelper.floorMod(physicalPos.x(), 32), MathHelper.floorMod(physicalPos.y(), 32)).sub(8, 8);
    }

    /**
     * Gets the physical position of the northwest corner of the room corresponding to the map position of the top left corner of a room.
     *
     * @param mapEntrancePos      the map position of the top left corner of the entrance room
     * @param mapRoomWidth        the width of a room on the map
     * @param physicalEntrancePos the physical position of the northwest corner of the entrance room
     * @param mapPos              the map position of the top left corner of the room
     * @return the physical position of the northwest corner of the room corresponding to the map position of the top left corner of a room
     */
    public static Vector2ic getPhysicalPosFromMap(Vector2ic mapEntrancePos, int mapRoomWidth, Vector2ic physicalEntrancePos, Vector2ic mapPos) {
        return new Vector2i(mapPos).sub(mapEntrancePos).div(mapRoomWidth + 4).mul(32).add(physicalEntrancePos);
    }

    private static byte getColor(MapState map, @Nullable Vector2ic pos) {
        return pos == null ? -1 : getColor(map, pos.x(), pos.y());
    }

    private static byte getColor(MapState map, int x, int z) {
        if (x < 0 || z < 0 || x >= 128 || z >= 128) {
            return -1;
        }
        return map.colors[x + (z << 7)];
    }

    private static boolean isEntranceColor(byte color) {
        return color == Room.RoomType.ENTRANCE.color;
    }
}
