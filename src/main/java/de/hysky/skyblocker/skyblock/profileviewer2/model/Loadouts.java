package de.hysky.skyblocker.skyblock.profileviewer2.model;

import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.google.gson.annotations.SerializedName;

public class Loadouts {
	@SerializedName("armor")
	public Map<String, Armour> armour = Map.of();

	public static class Armour {
		public int id;
		@SerializedName("HELMET")
		public Inventories.@Nullable AbstractInventoryContents helmet;
		@SerializedName("CHESTPLATE")
		public Inventories.@Nullable AbstractInventoryContents chestplate;
		@SerializedName("LEGGINGS")
		public Inventories.@Nullable AbstractInventoryContents leggings;
		@SerializedName("BOOTS")
		public Inventories.@Nullable AbstractInventoryContents boots;
	}
}
