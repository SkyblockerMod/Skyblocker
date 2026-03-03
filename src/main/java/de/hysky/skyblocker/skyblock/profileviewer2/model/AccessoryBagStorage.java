package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AccessoryBagStorage {
	@SerializedName("highest_magical_power")
	public int highestMagicalPower;
	@SerializedName("selected_power")
	public String selectedPower = "";
	@SerializedName("bag_upgrades_purchased")
	public int bagUpgradesPurchased;
	@SerializedName("unlocked_powers")
	public List<String> unlockedPowers = List.of();

	public Tuning tuning = new Tuning();

	public static class Tuning {
		@SerializedName("highest_unlocked_slot")
		public int highestUnlockedSlot;
		@SerializedName("refund_1")
		public boolean refund1;
		/*
		 * Slot 0 is your current active Stats Tuning Points, Slots 1-4 are presets.
		 */
		@SerializedName("slot_0")
		public TuningSlot activeSlot = new TuningSlot();

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
