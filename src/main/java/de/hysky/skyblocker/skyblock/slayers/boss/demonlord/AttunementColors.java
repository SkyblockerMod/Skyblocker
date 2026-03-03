package de.hysky.skyblocker.skyblock.slayers.boss.demonlord;

import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttunementColors {
	private static final Pattern COLOR_PATTERN = Pattern.compile("ASHEN|SPIRIT|CRYSTAL|AURIC|IMMUNE");

	/**
	 * Fetches highlight colour based on the Inferno Demonlord, or its demons', Hellion Shield Attunement
	 */
	public static int getColor(LivingEntity entity) {
		for (ArmorStand armorStandEntity : SlayerManager.getEntityArmorStands(entity, 2.5f)) {
			Matcher matcher = COLOR_PATTERN.matcher(armorStandEntity.getName().getString());
			if (matcher.find()) {
				return switch (matcher.group()) {
					case "ASHEN" -> Color.DARK_GRAY.getRGB();
					case "SPIRIT" -> Color.WHITE.getRGB();
					case "CRYSTAL" -> Color.CYAN.getRGB();
					case "AURIC" -> Color.YELLOW.getRGB();
					case "IMMUNE" -> Color.RED.getRGB();
					default -> MobGlow.NO_GLOW;
				};
			}
		}
		return MobGlow.NO_GLOW;
	}
}
