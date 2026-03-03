package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.UUID;

public class ProfileMember {
	@SerializedName("player_id")
	public UUID playerId;
	@SerializedName("accessory_bag_storage")
	public AccessoryBagStorage accessoryBagStorage = new AccessoryBagStorage();
	public Attributes attributes = new Attributes();
	public Bestiary bestiary = new Bestiary();
	/**
	 * Nota bene: this is for item collections, for boss collections you need to manually add up the boss kill counts.
	 */
	public Map<String, Long> collection = Map.of();
	public Events events = new Events();
	@SerializedName("fairy_soul")
	public FairySouls fairySouls = new FairySouls();
	public Currencies currencies = new Currencies();
	public Dungeons dungeons = new Dungeons();
	public Experimentation experimentation = new Experimentation();
	public Foraging foraging = new Foraging();
	@SerializedName("foraging_core")
	public ForagingCore foragingCore = new ForagingCore();
	public Forge forge = new Forge();
	@SerializedName("garden_player_data")
	public GardenPlayerData gardenPlayerData = new GardenPlayerData();
	@SerializedName("glacite_player_data")
	public GlacitePlayerData glacitePlayerData = new GlacitePlayerData();
	@SerializedName("inventory")
	public Inventories inventories = new Inventories();
	@SerializedName("item_data")
	public ItemData itemData = new ItemData();
	@SerializedName("jacobs_contest")
	public JacobsContest jacobsContest = new JacobsContest();
	@SerializedName("leveling")
	public Levelling levelling = new Levelling();
	@SerializedName("mining_core")
	public MiningCore miningCore = new MiningCore();
	@SerializedName("nether_island_player_data")
	public NetherIslandPlayerData netherIslandPlayerData = new NetherIslandPlayerData();
	@SerializedName("pets_data")
	public PetsData petsData = new PetsData();
	@SerializedName("player_data")
	public PlayerData playerData = new PlayerData();
	@SerializedName("player_stats")
	public PlayerStats playerStats = new PlayerStats();
	public ProfileMemberProfile profile = new ProfileMemberProfile();
	public Shards shards = new Shards();
	@SerializedName("skill_tree")
	public SkillTree skillTree = new SkillTree();
	public SlayerData slayer = new SlayerData();
	public Temples temples = new Temples();
	@SerializedName("trophy_fish")
	public JsonObject trophyFish = new JsonObject();
	@SerializedName("winter_player_data")
	public WinterPlayerData winterPlayerData = new WinterPlayerData();
}
