package me.xmrvizzy.skyblocker.skyblock.api.records.dungeons;

import com.google.gson.annotations.SerializedName;
import me.xmrvizzy.skyblocker.skyblock.api.records.PlayerProfiles;

import java.util.HashMap;

public record Dungeons(
        Dungeon catacombs,
        @SerializedName("master_catacombs") Dungeon masterCatacombs,
        HashMap<String, Class> classes,
        @SerializedName("used_classes") boolean usedClasses,
        @SerializedName("selected_class") String selectedClass,
        @SerializedName("secrets_found") int secretsFound,
        HashMap<String, Integer> essence,
        @SerializedName("unlocked_collections") boolean unlockedCollections,
        @SerializedName("boss_collections") HashMap<String, Collection> bossCollections,
        Journals journals

){
    public record Dungeon(
            String id,
            boolean visited,
            PlayerProfiles.PlayerProfile.Data.Level level,
            @SerializedName("highest_floor") String highestFloor,
            HashMap<Integer, Floor> floors

    ){}
    public record Collection(
            String name,
            String texture,
            int tier,
            boolean maxed,
            int killed,
            HashMap<String, Integer> floors,
            int unclaimed,
            String[] claimed
    ){}
    public record Class(PlayerProfiles.PlayerProfile.Data.Level experience, boolean current){}
}
