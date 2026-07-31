package de.hysky.skyblocker.utils.render;

import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.minecraft.world.phys.Vec3;

public final class RangeCircleRenderer implements Renderable {

	private static final float LINE_WIDTH = 0.05f;
	private static final int SEGMENTS = 128;

	private final Vec3 centre;
	private final float radius;
	private final int colour;

	public RangeCircleRenderer(Vec3 centre, float radius, int colour) {
		this.centre = centre;
		this.radius = radius;
		this.colour = colour;
	}

	@Override
	public void extractRendering(PrimitiveCollector collector) {
		collector.submitOutlinedCircle(
				centre,
				radius,
				LINE_WIDTH,
				SEGMENTS,
				colour
		);
	}
}
