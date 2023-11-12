package de.hysky.skyblocker.utils.waypoint;

import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.function.Supplier;

public class Waypoint {
    protected static final float DEFAULT_HIGHLIGHT_ALPHA = 0.5f;
    protected static final float DEFAULT_LINE_WIDTH = 5f;
    public final BlockPos pos;
    final Box box;
    final Supplier<Type> typeSupplier;
    final float[] colorComponents;
    final float alpha;
    final float lineWidth;
    final boolean throughWalls;
    private boolean shouldRender;

    protected Waypoint(BlockPos pos, Supplier<Type> typeSupplier, float[] colorComponents) {
        this(pos, typeSupplier, colorComponents, DEFAULT_HIGHLIGHT_ALPHA);
    }

    protected Waypoint(BlockPos pos, Type type, float[] colorComponents, float alpha) {
        this(pos, () -> type, colorComponents, alpha);
    }

    protected Waypoint(BlockPos pos, Supplier<Type> typeSupplier, float[] colorComponents, float alpha) {
        this(pos, typeSupplier, colorComponents, alpha, DEFAULT_LINE_WIDTH);
    }

    protected Waypoint(BlockPos pos, Supplier<Type> typeSupplier, float[] colorComponents, float alpha, float lineWidth) {
        this(pos, typeSupplier, colorComponents, alpha, lineWidth, true);
    }

    protected Waypoint(BlockPos pos, Supplier<Type> typeSupplier, float[] colorComponents, float alpha, float lineWidth, boolean throughWalls) {
        this(pos, typeSupplier, colorComponents, alpha, lineWidth, throughWalls, true);
    }

    protected Waypoint(BlockPos pos, Supplier<Type> typeSupplier, float[] colorComponents, float alpha, float lineWidth, boolean throughWalls, boolean shouldRender) {
        this.pos = pos;
        this.box = new Box(pos);
        this.typeSupplier = typeSupplier;
        this.colorComponents = colorComponents;
        this.alpha = alpha;
        this.lineWidth = lineWidth;
        this.throughWalls = throughWalls;
        this.shouldRender = shouldRender;
    }

    public boolean shouldRender() {
        return shouldRender;
    }

    public void setFound() {
        this.shouldRender = false;
    }

    public void setMissing() {
        this.shouldRender = true;
    }

    public void render(WorldRenderContext context) {
        switch (typeSupplier.get()) {
            case WAYPOINT -> RenderHelper.renderFilledThroughWallsWithBeaconBeam(context, pos, colorComponents, alpha);
            case OUTLINED_WAYPOINT -> {
                RenderHelper.renderFilledThroughWallsWithBeaconBeam(context, pos, colorComponents, alpha);
                RenderHelper.renderOutline(context, box, colorComponents, lineWidth, throughWalls);
            }
            case HIGHLIGHT -> RenderHelper.renderFilledThroughWalls(context, pos, colorComponents, alpha);
            case OUTLINED_HIGHLIGHT -> {
                RenderHelper.renderFilledThroughWalls(context, pos, colorComponents, alpha);
                RenderHelper.renderOutline(context, box, colorComponents, lineWidth, throughWalls);
            }
            case OUTLINE -> RenderHelper.renderOutline(context, box, colorComponents, lineWidth, throughWalls);
        }
    }

    public enum Type {
        WAYPOINT,
        OUTLINED_WAYPOINT,
        HIGHLIGHT,
        OUTLINED_HIGHLIGHT,
        OUTLINE;

        @Override
        public String toString() {
            return switch (this) {
                case WAYPOINT -> "Waypoint";
                case OUTLINED_WAYPOINT -> "Outlined Waypoint";
                case HIGHLIGHT -> "Highlight";
                case OUTLINED_HIGHLIGHT -> "Outlined Highlight";
                case OUTLINE -> "Outline";
            };
        }
    }
}
