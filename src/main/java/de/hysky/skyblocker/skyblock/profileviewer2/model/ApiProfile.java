package de.hysky.skyblocker.skyblock.profileviewer2.model;

import java.util.Map;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;

public class ApiProfile {
	@SerializedName("profile_id")
	public UUID profileId = UUID.randomUUID();
	public CommunityUpgrades communityUpgrades = new CommunityUpgrades();
	public Map<UUID, ProfileMember> members = Map.of();
	public CoopBanking banking = new CoopBanking();
	@SerializedName("cute_name")
	public String cuteName = "";
	public boolean selected = false;

	/// {@return whether the profile has been a co-op before as indicated by there having been more than one member.}
	///
	/// @implNote This does not account for invitations that were not accepted.
	public boolean hasBeenCoop() {
		return this.members.size() > 1;
	}
}
