package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.UUID;

public class ApiProfile {
	@SerializedName("profile_id")
	public UUID profileId = UUID.randomUUID();
	public CommunityUpgrades communityUpgrades = new CommunityUpgrades();
	public Map<UUID, ProfileMember> members = Map.of();
	public CoopBanking banking = new CoopBanking();
	@SerializedName("cute_name")
	public String cuteName = "";
	public boolean selected = false;
}
