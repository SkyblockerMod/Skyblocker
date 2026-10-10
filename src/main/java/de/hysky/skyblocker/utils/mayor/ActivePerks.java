package de.hysky.skyblocker.utils.mayor;

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;

public record ActivePerks(List<Perk> perks) {
	public boolean hasPerk(String perkName, @Nullable String descriptionContains) {
		return perks.stream().anyMatch(
				perk -> perk.name().equals(perkName)
						&& (descriptionContains == null || ChatFormatting.stripFormatting(perk.description()).contains(descriptionContains)));
	}
}
