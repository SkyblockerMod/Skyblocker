package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class CrystalsHud {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    protected static final Identifier MAP_TEXTURE = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/crystals_map.png"); 
    private static final Identifier MAP_ICON = new Identifier("textures/map/map_icons.png");
    private static final List<String> SMALL_LOCATIONS = List.of("Fairy Grotto", "King Yolkar", "Corleone", "Odawa", "Key Guardian");

    public static boolean visible = false;

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("skyblocker")
                .then(ClientCommandManager.literal("hud")
                        .then(ClientCommandManager.literal("crystals")
                                .executes(Scheduler.queueOpenScreenCommand(CrystalsHudConfigScreen::new))))));

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if (!SkyblockerConfigManager.get().locations.dwarvenMines.crystalsHud.enabled
                    || CLIENT.player == null
                    || !visible) {
                return;
            }
            render(context, tickDelta, SkyblockerConfigManager.get().locations.dwarvenMines.crystalsHud.x,
                    SkyblockerConfigManager.get().locations.dwarvenMines.crystalsHud.y);
        });
    }

    protected static int getDimensionsForConfig() {
        return (int) (62 * SkyblockerConfigManager.get().locations.dwarvenMines.crystalsHud.mapScaling);
    }


    /**
     * Renders the map to the players UI. renders the background image ({@link CrystalsHud#MAP_TEXTURE}) of the map then if enabled special locations on the map. then finally the player to the map.
     *
     * @param context DrawContext to draw map to
     * @param tickDelta For interpolating the player's yaw for map marker
     * @param hudX Top left X coordinate of the map
     * @param hudY Top left Y coordinate of the map
     */
    private static void render(DrawContext context, float tickDelta, int hudX, int hudY) {
        float scale = SkyblockerConfigManager.get().locations.dwarvenMines.crystalsHud.mapScaling;

        //make sure the map renders infront of some stuff - improve this in the future with better layering (1.20.5?)
        //and set position and scale
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(hudX, hudY, 200f);
        matrices.scale(scale, scale, 0f);

        //draw map texture
        context.drawTexture(MAP_TEXTURE, 0, 0, 0, 0, 62, 62, 62, 62);

        //if enabled add waypoint locations to map
        if (SkyblockerConfigManager.get().locations.dwarvenMines.crystalsHud.showLocations) {
            Map<String,CrystalsWaypoint> ActiveWaypoints = CrystalsLocationsManager.activeWaypoints;

            for (CrystalsWaypoint waypoint : ActiveWaypoints.values()) {
                Color waypointColor = waypoint.category.color;
                Vector2ic renderPos = transformLocation(waypoint.pos.getX(), waypoint.pos.getZ());
                int locationSize = SkyblockerConfigManager.get().locations.dwarvenMines.crystalsHud.locationSize;

                if (SMALL_LOCATIONS.contains(waypoint.name.getString())) {//if small location half the location size
                    locationSize /= 2;
                }

                //fill square of size locationSize around the coordinates of the location
                context.fill(renderPos.x() - locationSize / 2, renderPos.y() - locationSize / 2, renderPos.x() + locationSize / 2, renderPos.y() + locationSize / 2, waypointColor.getRGB());
            }
        }

        //draw player on map
        if (CLIENT.player == null || CLIENT.getNetworkHandler() == null) {
            return;
        }

        //get player location
        double playerX = CLIENT.player.getX();
        double playerZ = CLIENT.player.getZ();
        float playerRotation = CLIENT.player.getYaw(); //TODO make the transitions more rough?
        Vector2ic renderPos = transformLocation(playerX, playerZ);

        int renderX = renderPos.x() - 2;
        int renderY = renderPos.y() - 3;

        //position, scale and rotate the player marker
        matrices.translate(renderX, renderY, 0f);
        matrices.scale(0.75f, 0.75f, 0f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw2Cardinal(playerRotation)), 2.5f, 3.5f, 0);

        //draw marker on map
        context.drawTexture(MAP_ICON, 0, 0, 2, 0, 5, 7, 128, 128);

        //todo add direction (can not work out how to rotate)
        matrices.pop();
    }

    /**
     * Converts an X and Z coordinate in the crystal hollow to an X and Y coordinate on the map.
     *
     * @param x the world X coordinate
     * @param z the world Z coordinate
     * @return a vector representing the x and y values
     */
    protected static Vector2ic transformLocation(double x, double z) {
        //converts an x and z to a location on the map
        int transformedX = (int) ((x - 202) / 621 * 62);
        int transformedY = (int) ((z - 202) / 621 * 62);
        transformedX = MathHelper.clamp(transformedX, 0, 62);
        transformedY = MathHelper.clamp(transformedY, 0, 62);

        return new Vector2i(transformedX, transformedY);
    }

    /**
     * Converts yaw to the cardinal directions that a player marker can be rotated towards on a map.
     * The rotations of a marker follow this order: N, NNE, NE, ENE, E, ESE, SE, SSE, S, SSW, SW, WSW, W, WNW, NW, NNW.
     * <br><br>
     * Based off code from {@link net.minecraft.client.render.MapRenderer}
     */
    private static float yaw2Cardinal(float yaw) {
        yaw += 180; //flip direction
        byte clipped = (byte) ((yaw + (yaw < 0.0 ? -8.0 : 8.0)) * 16.0 / 360.0);

        return (clipped * 360f) / 16f;
    }

    /**
     * Works out if the crystals map should be rendered and sets {@link CrystalsHud#visible} accordingly.
     *
     */
    public static void update() {
        if (CLIENT.player == null || CLIENT.getNetworkHandler() == null || !SkyblockerConfigManager.get().locations.dwarvenMines.crystalsHud.enabled) {
            visible = false;
            return;
        }

        //get if the player is in the crystals
        visible = Utils.isInCrystalHollows();
    }
}
