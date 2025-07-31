package de.hysky.skyblocker.skyblock.profileviewer.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetherIslandPlayerData {
	@SerializedName("selected_faction")
	public String selectedFaction;
	@SerializedName("mages_reputation")
	public int magesReputation;
	@SerializedName("barbarians_reputation")
	public int barbariansReputation;

	public Matriarch matriarch = new Matriarch();

	public static class Matriarch {
		@SerializedName("pearls_collected")
		public int pearlsCollected;
		@SerializedName("last_attempt")
		public long lastAttempt;
		@SerializedName("recent_refreshes")
		public List<Long> recentRefreshes = new ArrayList<>();
	}

	@SerializedName("last_minibosses_killed")
	public List<Long> lastMinibossesKilled = new ArrayList<>();

	public Dojo dojo = new Dojo();

	public static class Dojo {
		@SerializedName("dojo_points_mob_kb")
		public int testOfForce;
		@SerializedName("dojo_points_wall_jump")
		public int testOfStamina;
		@SerializedName("dojo_points_archer")
		public int testOfMastery;
		@SerializedName("dojo_points_sword_swap")
		public int testOfDiscipline;
		@SerializedName("dojo_time_archer")
		public int testOfSwiftness;
		@SerializedName("dojo_points_lock_head")
		public int testOfControl;
		@SerializedName("dojo_points_fireball")
		public int testOfTenacity;

		@SerializedName("dojo_time_mob_kb")
		public int testOfForceTime;
		@SerializedName("dojo_time_wall_jump")
		public int testOfStaminaTime;
		@SerializedName("dojo_time_archer")
		public int testOfMasteryTime;
		@SerializedName("dojo_time_sword_swap")
		public int testOfDisciplineTime;
		@SerializedName("dojo_time_archer")
		public int testOfSwiftnessTime;
		@SerializedName("dojo_time_lock_head")
		public int testOfControlTime;
		@SerializedName("dojo_time_fireball")
		public int testOfTenacityTime;
	}

	@SerializedName("kuudra_completed_tiers")
	public KuudraCompletedTiers kuudraCompletedTiers = new KuudraCompletedTiers();

	public static class KuudraCompletedTiers {
		@SerializedName("none")
		public int basicTier;
		@SerializedName("hot")
		public int hotTier;
		@SerializedName("burning")
		public int burningTier;
		@SerializedName("fiery")
		public int fieryTier;
		@SerializedName("infernal")
		public int infernalTier;

		@SerializedName("highest_wave_none")
		public int highestBasicWave;
		@SerializedName("highest_wave_hot")
		public int highestHotWave;
		@SerializedName("highest_wave_burning")
		public int highestBurningWave;
		@SerializedName("highest_wave_fiery")
		public int highestFieryWave;
		@SerializedName("highest_wave_infernal")
		public int highestInfernalWave;
	}

	public Abiphone abiphone = new Abiphone();

	public static class Abiphone {
		@SerializedName("last_dye_called_year")
		public int lastDyeCalledYear;
		@SerializedName("has_used_sirius_personal_phone_number_item")
		public boolean hasSiriusContactedUnlocked;
		@SerializedName("selected_sort")
		public String selectedSort;
		@SerializedName("selected_ringtone")
		public String selectedRingtone;
		@SerializedName("trio_contact_addons")
		public int contactTrio;
		@SerializedName("active_contacts")
		public List<String> activeContacts = new ArrayList<>();

		public OperatorChip operatorChip = new OperatorChip();
		public static class OperatorChip {
			@SerializedName("repaired_index")
			public int repairedIndex;
		}

		public Games games = new Games();
		public static class Games {
			@SerializedName("tic_tac_toe_draws")
			public int ticTacToeDraws;
			@SerializedName("tic_tac_toe_losses")
			public int ticTacToeLoses;
			@SerializedName("snake_best_score")
			public int snakeBestScore;
		}

		@SerializedName("contact_data")
		public Map<String, ContactData> contactData = new HashMap<>();
		public static class ContactData {
			@SerializedName("talked_to")
			public Boolean talkedTo;
			@SerializedName("completed_quest")
			public Boolean completedQuest;
			@SerializedName("last_call")
			public Long lastCall;
			@SerializedName("dnd_enabled")
			public Boolean dndEnabled;
			@SerializedName("incoming_calls_count")
			public Integer incomingCallsCount;
			@SerializedName("last_call_incoming")
			public Long lastCallIncoming;
			@SerializedName("specific")
			public SpecificContactData specific;

			public static class SpecificContactData {
				@SerializedName("unlocked_target_practice_iv")
				public Boolean unlockedTargetPracticeIv;
				@SerializedName("last_reward_year")
				public Integer lastRewardYear;
				@SerializedName("last_mistake")
				public Long lastMistake;
				@SerializedName("color_index_given")
				public Integer colorIndexGiven;
				@SerializedName("gave_saving_grace")
				public Boolean gaveSavingGrace;
			}
		}
	}
	//TODO Quests Data, Kuudra Party Finder Data
}
