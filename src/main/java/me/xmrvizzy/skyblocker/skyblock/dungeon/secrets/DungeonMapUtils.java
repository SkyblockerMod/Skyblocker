package me.xmrvizzy.skyblocker.skyblock.dungeon.secrets;

import net.minecraft.block.MapColor;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import org.joml.Vector2i;

public class DungeonMapUtils {
    public static final byte ENTRANCE_COLOR = MapColor.DARK_GREEN.getRenderColorByte(MapColor.Brightness.HIGH);
    public static final byte ROOM_COLOR = MapColor.ORANGE.getRenderColorByte(MapColor.Brightness.LOWEST);
    public static final byte PUZZLE_COLOR = MapColor.MAGENTA.getRenderColorByte(MapColor.Brightness.HIGH);
    public static final byte MINIBOSS_COLOR = MapColor.YELLOW.getRenderColorByte(MapColor.Brightness.HIGH);
    public static final byte FAIRY_COLOR = MapColor.PINK.getRenderColorByte(MapColor.Brightness.HIGH);
    public static final byte BLOOD_COLOR = MapColor.BRIGHT_RED.getRenderColorByte(MapColor.Brightness.HIGH);
    public static final byte UNKNOWN_COLOR = MapColor.GRAY.getRenderColorByte(MapColor.Brightness.NORMAL);
    public static final byte BLACK_COLOR = MapColor.BLACK.getRenderColorByte(MapColor.Brightness.LOWEST);
    public static final byte WHITE_COLOR = MapColor.WHITE.getRenderColorByte(MapColor.Brightness.HIGH);

    public static Vector2i getEntrancePos(MapState map) {
        for (MapIcon icon : map.getIcons()) {
            if (icon.getType() == MapIcon.Type.FRAME) {
                int x = (icon.getX() >> 1) + 64;
                int z = (icon.getZ() >> 1) + 64;
                if (getColor(map, x, z) == ENTRANCE_COLOR) {
                    while (getColor(map, x - 1, z) == ENTRANCE_COLOR) {
                        x--;
                    }
                    while (getColor(map, x, z - 1) == ENTRANCE_COLOR) {
                        z--;
                    }
                    return new Vector2i(x, z);
                }
            }
        }
        return null;
    }

    public static int getRoomWidth(MapState map, Vector2i entrancePos) {
        int i = 0;
        while (getColor(map, entrancePos.x + i, entrancePos.y) == ENTRANCE_COLOR) {
            i++;
        }
        return i;
    }

    private static byte getColor(MapState map, int x, int z) {
        if (x < 0 || z < 0 || x >= 128 || z >= 128) {
            return -1;
        }
        return map.colors[x + (z << 7)];
    }
}
