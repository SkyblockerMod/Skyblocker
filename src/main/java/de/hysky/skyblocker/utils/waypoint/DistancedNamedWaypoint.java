package de.hysky.skyblocker.utils.waypoint;

import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.function.Supplier;

public class DistancedNamedWaypoint extends NamedWaypoint {
	public DistancedNamedWaypoint(BlockPos pos, Text name, float[] colorComponents, boolean enabled) {
		super(pos, name, colorComponents, enabled);
	}

	public DistancedNamedWaypoint(BlockPos pos, Text name, Supplier<Type> typeSupplier, float[] colorComponents) {
		super(pos, name, typeSupplier, colorComponents);
	}

	public boolean shouldRenderDistance() {
		return shouldRenderName();
	}

	@Override
	public void render(WorldRenderContext context) {
		super.render(context);
		if (shouldRenderDistance()) {
			double distance = context.camera().getPos().distanceTo(centerPos);
			float scale = Math.max((float) distance / 10, 1);
			RenderHelper.renderText(context, Text.literal(Math.round(distance) + "m").formatted(Formatting.YELLOW), centerPos.add(0, 1, 0), scale, MinecraftClient.getInstance().textRenderer.fontHeight + 1, true);
		}
	}
}
