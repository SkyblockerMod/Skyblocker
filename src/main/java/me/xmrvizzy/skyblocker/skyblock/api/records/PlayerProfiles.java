package me.xmrvizzy.skyblocker.skyblock.api.records;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import marcono1234.gson.recordadapter.RecordTypeAdapterFactory;
import org.jetbrains.annotations.Nullable;

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
        public record Items(
                Item[] armor,
                Item[][] wardrobe,
                Item[] inventory,
                Item[] enderchest,
                @SerializedName("talisman_bag") Item[] talismanBag,
                @SerializedName("fishing_bag") Item[] fishingBag,
                Item[] quiver,
                @SerializedName("potion_bag") Item[] potionBag,
                @SerializedName("personal_vault") Item[] personalVault,
                Item[] storage,
                Item[] weapons,
                Item[] hoes,
                Item[] pickaxes,
                Item[] rods,
                @SerializedName("highest_rarity_sword") Item highestRaritySword,
                @SerializedName("highest_rarity_bow") Item highestRarityBow,
                @SerializedName("highest_rarity_rod") Item highestRarityRod,
                @SerializedName("armor_set_rarity") String armorSetRarity
        ){
            public record Item(
                    @SerializedName("Count") byte count,
                    int damage,
                    Tag tag,
                    boolean isInactive,
                    boolean inBackpack,
                    Item[] containsItems
            ){
                public record Tag(
                        @SerializedName("ExtraAttributes") ExtraAttributes extraAttributes,
                        Display display,
                        @SerializedName("SkullOwner") SkullOwner skullOwner,
                        Enchant[] ench
                ){
                    public record ExtraAttributes(String id, HashMap<String, Integer> enchantments){}
                    public record Display(@SerializedName("Name") String name, @SerializedName("Lore") String[] lore, Integer color){}
                    public record SkullOwner(
                            @SerializedName("Id") String id,
                            @SerializedName("Properties") Properties properties
                    ){
                        public record Properties(HashMap<String, String>[] textures){}
                    }
                    public record Enchant(int lvl, int id){}
                }
            }
        }

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
            public record Slayer(
                    Level level,
                    Kills kills,
                    @SerializedName("claimed_levels") ClaimedLevels claimedLevels,
                    int xp,
                    @SerializedName("boss_kills_tier_0") int bossKillsTier0,
                    @SerializedName("boss_kills_tier_1") int bossKillsTier1,
                    @SerializedName("boss_kills_tier_2") int bossKillsTier2,
                    @SerializedName("boss_kills_tier_3") int bossKillsTier3
            ){
                public record Level(
                        short currentLevel,
                        int xp,
                        short maxLevel,
                        Double progress,
                        int xpForNext
                ){}
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
                        Level level,
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
                public record Journals(
                        @SerializedName("pages_completed") int pagesCompleted,
                        @SerializedName("journals_completed") int journalsCompleted,
                        @SerializedName("total_pages") Integer totalPages,
                        boolean maxed,
                        @SerializedName("journal_entries") Entry[] journalEntries

                ){
                    public record Entry(
                            String name,
                            @SerializedName("pages_collected") int pagesCollected,
                            @SerializedName("total_pages") Integer totalPages
                    ){}
                }
                public record Class(Level experience, boolean current){}
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
            }
            public record Fishing(
                    int total,
                    int treasure,
                    @SerializedName("treasure_large") int treasureLarge,
                    @SerializedName("shredder_fished") int shredderFished,
                    @SerializedName("shredder_bait") int shredderBait
            ){}
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
            public record Enchanting(boolean experimented, HashMap<String, Experiment> experiments){
                public record Experiment(
                        String name,
                        Stats stats,
                        Tier[] tiers

                ){
                    public record Stats(
                            @SerializedName("last_attempt") LastUpdated lastAttempt,
                            @SerializedName("bonus_clicks") int bonusClicks,
                            @SerializedName("last_claimed") LastUpdated lastClaimed
                    ){}
                    public record Tier(
                            String name,
                            int attempts,
                            int claims,
                            @SerializedName("best_score") int bestScore
                    ){}
                }
            }
            public record Mining(
                    Commissions commissions,
                    Forge forge,
                    Core core
            ){
                public record Forge(Process[] processes){
                    public record Process(
                            String id,
                            int slot,
                            long timeFinished,
                            String timeFinishedText,
                            String name
                    ){}
                }
                public record Commissions(int milestone){}
                public record Core(
                        Level tier,
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
            }
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
                public record ProfileUpgrades(
                        @SerializedName("island_size") int islandSize,
                        @SerializedName("minion_slots") int minionSlots,
                        @SerializedName("guest_count") int guestCount,
                        @SerializedName("coop_slots") int coopSlots,
                        @SerializedName("coins_allowance") int coinsAllowance
                ){}
            }
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

