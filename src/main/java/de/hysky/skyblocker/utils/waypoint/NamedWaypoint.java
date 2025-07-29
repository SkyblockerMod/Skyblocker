package de.hysky.skyblocker.utils.waypoint;

import com.google.common.primitives.Floats;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.CodecUtils;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.function.Supplier;

public class NamedWaypoint extends Waypoint {
    public static final Codec<NamedWaypoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(waypoint -> waypoint.pos),
            TextCodecs.CODEC.fieldOf("name").forGetter(waypoint -> waypoint.name),
            Codec.floatRange(0, 1).listOf().comapFlatMap(
                    colorComponentsList -> colorComponentsList.size() == 3 ? DataResult.success(Floats.toArray(colorComponentsList)) : DataResult.error(() -> "Expected 3 color components, got " + colorComponentsList.size() + " instead"),
                    Floats::asList
            ).fieldOf("colorComponents").forGetter(waypoint -> waypoint.colorComponents),
            Codec.FLOAT.fieldOf("alpha").forGetter(waypoint -> waypoint.alpha),
            Codec.BOOL.fieldOf("shouldRender").forGetter(Waypoint::isEnabled)
    ).apply(instance, NamedWaypoint::new));
    public static final Codec<NamedWaypoint> SKYTILS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(waypoint -> waypoint.pos.getX()),
            Codec.INT.fieldOf("y").forGetter(waypoint -> waypoint.pos.getY()),
            Codec.INT.fieldOf("z").forGetter(waypoint -> waypoint.pos.getZ()),
            Codec.either(Codec.STRING, Codec.INT).xmap(either -> either.map(Function.identity(), Object::toString), Either::left).fieldOf("name").forGetter(waypoint -> waypoint.name.getString()),
            Codec.INT.optionalFieldOf("color", ColorHelper.getArgb(128, 0, 255, 0)).forGetter(waypoint -> (int) (waypoint.alpha * 255) << 24 | (int) (waypoint.colorComponents[0] * 255) << 16 | (int) (waypoint.colorComponents[1] * 255) << 8 | (int) (waypoint.colorComponents[2] * 255)),
            Codec.BOOL.fieldOf("enabled").forGetter(Waypoint::isEnabled)
    ).apply(instance, NamedWaypoint::fromSkytils));
    static final Codec<NamedWaypoint> COLEWEIGHT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(waypoint -> waypoint.pos.getX()),
            Codec.INT.fieldOf("y").forGetter(waypoint -> waypoint.pos.getY()),
            Codec.INT.fieldOf("z").forGetter(waypoint -> waypoint.pos.getZ()),
            CodecUtils.optionalDouble(Codec.DOUBLE.optionalFieldOf("r")).forGetter(waypoint -> OptionalDouble.of(waypoint.colorComponents[0])),
            CodecUtils.optionalDouble(Codec.DOUBLE.optionalFieldOf("g")).forGetter(waypoint -> OptionalDouble.of(waypoint.colorComponents[1])),
            CodecUtils.optionalDouble(Codec.DOUBLE.optionalFieldOf("b")).forGetter(waypoint -> OptionalDouble.of(waypoint.colorComponents[2])),
            ColeweightOptions.CODEC.optionalFieldOf("options").forGetter(waypoint -> Optional.of(new ColeweightOptions(Optional.of(waypoint.name.getString()))))
    ).apply(instance, NamedWaypoint::fromColeweight));
	static final Codec<NamedWaypoint> SKYBLOCKER_LEGACY_ORDERED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockPos.CODEC.fieldOf("pos").forGetter(waypoint -> waypoint.pos),
			Codec.floatRange(0, 1).listOf().xmap(Floats::toArray, FloatArrayList::new).optionalFieldOf("colorComponents", new float[0]).forGetter(waypoint -> waypoint.colorComponents)
	).apply(instance, (pos, colorComponents) -> new OrderedNamedWaypoint(pos, "", new float[]{0, 1, 0})));

	public static final Comparator<NamedWaypoint> NAME_COMPARATOR = new NameComparator();

    public final Text name;
    public final Vec3d centerPos;

    public NamedWaypoint(NamedWaypoint namedWaypoint) {
        this(namedWaypoint.pos, namedWaypoint.name, namedWaypoint.typeSupplier, namedWaypoint.colorComponents, namedWaypoint.alpha, namedWaypoint.isEnabled());
    }

    public NamedWaypoint(BlockPos pos, String name, float[] colorComponents) {
        this(pos, name, colorComponents, true);
    }

    public NamedWaypoint(BlockPos pos, String name, float[] colorComponents, boolean enabled) {
        this(pos, name, colorComponents, DEFAULT_HIGHLIGHT_ALPHA, enabled);
    }

    public NamedWaypoint(BlockPos pos, String name, float[] colorComponents, float alpha, boolean enabled) {
        this(pos, Text.of(name), colorComponents, alpha, enabled);
    }

    public NamedWaypoint(BlockPos pos, Text name, float[] colorComponents, boolean enabled) {
        this(pos, name, () -> SkyblockerConfigManager.get().uiAndVisuals.waypoints.waypointType, colorComponents, DEFAULT_HIGHLIGHT_ALPHA, enabled);
    }

    public NamedWaypoint(BlockPos pos, Text name, float[] colorComponents, float alpha, boolean enabled) {
        this(pos, name, () -> SkyblockerConfigManager.get().uiAndVisuals.waypoints.waypointType, colorComponents, alpha, enabled);
    }

    public NamedWaypoint(BlockPos pos, Text name, Supplier<Type> typeSupplier, float[] colorComponents) {
        this(pos, name, typeSupplier, colorComponents, DEFAULT_HIGHLIGHT_ALPHA, true);
    }

    public NamedWaypoint(BlockPos pos, Text name, Supplier<Type> typeSupplier, float[] colorComponents, float alpha, boolean enabled) {
        super(pos, typeSupplier, colorComponents, alpha, DEFAULT_LINE_WIDTH, true, enabled);
        this.name = name;
        this.centerPos = pos.toCenterPos();
    }

    public static NamedWaypoint fromSkytils(int x, int y, int z, String name, int color, boolean enabled) {
        float alpha = ((color & 0xFF000000) >>> 24) / 255f;
        if (alpha == 0) {
            alpha = DEFAULT_HIGHLIGHT_ALPHA;
        }
        return new NamedWaypoint(new BlockPos(x, y, z), name, ColorUtils.getFloatComponents(color), alpha, enabled);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static NamedWaypoint fromColeweight(int x, int y, int z, OptionalDouble r, OptionalDouble g, OptionalDouble b, Optional<ColeweightOptions> options) {
        return new NamedWaypoint(new BlockPos(x, y, z), options.flatMap(ColeweightOptions::name).orElse("New Waypoint"), new float[]{(float) r.orElse(0), (float) g.orElse(1), (float) b.orElse(0)}, DEFAULT_HIGHLIGHT_ALPHA, true);
    }

    /**
     * Returns a copy of this waypoint. Note that this differs from {@link #NamedWaypoint(NamedWaypoint) the copy constructor} in that this should be overridden by subclasses and return a new instance of the runtime type.
     */
    public NamedWaypoint copy() {
        return new NamedWaypoint(pos, name, typeSupplier, colorComponents, alpha, isEnabled());
    }

    @Override
    public NamedWaypoint withX(int x) {
        return new NamedWaypoint(new BlockPos(x, pos.getY(), pos.getZ()), name, typeSupplier, colorComponents, alpha, isEnabled());
    }

    @Override
    public NamedWaypoint withY(int y) {
        return new NamedWaypoint(pos.withY(y), name, typeSupplier, colorComponents, alpha, isEnabled());
    }

    @Override
    public NamedWaypoint withZ(int z) {
        return new NamedWaypoint(new BlockPos(pos.getX(), pos.getY(), z), name, typeSupplier, colorComponents, alpha, isEnabled());
    }

    @Override
    public NamedWaypoint withColor(float[] colorComponents, float alpha) {
        return new NamedWaypoint(pos, name, typeSupplier, colorComponents, alpha, isEnabled());
    }

    public Text getName() {
        return name;
    }

    public NamedWaypoint withName(String name) {
        return new NamedWaypoint(pos, Text.literal(name), typeSupplier, colorComponents, alpha, isEnabled());
    }

    protected boolean shouldRenderName() {
        return shouldRender();
    }

    @Override
    public void render(WorldRenderContext context) {
        super.render(context);
        if (shouldRenderName()) {
            float scale = Math.max((float) context.camera().getPos().distanceTo(centerPos) / 10, 1);
            RenderHelper.renderText(context, name, centerPos.add(0, 1, 0), scale, true);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || super.equals(obj) && obj instanceof NamedWaypoint waypoint && name.equals(waypoint.name);
    }

    private record ColeweightOptions(Optional<String> name) {
        public static final Codec<ColeweightOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.either(Codec.STRING, Codec.INT).xmap(either -> either.map(Function.identity(), Object::toString), Either::left).optionalFieldOf("name").forGetter(ColeweightOptions::name)
        ).apply(instance, ColeweightOptions::new));
    }

	private static class NameComparator implements Comparator<NamedWaypoint> {

		@Override
		public int compare(NamedWaypoint o1, NamedWaypoint o2) {
			String string1 = o1.getName().getString();
			String string2 = o2.getName().getString();

			String prefix1 = string1.replaceFirst("\\d+$", "");
			String prefix2 = string2.replaceFirst("\\d+$", "");

			int i = prefix1.compareTo(prefix2);
			if (i != 0) return i;

			String num1 = string1.substring(prefix1.length());
			String num2 = string2.substring(prefix2.length());
			if (num1.isEmpty() || num2.isEmpty()) return string1.compareTo(string2);

			try {
				int i1 = Integer.parseInt(num1);
				int i2 = Integer.parseInt(num2);
				return Integer.compare(i1, i2);
			} catch (NumberFormatException e) {
				return string1.compareTo(string2);
			}
		}
	}
}
