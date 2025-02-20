package de.hysky.skyblocker.utils;

import com.mojang.serialization.Codec;
import de.hysky.skyblocker.skyblock.chat.ChatRuleLocationConfigScreen;
import net.minecraft.util.StringIdentifiable;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * All Skyblock locations
 */
public enum Location implements StringIdentifiable {
	PRIVATE_ISLAND("dynamic"),
	GARDEN("garden"),
	HUB("hub"),
	THE_FARMING_ISLAND("farming_1"),
	THE_PARK("foraging_1"),
	SPIDERS_DEN("combat_1", "Spider's Den"),
	BLAZING_FORTRESS("combat_2"),
	THE_END("combat_3"),
	CRIMSON_ISLE("crimson_isle"),
	GOLD_MINE("mining_1"),
	DEEP_CAVERNS("mining_2"),
	DWARVEN_MINES("mining_3"),
	DUNGEON_HUB("dungeon_hub"),
	WINTER_ISLAND("winter", "Jerry's Workshop"),
	THE_RIFT("rift"),
	DARK_AUCTION("dark_auction"),
	CRYSTAL_HOLLOWS("crystal_hollows"),
	DUNGEON("dungeon", "Dungeons"),
	KUUDRAS_HOLLOW("kuudra", "Kuudra's Hollow"),
	GLACITE_MINESHAFT("mineshaft"),
	/**
	 * <p>Goodbye 1.8 hello 1.21 (and foraging 50 for all)!</p>
	 * <p>If this is ever implemented, please remove it from the list of locations in {@link ChatRuleLocationConfigScreen} along with this line of javadoc.</p>
	 */
	MODERN_FORAGING_ISLAND("placeholder"),
	/**
	 * Unknown Skyblock location
	 */
	UNKNOWN("unknown");

	public static final Codec<Location> CODEC = StringIdentifiable.createCodec(Location::values);
	public static final Codec<EnumSet<Location>> SET_CODEC = CodecUtils.enumSetCodec(CODEC);

	/**
	 * location id from <a href="https://api.hypixel.net/v2/resources/games">Hypixel API</a>
	 */
	@NotNull
	private final String id;

	/**
	 * friendly name from <a href="https://api.hypixel.net/v2/resources/games">Hypixel API</a>
	 */
	@NotNull
	private final String friendlyName;

	/**
	 * @param id location id from <a href="https://api.hypixel.net/v2/resources/games">Hypixel API</a>
	 * @param friendlyName friendly name from <a href="https://api.hypixel.net/v2/resources/games">Hypixel API</a>
	 */
	Location(@NotNull String id, @NotNull String friendlyName) {
		this.id = id;
		this.friendlyName = friendlyName;
	}

	/**
	 * Alternative constructor to avoid replicating simple friendlyNames that can be obtained with manipulating the enum's name.
	 *
	 * @param id location id from <a href="https://api.hypixel.net/v2/resources/games">Hypixel API</a>
	 */
	Location(@NotNull String id) {
		this.id = id;
		this.friendlyName = WordUtils.capitalizeFully(name().replace('_', ' '));
	}

	/**
	 * @return location id
	 */
	@NotNull
	public String id() {
		return this.id;
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
					 .filter(loc -> loc.friendlyName.equals(friendlyName))
					 .findFirst()
					 .orElse(UNKNOWN);
	}

	@Override
	public String toString() {
		return friendlyName;
	}
}
