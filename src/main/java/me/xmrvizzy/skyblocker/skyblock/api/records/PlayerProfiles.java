package me.xmrvizzy.skyblocker.skyblock.api.records;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import marcono1234.gson.recordadapter.RecordTypeAdapterFactory;
import me.xmrvizzy.skyblocker.skyblock.api.records.dungeons.Dungeons;
import me.xmrvizzy.skyblocker.skyblock.api.records.mining.Mining;
import me.xmrvizzy.skyblocker.skyblock.api.records.misc.Misc;

import java.util.HashMap;

@JsonAdapter(RecordTypeAdapterFactory.class)
public record PlayerProfiles(HashMap<String, PlayerProfile> profiles) {
    public record PlayerProfile(
            @SerializedName("profile_id") String profileId,
            @SerializedName("cute_name") String cuteName,
            boolean current,
            @SerializedName("last_save") long lastSave,
            Items items,
            Data data

    ){
        public record Data(
                Stats stats,
                @SerializedName("fairy_bonus") Stats fairyBonus,
                @SerializedName("fairy_souls") FairySouls fairySouls,
                @SerializedName("levels") HashMap<String, Level> skills,
                @SerializedName("average_level") double averageLevel,
                @SerializedName("average_level_no_progress") double trueAverageLevel,
                @SerializedName("total_skill_xp") double totalSkillXp,
                @SerializedName("skill_bonus") HashMap<String, Stats> skillBonus,
                @SerializedName("average_level_rank") double averageLevelRank,
                @SerializedName("slayer_coins_spent") HashMap<String, Integer> slayerCoinsSpent,
                @SerializedName("slayer_bonus") HashMap<String, Stats> slayerBonus,
                HashMap<String, Slayer> slayers,
                @SerializedName("slayer_xp") int slayerXp,
                @SerializedName("display_name") String username,
                String uuid,
                double bank,
                double purse,
                @SerializedName("current_area") String currentArea,
                Entity[] kills,
                Entity[] deaths,
                @SerializedName("wardrobe_equipped_slot") int wardrobeEquippedSlot,
                @SerializedName("skin_data") SkinData skinData,
                Profile profile,
                Member[] members,
                Minion[] minions,
                @SerializedName("minion_slots") MinionSlots minionSlots,
                HashMap<String, Collection> collections,
                Social social,
                Dungeons dungeons,
                Fishing fishing,
                Farming farming,
                Enchanting Enchanting,
                Mining mining,
                Misc misc,
                @SerializedName("auctions_bought") Auctions auctionsBought,
                @SerializedName("auctions_sold") Auctions auctionsSold,
                @SerializedName("last_updated") LastUpdated lastUpdated,
                @SerializedName("first_join") LastUpdated firstJoin

        ){
            public record Stats(
                    int health,
                    int defense,
                    @SerializedName("effective_health") int effectiveHealth,
                    int strength,
                    int speed,
                    @SerializedName("crit_chance") double critChance,
                    @SerializedName("crit_damage") int critDamage,
                    @SerializedName("bonus_attack_speed") int bonusAttackSpeed,
                    int intelligence,
                    @SerializedName("sea_creature_chance") int seaCreatureChance,
                    @SerializedName("magic_find") int magicFind,
                    @SerializedName("pet_luck") int petLuck,
                    int ferocity,
                    @SerializedName("ability_damage") double abilityDamage,
                    @SerializedName("mining_speed") int miningSpeed,
                    @SerializedName("mining_fortune") int miningFortune,
                    @SerializedName("farming_fortune") int farmingFortune,
                    @SerializedName("foraging_fortune") int foragingFortune,
                    int pristine,
                    int damage,
                    @SerializedName("damage_increase") double damageIncrease
            ){}
            public record FairySouls(int collected, int total, double progress){}
            public record Level(
                    Double xp,
                    Integer level,
                    Integer maxLevel,
                    Long xpCurrent,
                    Integer xpForNext,
                    Double progress,
                    Integer levelCap,
                    Integer uncappedLevel,
                    Integer rank,
                    Double levelWithProgress,
                    Double unlockableLevelWithProgress
            ){}
            public record Entity(String type, String entityId, int amount, String entityName){}
            public record SkinData(@SerializedName("skinurl") String skinUrl, String model){}
            public record Profile(String gamemode){}
            public record Member(
                    String uuid,
                    @SerializedName("display_name") String displayName,
                    @SerializedName("last_updated") LastUpdated lastUpdated,
                    @SerializedName("skin_data") SkinData skinData
            ){}
            public record LastUpdated(long unix, String text){}
            public record Minion(
                    String id,
                    String type,
                    int tiers,
                    String name,
                    Integer[] levels

            ){}
            public record MinionSlots(int currentSlots, int toNext, int toNextSlot){}
            public record Collection(int tier, long amount, long totalAmount, UserAmount[] amounts){
                public record UserAmount(String username, long amount){}
            }
            public record Social(
                    @SerializedName("DISCORD") String discord,
                    @SerializedName("HYPIXEL") String hypixel,
                    @SerializedName("TWITTER") String twitter,
                    @SerializedName("YOUTUBE") String youtube,
                    @SerializedName("INSTAGRAM") String instagram,
                    @SerializedName("TWITCH") String twitch
            ){}
            public record Fishing(
                    int total,
                    int treasure,
                    @SerializedName("treasure_large") int treasureLarge,
                    @SerializedName("shredder_fished") int shredderFished,
                    @SerializedName("shredder_bait") int shredderBait
            ){}
            public record Auctions(
                    int uncommon,
                    int rare,
                    int epic,
                    int common,
                    int legendary,
                    int special
            ){}
        }

    }
}

