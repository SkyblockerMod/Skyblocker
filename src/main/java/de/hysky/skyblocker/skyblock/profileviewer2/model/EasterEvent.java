package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import de.hysky.skyblocker.SkyblockerMod;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

public class EasterEvent {
	@SerializedName("chocolate")
	public long currentChocolate;
	@SerializedName("chocolate_since_prestige")
	public long chocolateSincePrestige;
	@SerializedName("total_chocolate")
	public long allTimeChocolate;
	@SerializedName("employees")
	public Employees employeeLevels = new Employees();
	@SerializedName("last_viewed_chocolate_factory")
	public long lastViewedChocolateFactory;
	@SerializedName("rabbits")
	private JsonObject rawRabbits = new JsonObject();
	public Shop shop = new Shop();
	@SerializedName("rabbit_barn_capacity_level")
	public int barnCapacity;
	@SerializedName("chocolate_level")
	public int chocolateFactoryLevel;
	@SerializedName("time_tower")
	public TimeTower timeTower = new TimeTower();
	@SerializedName("el_dorado_progress")
	public int elDoradoProgress;
	@SerializedName("chocolate_multiplier_upgrades")
	public int chocolateMultiplierUpgrades;
	@SerializedName("rabbit_rarity_upgrades")
	public int rabbitRarityUpgrade;
	@SerializedName("golden_click_amount")
	public int goldenClickAmount;
	@SerializedName("golden_click_year")
	public int goldenClickYear;
	@SerializedName("click_upgrades")
	public int clickUpgrades;
	@SerializedName("supreme_chocolate_bars")
	public int supremeChocolateBars;
	@SerializedName("refined_dark_cacao_truffles")
	public int refinedDarkCocoaTrufflesConsumer;
	@SerializedName("rabbit_hitmen")
	public Hitmen hitmen = new Hitmen();

	public static class Hitmen {
		@SerializedName("rabbit_hitmen_slots")
		public int unlockedSlots;
		@SerializedName("missed_uncollected_eggs")
		public int uncollectedEggCount;
		/**
		 * Is this the last collected egg timestamp?
		 */
		@SerializedName("egg_slot_cooldown_mark")
		public long eggSlotCooldownTimestamp;
		@SerializedName("egg_slot_cooldown_sum")
		public long eggSlotCooldownSum;
	}

	public static class TimeTower {
		public int charges;
		public int level;
		@SerializedName("activation_time")
		public long activationTime;
		@SerializedName("last_charge_time")
		public long lastChargeTime;
	}

	public static class Shop {
		public int year;
		public List<String> rabbits = List.of();
		@SerializedName("chocolate_spent")
		public long chocolateSpent;
		@SerializedName("cocoa_fortune_upgrades")
		public int cocoaFortuneUpgrade;
	}


	private transient @Nullable CollectedEggs collectedEggs;
	private transient @Nullable Map<String, Integer> rabbitCollectionCount;

	public Map<String, Integer> getRabbitCount() {
		if (this.rabbitCollectionCount == null) {
			this.rabbitCollectionCount = new Object2IntOpenHashMap<>(this.rawRabbits.size());
			for (Map.Entry<String, JsonElement> entry : this.rawRabbits.entrySet()) {
				if (Objects.equals(entry.getKey(), "collected_eggs"))
					continue;
				this.rabbitCollectionCount.put(entry.getKey(), entry.getValue().getAsInt());
			}
		}
		return this.rabbitCollectionCount;
	}

	public CollectedEggs getLastCollectedEggs() {
		if (this.collectedEggs == null) {
			this.collectedEggs = SkyblockerMod.GSON.fromJson(this.rawRabbits.getAsJsonObject("collected_eggs"), CollectedEggs.class);
		}
		return this.collectedEggs;
	}

	/**
	 * Last collected egg timestamps, can be used calculate when the next egg is available.
	 */
	public static class CollectedEggs {
		public long breakfast;
		public long dinner;
		public long lunch;
		public long brunch;
		public long dejeuner;
		public long supper;
	}

	public static class Employees {
		@SerializedName("rabbit_bro")
		public long rabbitBro;
		@SerializedName("rabbit_cousin")
		public long rabbitCousin;
		@SerializedName("rabbit_sis")
		public long rabbitSis;
		@SerializedName("rabbit_father")
		public long rabbitFather;
		@SerializedName("rabbit_grandma")
		public long rabbitGrandma;
		@SerializedName("rabbit_uncle")
		public long rabbitUncle;
		@SerializedName("rabbit_dog")
		public long rabbitDog;
	}
}
