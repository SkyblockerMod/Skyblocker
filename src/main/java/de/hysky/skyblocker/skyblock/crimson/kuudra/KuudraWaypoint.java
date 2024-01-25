package de.hysky.skyblocker.skyblock.crimson.kuudra;

import java.util.function.Supplier;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.minecraft.util.math.BlockPos;

class KuudraWaypoint extends Waypoint {
	private static final Supplier<Type> TYPE = () -> SkyblockerConfigManager.get().locations.crimsonIsle.kuudra.waypointType;

	KuudraWaypoint(BlockPos pos, float[] colorComponents) {
		super(pos, TYPE, colorComponents, false);
	}
}
