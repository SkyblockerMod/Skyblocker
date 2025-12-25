package de.hysky.skyblocker.utils.waypoint;

import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class DistancedNamedWaypoint extends NamedWaypoint {
	public DistancedNamedWaypoint(BlockPos pos, Component name, float[] colorComponents, boolean enabled) {
		super(pos, name, colorComponents, enabled);
	}

	public DistancedNamedWaypoint(BlockPos pos, Component name, Supplier<Type> typeSupplier, float[] colorComponents) {
		super(pos, name, typeSupplier, colorComponents);
	}

	public boolean shouldRenderDistance() {
		return shouldRenderName();
	}

	@Override
	public void extractRendering(PrimitiveCollector collector) {
		super.extractRendering(collector);
		if (shouldRenderDistance()) {
			double distance = RenderHelper.getCamera().getPosition().distanceTo(centerPos);
			float scale = Math.max((float) distance / 10, 1);
			collector.submitText(Component.literal(Math.round(distance) + "m").withStyle(ChatFormatting.YELLOW), centerPos.add(0, 1, 0), scale, Minecraft.getInstance().font.lineHeight + 1, true);
		}
	}
}
