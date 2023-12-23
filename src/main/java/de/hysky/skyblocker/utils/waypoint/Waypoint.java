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

    public Waypoint(BlockPos pos, Supplier<Type> typeSupplier, float[] colorComponents) {
        this(pos, typeSupplier, colorComponents, DEFAULT_HIGHLIGHT_ALPHA, DEFAULT_LINE_WIDTH);
    }

    public Waypoint(BlockPos pos, Type type, float[] colorComponents, float alpha) {
        this(pos, () -> type, colorComponents, alpha, DEFAULT_LINE_WIDTH);
    }

    public Waypoint(BlockPos pos, Supplier<Type> typeSupplier, float[] colorComponents, float alpha, float lineWidth) {
        this(pos, typeSupplier, colorComponents, alpha, lineWidth, true);
    }

    public Waypoint(BlockPos pos, Supplier<Type> typeSupplier, float[] colorComponents, boolean throughWalls) {
        this(pos, typeSupplier, colorComponents, DEFAULT_HIGHLIGHT_ALPHA, DEFAULT_LINE_WIDTH, throughWalls);
    }

    public Waypoint(BlockPos pos, Supplier<Type> typeSupplier, float[] colorComponents, float alpha, float lineWidth, boolean throughWalls) {
        this(pos, typeSupplier, colorComponents, alpha, lineWidth, throughWalls, true);
    }

    public Waypoint(BlockPos pos, Supplier<Type> typeSupplier, float[] colorComponents, float alpha, float lineWidth, boolean throughWalls, boolean shouldRender) {
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

    protected float[] getColorComponents() {
        return colorComponents;
    }

    public void render(WorldRenderContext context) {
        switch (typeSupplier.get()) {
            case WAYPOINT -> RenderHelper.renderFilledWithBeaconBeam(context, pos, getColorComponents(), alpha, throughWalls);
            case OUTLINED_WAYPOINT -> {
                float[] colorComponents = getColorComponents();
                RenderHelper.renderFilledWithBeaconBeam(context, pos, colorComponents, alpha, throughWalls);
                RenderHelper.renderOutline(context, box, colorComponents, lineWidth, throughWalls);
            }
            case HIGHLIGHT -> RenderHelper.renderFilled(context, pos, getColorComponents(), alpha, throughWalls);
            case OUTLINED_HIGHLIGHT -> {
                float[] colorComponents = getColorComponents();
                RenderHelper.renderFilled(context, pos, colorComponents, alpha, throughWalls);
                RenderHelper.renderOutline(context, box, colorComponents, lineWidth, throughWalls);
            }
            case OUTLINE -> RenderHelper.renderOutline(context, box, getColorComponents(), lineWidth, throughWalls);
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
