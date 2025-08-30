package de.hysky.skyblocker.skyblock.profileviewer.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccessoryBagStorage {
	@SerializedName("highest_magical_power")
	public int highestMagicalPower;
	@SerializedName("selected_power")
	public String selectedPower;
	@SerializedName("bag_upgrades_purchased")
	public int bagUpgradesPurchased;
	@SerializedName("unlocked_powers")
	public List<String> unlockedPowers = new ArrayList<>();

	public Tuning tuning = new Tuning();

	public static class Tuning {
		@SerializedName("highest_unlocked_slot")
		public int highestUnlockedSlot;
		@SerializedName("refund_1")
		public boolean refund1;
		/*
		 * Slot 0 are your current active Stats Tuning Points, Slot 1-4 are presets
		 */
		public Map<String, TuningSlot> slots = new HashMap<>();

		public static class TuningSlot {
			public int health;
			public int defense;
			@SerializedName("walk_speed")
			public int walkSpeed;
			public int strength;
			@SerializedName("critical_damage")
			public int criticalDamage;
			@SerializedName("critical_chance")
			public int criticalChance;
			@SerializedName("attack_speed")
			public int attackSpeed;
			public int intelligence;
		}
	}
}
