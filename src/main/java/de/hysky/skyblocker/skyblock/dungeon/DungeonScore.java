package de.hysky.skyblocker.skyblock.dungeon;


import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DungeonScore {
    private static final SkyblockerConfig.DungeonScore SCORE_CONFIG = SkyblockerConfigManager.get().locations.dungeons.dungeonScore;
    private static final SkyblockerConfig.MimicMessages MIMIC_MESSAGES_CONFIG = SkyblockerConfigManager.get().locations.dungeons.mimicMessages;
    private static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Dungeon Score");
    private static final Pattern CLEARED_PATTERN = Pattern.compile("Cleared: (?<cleared>\\d+)%.*");
    private static final Pattern SECRETS_PATTERN = Pattern.compile("Secrets Found: (?<secper>\\d+\\.?\\d*)%");
    private static final Pattern PUZZLES_PATTERN = Pattern.compile(".+?(?=:): \\[(?<state>.)](?: \\(\\w+\\))?");
    private static final Pattern PUZZLE_COUNT_PATTERN = Pattern.compile("Puzzles: \\((?<count>\\d+)\\)");
    private static final Pattern TIME_PATTERN = Pattern.compile("Time: (?:(?<hours>\\d+(?=h))?h? ?(?<minutes>\\d+(?=m))?m? ?(?<seconds>\\d+(?=s))s|Soon!)");
    private static final Pattern CRYPTS_PATTERN = Pattern.compile("Crypts: (?<crypts>\\d+)");
    private static final Pattern COMPLETED_ROOMS_PATTERN = Pattern.compile(" *Completed Rooms: (?<rooms>\\d+)");
    private static final Pattern DUNGEON_START_PATTERN = Pattern.compile("(?:Auto-closing|Starting) in: \\d:\\d+");
    private static final Pattern FLOOR_PATTERN = Pattern.compile(".*?(?=T)The Catacombs \\((?<floor>[EFM]\\D*\\d*)\\)");
    private static final Pattern DEATHS_PATTERN = Pattern.compile("Team Deaths: (?<deaths>\\d+)");
    private static String currentFloor;
    private static boolean sent270;
    private static boolean sent300;
    private static boolean isMimicKilled;
    private static int puzzleCount;
    //Caching the dungeon start state to prevent unnecessary scoreboard pattern matching after dungeon starts
    private static boolean isDungeonStarted;
    private static boolean isMayorPaul;
    private static final HashMap<String, FloorRequirement> floorRequirements = new HashMap<>(Map.ofEntries(
            Map.entry("E", new FloorRequirement(30, 1200)),
            Map.entry("F1", new FloorRequirement(30, 600)),
            Map.entry("F2", new FloorRequirement(40, 600)),
            Map.entry("F3", new FloorRequirement(50, 600)),
            Map.entry("F4", new FloorRequirement(60, 720)),
            Map.entry("F5", new FloorRequirement(70, 600)),
            Map.entry("F6", new FloorRequirement(85, 720)),
            Map.entry("F7", new FloorRequirement(100, 840)),
            Map.entry("M1", new FloorRequirement(100, 480)),
            Map.entry("M2", new FloorRequirement(100, 480)),
            Map.entry("M3", new FloorRequirement(100, 480)),
            Map.entry("M4", new FloorRequirement(100, 480)),
            Map.entry("M5", new FloorRequirement(100, 480)),
            Map.entry("M6", new FloorRequirement(100, 600)),
            Map.entry("M7", new FloorRequirement(100, 840))
    ));

    public static void init() {
        Scheduler.INSTANCE.scheduleCyclic(DungeonScore::tick, 20);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (isEntityMimic(entity)) {
                if (MIMIC_MESSAGES_CONFIG.sendMimicMessages) MessageScheduler.INSTANCE.sendMessageAfterCooldown(MIMIC_MESSAGES_CONFIG.mimicMessage);
                setMimicKilled(true);
            }
        });
    }

    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!Utils.isInDungeons() || client.player == null) {
            reset();
            return;
        }
        if (!isDungeonStarted) {
            if (checkIfDungeonStarted()) onDungeonStart();
            return;
        }
        int score = calculateScore();
        if (SCORE_CONFIG.enableDungeonScore270 && !sent270 && score >= 270 && score < 300) {
            MessageScheduler.INSTANCE.sendMessageAfterCooldown("/pc " + Constants.PREFIX.get().getString() + SCORE_CONFIG.dungeonScore270Message.replaceAll("\\[score]", "270"));
            client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1f, 0.1f);
            sent270 = true;
        }
        if (SCORE_CONFIG.enableDungeonScore300 && !sent300 && score >= 300) {
            MessageScheduler.INSTANCE.sendMessageAfterCooldown("/pc " + Constants.PREFIX.get().getString() + SCORE_CONFIG.dungeonScore300Message.replaceAll("\\[score]", "300"));
            client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1f, 1f);
            sent300 = true;
        }
    }

    public static boolean isEntityMimic(Entity entity) {
        if (!(entity instanceof ZombieEntity zombie)) return false;
        if (!zombie.isBaby()) return false;
        try {
            DefaultedList<ItemStack> armor = (DefaultedList<ItemStack>) zombie.getArmorItems();
            if (armor.isEmpty()) return false;
            NbtCompound helmetNbt = armor.get(3).getNbt();
            if (helmetNbt == null) return false;
            return helmetNbt.getCompound("SkullOwner")
                    .getCompound("Properties")
                    .getList("textures", NbtElement.COMPOUND_TYPE)
                    .getCompound(0).getString("Value")
                    .equals("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTE5YzEyNTQzYmM3NzkyNjA1ZWY2OGUxZjg3NDlhZThmMmEzODFkOTA4NWQ0ZDRiNzgwYmExMjgyZDM1OTdhMCJ9fX0K");
        } catch (NullPointerException e) {
            return false;
        } catch (ClassCastException f) {
            f.printStackTrace();
            return false;
        }
    }

    private static boolean checkIfDungeonStarted() {
        for (String sidebarLine : Utils.STRING_SCOREBOARD) {
            Matcher matcher = DUNGEON_START_PATTERN.matcher(sidebarLine);
            if (matcher.matches()) return false;
        }
        return true;
    }

    private static void onDungeonStart() {
        setCurrentFloor();
        isDungeonStarted = true;
        puzzleCount = getPuzzleCount();
        isMayorPaul = Utils.getMayor().equals("Paul");
    }

    private static int calculateScore() {
        int timeScore = calculateTimeScore();
        int exploreScore = calculateExploreScore();
        int skillScore = calculateSkillScore();
        int paulScore = isMayorPaul ? 10 : 0;
        int cryptsScore = Math.min(getCrypts(), 5);
        int mimicScore = isMimicKilled ? 2 : 0;
        int totalScore = timeScore + exploreScore + skillScore + paulScore + cryptsScore + mimicScore;
        //Will be this way until ready for pr, so it's easy to debug.
        LOGGER.info("Total Score: {} (Time: {}, Explore: {}, Skill: {}, Paul: {}, Crypts: {}, Mimic: {})", totalScore, timeScore, exploreScore, skillScore, paulScore, cryptsScore, mimicScore);
        return totalScore;
    }

    private static int calculateExploreScore() {
        int completedRoomScore = (int) Math.floor(60.0 * getCompletedRooms() / getTotalRooms());
        int percentageRequirement = floorRequirements.get(currentFloor).percentage;
        int secretsScore = (int) Math.floor(40 * Math.min(percentageRequirement, getSecretsPercentage()) / percentageRequirement);
        return completedRoomScore + secretsScore;
    }

    private static int calculateTimeScore() {
        Matcher timeMatcher = PlayerListMgr.regexAt(45, TIME_PATTERN);
        if (timeMatcher == null) {
            LOGGER.error("Time pattern doesn't match");
            return 0;
        }
        int score = 100;
        int hours = Optional.ofNullable(timeMatcher.group("hours")).map(Integer::parseInt).orElse(0);
        int minutes = Optional.ofNullable(timeMatcher.group("minutes")).map(Integer::parseInt).orElse(0);
        int seconds = Optional.ofNullable(timeMatcher.group("seconds")).map(Integer::parseInt).orElse(0);
        int timeSpent = hours * 3600 + minutes * 60 + seconds;
        int timeRequirement = floorRequirements.get(currentFloor).timeLimit;
        if (timeSpent < timeRequirement) return score;

        double timePastRequirement = ((double) (timeSpent - timeRequirement) / timeRequirement) * 100;
        if (timePastRequirement >= 0 && timePastRequirement < 20) {
            score -= (int) timePastRequirement / 2;
        } else if (timePastRequirement >= 20 && timePastRequirement < 40) {
            score -= (int) (10 + (timePastRequirement - 20) / 4);
        } else if (timePastRequirement >= 40 && timePastRequirement < 50) {
            score -= (int) (15 + (timePastRequirement - 40) / 5);
        } else if (timePastRequirement >= 50 && timePastRequirement < 60) {
            score -= (int) (17 + (timePastRequirement - 50) / 6);
        } else if (timePastRequirement >= 60) {
            score -= (int) (18 + (2.0 / 3.0) + (timePastRequirement - 60) / 7);
        }
        return score;
    }

    private static int calculateSkillScore() {
        return 20 + (int) Math.floor(80.0 * getCompletedRooms() / getTotalRooms()) - (2 * getDeathCount()) - (10 * getFailedPuzzles());
    }

    private static void reset() {
        sent270 = false;
        sent300 = false;
        isDungeonStarted = false;
        isMimicKilled = false;
        isMayorPaul = false;
        puzzleCount = 0;
        currentFloor = "";
    }

    public static void setMimicKilled(boolean killed) {
        isMimicKilled = killed;
    }

    private static int getTotalRooms() {
        return (int) Math.round((getCompletedRooms()) / getClearPercentage()); //Clear% rounds to the closest integer so it can be off by 0.5% at most, this should be accurate enough
    }

    private static int getCompletedRooms() {
        Matcher completedRoomsMatcher = PlayerListMgr.regexAt(43, COMPLETED_ROOMS_PATTERN);
        if (completedRoomsMatcher == null) {
            LOGGER.error("Completed rooms pattern doesn't match");
            return 0;
        }
        return Integer.parseInt(completedRoomsMatcher.group("rooms"));
    }

    private static double getClearPercentage() {
        for (String sidebarLine : Utils.STRING_SCOREBOARD) {
            Matcher clearMatcher = CLEARED_PATTERN.matcher(sidebarLine);
            if (!clearMatcher.matches()) continue;
            return Double.parseDouble(clearMatcher.group("cleared")) / 100;
        }
        LOGGER.error("Clear pattern doesn't match");
        return 0;
    }

    private static int getDeathCount() {
        Matcher matcher = PlayerListMgr.regexAt(25, DEATHS_PATTERN);
        if (matcher == null) {
            LOGGER.error("Death count pattern doesn't match");
            return 0;
        }
        //TODO: Turn this into a map of players and their deathcounts, get party members' pets, check if they have spirit pet, if they have it reduce their death count by 0.5
        return Integer.parseInt(matcher.group("deaths"));
    }

    private static int getPuzzleCount() {
        Matcher matcher = PlayerListMgr.regexAt(47, PUZZLE_COUNT_PATTERN);
        if (matcher == null) {
            LOGGER.error("Puzzle count pattern doesn't match");
            return 0;
        }
        return Integer.parseInt(matcher.group("count"));
    }

    //Might be replaced to look for puzzle fail messages on chat instead of playerlist
    private static int getFailedPuzzles() {
        int failedPuzzles = 0;
        for (int index = 0; index < puzzleCount; index++) {
            Matcher puzzleMatcher = PlayerListMgr.regexAt(48 + index, PUZZLES_PATTERN);
            if (puzzleMatcher == null) {
                LOGGER.error("Puzzle pattern doesn't match");
                return 0;
            }
            if (puzzleMatcher.group("state").equals("✖")) failedPuzzles++;
        }
        return failedPuzzles;
    }

    private static double getSecretsPercentage() {
        Matcher matcher = PlayerListMgr.regexAt(44, SECRETS_PATTERN);
        if (matcher == null) {
            LOGGER.error("Secrets pattern doesn't match");
            return 0;
        }
        return Double.parseDouble(matcher.group("secper"));
    }

    private static int getCrypts() {
        Matcher matcher = PlayerListMgr.regexAt(33, CRYPTS_PATTERN);
        if (matcher == null) {
            LOGGER.error("Crypts pattern doesn't match");
            return 0;
        }
        return Integer.parseInt(matcher.group("crypts"));
    }

    public static void setCurrentFloor() {
        for (String sidebarLine : Utils.STRING_SCOREBOARD) {
            Matcher floorMatcher = FLOOR_PATTERN.matcher(sidebarLine);
            if (!floorMatcher.matches()) continue;
            currentFloor = floorMatcher.group("floor");
            return;
        }
        LOGGER.error("Floor pattern doesn't match");
    }

    record FloorRequirement(int percentage, int timeLimit) {
    }
}

