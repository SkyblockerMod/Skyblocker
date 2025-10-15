package de.hysky.skyblocker.skyblock.dungeon.secrets;

import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.ints.IntSortedSets;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.joml.Vector2ic;

import java.util.*;

public class DebugRoom extends Room {
    private final List<Waypoint> checkedBlocks = Collections.synchronizedList(new ArrayList<>());

    public DebugRoom(Type type, Vector2ic... physicalPositions) {
        super(type, physicalPositions);
    }

    public static DebugRoom ofSinglePossibleRoom(Type type, Vector2ic physicalPositions, String roomName, int[] roomData, Direction direction) {
        return ofSinglePossibleRoom(type, new Vector2ic[]{physicalPositions}, roomName, roomData, direction);
    }

    public static DebugRoom ofSinglePossibleRoom(Type type, Vector2ic[] physicalPositions, String roomName, int[] roomData, Direction direction) {
        DebugRoom room = new DebugRoom(type, physicalPositions);
        IntSortedSet segmentsX = IntSortedSets.unmodifiable(new IntRBTreeSet(room.segments.stream().mapToInt(Vector2ic::x).toArray()));
        IntSortedSet segmentsY = IntSortedSets.unmodifiable(new IntRBTreeSet(room.segments.stream().mapToInt(Vector2ic::y).toArray()));
        room.roomsData = Map.of(roomName, roomData);
        room.possibleRooms = List.of(MutableTriple.of(direction, DungeonMapUtils.getPhysicalCornerPos(direction, segmentsX, segmentsY), List.of(roomName)));
        return room;
    }

    @Override
    protected boolean checkBlock(ClientWorld world, BlockPos pos) {
        byte id = DungeonManager.NUMERIC_ID.getByte(Registries.BLOCK.getId(world.getBlockState(pos).getBlock()).toString());
        if (id == 0) {
            return false;
        }
        for (MutableTriple<Direction, Vector2ic, List<String>> directionRooms : possibleRooms) {
            int block = posIdToInt(DungeonMapUtils.actualToRelative(directionRooms.getLeft(), directionRooms.getMiddle(), pos), id);
            for (String room : directionRooms.getRight()) {
                checkedBlocks.add(new Waypoint(pos, SecretWaypoint.TYPE_SUPPLIER, Arrays.binarySearch(roomsData.get(room), block) >= 0 ? Room.GREEN_COLOR_COMPONENTS : Room.RED_COLOR_COMPONENTS));
            }
        }
        return false;
    }

    @Override
    public void extractRendering(PrimitiveCollector collector) {
        super.extractRendering(collector);
        synchronized (checkedBlocks) {
            for (Waypoint checkedBlock : checkedBlocks) {
                checkedBlock.extractRendering(collector);
            }
        }
    }
}
