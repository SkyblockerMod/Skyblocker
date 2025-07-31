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
	public Experimentation experimentation = new Experimentation();
	public Forge forge = new Forge();
	@SerializedName("garden_player_data")
	public GardenPlayerData gardenPlayerData = new GardenPlayerData();
	@SerializedName("glacite_player_data")
	public GlacitePlayerData glacitePlayerData = new GlacitePlayerData();
	@SerializedName("jacobs_contest")
	public JacobsContest jacobsContest = new JacobsContest();
	@SerializedName("item_data")
	public ItemData itemData = new ItemData();
	@SerializedName("winter_player_data")
	public WinterPlayerData winterPlayerData = new WinterPlayerData();
	public Leveling leveling = new Leveling();
	public AccessoryBagStorage accessoryBagStorage = new AccessoryBagStorage();
}
