package de.hysky.skyblocker.skyblock.profileviewer2.model;

import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.google.gson.annotations.SerializedName;

public class Inventories {
	@SerializedName("bag_contents")
	public @Nullable BagContents bagContents;
	@SerializedName("inv_contents")
	public @Nullable AbstractInventoryContents inventoryContents;
	@SerializedName("ender_chest_contents")
	public @Nullable AbstractInventoryContents enderChestContents;
	@SerializedName("backpack_icons")
	public @Nullable Map<String, AbstractInventoryContents> backpackIcons;
	@SerializedName("backpack_contents")
	public @Nullable Map<String, AbstractInventoryContents> backpackContents;
	@SerializedName("inv_armor")
	public @Nullable AbstractInventoryContents armourContents;
	@SerializedName("equipment_contents")
	public @Nullable AbstractInventoryContents equipmentContents;
	@SerializedName("personal_vault_contents")
	public @Nullable AbstractInventoryContents personalVaultContents;
	@SerializedName("wardrobe_contents")
	public @Nullable AbstractInventoryContents wardrobeContents;
	/** The non-zero indexed wardrobe slot in use. */
	@SerializedName("wardrobe_equipped_slot")
	public int equippedWardrobeSlot;
	@SerializedName("sacks_counts")
	public @Nullable Map<String, Integer> sacksCounts;

	public static class AbstractInventoryContents {
		public String data = "";
	}

	public static class BagContents {
		@SerializedName("sacks_bag")
		public @Nullable AbstractInventoryContents sacksBag;
		@SerializedName("potion_bag")
		public @Nullable AbstractInventoryContents potionBag;
		@SerializedName("talisman_bag")
		public @Nullable AbstractInventoryContents talismanBag;
		@SerializedName("fishing_bag")
		public @Nullable AbstractInventoryContents fishingBag;
		public @Nullable AbstractInventoryContents quiver;
	}
}
