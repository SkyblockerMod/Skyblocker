package de.hysky.skyblocker.skyblock.slayers.boss.demonlord;

import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class AttunementColors {
	private static final Pattern COLOR_PATTERN = Pattern.compile("ASHEN|SPIRIT|CRYSTAL|AURIC|IMMUNE");

	/**
	 * Fetches highlight colour based on the Inferno Demonlord, or its demons', Hellion Shield Attunement
	 */
	@Nullable
	public static Integer getColor(LivingEntity entity) {
		for (ArmorStandEntity armorStandEntity : SlayerManager.getEntityArmorStands(entity, 2.5f)) {
			Matcher matcher = COLOR_PATTERN.matcher(armorStandEntity.getName().getString());
			if (matcher.find()) {
				return switch (matcher.group()) {
					case "ASHEN" -> Color.DARK_GRAY.getRGB();
					case "SPIRIT" -> Color.WHITE.getRGB();
					case "CRYSTAL" -> Color.CYAN.getRGB();
					case "AURIC" -> Color.YELLOW.getRGB();
					case "IMMUNE" -> Color.RED.getRGB();
					default -> null;
				};
			}
		}
		return null;
	}
}
