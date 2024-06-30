package de.hysky.skyblocker.utils;

import java.util.Arrays;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringIdentifiable;

/**
 * All Skyblock locations
 */
public enum Location implements StringIdentifiable {
    /**
     * mode: dynamic
     */
    PRIVATE_ISLAND("dynamic"),
    /**
     * mode: garden
     */
    GARDEN("garden"),
    /**
     * mode: hub
     */
    HUB("hub"),
    /**
     * mode: farming_1
     */
    THE_FARMING_ISLAND("farming_1"),
    /**
     * mode: foraging_1
     */
    THE_PARK("foraging_1"),
    /**
     * mode: combat_1
     */
    SPIDERS_DEN("combat_1"),
    /**
     * mode: combat_2
     */
    BLAZING_FORTRESS("combat_2"),
    /**
     * mode: combat_3
     */
    THE_END("combat_3"),
    /**
     * mode: crimson_isle
     */
    CRIMSON_ISLE("crimson_isle"),
    /**
     * mode: mining_1
     */
    GOLD_MINE("mining_1"),
    /**
     * mode: mining_2
     */
    DEEP_CAVERNS("mining_2"),
    /**
     * mode: mining_3
     */
    DWARVEN_MINES("mining_3"),
    /**
     * mode: dungeon_hub
     */
    DUNGEON_HUB("dungeon_hub"),
    /**
     * mode: winter
     */
    WINTER_ISLAND("winter"),
    /**
     * mode: rift
     */
    THE_RIFT("rift"),
    /**
     * mode: dark_auction
     */
    DARK_AUCTION("dark_auction"),
    /**
     * mode: crystal_hollows
     */
    CRYSTAL_HOLLOWS("crystal_hollows"),
    /**
     * mode: dungeon
     */
    DUNGEON("dungeon"),
    /**
     * mode: kuudra
     */
    KUUDRAS_HOLLOW("kuudra"),
    /**
     * The freezing cold Glacite Mineshafts! *brr... so cold... :(*
     */
    GLACITE_MINESHAFT("mineshaft"),
    /**
     * Goodbye 1.8 hello 1.21 (and foraging 50 for all)!
     */
    MODERN_FORAGING_ISLAND("placeholder"),
    /**
     * Unknown Skyblock location
     */
    UNKNOWN("unknown");

    /**
     * location id from <a href="https://api.hypixel.net/v2/resources/games">Hypixel API</a>
     */
    private final String id;

    public static final Codec<Location> CODEC = StringIdentifiable.createCodec(Location::values);

    /**
     * @param id location id from <a href="https://api.hypixel.net/v2/resources/games">Hypixel API</a>
     */
    Location(String id) {
        this.id = id;
    }

    /**
     * @return location id
     */
    public String id() {
        return this.id;
    }

	@Override
	public String asString() {
	    return id();
	}

    /**
     * @param id location id from <a href="https://api.hypixel.net/v2/resources/games">Hypixel API</a>
     * @return location object
     */
    public static Location from(String id) {
        return Arrays.stream(Location.values()).filter(loc -> id.equals(loc.id())).findFirst().orElse(UNKNOWN);
    }
}
