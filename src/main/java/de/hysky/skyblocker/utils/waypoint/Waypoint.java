package de.hysky.skyblocker.utils.waypoint;

import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.Renderable;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents a waypoint with a position, type, color, alpha, line width, through walls, and enabled state.
 * <p>
 * Extend this class and override at least the withers to create custom waypoint types and behavior.
 */
public class Waypoint implements Renderable {
    protected static final float DEFAULT_HIGHLIGHT_ALPHA = 0.5f;
    protected static final float DEFAULT_LINE_WIDTH = 5f;
    public final BlockPos pos;
    final Supplier<Type> typeSupplier;
    /**
     * The color components of the waypoint.
     * <p>
     * For custom color behavior, override {@link #getRenderColorComponents()}.
     * This field must contain valid color components of the waypoint
     * even if this is not being used for rendering (i.e. {@link #getRenderColorComponents()} is overridden)
     * since this field is used for serialization.
     */
    public final float[] colorComponents;
    public final float alpha;
    public final float lineWidth;
    public final boolean throughWalls;
    private boolean enabled;

    // region Constructors
    public Waypoint(BlockPos pos, Type type, float[] colorComponents) {
        this(pos, type, colorComponents, DEFAULT_HIGHLIGHT_ALPHA);
    }

    public Waypoint(BlockPos pos, Supplier<Type> typeSupplier, float[] colorComponents) {
        this(pos, typeSupplier, colorComponents, DEFAULT_HIGHLIGHT_ALPHA, DEFAULT_LINE_WIDTH);
    }

    public Waypoint(BlockPos pos, Type type,                   float[] colorComponents, float alpha) { // @formatter:off
        this(pos, () -> type, colorComponents, alpha, DEFAULT_LINE_WIDTH); // @formatter:on
    }

    public Waypoint(BlockPos pos, Supplier<Type> typeSupplier, float[] colorComponents, float alpha, float lineWidth) {
        this(pos, typeSupplier, colorComponents, alpha, lineWidth, true);
    }

    public Waypoint(BlockPos pos, Type type,                   float[] colorComponents, boolean throughWalls) { // @formatter:off
        this(pos, () -> type, colorComponents, throughWalls); // @formatter:on
    }

    public Waypoint(BlockPos pos, Supplier<Type> typeSupplier, float[] colorComponents, boolean throughWalls) {
        this(pos, typeSupplier, colorComponents, DEFAULT_HIGHLIGHT_ALPHA, DEFAULT_LINE_WIDTH, throughWalls);
    }

    public Waypoint(BlockPos pos, Supplier<Type> typeSupplier, float[] colorComponents, float alpha, float lineWidth, boolean throughWalls) {
        this(pos, typeSupplier, colorComponents, alpha, lineWidth, throughWalls, true);
    }

    public Waypoint(BlockPos pos, Supplier<Type> typeSupplier, float[] colorComponents, float alpha, float lineWidth, boolean throughWalls, boolean enabled) {
        this.pos = pos;
        this.typeSupplier = typeSupplier;
        this.colorComponents = colorComponents;
        this.alpha = alpha;
        this.lineWidth = lineWidth;
        this.throughWalls = throughWalls;
        this.enabled = enabled;
    }
    // endregion

    // region Withers
    /**
     * Subclasses should override this method to return a new instance of the subclass with the specified x pos.
     */
    public Waypoint withX(int x) {
        return new Waypoint(new BlockPos(x, pos.getY(), pos.getZ()), typeSupplier, colorComponents, alpha, lineWidth, throughWalls, enabled);
    }

    /**
     * Subclasses should override this method to return a new instance of the subclass with the specified y pos.
     */
    public Waypoint withY(int y) {
        return new Waypoint(pos.withY(y), typeSupplier, colorComponents, alpha, lineWidth, throughWalls, enabled);
    }

    /**
     * Subclasses should override this method to return a new instance of the subclass with the specified z pos.
     */
    public Waypoint withZ(int z) {
        return new Waypoint(new BlockPos(pos.getX(), pos.getY(), z), typeSupplier, colorComponents, alpha, lineWidth, throughWalls, enabled);
    }

    /**
     * Subclasses should override this method to return a new instance of the subclass with the specified color components and alpha.
     */
    public Waypoint withColor(float[] colorComponents, float alpha) {
        return new Waypoint(pos, typeSupplier, colorComponents, alpha, lineWidth, throughWalls, enabled);
    }
    // endregion

    // region Getters and Setters
    /**
     * Whether the waypoint should be rendered.
     * <p>
     * Checked in {@link #render(WorldRenderContext)} before rendering.
     * <p>
     * Override this method for custom behavior.
     */
    public boolean shouldRender() {
        return enabled;
    }

    /**
     * Sets the waypoint as found and enabled as false.
     * <p>
     * Override this method for custom behavior.
     */
    public void setFound() {
        this.enabled = false;
    }

    /**
     * Sets the waypoint as missing and enabled as true.
     * <p>
     * Override this method for custom behavior.
     */
    public void setMissing() {
        this.enabled = true;
    }

    /**
     * Toggles the enabled state of the waypoint.
     * <p>
     * Override this method for custom behavior.
     */
    public void toggle() {
        if (enabled) {
            setFound();
        } else {
            setMissing();
        }
    }

    public final boolean isEnabled() {
        return enabled;
    }

    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    // endregion

    /**
     * Returns the render time color components of the waypoint.
     * <p>
     * Override this method for custom behavior.
     */
    public float[] getRenderColorComponents() {
        return colorComponents;
    }

    /**
     * Renders the waypoint.
     * <p>
     * Checks if the waypoint {@link #shouldRender() should be rendered}.
     * <p>
     * Override this method for custom behavior.
     */
    @Override
    public void render(WorldRenderContext context) {
        if (!shouldRender()) return;
        switch (typeSupplier.get()) {
            case WAYPOINT -> RenderHelper.renderFilledWithBeaconBeam(context, pos, getRenderColorComponents(), alpha, throughWalls);
            case OUTLINED_WAYPOINT -> {
                float[] colorComponents = getRenderColorComponents();
                RenderHelper.renderFilledWithBeaconBeam(context, pos, colorComponents, alpha, throughWalls);
                RenderHelper.renderOutline(context, pos, colorComponents, lineWidth, throughWalls);
            }
            case HIGHLIGHT -> RenderHelper.renderFilled(context, pos, getRenderColorComponents(), alpha, throughWalls);
            case OUTLINED_HIGHLIGHT -> {
                float[] colorComponents = getRenderColorComponents();
                RenderHelper.renderFilled(context, pos, colorComponents, alpha, throughWalls);
                RenderHelper.renderOutline(context, pos, colorComponents, lineWidth, throughWalls);
            }
            case OUTLINE -> RenderHelper.renderOutline(context, pos, getRenderColorComponents(), lineWidth, throughWalls);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, typeSupplier.get(), Arrays.hashCode(colorComponents), alpha, lineWidth, throughWalls, enabled);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || obj instanceof Waypoint other && pos.equals(other.pos) && typeSupplier.get() == other.typeSupplier.get() && Arrays.equals(colorComponents, other.colorComponents) && alpha == other.alpha && lineWidth == other.lineWidth && throughWalls == other.throughWalls && enabled == other.enabled;
    }

    public enum Type implements StringIdentifiable {
        WAYPOINT,
        OUTLINED_WAYPOINT,
        HIGHLIGHT,
        OUTLINED_HIGHLIGHT,
        OUTLINE;

        @Override
        public String asString() {
            return name().toLowerCase();
        }

        @Override
        public String toString() {
            return I18n.translate("skyblocker.waypoints.type." + name());
        }

        public Type withoutBeacon() {
            return switch (this) {
                case WAYPOINT -> HIGHLIGHT;
                case OUTLINED_WAYPOINT -> OUTLINED_HIGHLIGHT;
                default -> this;
            };
        }
    }
}
