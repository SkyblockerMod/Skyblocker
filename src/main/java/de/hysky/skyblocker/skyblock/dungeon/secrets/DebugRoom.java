package de.hysky.skyblocker.skyblock.dungeon.secrets;

import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DebugRoom extends Room {
    private final List<Waypoint> checkedBlocks = Collections.synchronizedList(new ArrayList<>());

    public DebugRoom(@NotNull Type type, @NotNull Vector2ic... physicalPositions) {
        super(type, physicalPositions);
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
    protected void render(WorldRenderContext context) {
        super.render(context);
        synchronized (checkedBlocks) {
            for (Waypoint checkedBlock : checkedBlocks) {
                checkedBlock.render(context);
            }
        }
    }
}
