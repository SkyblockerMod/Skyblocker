package de.hysky.skyblocker.skyblock.profileviewer2.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import org.jspecify.annotations.Nullable;

import de.hysky.skyblocker.annotations.GenEquals;
import de.hysky.skyblocker.annotations.GenHashCode;

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
		@SerializedName("refund_2")
		public boolean refund2;

		// Slot 0 is your current active Stats Tuning. If a template is being used then the values in
		// this slot will be equal to one of the templates.
		@SerializedName("slot_0")
		public TuningSlot activeSlot = new TuningSlot();

		// Slots 1-8 are the current

		@SerializedName("slot_1")
		public TuningTemplate slot1 = new TuningTemplate();
		@SerializedName("slot_2")
		public TuningTemplate slot2 = new TuningTemplate();
		@SerializedName("slot_3")
		public TuningTemplate slot3 = new TuningTemplate();
		@SerializedName("slot_4")
		public TuningTemplate slot4 = new TuningTemplate();
		@SerializedName("slot_5")
		public TuningTemplate slot5 = new TuningTemplate();
		@SerializedName("slot_6")
		public TuningTemplate slot6 = new TuningTemplate();
		@SerializedName("slot_7")
		public TuningTemplate slot7 = new TuningTemplate();
		@SerializedName("slot_8")
		public TuningTemplate slot8 = new TuningTemplate();

		public List<TuningSlot> unlockedSlots() {
			List<TuningSlot> allSlots = List.of(
					this.activeSlot,
					this.slot1,
					this.slot2,
					this.slot3,
					this.slot4,
					this.slot5,
					this.slot6,
					this.slot7,
					this.slot8);

			return allSlots.stream()
					.filter(slot -> slot instanceof TuningTemplate template ? template.purchased() : true)
					.toList();
		}

		public static class TuningSlot {
			public int health;
			@SerializedName("defense")
			public int defence;
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

			@Override
			@GenEquals
			public native boolean equals(Object o);

			@Override
			@GenHashCode
			public native int hashCode();
		}

		public static class TuningTemplate extends TuningSlot {
			@SerializedName("purchase_ts")
			public @Nullable Long purchaseTimestamp;

			public boolean purchased() {
				return this.purchaseTimestamp != null;
			}
		}
	}
}
