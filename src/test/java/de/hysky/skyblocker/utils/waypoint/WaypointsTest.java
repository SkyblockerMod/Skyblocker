package de.hysky.skyblocker.utils.waypoint;

import de.hysky.skyblocker.skyblock.waypoint.Waypoints;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

public class WaypointsTest {
    @Test
    void testFromSkytilsBase64() {
        String waypointCategoriesSkytilsBase64 = "eyJjYXRlZ29yaWVzIjpbeyJuYW1lIjoiY2F0ZWdvcnkiLCJ3YXlwb2ludHMiOlt7Im5hbWUiOiJ3YXlwb2ludCIsIngiOjAsInkiOjAsInoiOjAsImVuYWJsZWQiOmZhbHNlLCJjb2xvciI6MzMwMjMsImFkZGVkQXQiOjF9LHsibmFtZSI6MSwieCI6LTEsInkiOjAsInoiOjEsImVuYWJsZWQiOnRydWUsImNvbG9yIjowLCJhZGRlZEF0IjoxfV0sImlzbGFuZCI6Imh1YiJ9XX0=";
        Collection<WaypointCategory> waypointCategories = Waypoints.fromSkytilsBase64(waypointCategoriesSkytilsBase64);
        Collection<WaypointCategory> expectedWaypointCategories = List.of(new WaypointCategory("category", "hub", List.of(new NamedWaypoint(BlockPos.ORIGIN, "waypoint", new float[]{0f, 0.5f, 1f}, false), new NamedWaypoint(new BlockPos(-1, 0, 1), "1", new float[]{0f, 0f, 0f}, true))));

        Assertions.assertEquals(expectedWaypointCategories, waypointCategories);
    }

    @Test
    void testToSkytilsBase64() {
        Collection<WaypointCategory> waypointCategories = List.of(new WaypointCategory("category", "hub", List.of(new NamedWaypoint(BlockPos.ORIGIN, "waypoint", new float[]{0f, 0.5f, 1f}, false), new NamedWaypoint(new BlockPos(-1, 0, 1), "1", new float[]{0f, 0f, 0f}, true))));
        String waypointCategoriesSkytilsBase64 = Waypoints.toSkytilsBase64(waypointCategories);
        String expectedWaypointCategoriesSkytilsBase64 = "ewogICJjYXRlZ29yaWVzIjogWwogICAgewogICAgICAibmFtZSI6ICJjYXRlZ29yeSIsCiAgICAgICJpc2xhbmQiOiAiaHViIiwKICAgICAgIndheXBvaW50cyI6IFsKICAgICAgICB7CiAgICAgICAgICAibmFtZSI6ICJ3YXlwb2ludCIsCiAgICAgICAgICAiY29sb3IiOiAzMjc2NywKICAgICAgICAgICJlbmFibGVkIjogZmFsc2UsCiAgICAgICAgICAieCI6IDAsCiAgICAgICAgICAieSI6IDAsCiAgICAgICAgICAieiI6IDAKICAgICAgICB9LAogICAgICAgIHsKICAgICAgICAgICJuYW1lIjogIjEiLAogICAgICAgICAgImNvbG9yIjogMCwKICAgICAgICAgICJlbmFibGVkIjogdHJ1ZSwKICAgICAgICAgICJ4IjogLTEsCiAgICAgICAgICAieSI6IDAsCiAgICAgICAgICAieiI6IDEKICAgICAgICB9CiAgICAgIF0KICAgIH0KICBdCn0=";

        Assertions.assertEquals(expectedWaypointCategoriesSkytilsBase64, waypointCategoriesSkytilsBase64);
    }
}
