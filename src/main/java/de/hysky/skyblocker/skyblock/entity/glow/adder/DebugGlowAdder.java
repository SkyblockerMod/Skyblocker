package de.hysky.skyblocker.skyblock.entity.glow.adder;

import java.awt.Color;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.debug.SnapshotDebug;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.chicken.Chicken;

/// Makes chicks glow yellow for testing the Mob Glow when in debug mode and a snapshot.
public class DebugGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final DebugGlowAdder INSTANCE = new DebugGlowAdder();

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		if (entity instanceof Chicken chicken && chicken.isBaby()) {
			return Color.YELLOW.getRGB();
		}

		return NO_GLOW;
	}

	@Override
	public boolean isEnabled() {
		return Debug.debugEnabled() && SnapshotDebug.isInSnapshot();
	}
}
