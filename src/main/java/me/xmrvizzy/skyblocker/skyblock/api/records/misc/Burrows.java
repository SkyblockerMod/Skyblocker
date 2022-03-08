package me.xmrvizzy.skyblocker.skyblock.api.records.misc;

import com.google.gson.annotations.SerializedName;

public record Burrows(
        @SerializedName("dug_next") Rarities dugNext,
        @SerializedName("dug_combat") Rarities dugCombat,
        @SerializedName("dug_treasure") Rarities dugTreasure,
        @SerializedName("chains_complete") Rarities chainsComplete
){
    public record Rarities(
            int total,
            @SerializedName("null") int common,
            int uncommon,
            int rare,
            int epic,
            int legendary
    ){}
}
