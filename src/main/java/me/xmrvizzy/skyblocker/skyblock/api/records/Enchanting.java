package me.xmrvizzy.skyblocker.skyblock.api.records;

import com.google.gson.annotations.SerializedName;
import me.xmrvizzy.skyblocker.skyblock.api.records.PlayerProfiles;

import java.util.HashMap;

public record Enchanting(boolean experimented, HashMap<String, Experiment> experiments){
    public record Experiment(
            String name,
            Stats stats,
            Tier[] tiers

    ){
        public record Stats(
                @SerializedName("last_attempt") PlayerProfiles.PlayerProfile.Data.LastUpdated lastAttempt,
                @SerializedName("bonus_clicks") int bonusClicks,
                @SerializedName("last_claimed") PlayerProfiles.PlayerProfile.Data.LastUpdated lastClaimed
        ){}
        public record Tier(
                String name,
                int attempts,
                int claims,
                @SerializedName("best_score") int bestScore
        ){}
    }
}
