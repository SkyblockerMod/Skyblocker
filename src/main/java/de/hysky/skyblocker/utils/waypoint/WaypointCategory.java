package de.hysky.skyblocker.utils.waypoint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.skyblock.waypoint.OrderedWaypoints;
import de.hysky.skyblocker.utils.InstancedUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WaypointCategory {
    public static final Codec<WaypointCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(WaypointCategory::name),
            Codec.STRING.fieldOf("island").forGetter(WaypointCategory::island),
            NamedWaypoint.CODEC.listOf().fieldOf("waypoints").forGetter(WaypointCategory::waypoints),
            Codec.BOOL.lenientOptionalFieldOf("ordered", false).forGetter(WaypointCategory::ordered),
            Codec.INT.lenientOptionalFieldOf("currentIndex", 0).forGetter(category -> category.currentIndex)
    ).apply(instance, WaypointCategory::new));
    public static final Codec<WaypointCategory> SKYTILS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(WaypointCategory::name),
            Codec.STRING.fieldOf("island").forGetter(WaypointCategory::island),
            NamedWaypoint.SKYTILS_CODEC.listOf().fieldOf("waypoints").forGetter(WaypointCategory::waypoints)
    ).apply(instance, WaypointCategory::new));

    private final String name;
    private final String island;
    private final List<NamedWaypoint> waypoints;
    private final boolean ordered;
    protected int currentIndex;

    public WaypointCategory(String name, String island, List<NamedWaypoint> waypoints) {
        this(name, island, waypoints, false, 0);
    }

    public WaypointCategory(String name, String island, List<NamedWaypoint> waypoints, boolean ordered, int currentIndex) {
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

    public String island() {
        return island;
    }

    public List<NamedWaypoint> waypoints() {
        return waypoints;
    }

    public boolean ordered() {
        return ordered;
    }

    public WaypointCategory withName(String name) {
        return new WaypointCategory(name, island, waypoints, ordered, currentIndex);
    }

    public WaypointCategory withIsland(String island) {
        return new WaypointCategory(name, island, waypoints, ordered, currentIndex);
    }

    public WaypointCategory withOrdered(boolean ordered) {
        return new WaypointCategory(name, island, waypoints, ordered, currentIndex);
    }

    public WaypointCategory filterWaypoints(Predicate<NamedWaypoint> predicate) {
        return new WaypointCategory(name, island, waypoints.stream().filter(predicate).toList(), ordered, currentIndex);
    }

    /**
     * Returns a deep copy of this {@link WaypointCategory} with a mutable waypoints list for editing.
     */
    public WaypointCategory deepCopy() {
        return new WaypointCategory(name, island, waypoints.stream().map(NamedWaypoint::copy).collect(Collectors.toList()), ordered, currentIndex);
    }

    public NamedWaypoint createWaypoint(BlockPos pos) {
        String name = "Waypoint " + (waypoints.size() + 1);
        return ordered ? new OrderedNamedWaypoint(pos, name, new float[]{0f, 1f, 0f}) : new NamedWaypoint(pos, name, new float[]{0f, 1f, 0f});
    }

    /**
     * Converts the given waypoint to the correct type based on whether this category is ordered or not.
     */
    public NamedWaypoint convertWaypoint(NamedWaypoint waypoint) {
        if (ordered) {
            return waypoint instanceof OrderedNamedWaypoint ? waypoint : new OrderedNamedWaypoint(waypoint);
        } else {
            return waypoint instanceof OrderedNamedWaypoint ? new NamedWaypoint(waypoint) : waypoint;
        }
    }

    public void render(WorldRenderContext context) {
        if (ordered) {
            for (int i = 0; i < waypoints.size(); i++) {
                NamedWaypoint waypoint = waypoints.get(i);
                if (waypoint.pos.isWithinDistance(MinecraftClient.getInstance().player.getPos(), OrderedWaypoints.RADIUS)) {
                    currentIndex = i;
                }
            }

            int categoryPreviousIndex = (currentIndex - 1 + waypoints.size()) % waypoints.size();
            int categoryNextIndex = (currentIndex + 1) % waypoints.size();
            for (int i = 0; i < waypoints.size(); i++) {
                NamedWaypoint waypoint = waypoints.get(i);
                if (waypoint instanceof OrderedNamedWaypoint orderedNamedWaypoint) {
                    orderedNamedWaypoint.index = i;
                    if (i == categoryPreviousIndex) {
                        orderedNamedWaypoint.relativeIndex = OrderedWaypoints.RelativeIndex.PREVIOUS;
                    } else if (i == categoryNextIndex) {
                        orderedNamedWaypoint.relativeIndex = OrderedWaypoints.RelativeIndex.NEXT;
                    } else if (i == currentIndex) {
                        orderedNamedWaypoint.relativeIndex = OrderedWaypoints.RelativeIndex.CURRENT;
                    } else {
                        orderedNamedWaypoint.relativeIndex = OrderedWaypoints.RelativeIndex.NONE;
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
