package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.LevelInfo;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.LevelCalculator;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;

import java.util.Locale;
import java.util.Map;

public class SlayerData {
	@SerializedName("slayer_bosses")
	public Map<String, SlayerBoss> slayerBosses = Map.of();

	public SlayerBoss getSlayerData(SlayerType slayer) {
		return this.slayerBosses.getOrDefault(slayer.friendlyName.toLowerCase(Locale.ENGLISH), new SlayerBoss());
	}

	public double getSlayerExperience(SlayerType slayer) {
		return getSlayerData(slayer).xp;
	}

	public LevelInfo getSkillLevel(SlayerType slayer) {
		return LevelCalculator.getSlayerLevel((long) getSlayerExperience(slayer), slayer);
	}
}
