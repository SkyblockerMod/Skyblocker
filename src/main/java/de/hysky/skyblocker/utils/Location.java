package de.hysky.skyblocker.utils;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * All Skyblock locations
 */
public enum Location implements StringIdentifiable {
	PRIVATE_ISLAND("dynamic", "Private Island"),
	GARDEN("garden", "Garden"),
	HUB("hub", "Hub"),
	THE_FARMING_ISLAND("farming_1", "The Farming Islands"),
	THE_PARK("foraging_1", "The Park", true),
	SPIDERS_DEN("combat_1", "Spider's Den"),
	@Deprecated BLAZING_FORTRESS("combat_2", "Blazing Fortress"),
	THE_END("combat_3", "The End"),
	CRIMSON_ISLE("crimson_isle", "Crimson Isle"),
	GOLD_MINE("mining_1", "Gold Mine"),
	DEEP_CAVERNS("mining_2", "Deep Caverns"),
	DWARVEN_MINES("mining_3", "Dwarven Mines"),
	BACKWATER_BAYOU("fishing_1", "Backwater Bayou"),
	DUNGEON_HUB("dungeon_hub", "Dungeon Hub"),
	WINTER_ISLAND("winter", "Jerry's Workshop"),
	THE_RIFT("rift", "The Rift"),
	DARK_AUCTION("dark_auction", "Dark Auction"),
	CRYSTAL_HOLLOWS("crystal_hollows", "Crystal Hollows"),
	DUNGEON("dungeon", "Dungeons"),
	KUUDRAS_HOLLOW("kuudra", "Kuudra's Hollow"),
	/**
	 * The freezing cold Glacite Mineshafts! *brr... so cold... :(*
	 */
	GLACITE_MINESHAFTS("mineshaft", "Glacite Mineshafts"),
	/**
	 * <p>Goodbye 1.8 hello 1.21 (and foraging 50 for all)!</p>
	 */
	GALATEA("foraging_2", "Galatea", true),
	/**
	 * Unknown Skyblock location
	 */
	UNKNOWN("unknown", "Unknown");

	public static final Codec<Location> CODEC = StringIdentifiable.createCodec(Location::values);
	public static final Codec<EnumSet<Location>> SET_CODEC = CodecUtils.enumSetCodec(CODEC, Location.class);

	/**
	 * location id from <a href="https://api.hypixel.net/v2/resources/games">Hypixel API</a>
	 */
	private final String id;

	private final String friendlyName;

	/**
	 * if the location is based on a modern server (not 1.8.9)
	 */
	private final boolean modern;

	/**
	 * @param id           location id from <a href="https://api.hypixel.net/v2/resources/games">Hypixel API</a>
	 * @param friendlyName friendly name from <a href="https://api.hypixel.net/v2/resources/games">Hypixel API</a>
	 */
	Location(String id, String friendlyName) {
		this.id = id;
		this.friendlyName = friendlyName;
		this.modern = false;
	}

	/**
	 * Alternative constructor to have the option of setting the location to modern.
	 *
	 * @param id     location id from <a href="https://api.hypixel.net/v2/resources/games">Hypixel API</a>
	 * @param modern the location is 1.21+ server
	 */
	Location(String id, String friendlyName, boolean modern) {
		this.id = id;
		this.friendlyName = friendlyName;
		this.modern = modern;
	}

	/**
	 * @return location id
	 */
	@NotNull
	public String id() {
		return this.id;
	}

	public boolean isModern() {
		return this.modern;
	}

	@Override
	public String asString() {
		return id();
	}

	/**
	 * @param id location id from <a href="https://api.hypixel.net/v2/resources/games">Hypixel API</a>
	 * @return The {@link Location} with this id, or {@link #UNKNOWN} if not found
	 */
	@NotNull
	public static Location from(String id) {
		return Arrays.stream(Location.values())
				.filter(loc -> loc.id.equals(id))
				.findFirst()
				.orElse(UNKNOWN);
	}

	/**
	 * @param friendlyName friendly name from <a href="https://api.hypixel.net/v2/resources/games">Hypixel API</a>
	 * @return The {@link Location} with this friendly name or {@link #UNKNOWN} if not found
	 */
	@NotNull
	public static Location fromFriendlyName(String friendlyName) {
		return Arrays.stream(Location.values())
				.filter(loc -> loc.friendlyName.equalsIgnoreCase(friendlyName))
				.findFirst()
				.orElse(UNKNOWN);
	}

	@Override
	public String toString() {
		return friendlyName;
	}
}
