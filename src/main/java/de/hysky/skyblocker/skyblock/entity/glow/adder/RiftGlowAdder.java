package de.hysky.skyblocker.skyblock.entity.glow.adder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class RiftGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final RiftGlowAdder INSTANCE = new RiftGlowAdder();

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		return entity instanceof Player p && SkyblockerConfigManager.get().otherLocations.rift.blobbercystGlow && p.getName().getString().equals("Blobbercyst ") ? ChatFormatting.GREEN.getColor() : NO_GLOW;
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInTheRift();
	}
}
