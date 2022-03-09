package me.xmrvizzy.skyblocker.skyblock.dungeon;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.color.QuadColor;
import me.xmrvizzy.skyblocker.utils.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DungeonBlaze {
    private static final Logger LOGGER = LoggerFactory.getLogger(DungeonBlaze.class.getName());
    static Entity highestBlaze = null;
    static Entity lowestBlaze = null;
    static boolean renderHooked = false;
    
    public static void update() {
        if (!Utils.isInDungeons) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if(!renderHooked){

            WorldRenderEvents.END.register(DungeonBlaze::blazeRenderer);
            renderHooked = true;
        }
        assert client.world != null;
        Iterable<Entity> entities = client.world.getEntities();
        int highestHealth = 0;
        int lowestHealth = 99999999;

        for (Entity entity : entities) {
            if (entity.getName().getString().contains("Blaze") && entity.getName().getString().contains("/")) {
        
                String blazeName = entity.getName().getString();
                try {
                    
                    int health = Integer.parseInt(blazeName.substring(blazeName.indexOf("/") + 1, blazeName.length() - 1));
                  
                    if (health > highestHealth) {
                        highestHealth = health;
                        
                        highestBlaze = entity;
                        
                    }
                    if (health < lowestHealth) {
                        lowestHealth = health;
                        lowestBlaze = entity;
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    public static void blazeRenderer(WorldRenderContext wrc) {
        QuadColor outlineColorRed = QuadColor.single( 0.0F, 1.0F, 0.0F, 1f);
        QuadColor outlineColorGreen = QuadColor.single(1.0F, 0.0F, 0.0F, 1f);
        try {
            if(highestBlaze != null && lowestBlaze != null && highestBlaze.isAlive() && lowestBlaze.isAlive() && SkyblockerConfig.get().locations.dungeons.blazesolver){
                /* Outline */
                if(highestBlaze.getY() <69) {
                    Box blaze = highestBlaze.getBoundingBox().expand(0.3, 0.9, 0.3).offset(0, -1.1, 0);
                    RenderUtils.drawBoxOutline(blaze, outlineColorRed, 5f);
                }

                /* Outline */
                if(lowestBlaze.getY() >69) {
                    Box blaze = lowestBlaze.getBoundingBox().expand(0.3, 0.9, 0.3).offset(0, -1.1, 0);
                    RenderUtils.drawBoxOutline(blaze, outlineColorRed, 5f);
                }
            }
        }catch(Exception e) {
            LOGGER.warn("[Skyblocker BlazeRenderer] " + e);
        }
    }
}