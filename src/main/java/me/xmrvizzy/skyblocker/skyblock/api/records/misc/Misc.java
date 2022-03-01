package me.xmrvizzy.skyblocker.skyblock.api.records.misc;

import com.google.gson.annotations.SerializedName;

public record Misc(
        Milestones milestones,
        Gifts gifts,
        Winter winter,
        Dragons dragons,
        Protector protector,
        Damage damage,
        Burrows burrows,
        ProfileUpgrades profileUpgrades
){
    public record Milestones(
            @SerializedName("ores_mined") int oresMined,
            @SerializedName("sea_creatures_killed") int seaCreaturesKilled
    ){}
    public record Gifts(@SerializedName("gifts_given") int giftsGiven){}
    public record Winter(
            @SerializedName("most_winter_snowballs_hit") int mostWinterSnowballsHit,
            @SerializedName("most_winter_damage_dealt") int mostWinterDamageDealt,
            @SerializedName("most_winter_magma_damage_dealt") int mostWinterMagmaDamageDealt
    ){}
    public record Dragons(
            @SerializedName("ender_crystals_destroyed") int enderCrystalsDestroyed,
            @SerializedName("last_hits") int lastHits,
            @SerializedName("deaths") int deaths
    ){}
    public record Protector(
            @SerializedName("last_hits") int lastHits,
            int deaths
    ){}
    public record Damage(@SerializedName("highest_critical_damage") double highestCriticalDamage){}
    public record ProfileUpgrades(
            @SerializedName("island_size") int islandSize,
            @SerializedName("minion_slots") int minionSlots,
            @SerializedName("guest_count") int guestCount,
            @SerializedName("coop_slots") int coopSlots,
            @SerializedName("coins_allowance") int coinsAllowance
    ){}
}
