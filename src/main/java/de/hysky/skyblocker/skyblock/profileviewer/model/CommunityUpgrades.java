package de.hysky.skyblocker.skyblock.profileviewer.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CommunityUpgrades {
//	@Nullable
//	@SerializedName("currently_upgrading")
//	public String currentlyUpgrading = null;
	@SerializedName("upgrade_states")
	public List<UpgradeState> upgradeStates = List.of();

	public int getUpgradeTier(String upgradeId) {
		int tier = 0;
		for (var upgradeState : upgradeStates) {
			if (!Objects.equals(upgradeId, upgradeState.upgradeId))
				continue;
			tier = Math.max(tier, upgradeState.tier);
		}
		return tier;
	}

	public static class UpgradeState {
		@SerializedName("upgrade")
		public String upgradeId;
		public int tier;
		@SerializedName("started_ms")
		public long startedMs;
		@SerializedName("started_by")
		public UUID startedBy;
		@SerializedName("claimed_by")
		public UUID claimedBy;
	}
}
