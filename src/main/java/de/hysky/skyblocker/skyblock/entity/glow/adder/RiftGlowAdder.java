package de.hysky.skyblocker.skyblock.entity.glow.adder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;

public class RiftGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final RiftGlowAdder INSTANCE = new RiftGlowAdder();

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		return entity instanceof PlayerEntity p && SkyblockerConfigManager.get().otherLocations.rift.blobbercystGlow && p.getName().getString().equals("Blobbercyst ") ? Formatting.GREEN.getColorValue() : NO_GLOW;
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInTheRift();
	}
}
