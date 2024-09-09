package de.hysky.skyblocker.skyblock.slayers;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.slayers.boss.vampire.ManiaIndicator;
import de.hysky.skyblocker.skyblock.slayers.boss.vampire.StakeIndicator;
import de.hysky.skyblocker.skyblock.slayers.boss.vampire.TwinClawsIndicator;
import de.hysky.skyblocker.skyblock.slayers.features.SlainTime;
import de.hysky.skyblocker.utils.RomanNumerals;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO Slayer Packet system that can provide information about the current slayer boss, abstract so that different bosses can have different info
public class SlayerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlayerManager.class);
    private static final Map<SlayerAction, Runnable> actions = new HashMap<>();
    //Patterns
    private static final Pattern SLAYER_PATTERN = Pattern.compile("Revenant Horror|Tarantula Broodfather|Sven Packmaster|Voidgloom Seraph|Inferno Demonlord|Riftstalker Bloodfiend");
    private static final Pattern SLAYER_TIER_PATTERN = Pattern.compile("^(Revenant Horror|Tarantula Broodfather|Sven Packmaster|Voidgloom Seraph|Inferno Demonlord|Riftstalker Bloodfiend)\\s+(I|II|III|IV|V)$");
    private static final Pattern patternFixed = Pattern.compile(".*(Your Slayer Quest has been cancelled!|SLAYER QUEST STARTED!|NICE! SLAYER BOSS SLAIN!|SLAYER QUEST FAILED!).*");
    private static final Pattern patternXpNeeded = Pattern.compile(".*((Wolf|Zombie|Spider|Enderman|Blaze|Vampire) Slayer LVL [0-9] - (Next LVL in [0-9]{1,3}(,[0-9]{3})* XP!|LVL MAXED OUT!)).*");
    private static final Pattern patternLvlUp = Pattern.compile(".*LVL UP! ➜ (Wolf|Zombie|Spider|Enderman|Blaze|Vampire) Slayer LVL [1-9].*");
    public static String slayerType = "";
    public static String slayerTier = "";
    public static int xpRemaining = 0;
    public static int level = -1;
    public static boolean bossSpawned;
    private static Instant startTime;
    private static SlayerQuest quest;

    public static void init() {
        actions.put(SlayerAction.CANCELLED, () -> quest = null);
        actions.put(SlayerAction.FAILED, () -> quest = null);
        actions.put(SlayerAction.STARTED, () -> quest = new SlayerQuest());
        actions.put(SlayerAction.SLAIN, () -> {
            quest.slain = true;
            SlainTime.onBossDeath(startTime);
        });
        actions.put(SlayerAction.COMPLETE, () -> {
            if (!quest.slain)
                SlainTime.onBossDeath(startTime);
            quest = null;
        });

        ClientReceiveMessageEvents.GAME.register(SlayerManager::onChatMessage);
        Scheduler.INSTANCE.scheduleCyclic(SlayerManager::getSlayerBossInfo, 20);
        Scheduler.INSTANCE.scheduleCyclic(SlayerManager::bossSpawnAlert, 10);
        Scheduler.INSTANCE.scheduleCyclic(TwinClawsIndicator::updateIce, SkyblockerConfigManager.get().slayers.vampireSlayer.holyIceUpdateFrequency);
        Scheduler.INSTANCE.scheduleCyclic(ManiaIndicator::updateMania, SkyblockerConfigManager.get().slayers.vampireSlayer.maniaUpdateFrequency);
        Scheduler.INSTANCE.scheduleCyclic(StakeIndicator::updateStake, SkyblockerConfigManager.get().slayers.vampireSlayer.steakStakeUpdateFrequency);
    }

    private static void onChatMessage(Text text, boolean b) {
        String message = text.getString();
        Matcher matcherFixed = patternFixed.matcher(message);
        Matcher matcherNextLvl = patternXpNeeded.matcher(message);
        Matcher matcherLvlUp = patternLvlUp.matcher(message);

        if (matcherFixed.matches()) {
            for (SlayerAction action : SlayerAction.values()) {
                if (message.toLowerCase().contains(action.name().toLowerCase())) {
                    actions.get(action).run();
                    break;
                }
            }
        } else if (matcherNextLvl.matches()) {
            if (message.contains("LVL MAXED OUT")) {
                level = message.contains("Vampire") ? 5 : 9;
                xpRemaining = -1;
            } else {
                int xpIndex = message.indexOf("Next LVL in ") + "Next LVL in ".length();
                int xpEndIndex = message.indexOf(" XP!", xpIndex);
                if (xpEndIndex != -1) {
                    level = Integer.parseInt(Pattern.compile("\\d+").matcher(message).results().map(m -> m.group()).findFirst().orElse(null));
                    xpRemaining = Integer.parseInt(message.substring(xpIndex, xpEndIndex).trim().replace(",", ""));
                } else LOGGER.error("[Skyblocker] error getting xpNeeded (xpEndIndex == -1)");
            }
            actions.get(SlayerAction.COMPLETE).run();
        } else if (matcherLvlUp.matches()) {
            level = Integer.parseInt(message.replaceAll("(\\d+).+", "$1"));
            actions.get(SlayerAction.COMPLETE).run();
        }
    }

    private static void bossSpawnAlert() {
        try {
            for (String line : Utils.STRING_SCOREBOARD) {
                if (line.contains("Slay the boss!")) {
                    if (quest != null && !bossSpawned && !quest.slain) {
                        if (SkyblockerConfigManager.get().slayers.bossSpawnAlert)
                            Utils.warn(I18n.translate("skyblocker.slayer.bossSpawnAlert"));
                        bossSpawned = true;
                        quest.lfMinis = false;
                        startTime = Instant.now();
                        System.out.println("setting time");
                    }
                    return;
                }
            }
            bossSpawned = false;
        } catch (IndexOutOfBoundsException e) {
            LOGGER.error("[Skyblocker] Failed to make a boss spawn alert", e);
        }
    }

    private static void getSlayerBossInfo() {
        try {
            for (String line : Utils.STRING_SCOREBOARD) {
                //if (line.equals("Boss slain!") && quest != null) quest.slain = true;
                Matcher matcher = SLAYER_TIER_PATTERN.matcher(line);
                if (matcher.find()) {
                    if (!slayerType.isEmpty() && !matcher.group(1).equals(slayerType)) {
                        xpRemaining = 0;
                        level = -1;
                    } else if (slayerType.isEmpty()) quest = new SlayerQuest();
                    slayerType = matcher.group(1);
                    slayerTier = matcher.group(2);
                    return;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            LOGGER.error("[Skyblocker] Failed to get slayer boss info", e);
        }
    }

    public static int calculateBossesNeeded() {
        int tier = RomanNumerals.romanToDecimal(slayerTier);
        if (tier == 0) return -1;

        int xpPerTier;
        if (slayerType.equals("Vampire")) {
            xpPerTier = SlayerConstants.vampireXpPerTier[tier - 1];
        } else {
            xpPerTier = SlayerConstants.regularXpPerTier[tier - 1];
        }

        return (int) Math.ceil((double) xpRemaining / xpPerTier);
    }

    //TODO: Cache this, probably included in Packet system
    public static List<Entity> getEntityArmorStands(Entity entity) {
        return entity.getEntityWorld().getOtherEntities(entity, entity.getBoundingBox().expand(0.3F, 2.5F, 0.3F), x -> x instanceof ArmorStandEntity && x.hasCustomName());
    }

    //Eventually this should be modified so that if you hit a slayer boss all slayer features will work on that boss.
    public static Entity getSlayerEntity() {
        if (MinecraftClient.getInstance().world != null) {
            for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {
                if (entity.hasCustomName()) {
                    String entityName = entity.getCustomName().getString();
                    Matcher matcher = SLAYER_PATTERN.matcher(entityName);
                    if (matcher.find()) {
                        String username = MinecraftClient.getInstance().getSession().getUsername();
                        for (Entity armorStand : getEntityArmorStands(entity)) {
                            if (armorStand.getDisplayName().getString().contains(username)) {
                                return entity;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns whether client is in Slayer Quest or no.
     * Note: does not check if boss spawned or no.
     */
    public static boolean isInSlayer() {
        return quest != null;
    }

    /**
     * Returns whether Slayer Boss Spawned or no.
     */
    public static boolean isBossSpawned() {
        return quest != null && bossSpawned;
    }

    public static boolean isInSlayerType(String slayer) {
        return quest != null && bossSpawned && slayerType.equals(slayer);
    }

    public static boolean isInSlayerQuestType(String slayer) {
        return quest != null && slayerType.equals(slayer);
    }

    public static String getSlayerType() {
        return slayerType;
    }

    public static SlayerQuest getSlayerQuest() {
        return quest;
    }

    enum SlayerAction {
        CANCELLED,
        STARTED,
        COMPLETE,
        SLAIN,
        FAILED,
        NEXT_LVL,
        MAXED_OUT,
        LVL_UP
    }

    public static class SlayerQuest {

        public boolean slain = false;
        public boolean lfMinis = true;

        public SlayerQuest() {
        }

        public boolean isSlain() {
            return slain;
        }

        public boolean isLfMinis() {
            return lfMinis;
        }
    }

}