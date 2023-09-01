package me.xmrvizzy.skyblocker.skyblock.dungeon;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This class provides functionality to render outlines around Blaze entities
 */
public class DungeonBlaze {
    private static final Logger LOGGER = LoggerFactory.getLogger(DungeonBlaze.class.getName());
    private static final float[] GREEN_COLOR_COMPONENTS = {0.0F, 1.0F, 0.0F};
    private static final float[] WHITE_COLOR_COMPONENTS = {1.0f, 1.0f, 1.0f};

    private static ArmorStandEntity highestBlaze = null;
    private static ArmorStandEntity lowestBlaze = null;
    private static ArmorStandEntity nextHighestBlaze = null;
    private static ArmorStandEntity nextLowestBlaze = null;

    public static void init() {
        SkyblockerMod.getInstance().scheduler.scheduleCyclic(DungeonBlaze::update, 10);
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(DungeonBlaze::blazeRenderer);
    }

    /**
     * Updates the state of Blaze entities and triggers the rendering process if necessary.
     */
    public static void update() {
        ClientWorld world = MinecraftClient.getInstance().world;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (world == null || player == null || !Utils.isInDungeons()) return;
        List<ObjectIntPair<ArmorStandEntity>> blazes = getBlazesInWorld(world, player);
        sortBlazes(blazes);
        updateBlazeEntities(blazes);
    }

    /**
     * Retrieves Blaze entities in the world and parses their health information.
     *
     * @param world The client world to search for Blaze entities.
     * @return A list of Blaze entities and their associated health.
     */
    private static List<ObjectIntPair<ArmorStandEntity>> getBlazesInWorld(ClientWorld world, ClientPlayerEntity player) {
        List<ObjectIntPair<ArmorStandEntity>> blazes = new ArrayList<>();
        for (ArmorStandEntity blaze : world.getEntitiesByClass(ArmorStandEntity.class, player.getBoundingBox().expand(500D), EntityPredicates.NOT_MOUNTED)) {
            String blazeName = blaze.getName().getString();
            if (blazeName.contains("Blaze") && blazeName.contains("/")) {
                try {
                    int health = Integer.parseInt(blazeName.substring(blazeName.indexOf("/") + 1, blazeName.length() - 1));
                    blazes.add(ObjectIntPair.of(blaze, health));
                } catch (NumberFormatException e) {
                    handleException(e);
                }
            }
        }
        return blazes;
    }

    /**
     * Sorts the Blaze entities based on their health values.
     *
     * @param blazes The list of Blaze entities to be sorted.
     */
    private static void sortBlazes(List<ObjectIntPair<ArmorStandEntity>> blazes) {
        blazes.sort(Comparator.comparingInt(ObjectIntPair::rightInt));
    }

    /**
     * Updates information about Blaze entities based on sorted list.
     *
     * @param blazes The sorted list of Blaze entities with associated health values.
     */
    private static void updateBlazeEntities(List<ObjectIntPair<ArmorStandEntity>> blazes) {
        if (!blazes.isEmpty()) {
            lowestBlaze = blazes.get(0).left();
            int highestIndex = blazes.size() - 1;
            highestBlaze = blazes.get(highestIndex).left();
            if (blazes.size() > 1) {
                nextLowestBlaze = blazes.get(1).left();
                nextHighestBlaze = blazes.get(highestIndex - 1).left();
            }
        }
    }

    /**
     * Renders outlines for Blaze entities based on health and position.
     *
     * @param wrc The WorldRenderContext used for rendering.
     */
    public static void blazeRenderer(WorldRenderContext wrc) {
        try {
            if (highestBlaze != null && lowestBlaze != null && highestBlaze.isAlive() && lowestBlaze.isAlive() && SkyblockerConfig.get().locations.dungeons.blazesolver) {
                if (highestBlaze.getY() < 69) {
                    renderBlazeOutline(highestBlaze, nextHighestBlaze, wrc);
                }
                if (lowestBlaze.getY() > 69) {
                    renderBlazeOutline(lowestBlaze, nextLowestBlaze, wrc);
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Renders outlines for Blaze entities and connections between them.
     *
     * @param blaze     The Blaze entity for which to render an outline.
     * @param nextBlaze The next Blaze entity for connection rendering.
     * @param wrc       The WorldRenderContext used for rendering.
     */
    private static void renderBlazeOutline(ArmorStandEntity blaze, ArmorStandEntity nextBlaze, WorldRenderContext wrc) {
        Box blazeBox = blaze.getBoundingBox().expand(0.3, 0.9, 0.3).offset(0, -1.1, 0);
        RenderHelper.renderOutline(wrc, blazeBox, GREEN_COLOR_COMPONENTS, 5f);

        if (nextBlaze != null && nextBlaze.isAlive() && nextBlaze != blaze) {
            Box nextBlazeBox = nextBlaze.getBoundingBox().expand(0.3, 0.9, 0.3).offset(0, -1.1, 0);
            RenderHelper.renderOutline(wrc, nextBlazeBox, WHITE_COLOR_COMPONENTS, 5f);

            Vec3d blazeCenter = blazeBox.getCenter();
            Vec3d nextBlazeCenter = nextBlazeBox.getCenter();

            RenderHelper.renderLinesFromPoints(wrc, new Vec3d[]{blazeCenter, nextBlazeCenter}, WHITE_COLOR_COMPONENTS, 1f, 5f);
        }
    }

    /**
     * Handles exceptions by logging and printing stack traces.
     *
     * @param e The exception to handle.
     */
    private static void handleException(Exception e) {
        LOGGER.warn("[Skyblocker BlazeRenderer] Encountered an unknown exception", e);
    }
}
