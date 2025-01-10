package de.hysky.skyblocker.utils.waypoint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.utils.InstancedUtils;
import de.hysky.skyblocker.utils.Location;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WaypointGroup {
    public static final Codec<WaypointGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(WaypointGroup::name),
            Codec.STRING.fieldOf("island").xmap(Location::from, Location::id).forGetter(WaypointGroup::island),
            NamedWaypoint.CODEC.listOf().fieldOf("waypoints").forGetter(WaypointGroup::waypoints),
            Codec.BOOL.lenientOptionalFieldOf("ordered", false).forGetter(WaypointGroup::ordered),
            Codec.INT.lenientOptionalFieldOf("currentIndex", 0).forGetter(group -> group.currentIndex)
    ).apply(instance, WaypointGroup::new));
    public static final Codec<WaypointGroup> SKYTILS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(WaypointGroup::name),
            Codec.STRING.fieldOf("island").xmap(Location::from, Location::id).forGetter(WaypointGroup::island),
            NamedWaypoint.SKYTILS_CODEC.listOf().fieldOf("waypoints").forGetter(WaypointGroup::waypoints)
    ).apply(instance, WaypointGroup::new));
    public static final Codec<WaypointGroup> COLEWEIGHT_CODEC = NamedWaypoint.COLEWEIGHT_CODEC.listOf().xmap(coleWeightWaypoints -> new WaypointGroup("Coleweight", Location.UNKNOWN, coleWeightWaypoints, true), WaypointGroup::waypoints);
    public static final int WAYPOINT_ACTIVATION_RADIUS = 2;

    private final String name;
    private final Location island;
    private final List<NamedWaypoint> waypoints;
    private final boolean ordered;
    protected int currentIndex;

    public WaypointGroup(String name, Location island, List<NamedWaypoint> waypoints) {
        this(name, island, waypoints, false);
    }

    public WaypointGroup(String name, Location island, List<NamedWaypoint> waypoints, boolean ordered) {
        this(name, island, waypoints, ordered, 0);
    }

    public WaypointGroup(String name, Location island, List<NamedWaypoint> waypoints, boolean ordered, int currentIndex) {
        this.name = name;
        this.island = island;
        // Set ordered first since convertWaypoint depends on it
        this.ordered = ordered;
        this.waypoints = waypoints.stream().map(this::convertWaypoint).collect(Collectors.toList());
        this.currentIndex = currentIndex;
    }

    public String name() {
        return name;
    }

    public Location island() {
        return island;
    }

    public List<NamedWaypoint> waypoints() {
        return waypoints;
    }

    public boolean ordered() {
        return ordered;
    }

    public WaypointGroup withName(String name) {
        return new WaypointGroup(name, island, waypoints, ordered, currentIndex);
    }

    public WaypointGroup withIsland(Location island) {
        return new WaypointGroup(name, island, waypoints, ordered, currentIndex);
    }

    public WaypointGroup withOrdered(boolean ordered) {
        return new WaypointGroup(name, island, waypoints, ordered, currentIndex);
    }

    public WaypointGroup filterWaypoints(Predicate<NamedWaypoint> predicate) {
        return new WaypointGroup(name, island, waypoints.stream().filter(predicate).toList(), ordered, currentIndex);
    }

    /**
     * Returns a deep copy of this {@link WaypointGroup} with a mutable waypoints list for editing.
     */
    public WaypointGroup deepCopy() {
        return new WaypointGroup(name, island, waypoints.stream().map(NamedWaypoint::copy).collect(Collectors.toList()), ordered, currentIndex);
    }

    public NamedWaypoint createWaypoint(BlockPos pos) {
        String name = "Waypoint " + (waypoints.size() + 1);
        return ordered ? new OrderedNamedWaypoint(pos, name, new float[]{0f, 1f, 0f}) : new NamedWaypoint(pos, name, new float[]{0f, 1f, 0f});
    }

    /**
     * Converts the given waypoint to the correct type based on whether this group is ordered or not.
     */
    public NamedWaypoint convertWaypoint(NamedWaypoint waypoint) {
        if (ordered) {
            return waypoint instanceof OrderedNamedWaypoint ? waypoint : new OrderedNamedWaypoint(waypoint);
        } else {
            return waypoint instanceof OrderedNamedWaypoint ? new NamedWaypoint(waypoint) : waypoint;
        }
    }

    public void render(WorldRenderContext context) {
        if (ordered && !waypoints.isEmpty()) {
            for (int i = 0; i < waypoints.size(); i++) {
                NamedWaypoint waypoint = waypoints.get(i);
                if (waypoint.pos.isWithinDistance(MinecraftClient.getInstance().player.getPos(), WAYPOINT_ACTIVATION_RADIUS)) {
                    currentIndex = i;
                }
            }

            int previousIndex = (currentIndex - 1 + waypoints.size()) % waypoints.size();
            int nextIndex = (currentIndex + 1) % waypoints.size();
            for (int i = 0; i < waypoints.size(); i++) {
                NamedWaypoint waypoint = waypoints.get(i);
                if (waypoint instanceof OrderedNamedWaypoint orderedNamedWaypoint) {
                    orderedNamedWaypoint.index = i;
                    if (i == previousIndex) {
                        orderedNamedWaypoint.relativeIndex = OrderedNamedWaypoint.RelativeIndex.PREVIOUS;
                    } else if (i == nextIndex) {
                        orderedNamedWaypoint.relativeIndex = OrderedNamedWaypoint.RelativeIndex.NEXT;
                    } else if (i == currentIndex) {
                        orderedNamedWaypoint.relativeIndex = OrderedNamedWaypoint.RelativeIndex.CURRENT;
                    } else {
                        orderedNamedWaypoint.relativeIndex = OrderedNamedWaypoint.RelativeIndex.NONE;
                    }
                }
            }
        }
        for (NamedWaypoint waypoint : waypoints) {
            if (waypoint.shouldRender()) {
                waypoint.render(context);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        try {
            return (boolean) InstancedUtils.equals(getClass()).invokeExact(this, o);
        } catch (Throwable ignored) {
            return super.equals(o);
        }
    }

    @Override
    public int hashCode() {
        try {
            return (int) InstancedUtils.hashCode(getClass()).invokeExact(this);
        } catch (Throwable ignored) {
            return super.hashCode();
        }
    }

    @Override
    public String toString() {
        try {
            return (String) InstancedUtils.toString(getClass()).invokeExact(this);
        } catch (Throwable ignored) {
            return super.toString();
        }
    }
}
