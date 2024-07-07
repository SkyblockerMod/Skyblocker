package de.hysky.skyblocker.utils.waypoint;

import de.hysky.skyblocker.skyblock.waypoint.Waypoints;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class WaypointsTest {
    @BeforeAll
    public static void setup() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void testFromSkytilsBase64() {
        String waypointCategoriesSkytilsBase64 = "eyJjYXRlZ29yaWVzIjpbeyJuYW1lIjoiY2F0ZWdvcnkiLCJ3YXlwb2ludHMiOlt7Im5hbWUiOiJ3YXlwb2ludCIsIngiOjAsInkiOjAsInoiOjAsImVuYWJsZWQiOmZhbHNlLCJjb2xvciI6LTg3MjM4MjIwOSwiYWRkZWRBdCI6MX0seyJuYW1lIjoxLCJ4IjotMSwieSI6MCwieiI6MSwiZW5hYmxlZCI6dHJ1ZSwiY29sb3IiOjAsImFkZGVkQXQiOjF9XSwiaXNsYW5kIjoiaHViIn1dfQ==";
        List<WaypointCategory> waypointCategories = Waypoints.fromSkytils(waypointCategoriesSkytilsBase64, "");
        List<WaypointCategory> expectedWaypointCategories = List.of(new WaypointCategory("category", "hub", List.of(new NamedWaypoint(BlockPos.ORIGIN, "waypoint", new float[]{0f, 0.5019608f, 1f}, 0.8f, false), new NamedWaypoint(new BlockPos(-1, 0, 1), "1", new float[]{0f, 0f, 0f}, true))));

        Assertions.assertEquals(expectedWaypointCategories, waypointCategories);
    }

    @Test
    void testToSkytilsBase64() {
        List<WaypointCategory> waypointCategories = List.of(new WaypointCategory("category", "hub", List.of(new NamedWaypoint(BlockPos.ORIGIN, "waypoint", new float[]{0f, 0.5f, 1f}, 0.8f, false), new NamedWaypoint(new BlockPos(-1, 0, 1), "1", new float[]{0f, 0f, 0f}, true))));
        String waypointCategoriesSkytilsBase64 = Waypoints.toSkytilsBase64(waypointCategories);
        String expectedWaypointCategoriesSkytilsBase64 = "eyJjYXRlZ29yaWVzIjpbeyJuYW1lIjoiY2F0ZWdvcnkiLCJpc2xhbmQiOiJodWIiLCJ3YXlwb2ludHMiOlt7Im5hbWUiOiJ3YXlwb2ludCIsImNvbG9yIjotODcyMzgyNDY1LCJlbmFibGVkIjpmYWxzZSwieCI6MCwieSI6MCwieiI6MH0seyJuYW1lIjoiMSIsImNvbG9yIjoyMTMwNzA2NDMyLCJlbmFibGVkIjp0cnVlLCJ4IjotMSwieSI6MCwieiI6MX1dfV19";

        Assertions.assertEquals(expectedWaypointCategoriesSkytilsBase64, waypointCategoriesSkytilsBase64);
    }

    //https://sharetext.me/gq22cbhdmo
    @Test
    void testFromSkytilsBase64GlacialCaveWaypoints() {
        String waypointCategoriesSkytilsBase64 = "eyJjYXRlZ29yaWVzIjogW3sibmFtZSI6ICJGcm96ZW4gVHJlYXN1cmUgTG9jYXRpb25zIiwid2F5cG9pbnRzIjogW3sibmFtZSI6ICIyNCIsIngiOiA2NCwieSI6IDc4LCJ6IjogMjgsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwODU0MDY3MTksImFkZGVkQXQiOiAxNjY5OTk5NzUwNjc3fSx7Im5hbWUiOiAiOSIsIngiOiA0NSwieSI6IDc5LCJ6IjogNDksImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwODUyNzM1OTksImFkZGVkQXQiOiAxNjY5OTk5NTEwMTA3fSx7Im5hbWUiOiAiMjAiLCJ4IjogNjAsInkiOiA3NiwieiI6IDUxLCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiA5NTMzNTE5MzUsImFkZGVkQXQiOiAxNjY5OTk5NzQ5MzI3fSx7Im5hbWUiOiAiMjMiLCJ4IjogNjMsInkiOiA3NiwieiI6IDk1LCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMDIwNDYxMDUyLCJhZGRlZEF0IjogMTY2OTk5OTc1MDQ3N30seyJuYW1lIjogIjIyIiwieCI6IDYzLCJ5IjogNzYsInoiOiA1MiwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTA1MjQ0MjYxMSwiYWRkZWRBdCI6IDE2Njk5OTk3NTAyMjd9LHsibmFtZSI6ICI0MCIsIngiOiA5NCwieSI6IDc3LCJ6IjogNDIsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDk4NDYxMjg1NywiYWRkZWRBdCI6IDE2NzAwMDAyMjcwMjR9LHsibmFtZSI6ICIzOCIsIngiOiA5MSwieSI6IDc3LCJ6IjogMjcsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwNTI3NzAyOTIsImFkZGVkQXQiOiAxNjcwMDAwMjI2NjI1fSx7Im5hbWUiOiAiMTUiLCJ4IjogNTAsInkiOiA4MCwieiI6IDg4LCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMDcxMjUxMTk5LCJhZGRlZEF0IjogMTY2OTk5OTUxMTUwNH0seyJuYW1lIjogIjE0IiwieCI6IDUwLCJ5IjogNzksInoiOiAzNCwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTEwNDkzNjcwMywiYWRkZWRBdCI6IDE2Njk5OTk1MTEzMDZ9LHsibmFtZSI6ICIxOSIsIngiOiA1OCwieSI6IDc5LCJ6IjogODksImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDExNTU2NjE4MTgsImFkZGVkQXQiOiAxNjY5OTk5NTE3ODEwfSx7Im5hbWUiOiAiMzAiLCJ4IjogNzgsInkiOiA3NCwieiI6IDk5LCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMTE5NDIwNDAzLCJhZGRlZEF0IjogMTY3MDAwMDAyMTgyM30seyJuYW1lIjogIjExIiwieCI6IDQ2LCJ5IjogODAsInoiOiA4NCwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTA3MTY0NDY2MiwiYWRkZWRBdCI6IDE2Njk5OTk1MTA3MDh9LHsibmFtZSI6ICI0MyIsIngiOiA5NywieSI6IDgxLCJ6IjogNzcsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwNTE5ODM4NjUsImFkZGVkQXQiOiAxNjcwMDAwMjI3Njc2fSx7Im5hbWUiOiAiMTciLCJ4IjogNTUsInkiOiA3OSwieiI6IDM0LCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMTA1MTk5MDk4LCJhZGRlZEF0IjogMTY2OTk5OTUxMTkwNX0seyJuYW1lIjogIjQiLCJ4IjogMzksInkiOiA4MCwieiI6IDczLCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMTUzMjM2NDc5LCJhZGRlZEF0IjogMTY2OTk5OTE5ODkyN30seyJuYW1lIjogIjQxIiwieCI6IDk1LCJ5IjogNzYsInoiOiA1OCwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTE1MTk5MTgwMSwiYWRkZWRBdCI6IDE2NzAwMDAyMjcyMjV9LHsibmFtZSI6ICI0MiIsIngiOiA5NywieSI6IDc1LCJ6IjogNzAsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwNTE3MjE3MDYsImFkZGVkQXQiOiAxNjcwMDAwMjI3NDczfSx7Im5hbWUiOiAiMTAiLCJ4IjogNDUsInkiOiA3OSwieiI6IDcwLCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMDcyMzY1NTYxLCJhZGRlZEF0IjogMTY2OTk5OTUxMDUwOH0seyJuYW1lIjogIjI4IiwieCI6IDc1LCJ5IjogODIsInoiOiAyMCwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTA1MjkwMTM1OSwiYWRkZWRBdCI6IDE2Njk5OTk5ODY0MjZ9LHsibmFtZSI6ICIzIiwieCI6IDM2LCJ5IjogODAsInoiOiA4MCwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogOTUyNjI5NTAzLCJhZGRlZEF0IjogMTY2OTk5OTE5ODcyN30seyJuYW1lIjogIjciLCJ4IjogNDMsInkiOiA3NywieiI6IDUwLCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMTIwNTM0MjcxLCJhZGRlZEF0IjogMTY2OTk5OTE5OTQ2N30seyJuYW1lIjogIjgiLCJ4IjogNDMsInkiOiA3OSwieiI6IDczLCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMTUyNzEwMzk5LCJhZGRlZEF0IjogMTY2OTk5OTMxMTAyOX0seyJuYW1lIjogIjIiLCJ4IjogMzUsInkiOiA4LCJ6IjogNzEsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwNTQzNDMxNjQsImFkZGVkQXQiOiAxNjY5OTk5MTk4NTY3fSx7Im5hbWUiOiAiMzQiLCJ4IjogODksInkiOiA3NywieiI6IDg0LCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMDg2NDU1Nzk3LCJhZGRlZEF0IjogMTY3MDAwMDAyMjUyOX0seyJuYW1lIjogIjI2IiwieCI6IDczLCJ5IjogNzYsInoiOiAzMSwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTExOTE1Nzc1OSwiYWRkZWRBdCI6IDE2Njk5OTk3NTEwNzd9LHsibmFtZSI6ICIxMiIsIngiOiA0NywieSI6IDc3LCJ6IjogNjUsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwNzE4NDEyNzcsImFkZGVkQXQiOiAxNjY5OTk5NTEwOTA4fSx7Im5hbWUiOiAiMTYiLCJ4IjogNTIsInkiOiA3NSwieiI6IDQ1LCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMTM4NjIyNDU3LCJhZGRlZEF0IjogMTY2OTk5OTUxMTcwM30seyJuYW1lIjogIjMzIiwieCI6IDgyLCJ5IjogNzgsInoiOiAyNiwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTEwMzAzNDM2NywiYWRkZWRBdCI6IDE2NzAwMDAwMjIzNzl9LHsibmFtZSI6ICIyMSIsIngiOiA2MSwieSI6IDc4LCJ6IjogOTIsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwMjExODE5NDIsImFkZGVkQXQiOiAxNjY5OTk5NzQ5ODc3fSx7Im5hbWUiOiAiMjciLCJ4IjogNzMsInkiOiA3OSwieiI6IDUyLCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMDUxMDAwODI0LCJhZGRlZEF0IjogMTY2OTk5OTk4NjIzMH0seyJuYW1lIjogIjQ2IiwieCI6IDEwMywieSI6IDc0LCJ6IjogOTgsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDExMTg1Njg0MjUsImFkZGVkQXQiOiAxNjcwMDAwMjI4MjIzfSx7Im5hbWUiOiAiNDciLCJ4IjogMTA0LCJ5IjogNzgsInoiOiA2OCwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogOTUzNDc5OTM1LCJhZGRlZEF0IjogMTY3MDAwMDM1Nzk3NH0seyJuYW1lIjogIjYiLCJ4IjogNDIsInkiOiA3NywieiI6IDU4LCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMDE4NDk0OTcwLCJhZGRlZEF0IjogMTY2OTk5OTE5OTMyNX0seyJuYW1lIjogIjUiLCJ4IjogNDEsInkiOiA3OSwieiI6IDgxLCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMTM2MzI4MTkxLCJhZGRlZEF0IjogMTY2OTk5OTE5OTEyMX0seyJuYW1lIjogIjM2IiwieCI6IDkwLCJ5IjogNzcsInoiOiA0NiwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTExOTA5MDQzMSwiYWRkZWRBdCI6IDE2NzAwMDAwMjI5Mjh9LHsibmFtZSI6ICIxIiwieCI6IDMyLCJ5IjogODAsInoiOiA3NCwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTM4ODc3MzM3MSwiYWRkZWRBdCI6IDE2Njk5OTkxMDI4ODJ9LHsibmFtZSI6ICIzMSIsIngiOiA3OCwieSI6IDc3LCJ6IjogNDAsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwMTkyMTU4NjAsImFkZGVkQXQiOiAxNjcwMDAwMDIxOTc1fSx7Im5hbWUiOiAiMjkiLCJ4IjogNzYsInkiOiA3NiwieiI6IDU1LCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMTUzNTY0NjU1LCJhZGRlZEF0IjogMTY2OTk5OTk4NjYyN30seyJuYW1lIjogIjI1IiwieCI6IDY2LCJ5IjogODEsInoiOiAyOCwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTA1MjcwNDc1MCwiYWRkZWRBdCI6IDE2Njk5OTk3NTA5Mjd9LHsibmFtZSI6ICIzNSIsIngiOiA5MCwieSI6IDc3LCJ6IjogMzgsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwMTkzNDY5MzMsImFkZGVkQXQiOiAxNjcwMDAwMDIyNzI0fSx7Im5hbWUiOiAiMTgiLCJ4IjogNTUsInkiOiA4MCwieiI6IDM4LCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMDg4NjE4NDkzLCJhZGRlZEF0IjogMTY2OTk5OTUxMjE1N30seyJuYW1lIjogIjM5IiwieCI6IDkyLCJ5IjogNzQsInoiOiAxMDgsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwODY4NDkwMTQsImFkZGVkQXQiOiAxNjcwMDAwMjI2ODc5fSx7Im5hbWUiOiAiMTMiLCJ4IjogNTAsInkiOiA3NiwieiI6IDUyLCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMDcxOTcyMzQ0LCJhZGRlZEF0IjogMTY2OTk5OTUxMTEwMn0seyJuYW1lIjogIjQ0IiwieCI6IDk4LCJ5IjogNzcsInoiOiA3NiwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTA4NTg2NTk3OCwiYWRkZWRBdCI6IDE2NzAwMDAyMjc4ODF9LHsibmFtZSI6ICIzMiIsIngiOiA3OSwieSI6IDgwLCJ6IjogNzMsImVuYWJsZWQiOiB0cnVlLCJjb2xvciI6IDEwNTI3NzAyOTgsImFkZGVkQXQiOiAxNjcwMDAwMDIyMTc0fSx7Im5hbWUiOiAiMzciLCJ4IjogOTEsInkiOiA3NiwieiI6IDM4LCJlbmFibGVkIjogdHJ1ZSwiY29sb3IiOiAxMDUxMTMxMzkxLCJhZGRlZEF0IjogMTY3MDAwMDIyNjQyM30seyJuYW1lIjogIjQ1IiwieCI6IDk4LCJ5IjogNzgsInoiOiA3NSwiZW5hYmxlZCI6IHRydWUsImNvbG9yIjogMTIwMjg0NzczNywiYWRkZWRBdCI6IDE2NzAwMDAyMjgwNzN9XSwiaXNsYW5kIjogIndpbnRlciJ9XX0=";
        List<WaypointCategory> waypointCategories = Waypoints.fromSkytils(waypointCategoriesSkytilsBase64, "");
        List<WaypointCategory> expectedWaypointCategories = List.of(new WaypointCategory("Frozen Treasure Locations", "winter", List.of(
                new NamedWaypoint(new BlockPos(64, 78, 28), "24", new float[]{177 / 255f, 253 / 255f, 255 / 255f}, 64 / 255f, true),
                new NamedWaypoint(new BlockPos(45, 79, 49), "9", new float[]{175 / 255f, 245 / 255f, 255 / 255f}, 64 / 255f, true),
                new NamedWaypoint(new BlockPos(60, 76, 51), "20", new float[]{210 / 255f, 254 / 255f, 255 / 255f}, 56 / 255f, true),
                new NamedWaypoint(new BlockPos(63, 76, 95), "23", new float[]{210 / 255f, 255 / 255f, 252 / 255f}, 60 / 255f, true),
                new NamedWaypoint(new BlockPos(63, 76, 52), "22", new float[]{186 / 255f, 255 / 255f, 243 / 255f}, 62 / 255f, true),
                new NamedWaypoint(new BlockPos(94, 77, 42), "40", new float[]{175 / 255f, 255 / 255f, 249 / 255f}, 58 / 255f, true),
                new NamedWaypoint(new BlockPos(91, 77, 27), "38", new float[]{191 / 255f, 255 / 255f, 244 / 255f}, 62 / 255f, true),
                new NamedWaypoint(new BlockPos(50, 80, 88), "15", new float[]{217 / 255f, 254 / 255f, 255 / 255f}, 63 / 255f, true),
                new NamedWaypoint(new BlockPos(50, 79, 34), "14", new float[]{219 / 255f, 254 / 255f, 255 / 255f}, 65 / 255f, true),
                new NamedWaypoint(new BlockPos(58, 79, 89), "19", new float[]{225 / 255f, 255 / 255f, 250 / 255f}, 68 / 255f, true),
                new NamedWaypoint(new BlockPos(78, 74, 99), "30", new float[]{184 / 255f, 255 / 255f, 243 / 255f}, 66 / 255f, true),
                new NamedWaypoint(new BlockPos(46, 80, 84), "11", new float[]{223 / 255f, 255 / 255f, 246 / 255f}, 63 / 255f, true),
                new NamedWaypoint(new BlockPos(97, 81, 77), "43", new float[]{179 / 255f, 255 / 255f, 249 / 255f}, 62 / 255f, true),
                new NamedWaypoint(new BlockPos(55, 79, 34), "17", new float[]{223 / 255f, 255 / 255f, 250 / 255f}, 65 / 255f, true),
                new NamedWaypoint(new BlockPos(39, 80, 73), "4", new float[]{188 / 255f, 253 / 255f, 255 / 255f}, 68 / 255f, true),
                new NamedWaypoint(new BlockPos(95, 76, 58), "41", new float[]{169 / 255f, 255 / 255f, 249 / 255f}, 68 / 255f, true),
                new NamedWaypoint(new BlockPos(97, 75, 70), "42", new float[]{175 / 255f, 255 / 255f, 234 / 255f}, 62 / 255f, true),
                new NamedWaypoint(new BlockPos(45, 79, 70), "10", new float[]{234 / 255f, 255 / 255f, 249 / 255f}, 63 / 255f, true),
                new NamedWaypoint(new BlockPos(75, 82, 20), "28", new float[]{193 / 255f, 255 / 255f, 239 / 255f}, 62 / 255f, true),
                new NamedWaypoint(new BlockPos(36, 80, 80), "3", new float[]{199 / 255f, 248 / 255f, 255 / 255f}, 56 / 255f, true),
                new NamedWaypoint(new BlockPos(43, 77, 50), "7", new float[]{201 / 255f, 254 / 255f, 255 / 255f}, 66 / 255f, true),
                new NamedWaypoint(new BlockPos(43, 79, 73), "8", new float[]{180 / 255f, 246 / 255f, 255 / 255f}, 68 / 255f, true),
                new NamedWaypoint(new BlockPos(35, 8, 71), "2", new float[]{215 / 255f, 255 / 255f, 252 / 255f}, 62 / 255f, true),
                new NamedWaypoint(new BlockPos(89, 77, 84), "34", new float[]{193 / 255f, 255 / 255f, 245 / 255f}, 64 / 255f, true),
                new NamedWaypoint(new BlockPos(73, 76, 31), "26", new float[]{180 / 255f, 253 / 255f, 255 / 255f}, 66 / 255f, true),
                new NamedWaypoint(new BlockPos(47, 77, 65), "12", new float[]{226 / 255f, 255 / 255f, 253 / 255f}, 63 / 255f, true),
                new NamedWaypoint(new BlockPos(52, 75, 45), "16", new float[]{221 / 255f, 255 / 255f, 249 / 255f}, 67 / 255f, true),
                new NamedWaypoint(new BlockPos(82, 78, 26), "33", new float[]{190 / 255f, 247 / 255f, 255 / 255f}, 65 / 255f, true),
                new NamedWaypoint(new BlockPos(61, 78, 92), "21", new float[]{221 / 255f, 255 / 255f, 246 / 255f}, 60 / 255f, true),
                new NamedWaypoint(new BlockPos(73, 79, 52), "27", new float[]{164 / 255f, 255 / 255f, 248 / 255f}, 62 / 255f, true),
                new NamedWaypoint(new BlockPos(103, 74, 98), "46", new float[]{171 / 255f, 255 / 255f, 233 / 255f}, 66 / 255f, true),
                new NamedWaypoint(new BlockPos(104, 78, 68), "47", new float[]{212 / 255f, 242 / 255f, 255 / 255f}, 56 / 255f, true),
                new NamedWaypoint(new BlockPos(42, 77, 58), "6", new float[]{180 / 255f, 255 / 255f, 250 / 255f}, 60 / 255f, true),
                new NamedWaypoint(new BlockPos(41, 79, 81), "5", new float[]{186 / 255f, 253 / 255f, 255 / 255f}, 67 / 255f, true),
                new NamedWaypoint(new BlockPos(90, 77, 46), "36", new float[]{179 / 255f, 246 / 255f, 255 / 255f}, 66 / 255f, true),
                new NamedWaypoint(new BlockPos(32, 80, 74), "1", new float[]{198 / 255f, 255 / 255f, 251 / 255f}, 82 / 255f, true),
                new NamedWaypoint(new BlockPos(78, 77, 40), "31", new float[]{191 / 255f, 255 / 255f, 244 / 255f}, 60 / 255f, true),
                new NamedWaypoint(new BlockPos(76, 76, 55), "29", new float[]{193 / 255f, 255 / 255f, 239 / 255f}, 68 / 255f, true),
                new NamedWaypoint(new BlockPos(66, 81, 28), "25", new float[]{190 / 255f, 255 / 255f, 238 / 255f}, 62 / 255f, true),
                new NamedWaypoint(new BlockPos(90, 77, 38), "35", new float[]{193 / 255f, 255 / 255f, 245 / 255f}, 60 / 255f, true),
                new NamedWaypoint(new BlockPos(55, 80, 38), "18", new float[]{226 / 255f, 255 / 255f, 253 / 255f}, 64 / 255f, true),
                new NamedWaypoint(new BlockPos(92, 74, 108), "39", new float[]{199 / 255f, 255 / 255f, 246 / 255f}, 64 / 255f, true),
                new NamedWaypoint(new BlockPos(50, 76, 52), "13", new float[]{228 / 255f, 255 / 255f, 248 / 255f}, 63 / 255f, true),
                new NamedWaypoint(new BlockPos(98, 77, 76), "44", new float[]{184 / 255f, 255 / 255f, 250 / 255f}, 64 / 255f, true),
                new NamedWaypoint(new BlockPos(79, 80, 73), "32", new float[]{191 / 255f, 255 / 255f, 250 / 255f}, 62 / 255f, true),
                new NamedWaypoint(new BlockPos(91, 76, 38), "37", new float[]{166 / 255f, 253 / 255f, 255 / 255f}, 62 / 255f, true),
                new NamedWaypoint(new BlockPos(98, 78, 75), "45", new float[]{177 / 255f, 255 / 255f, 249 / 255f}, 71 / 255f, true)
        )));

        Assertions.assertEquals(expectedWaypointCategories, waypointCategories);
    }

    //https://pastebin.com/c4PjUZjJ
    @Test
    void testFromSkytilsBase64CrystalHollowsWaypoints() {
        String waypointsSkytilsBase64 = "W3sibmFtZSI6IlNob3V0b3V0IFRlYmV5IGFuZCB0cmV2YW55YSIsIngiOjUxMCwieSI6NTMsInoiOjM5MywiaXNsYW5kIjoiY3J5c3RhbF9ob2xsb3dzIiwiZW5hYmxlZCI6dHJ1ZSwiY29sb3IiOi02NTUzNn0seyJuYW1lIjoiMDciLCJ4Ijo0NzksInkiOjM5LCJ6Ijo0MDgsImlzbGFuZCI6ImNyeXN0YWxfaG9sbG93cyIsImVuYWJsZWQiOnRydWUsImNvbG9yIjotNjU1MzZ9LHsibmFtZSI6IjExIiwieCI6NDk1LCJ5IjozNCwieiI6NDE4LCJpc2xhbmQiOiJjcnlzdGFsX2hvbGxvd3MiLCJlbmFibGVkIjp0cnVlLCJjb2xvciI6LTY1NTM2fSx7Im5hbWUiOiIwOSIsIngiOjQ4NSwieSI6MzMsInoiOjQwMiwiaXNsYW5kIjoiY3J5c3RhbF9ob2xsb3dzIiwiZW5hYmxlZCI6dHJ1ZSwiY29sb3IiOi02NTUzNn0seyJuYW1lIjoiMjMiLCJ4Ijo1MTQsInkiOjU1LCJ6IjozODMsImlzbGFuZCI6ImNyeXN0YWxfaG9sbG93cyIsImVuYWJsZWQiOnRydWUsImNvbG9yIjotNjU1MzZ9LHsibmFtZSI6IjA2IiwieCI6NDgzLCJ5Ijo0MiwieiI6NDA1LCJpc2xhbmQiOiJjcnlzdGFsX2hvbGxvd3MiLCJlbmFibGVkIjp0cnVlLCJjb2xvciI6LTY1NTM2fSx7Im5hbWUiOiIwMSIsIngiOjUwMiwieSI6NDgsInoiOjQwMywiaXNsYW5kIjoiY3J5c3RhbF9ob2xsb3dzIiwiZW5hYmxlZCI6dHJ1ZSwiY29sb3IiOi02NTUzNn0seyJuYW1lIjoiMTgiLCJ4Ijo1MDMsInkiOjU2LCJ6Ijo0MzYsImlzbGFuZCI6ImNyeXN0YWxfaG9sbG93cyIsImVuYWJsZWQiOnRydWUsImNvbG9yIjotNjU1MzZ9LHsibmFtZSI6IjAwIC0gU3RhcnQiLCJ4Ijo1MDMsInkiOjQ4LCJ6Ijo0MDAsImlzbGFuZCI6ImNyeXN0YWxfaG9sbG93cyIsImVuYWJsZWQiOnRydWUsImNvbG9yIjotNjU1MzZ9LHsibmFtZSI6IjEzIiwieCI6NDc4LCJ5Ijo0NCwieiI6NDE5LCJpc2xhbmQiOiJjcnlzdGFsX2hvbGxvd3MiLCJlbmFibGVkIjp0cnVlLCJjb2xvciI6LTY1NTM2fSx7Im5hbWUiOiIxOSIsIngiOjUwMSwieSI6NTcsInoiOjQzOCwiaXNsYW5kIjoiY3J5c3RhbF9ob2xsb3dzIiwiZW5hYmxlZCI6dHJ1ZSwiY29sb3IiOi02NTUzNn0seyJuYW1lIjoiMDIiLCJ4Ijo0OTYsInkiOjQ1LCJ6Ijo0MDcsImlzbGFuZCI6ImNyeXN0YWxfaG9sbG93cyIsImVuYWJsZWQiOnRydWUsImNvbG9yIjotNjU1MzZ9LHsibmFtZSI6IjA0IiwieCI6NDk1LCJ5Ijo1MywieiI6NDA0LCJpc2xhbmQiOiJjcnlzdGFsX2hvbGxvd3MiLCJlbmFibGVkIjp0cnVlLCJjb2xvciI6LTY1NTM2fSx7Im5hbWUiOiIwNSIsIngiOjQ3OSwieSI6NDksInoiOjQwNywiaXNsYW5kIjoiY3J5c3RhbF9ob2xsb3dzIiwiZW5hYmxlZCI6dHJ1ZSwiY29sb3IiOi02NTUzNn0seyJuYW1lIjoiMTIiLCJ4Ijo1MDQsInkiOjQxLCJ6Ijo0MTksImlzbGFuZCI6ImNyeXN0YWxfaG9sbG93cyIsImVuYWJsZWQiOnRydWUsImNvbG9yIjotNjU1MzZ9LHsibmFtZSI6IjAzIiwieCI6NDkwLCJ5Ijo0NSwieiI6MzkyLCJpc2xhbmQiOiJjcnlzdGFsX2hvbGxvd3MiLCJlbmFibGVkIjp0cnVlLCJjb2xvciI6LTY1NTM2fSx7Im5hbWUiOiIxMCIsIngiOjQ4OCwieSI6MzIsInoiOjQyMSwiaXNsYW5kIjoiY3J5c3RhbF9ob2xsb3dzIiwiZW5hYmxlZCI6dHJ1ZSwiY29sb3IiOi02NTUzNn0seyJuYW1lIjoiMjIiLCJ4Ijo1MDcsInkiOjUyLCJ6IjozODYsImlzbGFuZCI6ImNyeXN0YWxfaG9sbG93cyIsImVuYWJsZWQiOnRydWUsImNvbG9yIjotNjU1MzZ9LHsibmFtZSI6IjE2IiwieCI6NDg4LCJ5Ijo1NSwieiI6NDIxLCJpc2xhbmQiOiJjcnlzdGFsX2hvbGxvd3MiLCJlbmFibGVkIjp0cnVlLCJjb2xvciI6LTY1NTM2fSx7Im5hbWUiOiI5OSAtIEVuZCIsIngiOjUxMCwieSI6NTIsInoiOjM5MywiaXNsYW5kIjoiY3J5c3RhbF9ob2xsb3dzIiwiZW5hYmxlZCI6dHJ1ZSwiY29sb3IiOi02NTUzNn0seyJuYW1lIjoiMTUiLCJ4Ijo0ODYsInkiOjU1LCJ6Ijo0MjgsImlzbGFuZCI6ImNyeXN0YWxfaG9sbG93cyIsImVuYWJsZWQiOnRydWUsImNvbG9yIjotNjU1MzZ9LHsibmFtZSI6IjE0IiwieCI6NDc1LCJ5Ijo0NCwieiI6NDI5LCJpc2xhbmQiOiJjcnlzdGFsX2hvbGxvd3MiLCJlbmFibGVkIjp0cnVlLCJjb2xvciI6LTY1NTM2fSx7Im5hbWUiOiIxNyIsIngiOjUwOSwieSI6NTAsInoiOjQzMiwiaXNsYW5kIjoiY3J5c3RhbF9ob2xsb3dzIiwiZW5hYmxlZCI6dHJ1ZSwiY29sb3IiOi02NTUzNn0seyJuYW1lIjoiMjAiLCJ4Ijo1MDUsInkiOjU4LCJ6Ijo0MjYsImlzbGFuZCI6ImNyeXN0YWxfaG9sbG93cyIsImVuYWJsZWQiOnRydWUsImNvbG9yIjotNjU1MzZ9LHsibmFtZSI6IjA4IiwieCI6NDgwLCJ5IjozOCwieiI6NDA1LCJpc2xhbmQiOiJjcnlzdGFsX2hvbGxvd3MiLCJlbmFibGVkIjp0cnVlLCJjb2xvciI6LTY1NTM2fSx7Im5hbWUiOiIyMSIsIngiOjQ5NywieSI6NTUsInoiOjM5MSwiaXNsYW5kIjoiY3J5c3RhbF9ob2xsb3dzIiwiZW5hYmxlZCI6dHJ1ZSwiY29sb3IiOi02NTUzNn1d";
        List<WaypointCategory> waypointCategories = Waypoints.fromSkytils(waypointsSkytilsBase64, "crystal_hollows");
        List<WaypointCategory> expectedWaypointCategories = List.of(new WaypointCategory("New Category", "crystal_hollows", List.of(
                new NamedWaypoint(new BlockPos(510, 53, 393), "Shoutout Tebey and trevanya", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(479, 39, 408), "07", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(495, 34, 418), "11", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(485, 33, 402), "09", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(514, 55, 383), "23", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(483, 42, 405), "06", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(502, 48, 403), "01", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(503, 56, 436), "18", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(503, 48, 400), "00 - Start", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(478, 44, 419), "13", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(501, 57, 438), "19", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(496, 45, 407), "02", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(495, 53, 404), "04", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(479, 49, 407), "05", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(504, 41, 419), "12", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(490, 45, 392), "03", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(488, 32, 421), "10", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(507, 52, 386), "22", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(488, 55, 421), "16", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(510, 52, 393), "99 - End", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(486, 55, 428), "15", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(475, 44, 429), "14", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(509, 50, 432), "17", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(505, 58, 426), "20", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(480, 38, 405), "08", new float[]{1, 0, 0}, 1, true),
                new NamedWaypoint(new BlockPos(497, 55, 391), "21", new float[]{1, 0, 0}, 1, true)
        )));

        Assertions.assertEquals(expectedWaypointCategories, waypointCategories);
    }

    @Test
    void testFromSkytilsV1Gzip() {
        String waypointsSkytilsV1Gzip = "<Skytils-Waypoint-Data>(V1):H4sIAAAAAAAC/51STWuEMBD9KyHntJjJl7u3UugfsIdC2UOq6SKk0cZIaxf/e+OC4moL4i2TmXnvzby54FwHc658aRp8RK8X7PSHiS+ctXVtO5Tpd4OyugoNJvhLd3VVurAs/Wy1NzH/HaM7yinBXXwpSfDP8HNICDZOv1lTxDD41hCsi8IUDyHGVCp+4JLGPi57MoMN2gf07EvtznaCl+kCnQJsgGeRgfM5/BJ4UHkFVhNwukm34sD6E8FlY7UbKnHpmqBdHtvmhE+tsejRWLtpqaM8BPfJKDGlS4mMrSXmla38NSu4lDDYsVItEg5ws46XyT8QKxa5j4UBCEhuPN1xKluYIBVUsZt55oujbJxJTObu3BxPmAL51yGhkU2tL4nuJBMCgP57XKf+FzPhnyPAAwAA";
        List<WaypointCategory> waypointCategories = Waypoints.fromSkytils(waypointsSkytilsV1Gzip, "default_island");
        List<WaypointCategory> expectedWaypointCategories = List.of(new WaypointCategory("Supply Safe Spots", "default_island", List.of(
                new NamedWaypoint(new BlockPos(-141, 76, -90), "Square", new float[]{0, 1, 0}, 128 / 255f, true),
                new NamedWaypoint(new BlockPos(-68, 76, -122), "Start Triangle", new float[]{0, 1, 0}, 128 / 255f, true),
                new NamedWaypoint(new BlockPos(-90, 77, -128), "Triangle", new float[]{0, 1, 0}, 128 / 255f, true)
        )), new WaypointCategory("Fuel Cell Safe Spots", "default_island", List.of(
                new NamedWaypoint(new BlockPos(-81, 77, -133), "Triangle 2.0", new float[]{20 / 255f, 0, 1}, 1, true),
                new NamedWaypoint(new BlockPos(-125, 77, -136), "X", new float[]{20 / 255f, 0, 1}, 1, true),
                new NamedWaypoint(new BlockPos(-141, 76, -90), "Square", new float[]{20 / 255f, 0, 1}, 1, true),
                new NamedWaypoint(new BlockPos(-135, 75, -123), "X 2.0", new float[]{20 / 255f, 0, 1}, 1, true),
                new NamedWaypoint(new BlockPos(-70, 77, -121), "Triangle ", new float[]{20 / 255f, 0, 1}, 1, true)
        )));

        Assertions.assertEquals(expectedWaypointCategories, waypointCategories);
    }
}
