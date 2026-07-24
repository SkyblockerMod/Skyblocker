package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class SlayerBoss {
	@SerializedName("claimed_levels")
	public Map<String, Boolean> claimedLevels = Map.of();
	@SerializedName("boss_kills_tier_0")
	int bossKills0;
	@SerializedName("boss_kills_tier_1")
	int bossKills1;
	@SerializedName("boss_kills_tier_2")
	int bossKills2;
	@SerializedName("boss_kills_tier_3")
	int bossKills3;
	@SerializedName("boss_kills_tier_4")
	int bossKills4;
	public int xp;

	public int getBossKillsByZeroIndexedTier(int tier) {
		return switch (tier) {
			case 0 -> this.bossKills0;
			case 1 -> this.bossKills1;
			case 2 -> this.bossKills2;
			case 3 -> this.bossKills3;
			case 4 -> this.bossKills4;
			default -> 0;
		};
	}

	public int getTotalBossKills() {
		int total = 0;
		for (int i = 0; i <= 4; i++) {
			total += getBossKillsByZeroIndexedTier(i);
		}
		return total;
	}

	public int getTierWithMostKills() {
		for (int i = 4; i >= 0; i--) {
			if (getBossKillsByZeroIndexedTier(i) > 1) return i;
		}
		return -1;
	}
}
