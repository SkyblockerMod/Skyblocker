package de.hysky.skyblocker.skyblock.entity.glow.adder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.end.TheEnd;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.util.Formatting;

public class EndGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final EndGlowAdder INSTANCE = new EndGlowAdder();

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		return entity instanceof EndermanEntity enderman && TheEnd.isSpecialZealot(enderman) ? Formatting.RED.getColorValue() : NO_GLOW;
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInTheEnd();
	}
}
