package de.hysky.skyblocker.utils.waypoint;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WaypointTest {
    private Waypoint.Type type;
    private final float[] colorComponents = new float[]{0f, 0.5f, 1f};

    @Test
    void testDefaultConstructor() {
        Waypoint waypoint = new Waypoint(BlockPos.ORIGIN, () -> type, colorComponents);
        Assertions.assertEquals(BlockPos.ORIGIN, waypoint.pos);
        Assertions.assertEquals(type, waypoint.typeSupplier.get());
        Assertions.assertEquals(0f, waypoint.colorComponents[0]);
        Assertions.assertEquals(0.5f, waypoint.colorComponents[1]);
        Assertions.assertEquals(1f, waypoint.colorComponents[2]);
        Assertions.assertEquals(Waypoint.DEFAULT_HIGHLIGHT_ALPHA, waypoint.alpha);
        Assertions.assertEquals(Waypoint.DEFAULT_LINE_WIDTH, waypoint.lineWidth);
        Assertions.assertTrue(waypoint.throughWalls);
        Assertions.assertTrue(waypoint.shouldRender());
    }

    @Test
    void testTypeConstructor() {
        Waypoint waypoint = new Waypoint(BlockPos.ORIGIN, Waypoint.Type.WAYPOINT, colorComponents, Waypoint.DEFAULT_HIGHLIGHT_ALPHA);
        Assertions.assertEquals(Waypoint.Type.WAYPOINT, waypoint.typeSupplier.get());
    }

    @Test
    void testLineWidthConstructor() {
        Waypoint waypoint = new Waypoint(BlockPos.ORIGIN, () -> type, colorComponents, Waypoint.DEFAULT_HIGHLIGHT_ALPHA, 10f);
        Assertions.assertEquals(10f, waypoint.lineWidth);
    }

    @Test
    void testThroughWallsConstructor() {
        Waypoint waypoint = new Waypoint(BlockPos.ORIGIN, () -> type, colorComponents, Waypoint.DEFAULT_HIGHLIGHT_ALPHA, Waypoint.DEFAULT_LINE_WIDTH, false);
        Assertions.assertFalse(waypoint.throughWalls);
    }

    @Test
    void testShouldRenderConstructor() {
        Waypoint waypoint = new Waypoint(BlockPos.ORIGIN, () -> type, colorComponents, Waypoint.DEFAULT_HIGHLIGHT_ALPHA, Waypoint.DEFAULT_LINE_WIDTH, true, false);
        Assertions.assertFalse(waypoint.shouldRender());
    }

    @Test
    void testFound() {
        Waypoint waypoint = new Waypoint(BlockPos.ORIGIN, () -> type, colorComponents);
        Assertions.assertTrue(waypoint.shouldRender());
        waypoint.setFound();
        Assertions.assertFalse(waypoint.shouldRender());
        waypoint.setMissing();
        Assertions.assertTrue(waypoint.shouldRender());
    }

    @Test
    void testType() {
        Waypoint waypoint = new Waypoint(BlockPos.ORIGIN, () -> type, colorComponents);
        Assertions.assertEquals(type, waypoint.typeSupplier.get());
        type = Waypoint.Type.WAYPOINT;
        Assertions.assertEquals(type, waypoint.typeSupplier.get());
        type = Waypoint.Type.OUTLINED_HIGHLIGHT;
        Assertions.assertEquals(type, waypoint.typeSupplier.get());
    }
}
