package me.xmrvizzy.skyblocker.skyblock.tabhud.util;

import me.xmrvizzy.skyblocker.utils.Utils;

/**
 * Uses data from the player list to determine the area the player is in.
 */
public class PlayerLocator {

    public static enum Location {
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
        UNKNOWN("unknown");

        public String internal;

        private Location(String i) {
            // as used internally by the mod, e.g. in the json
            this.internal = i;
        }

    }

    public static Location getPlayerLocation() {

        if (!Utils.isOnSkyblock()) {
            return Location.UNKNOWN;
        }

        String areaDesciptor = PlayerListMgr.strAt(41);

        if (areaDesciptor == null || areaDesciptor.length() < 6) {
            return Location.UNKNOWN;
        }

        if (areaDesciptor.startsWith("Dungeon")) {
            return Location.DUNGEON;
        }

        switch (areaDesciptor.substring(6)) {
            case "Private Island":
                String islandType = PlayerListMgr.strAt(44);
                if (islandType == null) {
                    return Location.UNKNOWN;
                } else if (islandType.endsWith("Guest")) {
                    return Location.GUEST_ISLAND;
                } else {
                    return Location.HOME_ISLAND;
                }
            case "Crimson Isle":
                return Location.CRIMSON_ISLE;
            case "Dungeon Hub":
                return Location.DUNGEON_HUB;
            case "The Farming Islands":
                return Location.FARMING_ISLAND;
            case "The Park":
                return Location.PARK;
            case "Dwarven Mines":
                return Location.DWARVEN_MINES;
            case "Crystal Hollows":
                return Location.CRYSTAL_HOLLOWS;
            case "The End":
                return Location.END;
            case "Gold Mine":
                return Location.GOLD_MINE;
            case "Deep Caverns":
                return Location.DEEP_CAVERNS;
            case "Hub":
                return Location.HUB;
            case "Spider's Den":
                return Location.SPIDER_DEN;
            case "Jerry's Workshop":
                return Location.JERRY;
            case "Garden":
                return Location.GARDEN;
            case "Instanced":
                return Location.INSTANCED;
            case "The Rift":
            	return Location.THE_RIFT;
            case "Dark Auction":
            	return Location.DARK_AUCTION;
            default:
                return Location.UNKNOWN;
        }
    }
}
