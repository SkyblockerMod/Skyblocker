package de.hysky.skyblocker.skyblock.profileviewer.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class Slayer {
	@SerializedName("slayer_bosses")
	public Map<String, SlayerBoss> slayerBosses = Map.of();
}
