package de.hysky.skyblocker.skyblock.profileviewer2.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class GardenPlayerData {
	public int copper;
	@SerializedName("larva_consumed")
	public int larvaConsumed;
	@SerializedName("analyzed_greenhouse_crops")
	public List<String> analyzedGreenhouseMutations = List.of();
	@SerializedName("discovered_greenhouse_crops")
	public List<String> discoveredGreenhouseMutations = List.of();
}
