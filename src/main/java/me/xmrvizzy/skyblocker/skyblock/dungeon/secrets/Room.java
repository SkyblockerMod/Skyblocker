package me.xmrvizzy.skyblocker.skyblock.dungeon.secrets;

import net.minecraft.block.MapColor;
import org.joml.Vector2ic;

import java.util.Set;

public class Room {
    private final RoomType type;
    private String name;
    private final Set<Vector2ic> segments;

    public Room(RoomType type, Vector2ic... physicalPositions) {
        this.type = type;
        this.segments = Set.of(physicalPositions);
    }

    public RoomType getType() {
        return type;
    }

    public boolean containsSegment(Vector2ic segment) {
        return segments.contains(segment);
    }

    public enum RoomType {
        ENTRANCE(MapColor.DARK_GREEN.getRenderColorByte(MapColor.Brightness.HIGH)),
        ROOM(MapColor.ORANGE.getRenderColorByte(MapColor.Brightness.LOWEST)),
        PUZZLE(MapColor.MAGENTA.getRenderColorByte(MapColor.Brightness.HIGH)),
        MINIBOSS(MapColor.YELLOW.getRenderColorByte(MapColor.Brightness.HIGH)),
        FAIRY(MapColor.PINK.getRenderColorByte(MapColor.Brightness.HIGH)),
        BLOOD(MapColor.BRIGHT_RED.getRenderColorByte(MapColor.Brightness.HIGH)),
        UNKNOWN(MapColor.GRAY.getRenderColorByte(MapColor.Brightness.NORMAL));
        final byte color;

        RoomType(byte color) {
            this.color = color;
        }
    }
}
