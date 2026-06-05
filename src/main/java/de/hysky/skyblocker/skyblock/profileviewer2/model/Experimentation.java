package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;

public class Experimentation {
	@SerializedName("claims_resets")
	public int claimsReset;
	@SerializedName("claims_resets_timestamp")
	public long claimsResetTimestamp;
	@SerializedName("serums_drank")
	public int serumsDrank;
	@SerializedName("claimed_retroactive_rng")
	public boolean claimedRetroactiveRng;
	@SerializedName("charge_track_timestamp")
	public long chargeTrackTimestamp;

	public ExperimentationStats numbers = new ExperimentationStats();
	public ExperimentationStats pairings = new ExperimentationStats();
	public ExperimentationStats simon = new ExperimentationStats();

	public static class ExperimentationStats {
		@SerializedName("last_attempt")
		public int lastAttempt;
		@SerializedName("attempts_1")
		public int attempts1;
		@SerializedName("claims_1")
		public int claims1;
		@SerializedName("best_score_1")
		public int bestScore1;
		@SerializedName("attempts_2")
		public int attempts2;
		@SerializedName("claims_2")
		public int claims2;
		@SerializedName("best_score_2")
		public int bestScore2;
		@SerializedName("attempts_3")
		public int attempts3;
		@SerializedName("claims_3")
		public int claims3;
		@SerializedName("best_score_3")
		public int bestScore3;
		@SerializedName("bonus_clicks")
		public int bonusClicks;
		@SerializedName("last_claimed")
		public long lastClaimed;
		public boolean claimed;
	}
}
