package de.hysky.skyblocker.utils.waypoint;

import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
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
	public void extractRendering(PrimitiveCollector collector) {
		super.extractRendering(collector);
		if (shouldRenderDistance()) {
			double distance = RenderHelper.getCamera().getCameraPos().distanceTo(centerPos);
			float scale = Math.max((float) distance / 10, 1);
			collector.submitText(Text.literal(Math.round(distance) + "m").formatted(Formatting.YELLOW), centerPos.add(0, 1, 0), scale, MinecraftClient.getInstance().textRenderer.fontHeight + 1, true);
		}
	}
}
