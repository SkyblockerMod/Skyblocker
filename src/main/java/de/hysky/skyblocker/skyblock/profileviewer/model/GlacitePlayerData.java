package de.hysky.skyblocker.skyblock.profileviewer.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlacitePlayerData {
	@SerializedName("fossil_dust")
	public double fossilDust;
	@SerializedName("mineshafts_entered")
	public int mineshaftsEntered;
	@SerializedName("corpses_looted")
	public Map<String, Integer> corpsesLooted = new HashMap<>();
	@SerializedName("fossils_donated")
	public List<String> fossilsDonated = new ArrayList<>();
}
