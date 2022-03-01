package me.xmrvizzy.skyblocker.skyblock.api.records;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public record Farming(
        boolean talked,
        @SerializedName("current_badges") Badges currentBadges,
        @SerializedName("total_badges") Badges totalBadges,
        Perks perks,
        @SerializedName("unique_golds") int unique_golds,
        HashMap<String, Crop> crops,
        Contests contests
){
    public record Badges(int bronze, int silver, int gold){}
    public record Perks(@SerializedName("double_drops") int doubleDrops, @SerializedName("farming_level_cap") int farmingLevelCap){}
    public record Crop(
            String name,
            boolean attended,
            @SerializedName("unique_gold") boolean uniqueGold,
            int contests,
            @SerializedName("personal_best") int personalBest,
            Badges badges
    ){}
    public record Contests(@SerializedName("attended_contests") int attendedContests, @SerializedName("all_contests") Contest[] allContests){}
    public record Contest(String date, String crop, int collected, boolean claimed, String medal, Placing placing){
        public record Placing(int position, double percentage){}
    }
}
