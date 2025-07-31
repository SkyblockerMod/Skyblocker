package de.hysky.skyblocker.skyblock.profileviewer.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.UUID;

public class ProfileMember {
	@SerializedName("player_id")
	public UUID playerId;
	/**
	 * Nota bene: this is for item collections, for boss collections you need to manually add up the boss kill counts.
	 */
	public Map<String, Integer> collection = Map.of();
	public Slayer slayer = new Slayer();
	@SerializedName("fairy_soul")
	public FairySouls fairySouls = new FairySouls();
	public ProfileMemberProfile profile = new ProfileMemberProfile();
	public Currencies currencies = new Currencies();
	public Dungeons dungeons = new Dungeons();
}
