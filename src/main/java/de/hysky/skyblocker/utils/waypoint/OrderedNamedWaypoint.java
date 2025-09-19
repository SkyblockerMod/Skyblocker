package de.hysky.skyblocker.utils.waypoint;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.function.Supplier;

public class OrderedNamedWaypoint extends NamedWaypoint {
	private static final float[] RED_COLOR_COMPONENTS = {1f, 0f, 0f};
	private static final float[] WHITE_COLOR_COMPONENTS = {1f, 1f, 1f};
	private static final float[] GREEN_COLOR_COMPONENTS = {0f, 1f, 0f};
	private static final float[] FLOAT_ARRAY = new float[4];

	int index;
	RelativeIndex relativeIndex;

	public OrderedNamedWaypoint(NamedWaypoint namedWaypoint) {
		this(namedWaypoint.pos, namedWaypoint.name, namedWaypoint.typeSupplier, namedWaypoint.colorComponents, namedWaypoint.alpha, namedWaypoint.isEnabled());
	}

	public OrderedNamedWaypoint(BlockPos pos, String name, float[] colorComponents) {
		this(pos, Text.of(name), () -> SkyblockerConfigManager.get().uiAndVisuals.waypoints.waypointType, colorComponents, DEFAULT_HIGHLIGHT_ALPHA, true);
	}

	public OrderedNamedWaypoint(BlockPos pos, Text name, Supplier<Type> typeSupplier, float[] colorComponents, float alpha, boolean shouldRender) {
		super(pos, name, typeSupplier, colorComponents, alpha, shouldRender);
	}

	@Override
	public OrderedNamedWaypoint copy() {
		return new OrderedNamedWaypoint(this);
	}

	@Override
	public OrderedNamedWaypoint withX(int x) {
		return new OrderedNamedWaypoint(new BlockPos(x, pos.getY(), pos.getZ()), name, typeSupplier, colorComponents, alpha, isEnabled());
	}

	@Override
	public OrderedNamedWaypoint withY(int y) {
		return new OrderedNamedWaypoint(new BlockPos(pos.getX(), y, pos.getZ()), name, typeSupplier, colorComponents, alpha, isEnabled());
	}

	@Override
	public OrderedNamedWaypoint withZ(int z) {
		return new OrderedNamedWaypoint(new BlockPos(pos.getX(), pos.getY(), z), name, typeSupplier, colorComponents, alpha, isEnabled());
	}

	@Override
	public OrderedNamedWaypoint withColor(float[] colorComponents, float alpha) {
		return new OrderedNamedWaypoint(pos, name, typeSupplier, colorComponents, alpha, isEnabled());
	}

	@Override
	public OrderedNamedWaypoint withName(String name) {
		return new OrderedNamedWaypoint(pos, Text.of(name), typeSupplier, colorComponents, alpha, isEnabled());
	}

	@Override
	public boolean shouldRender() {
		return super.shouldRender() && relativeIndex.shouldRender();
	}

	@Override
	public float[] getRenderColorComponents() {
		return switch (relativeIndex) {
			case NONE -> super.getRenderColorComponents();
			case PREVIOUS -> RED_COLOR_COMPONENTS;
			case CURRENT -> WHITE_COLOR_COMPONENTS;
			case NEXT -> GREEN_COLOR_COMPONENTS;
		};
	}

	@Override
	public void render(WorldRenderContext context) {
		super.render(context);
		UIAndVisualsConfig.Waypoints waypoints = SkyblockerConfigManager.get().uiAndVisuals.waypoints;
		if (waypoints.renderLine && relativeIndex == RelativeIndex.NEXT && shouldRender()) {
			float[] components = waypoints.lineColor.getComponents(FLOAT_ARRAY);
			RenderHelper.renderLineFromCursor(context, centerPos, components, components[3], waypoints.lineWidth);
		}
		if (shouldRenderName()) {
			float scale = Math.max((float) context.camera().getPos().distanceTo(centerPos) / 10, 1);
			RenderHelper.renderText(context, Text.of(String.valueOf(index + 1)), centerPos.add(0, 1, 0), scale, MinecraftClient.getInstance().textRenderer.fontHeight + 1, true);
		}
	}

	public enum RelativeIndex {
		NONE, PREVIOUS, CURRENT, NEXT;

		public boolean shouldRender() {
			return this != NONE;
		}
	}
}
