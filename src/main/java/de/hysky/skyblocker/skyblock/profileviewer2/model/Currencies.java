package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class Currencies {
	@SerializedName("coin_purse")
	public double coinsInPurse;
	@SerializedName("motes_purse")
	public double riftMotes;
	public Map<String, EssenceCurrency> essence = Map.of();

	public static class EssenceCurrency {
		public int current;
	}
}
