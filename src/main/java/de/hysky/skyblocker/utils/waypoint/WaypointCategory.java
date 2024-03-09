package de.hysky.skyblocker.utils.waypoint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

import java.util.ArrayList;
import java.util.List;

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

    public WaypointCategory(WaypointCategory waypointCategory) {
        this(waypointCategory.name(), waypointCategory.island(), new ArrayList<>(waypointCategory.waypoints()));
    }

    public void render(WorldRenderContext context) {
        for (NamedWaypoint waypoint : waypoints) {
            waypoint.render(context);
        }
    }
}
