package de.hysky.skyblocker.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO Slayer Packet system that can provide information about the current slayer boss, abstract so that different bosses can have different info
public class SlayerUtils {
    private static ArmorStandEntity slayerArmorStandEntity;
    public static final String REVENANT = "Revenant Horror";
    public static final String TARA = "Tarantula Broodfather";
    public static final String SVEN = "Sven Packmaster";
    public static final String VOIDGLOOM = "Voidgloom Seraph";
    public static final String VAMPIRE = "Riftstalker Bloodfiend";
    public static final String DEMONLORD = "Inferno Demonlord";
    private static final Logger LOGGER = LoggerFactory.getLogger(SlayerUtils.class);
    public static final Pattern SLAYER_PATTERN = Pattern.compile("Revenant Horror|Tarantula Broodfather|Sven Packmaster|Voidgloom Seraph|Inferno Demonlord|Riftstalker Bloodfiend");

    //TODO: Cache this, probably included in Packet system
    public static List<Entity> getEntityArmorStands(Entity entity, float expandY) {
        return entity.getEntityWorld().getOtherEntities(entity, entity.getBoundingBox().expand(0.3F, expandY, 0.3F), x -> x instanceof ArmorStandEntity && x.hasCustomName());
    }

    //Eventually this should be modified so that if you hit a slayer boss all slayer features will work on that boss.
    public static ArmorStandEntity getSlayerArmorStandEntity() {
		// TODO: This should be set when the system to detect isInSlayer is made event-driven
        if (slayerArmorStandEntity != null && slayerArmorStandEntity.isAlive()) {
            return slayerArmorStandEntity;
        }

        if (MinecraftClient.getInstance().world != null) {
            for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {
                if (entity.hasCustomName()) {
                    String entityName = entity.getCustomName().getString();
                    Matcher matcher = SLAYER_PATTERN.matcher(entityName);
                    if (matcher.find()) {
                        String username = MinecraftClient.getInstance().getSession().getUsername();
                        for (Entity armorStand : getEntityArmorStands(entity, 1.5f)) {
                            if (armorStand.getDisplayName().getString().contains(username)) {
                                slayerArmorStandEntity = (ArmorStandEntity) entity;
                                return slayerArmorStandEntity;
                            }
                        }
                    }
                }
            }
        }

        slayerArmorStandEntity = null;
        return null;
    }

    public static boolean isInSlayer() {
        try {
            for (String line : Utils.STRING_SCOREBOARD) {
                if (line.contains("Slay the boss!")) {
					return true;
				}
            }
        } catch (NullPointerException e) {
            LOGGER.error("[Skyblocker] Error while checking if player is in slayer", e);
        }
        return false;
    }

    public static boolean isInSlayerType(String slayer) {
        try {
            boolean inFight = false;
            boolean type = false;
            for (String line : Utils.STRING_SCOREBOARD) {
                switch (line) {
                    case String a when a.contains("Slay the boss!") -> inFight = true;
                    case String b when b.contains(slayer) -> type = true;
                    default -> { continue; }
                }
                if (inFight && type) return true;
            }
        } catch (NullPointerException e) {
            LOGGER.error("[Skyblocker] Error while checking if player is in slayer", e);
        }
        return false;
    }

    public static boolean isInSlayerQuestType(String slayer) {
        try {
            boolean quest = false;
            boolean type = false;
            for (String line : Utils.STRING_SCOREBOARD) {
                if (line.contains("Slayer Quest")) quest = true;
                if (line.contains(slayer)) type = true;
                if (quest && type) return true;
            }
        } catch (NullPointerException e) {
            LOGGER.error("[Skyblocker] Error while checking if player is in slayer quest type", e);
        }
        return false;
    }

    public static String getSlayerType() {
        try {
            for (String line : Utils.STRING_SCOREBOARD) {
                Matcher matcher = SLAYER_PATTERN.matcher(line);
                if (matcher.find()) return matcher.group();
            }
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            LOGGER.error("[Skyblocker] Error while checking slayer type", e);
        }
        return "";
    }
}
