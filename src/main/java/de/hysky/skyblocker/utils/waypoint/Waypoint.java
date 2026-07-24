package de.hysky.skyblocker.utils.waypoint;

import com.mojang.serialization.Codec;
import de.hysky.skyblocker.utils.render.Renderable;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.AABB;

/**
 * Represents a waypoint with a position, type, color, alpha, line width, through walls, and enabled state.
 * <p>
 * Extend this class and override at least the withers to create custom waypoint types and behavior.
 */
public class Waypoint implements Renderable {
	protected static final float DEFAULT_HIGHLIGHT_ALPHA = 0.5f;
	protected static final float DEFAULT_LINE_WIDTH = 5f;
	public final BlockPos pos;
	private final transient AABB box;
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
		this.box = new AABB(this.pos);
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
		return new Waypoint(pos.atY(y), typeSupplier, colorComponents, alpha, lineWidth, throughWalls, enabled);
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

	public Waypoint withThroughWalls(boolean throughWalls) {
		return new Waypoint(pos, typeSupplier, colorComponents, alpha, lineWidth, throughWalls, enabled);
	}

	public Waypoint withTypeSupplier(Supplier<Type> typeSupplier) {
		return new Waypoint(pos, typeSupplier, colorComponents, alpha, lineWidth, throughWalls, enabled);
	}
	// endregion

	// region Getters and Setters
	/**
	 * Whether the waypoint should be rendered.
	 * <p>
	 * Checked in {@link #extractRendering(PrimitiveCollector)} before rendering.
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

	// region Rendering
	/**
	 * Returns the render type of the waypoint.
	 * <p>
	 * Override this method for custom behavior.
	 */
	public Type getRenderType() {
		return typeSupplier.get();
	}

	/**
	 * Used to get the box that will be used to render the waypoint.
	 */
	protected AABB getRenderBox() {
		return this.box;
	}

	/**
	 * Returns the render time color components of the waypoint.
	 * <p>
	 * Override this method for custom behavior.
	 */
	public float[] getRenderColorComponents() {
		return colorComponents;
	}

	/**
	 * Returns whether the waypoint should be rendered through walls.
	 * <p>
	 * Override this method for custom behavior.
	 */
	public boolean shouldRenderThroughWalls() {
		return throughWalls;
	}

	/**
	 * Renders the waypoint.
	 * <p>
	 * Checks if the waypoint {@link #shouldRender() should be rendered}.
	 * <p>
	 * Override this method for custom behavior.
	 */
	@Override
	public void extractRendering(PrimitiveCollector collector) {
		if (!shouldRender()) return;

		AABB box = getRenderBox();
		float[] colorComponents = getRenderColorComponents();
		boolean throughWalls = shouldRenderThroughWalls();

		switch (getRenderType()) {
			case WAYPOINT -> collector.submitFilledBoxWithBeaconBeam(box, colorComponents, alpha, throughWalls);
			case OUTLINED_WAYPOINT -> {
				collector.submitFilledBoxWithBeaconBeam(box, colorComponents, alpha, throughWalls);
				collector.submitOutlinedBox(box, colorComponents, lineWidth, throughWalls);
			}
			case HIGHLIGHT -> collector.submitFilledBox(box, colorComponents, alpha, throughWalls);
			case OUTLINED_HIGHLIGHT -> {
				collector.submitFilledBox(box, colorComponents, alpha, throughWalls);
				collector.submitOutlinedBox(box, colorComponents, lineWidth, throughWalls);
			}
			case OUTLINE -> collector.submitOutlinedBox(box, colorComponents, lineWidth, throughWalls);
		}
	}
	// endregion

	@Override
	public int hashCode() {
		return Objects.hash(pos, typeSupplier.get(), Arrays.hashCode(colorComponents), alpha, lineWidth, throughWalls, enabled);
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) || obj instanceof Waypoint other && pos.equals(other.pos) && typeSupplier.get() == other.typeSupplier.get() && Arrays.equals(colorComponents, other.colorComponents) && alpha == other.alpha && lineWidth == other.lineWidth && throughWalls == other.throughWalls && enabled == other.enabled;
	}

	public enum Type implements StringRepresentable {
		WAYPOINT,
		OUTLINED_WAYPOINT,
		HIGHLIGHT,
		OUTLINED_HIGHLIGHT,
		OUTLINE;

		public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ENGLISH);
		}

		@Override
		public String toString() {
			return I18n.get("skyblocker.waypoints.type." + name());
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
