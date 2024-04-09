package de.hysky.skyblocker.skyblock.tabhud.util;

import de.hysky.skyblocker.utils.Utils;

/**
 * Uses data from the player list to determine the area the player is in.
 */
public class PlayerLocator {

    public enum Location {
        DUNGEON("dungeon"),
        GUEST_ISLAND("guest_island"),
        HOME_ISLAND("home_island"),
        CRIMSON_ISLE("crimson_isle"),
        DUNGEON_HUB("dungeon_hub"),
        FARMING_ISLAND("farming_island"),
        PARK("park"),
        DWARVEN_MINES("dwarven_mines"),
        CRYSTAL_HOLLOWS("crystal_hollows"),
        END("end"),
        GOLD_MINE("gold_mine"),
        DEEP_CAVERNS("deep_caverns"),
        HUB("hub"),
        SPIDER_DEN("spider_den"),
        JERRY("jerry_workshop"),
        GARDEN("garden"),
        INSTANCED("kuudra"),
        THE_RIFT("rift"),
        DARK_AUCTION("dark_auction"),
        GLACITE_MINESHAFT("mineshaft"),
        UNKNOWN("unknown");

        public final String internal;

        Location(String i) {
            // as used internally by the mod, e.g. in the json
            this.internal = i;
        }

    }

    public static Location getPlayerLocation() {

        if (!Utils.isOnSkyblock()) {
            return Location.UNKNOWN;
        }

        String areaDescriptor = PlayerListMgr.strAt(41);

        if (areaDescriptor == null || areaDescriptor.length() < 6) {
            return Location.UNKNOWN;
        }

        if (areaDescriptor.startsWith("Dungeon")) {
            return Location.DUNGEON;
        }

        return switch (areaDescriptor.substring(6)) {
            case "Private Island" -> {
                String islandType = PlayerListMgr.strAt(44);
                if (islandType == null) {
                    yield Location.UNKNOWN;
                } else if (islandType.endsWith("Guest")) {
                    yield Location.GUEST_ISLAND;
                } else {
                    yield Location.HOME_ISLAND;
                }
            }
            case "Crimson Isle" -> Location.CRIMSON_ISLE;
            case "Dungeon Hub" -> Location.DUNGEON_HUB;
            case "The Farming Islands" -> Location.FARMING_ISLAND;
            case "The Park" -> Location.PARK;
            case "Dwarven Mines" -> Location.DWARVEN_MINES;
            case "Crystal Hollows" -> Location.CRYSTAL_HOLLOWS;
            case "The End" -> Location.END;
            case "Gold Mine" -> Location.GOLD_MINE;
            case "Deep Caverns" -> Location.DEEP_CAVERNS;
            case "Hub" -> Location.HUB;
            case "Spider's Den" -> Location.SPIDER_DEN;
            case "Jerry's Workshop" -> Location.JERRY;
            case "Garden" -> Location.GARDEN;
            case "Instanced" -> Location.INSTANCED;
            case "The Rift" -> Location.THE_RIFT;
            case "Dark Auction" -> Location.DARK_AUCTION;
            default -> Location.UNKNOWN;
        };
    }
}
