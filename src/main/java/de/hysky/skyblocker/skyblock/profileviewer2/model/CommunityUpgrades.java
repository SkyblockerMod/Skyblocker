package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

public class CommunityUpgrades {
	@SerializedName("currently_upgrading")
	public @Nullable CurrentlyUpgrading currentlyUpgrading;
	@SerializedName("upgrade_states")
	public List<UpgradeState> upgradeStates = List.of();

	public int getUpgradeTier(String upgradeId) {
		int tier = 0;
		for (UpgradeState upgradeState : this.upgradeStates) {
			if (!Objects.equals(upgradeId, upgradeState.upgradeId))
				continue;
			tier = Math.max(tier, upgradeState.tier);
		}
		return tier;
	}

	public static class CurrentlyUpgrading {
		@SerializedName("upgrade")
		public String upgradeId = "";
		@SerializedName("new_tier")
		public int newTier;
		@SerializedName("start_ms")
		public long startedAt;
		@SerializedName("who_started")
		public UUID startedBy = UUID.randomUUID();
	}

	public static class UpgradeState {
		@SerializedName("upgrade")
		public String upgradeId = "";
		public int tier;
		@SerializedName("started_ms")
		public long startedAt;
		@SerializedName("started_by")
		public UUID startedBy = UUID.randomUUID();
		@SerializedName("claimed_by")
		public UUID claimedBy = UUID.randomUUID();
	}
}
