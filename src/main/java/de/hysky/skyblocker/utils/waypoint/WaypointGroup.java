package de.hysky.skyblocker.utils.waypoint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.annotations.GenEquals;
import de.hysky.skyblocker.annotations.GenHashCode;
import de.hysky.skyblocker.annotations.GenToString;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WaypointGroup {
    public static final Waypoint.Type DEFAULT_TYPE = Waypoint.Type.WAYPOINT;
    public static final Codec<WaypointGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(WaypointGroup::name),
            Codec.STRING.fieldOf("island").xmap(Location::from, Location::id).forGetter(WaypointGroup::island),
            NamedWaypoint.CODEC.listOf().fieldOf("waypoints").forGetter(WaypointGroup::waypoints),
            Codec.BOOL.lenientOptionalFieldOf("ordered", false).forGetter(WaypointGroup::ordered),
            Codec.BOOL.lenientOptionalFieldOf("render_through_walls", true).forGetter(WaypointGroup::renderThroughWalls),
            Waypoint.Type.CODEC.lenientOptionalFieldOf("waypoint_type", DEFAULT_TYPE).forGetter(WaypointGroup::waypointType)
    ).apply(instance, WaypointGroup::new));
    public static final Codec<WaypointGroup> SKYTILS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(WaypointGroup::name),
            Codec.STRING.fieldOf("island").xmap(Location::from, Location::id).forGetter(WaypointGroup::island),
            NamedWaypoint.SKYTILS_CODEC.listOf().fieldOf("waypoints").forGetter(WaypointGroup::waypoints)
    ).apply(instance, WaypointGroup::new));
    public static final Codec<WaypointGroup> COLEWEIGHT_CODEC = NamedWaypoint.COLEWEIGHT_CODEC.listOf().xmap(coleWeightWaypoints -> new WaypointGroup("Coleweight", Location.UNKNOWN, coleWeightWaypoints, true, true, DEFAULT_TYPE), WaypointGroup::waypoints);
    public static final Codec<WaypointGroup> SKYBLOCKER_LEGACY_ORDERED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(WaypointGroup::name),
            Codec.BOOL.fieldOf("enabled").forGetter(group -> !group.waypoints().isEmpty() && group.waypoints().stream().allMatch(Waypoint::isEnabled)),
            NamedWaypoint.SKYBLOCKER_LEGACY_ORDERED_CODEC.listOf().fieldOf("waypoints").forGetter(WaypointGroup::waypoints)
    ).apply(instance, (name, enabled, waypoints) -> {
        waypoints.forEach(enabled ? Waypoint::setMissing : Waypoint::setFound);
        return new WaypointGroup(name, Location.UNKNOWN, waypoints, true, true, DEFAULT_TYPE);
    }));
    public static final int WAYPOINT_ACTIVATION_RADIUS = 2;

    private final String name;
    private final Location island;
    private final List<NamedWaypoint> waypoints;
    private final boolean ordered;
    private final boolean renderThroughWalls;
    private final Waypoint.Type waypointType;
    private transient int currentIndex = 0;

    public WaypointGroup(String name, Location island) {
        this(name, island, List.of(), false, true, SkyblockerConfigManager.get().uiAndVisuals.waypoints.waypointType);
    }

    public WaypointGroup(String name, Location island, List<NamedWaypoint> waypoints) {
        this(name, island, waypoints, false, true, DEFAULT_TYPE);
    }

    public WaypointGroup(String name, Location island, List<NamedWaypoint> waypoints, boolean ordered, boolean renderThroughWalls, Waypoint.Type waypointType) {
        this.name = name;
        this.island = island;
        // Set ordered first since convertWaypoint depends on it
        this.ordered = ordered;
        this.renderThroughWalls = renderThroughWalls;
        this.waypointType = waypointType;
        this.waypoints = waypoints.stream().map(this::convertWaypoint).collect(Collectors.toList());
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

    public boolean renderThroughWalls() {
        return renderThroughWalls;
    }

    public Waypoint.Type waypointType() {
        return waypointType;
    }

    public int currentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    /**
     * Resets the current ordered waypoint index on world change.
     */
    public void resetCurrentIndex() {
        setCurrentIndex(0);
    }

    public WaypointGroup withName(String name) {
        return new WaypointGroup(name, island, waypoints, ordered, renderThroughWalls, waypointType);
    }

    public WaypointGroup withIsland(Location island) {
        return new WaypointGroup(name, island, waypoints, ordered, renderThroughWalls, waypointType);
    }

    public WaypointGroup withOrdered(boolean ordered) {
        return new WaypointGroup(name, island, waypoints, ordered, renderThroughWalls, waypointType);
    }

    public WaypointGroup withRenderThroughWalls(boolean renderThroughWalls) {
        return new WaypointGroup(name, island, waypoints, ordered, renderThroughWalls, waypointType);
    }

    public WaypointGroup withWaypointType(Waypoint.Type waypointType) {
        return new WaypointGroup(name, island, waypoints, ordered, renderThroughWalls, waypointType);
    }

    public WaypointGroup filterWaypoints(Predicate<NamedWaypoint> predicate) {
        return new WaypointGroup(name, island, waypoints.stream().filter(predicate).toList(), ordered, renderThroughWalls, waypointType);
    }

    public WaypointGroup sortWaypoints(Comparator<NamedWaypoint> comparator) {
        return new WaypointGroup(name, island, waypoints.stream().sorted(comparator).toList(), ordered, renderThroughWalls, waypointType);
    }

    /**
     * Returns a deep copy of this {@link WaypointGroup} with a mutable waypoints list for editing.
     */
    public WaypointGroup deepCopy() {
        return new WaypointGroup(name, island, waypoints.stream().map(NamedWaypoint::copy).collect(Collectors.toList()), ordered, renderThroughWalls, waypointType);
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
            return (waypoint instanceof OrderedNamedWaypoint ? waypoint : new OrderedNamedWaypoint(waypoint)).withThroughWalls(renderThroughWalls()).withTypeSupplier(this::waypointType);
        } else {
            return (waypoint instanceof OrderedNamedWaypoint ? new NamedWaypoint(waypoint) : waypoint).withThroughWalls(renderThroughWalls()).withTypeSupplier(this::waypointType);
        }
    }

    public void tick() {
        if (MinecraftClient.getInstance().player == null || !ordered || waypoints.isEmpty()) return;
        for (int i = 0; i < waypoints.size(); i++) {
            NamedWaypoint waypoint = waypoints.get(i);
            boolean notBackwards = SkyblockerConfigManager.get().uiAndVisuals.waypoints.allowGoingBackwards || i > currentIndex;
            boolean notSkipping = SkyblockerConfigManager.get().uiAndVisuals.waypoints.allowSkippingWaypoints || i == (currentIndex + 1) % waypoints.size() || i == (currentIndex - 1 + waypoints.size()) % waypoints.size();
            if (notBackwards && notSkipping && waypoint.pos.isWithinDistance(MinecraftClient.getInstance().player.getPos(), WAYPOINT_ACTIVATION_RADIUS)) {
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

    public void extractRendering(PrimitiveCollector collector) {
        for (NamedWaypoint waypoint : waypoints) {
            if (waypoint.shouldRender()) {
                waypoint.extractRendering(collector);
            }
        }
    }

    @Override
    @GenEquals
    public native boolean equals(Object o);

    @Override
    @GenHashCode
    public native int hashCode();

    @Override
    @GenToString
    public native String toString();
}
