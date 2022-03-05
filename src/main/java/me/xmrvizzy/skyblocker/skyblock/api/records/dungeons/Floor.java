package me.xmrvizzy.skyblocker.skyblock.api.records.dungeons;

import com.google.gson.annotations.SerializedName;

public record Floor(
        String name,
        Stats stats,
        @SerializedName("most_damage") MostDamage mostDamage,
        Bonuses bonuses

){
    public record Stats(
            @SerializedName("times_played") int timesPlayed,
            @SerializedName("best_score") int bestScore,
            @SerializedName("mobs_killed") int mobsKilled,
            @SerializedName("most_mobs_killed") int mostMobsKilled,
            @SerializedName("most_healing") double mostHealing,
            @SerializedName("tier_completions") int tierCompletions,
            @SerializedName("fastest_time") long fastestTime,
            @SerializedName("watcher_kills") int watcherKills,
            @SerializedName("best_runs") Run[] bestRuns
    ){}
    public record MostDamage(
            @SerializedName("class") String classUsed,
            @SerializedName("value") double damage
    ){}
    public record Run(
            long timestamp,
            @SerializedName("score_exploration") int scoreExploration,
            @SerializedName("score_speed") int scoreSpeed,
            @SerializedName("score_skill") int scoreSkill,
            @SerializedName("score_bonus") int scoreBonus,
            @SerializedName("dungeon_class") int dungeonClass,
            String[] teammates,
            @SerializedName("elapsed_time") long elapsedTime,
            @SerializedName("damaged_dealt") int damageDealt,
            int deaths,
            @SerializedName("mobs_killed") int mobsKilled,
            @SerializedName("secrets_found") int secretsFound,
            @SerializedName("damage_mitigated") double damageMitigated,
            @SerializedName("ally_healing") int allyHealing
    ){}
    public record Bonuses(@SerializedName("item_boost") int itemBoost){}
}
