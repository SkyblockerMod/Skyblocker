package me.xmrvizzy.skyblocker.skyblock.dungeon;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.RenderHelper;
import me.xmrvizzy.skyblocker.utils.RenderUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.color.QuadColor;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DungeonBlaze {
    private static final Logger LOGGER = LoggerFactory.getLogger(DungeonBlaze.class.getName());
    private static final float[] WHITE_COLOR_COMPONENTS = { 1.0f, 1.0f, 1.0f };
    static Entity highestBlaze = null;
    static Entity lowestBlaze = null;
    static Entity nextHighestBlaze = null;
    static Entity nextLowestBlaze = null;
    static boolean renderHooked = false;
    
    public static void update() {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null || !Utils.isInDungeons()) return;
        if (!renderHooked){

            WorldRenderEvents.BEFORE_DEBUG_RENDER.register(DungeonBlaze::blazeRenderer);
            renderHooked = true;
        }
        Iterable<Entity> entities = world.getEntities();
        List<ObjectIntPair<Entity>> blazes = new ArrayList<>();

        for (Entity entity : entities) {
    		String blazeName = entity.getName().getString();
    		
            if (blazeName.contains("Blaze") && blazeName.contains("/")) {
                try {
                    int health = Integer.parseInt(blazeName.substring(blazeName.indexOf("/") + 1, blazeName.length() - 1));
                    
                	blazes.add(ObjectIntPair.of(entity, health));
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        }

        // Order the blazes in the list from the lowest health to the highest health
        blazes.sort(Comparator.comparingInt(ObjectIntPair::rightInt));

        // Ensure that there are blazes in the list
        if (!blazes.isEmpty()) {
            lowestBlaze = blazes.get(0).left();

            int highestIndex = blazes.size() - 1;
            highestBlaze = blazes.get(highestIndex).left();

            // If there's more than 1 blaze
            if (blazes.size() > 1) {
            	nextLowestBlaze = blazes.get(1).left();
            	nextHighestBlaze = blazes.get(highestIndex - 1).left();
            }
        }
        
    }
    public static void blazeRenderer(WorldRenderContext wrc) {
        QuadColor outlineColorRed = QuadColor.single( 0.0F, 1.0F, 0.0F, 1f);
        QuadColor outlineColorGreen = QuadColor.single(1.0F, 0.0F, 0.0F, 1f);
        QuadColor outlineColorWhite = QuadColor.single(1.0f, 1.0f, 1.0f, 1.0f);
        
        try {
            if (highestBlaze != null && lowestBlaze != null && highestBlaze.isAlive() && lowestBlaze.isAlive() && SkyblockerConfig.get().locations.dungeons.blazesolver){
                /* Outline */
                if (highestBlaze.getY() < 69) {
                    Box blaze = highestBlaze.getBoundingBox().expand(0.3, 0.9, 0.3).offset(0, -1.1, 0);
                    RenderUtils.drawBoxOutline(blaze, outlineColorRed, 5f);
                    
                    if (nextHighestBlaze != null && nextHighestBlaze.isAlive() && nextHighestBlaze != highestBlaze) {
                        Box nextBlaze = nextHighestBlaze.getBoundingBox().expand(0.3, 0.9, 0.3).offset(0, -1.1, 0);
                        RenderUtils.drawBoxOutline(nextBlaze, outlineColorWhite, 5f);
                        RenderHelper.renderLinesFromPoints(wrc, new Vec3d[] { blaze.getCenter(), nextBlaze.getCenter() }, WHITE_COLOR_COMPONENTS, 1f, 5f);
                    }
                }

                /* Outline */
                if (lowestBlaze.getY() > 69) {
                    Box blaze = lowestBlaze.getBoundingBox().expand(0.3, 0.9, 0.3).offset(0, -1.1, 0);
                    RenderUtils.drawBoxOutline(blaze, outlineColorRed, 5f);
                    
                    if (nextLowestBlaze != null && nextLowestBlaze.isAlive() && nextLowestBlaze != lowestBlaze) {
                        Box nextBlaze = nextLowestBlaze.getBoundingBox().expand(0.3, 0.9, 0.3).offset(0, -1.1, 0);
                        RenderUtils.drawBoxOutline(nextBlaze, outlineColorWhite, 5f);
                        RenderHelper.renderLinesFromPoints(wrc, new Vec3d[] { blaze.getCenter(), nextBlaze.getCenter() }, WHITE_COLOR_COMPONENTS, 1f, 5f);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("[Skyblocker BlazeRenderer] " + e);
            e.printStackTrace();
        }
    }
}