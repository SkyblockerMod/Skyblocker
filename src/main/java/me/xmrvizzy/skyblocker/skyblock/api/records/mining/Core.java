package me.xmrvizzy.skyblocker.skyblock.api.records.mining;

import com.google.gson.annotations.SerializedName;
import me.xmrvizzy.skyblocker.skyblock.api.records.PlayerProfiles;

import java.util.HashMap;

public record Core(
        PlayerProfiles.PlayerProfile.Data.Level tier,
        Spent tokens,
        @SerializedName("selected_pickaxe_ability") String selectedMiningAbility,
        HashMap<String, Spent> powder,
        @SerializedName("crystal_nucleus") Nucleus crystalNucleus,
        @SerializedName("daily_ores") DailyOres dailyOres,
        @SerializedName("hotm_last_reset") long hotmLastReset,
        @SerializedName("crystal_hollows_last_access") long crystalHollowsLastAccess
){
    public record Spent(int total, int spent, int available){}
    public record Nucleus(
            @SerializedName("times_completed") int timesCompleted,
            HashMap<String, Crystal> crystals,
            Goblin goblin
    ){
        public record Crystal(
                String state,
                @SerializedName("total_placed") int totalPlaced,
                @SerializedName("total_found") int totalFound
        ){}
        public record Goblin(
                @SerializedName("king_quest_active") boolean kingQuestActive,
                @SerializedName("king_quest_completed") boolean kingQuestCompleted
        ){}
    }
    public record DailyOres(
            int mined,
            int day,
            @SerializedName("daily_ores") HashMap<String, Ore> dailyOres
    ){
        public record Ore(int day, int count){}
    }
}
