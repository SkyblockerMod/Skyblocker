package de.hysky.skyblocker.utils.waypoint;

import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.Renderable;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

public class Waypoint implements Renderable {
    protected static final float DEFAULT_HIGHLIGHT_ALPHA = 0.5f;
    protected static final float DEFAULT_LINE_WIDTH = 5f;
    public final BlockPos pos;
    final Box box;
    final Supplier<Type> typeSupplier;
    protected final float[] colorComponents;
    public final float alpha;
    public final float lineWidth;
    public final boolean throughWalls;
    private boolean shouldRender;

    public Waypoint(BlockPos pos, Type type, float[] colorComponents) {
        this(pos, type, colorComponents, DEFAULT_HIGHLIGHT_ALPHA);
    }

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

    public Waypoint withX(int x) {
        return new Waypoint(new BlockPos(x, pos.getY(), pos.getZ()), typeSupplier, getColorComponents(), alpha, lineWidth, throughWalls, shouldRender());
    }

    public Waypoint withY(int y) {
        return new Waypoint(pos.withY(y), typeSupplier, getColorComponents(), alpha, lineWidth, throughWalls, shouldRender());
    }

    public Waypoint withZ(int z) {
        return new Waypoint(new BlockPos(pos.getX(), pos.getY(), z), typeSupplier, getColorComponents(), alpha, lineWidth, throughWalls, shouldRender());
    }

    public Waypoint withColor(float[] colorComponents, float alpha) {
        return new Waypoint(pos, typeSupplier, colorComponents, alpha, lineWidth, throughWalls, shouldRender());
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

    public void toggle() {
        this.shouldRender = !this.shouldRender;
    }

    public void setShouldRender(boolean shouldRender) {
        this.shouldRender = shouldRender;
    }

    public float[] getColorComponents() {
        return colorComponents;
    }

    @Override
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

    @Override
    public int hashCode() {
        return Objects.hash(pos, typeSupplier.get(), Arrays.hashCode(colorComponents), alpha, lineWidth, throughWalls, shouldRender);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || obj instanceof Waypoint other && pos.equals(other.pos) && typeSupplier.get() == other.typeSupplier.get() && Arrays.equals(colorComponents, other.colorComponents) && alpha == other.alpha && lineWidth == other.lineWidth && throughWalls == other.throughWalls && shouldRender == other.shouldRender;
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
    }
}
