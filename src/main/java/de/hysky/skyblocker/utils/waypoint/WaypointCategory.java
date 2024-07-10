package de.hysky.skyblocker.utils.waypoint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public record WaypointCategory(String name, String island, List<NamedWaypoint> waypoints) {
    public static final Codec<WaypointCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(WaypointCategory::name),
            Codec.STRING.fieldOf("island").forGetter(WaypointCategory::island),
            NamedWaypoint.CODEC.listOf().fieldOf("waypoints").forGetter(WaypointCategory::waypoints)
    ).apply(instance, WaypointCategory::new));
    public static final Codec<WaypointCategory> SKYTILS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(WaypointCategory::name),
            Codec.STRING.fieldOf("island").forGetter(WaypointCategory::island),
            NamedWaypoint.SKYTILS_CODEC.listOf().fieldOf("waypoints").forGetter(WaypointCategory::waypoints)
    ).apply(instance, WaypointCategory::new));

    public static UnaryOperator<WaypointCategory> filter(Predicate<NamedWaypoint> predicate) {
        return waypointCategory -> new WaypointCategory(waypointCategory.name(), waypointCategory.island(), waypointCategory.waypoints().stream().filter(predicate).toList());
    }

    public WaypointCategory withName(String name) {
        return new WaypointCategory(name, island(), waypoints());
    }

    public WaypointCategory withIsland(String island) {
        return new WaypointCategory(name(), island, waypoints());
    }

    public WaypointCategory deepCopy() {
        return new WaypointCategory(name(), island(), waypoints().stream().map(NamedWaypoint::copy).collect(Collectors.toList()));
    }

    public void render(WorldRenderContext context) {
        for (NamedWaypoint waypoint : waypoints) {
            if (waypoint.shouldRender()) {
                waypoint.render(context);
            }
        }
    }
}
