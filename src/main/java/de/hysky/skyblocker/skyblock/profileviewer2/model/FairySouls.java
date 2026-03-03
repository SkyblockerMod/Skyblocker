package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;

public class FairySouls {
	@SerializedName("fairy_exchanges")
	public int fairyExchanges;
	@SerializedName("total_collected")
	public int totalCollected;
	@SerializedName("unspent_souls")
	public int unspentSouls;
}
