package de.hysky.skyblocker.skyblock.entity.glow.adder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.end.TheEnd;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;

public class EndGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final EndGlowAdder INSTANCE = new EndGlowAdder();

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		return entity instanceof EnderMan enderman && TheEnd.isSpecialZealot(enderman) ? ChatFormatting.RED.getColor() : NO_GLOW;
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInTheEnd();
	}
}
