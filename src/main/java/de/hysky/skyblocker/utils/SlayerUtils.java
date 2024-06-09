package de.hysky.skyblocker.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

//TODO Slayer Packet system that can provide information about the current slayer boss, abstract so that different bosses can have different info
public class SlayerUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlayerUtils.class);

    //TODO: Cache this, probably included in Packet system
    public static List<Entity> getEntityArmorStands(Entity entity) {
        return entity.getEntityWorld().getOtherEntities(entity, entity.getBoundingBox().expand(1F, 2.5F, 1F), x -> x instanceof ArmorStandEntity && x.hasCustomName());
    }

    //Eventually this should be modified so that if you hit a slayer boss all slayer features will work on that boss.
    public static Entity getSlayerEntity() {
        if (MinecraftClient.getInstance().world != null) {
            for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {
                //Check if entity is Bloodfiend
                if ((entity.hasCustomName() && entity.getCustomName().getString().contains("Bloodfiend"))
                        || (entity.hasCustomName() && entity.getCustomName().getString().contains("Demonlord"))) {
                    //Grab the players username
                    String username = MinecraftClient.getInstance().getSession().getUsername();
                    //Check all armor stands around the boss
                    for (Entity armorStand : getEntityArmorStands(entity)) {
                        //Check if the display name contains the players username
                        if (armorStand.getDisplayName().getString().contains(username)) {
                            return entity;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static boolean isInSlayer() {
        try {
            for (int i = 0; i < Utils.STRING_SCOREBOARD.size(); i++) {
                String line = Utils.STRING_SCOREBOARD.get(i);

                if (line.contains("Slay the boss!")) return true;
            }
        } catch (NullPointerException e) {
            LOGGER.error("[Skyblocker] Error while checking if player is in slayer", e);
        }

        return false;
    }
}