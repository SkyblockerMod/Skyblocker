package me.xmrvizzy.skyblocker.skyblock.api.records;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public record Slayer(
        PlayerProfiles.PlayerProfile.Data.Level level,
        Kills kills,
        @SerializedName("claimed_levels") ClaimedLevels claimedLevels,
        int xp,
        @SerializedName("boss_kills_tier_0") int bossKillsTier0,
        @SerializedName("boss_kills_tier_1") int bossKillsTier1,
        @SerializedName("boss_kills_tier_2") int bossKillsTier2,
        @SerializedName("boss_kills_tier_3") int bossKillsTier3
){
    public record Kills(@Nullable HashMap<String, Integer> kills){}
    public record ClaimedLevels(
            @SerializedName("level_1") boolean level1,
            @SerializedName("level_2") boolean level2,
            @SerializedName("level_3") boolean level3,
            @SerializedName("level_4") boolean level4,
            @SerializedName("level_5") boolean level5,
            @SerializedName("level_6") boolean level6,
            @SerializedName("level_7_special") boolean level7
    ){}

}
