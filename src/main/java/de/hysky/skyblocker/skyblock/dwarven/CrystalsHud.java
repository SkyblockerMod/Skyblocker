package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.atlas.Sprite;
import net.minecraft.util.Identifier;
import org.apache.commons.math3.analysis.UnivariateMatrixFunction;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;

public class CrystalsHud {
    public static final MinecraftClient client = MinecraftClient.getInstance();

    protected static final Identifier MAP_TEXTURE = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/crystals_map.png"); //todo is this the right place to store file

    private static final Identifier MAP_ICON = new Identifier("textures/map/map_icons.png");

    public static boolean visable  = false;

    public static final int LOCATION_SIZE  = 10; //todo possible config option



    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("skyblocker")
                .then(ClientCommandManager.literal("hud")
                        .then(ClientCommandManager.literal("crystals")
                                .executes(Scheduler.queueOpenScreenCommand(CrystalsHudConfigScreen::new))))));

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if (!SkyblockerConfigManager.get().locations.dwarvenMines.crystalsHud.enabled
                    || client.options.playerListKey.isPressed()
                    || client.player == null
                    || !visable) {
                return;
            }
            render(context, SkyblockerConfigManager.get().locations.dwarvenMines.crystalsHud.x,
                    SkyblockerConfigManager.get().locations.dwarvenMines.crystalsHud.y);
        });
    }

    public static IntIntPair getDimForConfig() {
        return IntIntPair.of(62, 62);
    }

    public static void render( DrawContext context, int hudX, int hudY) {

        if (SkyblockerConfigManager.get().locations.dwarvenMines.crystalsHud.enableBackground) {
            context.fill(hudX, hudY, hudX + 62, hudY + 62, 0x64000000);
        }


        //draw map texture
        context.
                drawTexture(MAP_TEXTURE,hudX,hudY,0,0,62,62,62,62);
        //if enabled add waypoint locations to map
        if (SkyblockerConfigManager.get().locations.dwarvenMines.crystalsHud.showLocations){
            Map<String,CrystalsWaypoint> ActiveWaypoints=  SkyblockerConfigManager.get().locations.dwarvenMines.crystalsWaypoints.ActiveWaypoints;
            for (CrystalsWaypoint waypoint : ActiveWaypoints.values()){
                Color waypointColor = waypoint.category.color;
                Pair<Integer, Integer> renderPos  = transformLocation(waypoint.pos.getX(),waypoint.pos.getZ());
                //fill square of size LOCATION_SIZE around the coordinates of the location
                context.fill(hudX+renderPos.first()-LOCATION_SIZE/2,hudY+renderPos.second()-LOCATION_SIZE/2,hudX+renderPos.first()+LOCATION_SIZE/2,hudY+renderPos.second()+LOCATION_SIZE/2,waypointColor.getRGB());
            }
        }
        //draw player on map
        if (client.player == null || client.getNetworkHandler() == null) {
            return;
        }
        //get player location
        double playerX = client.player.getX();
        double playerZ = client.player.getZ();
        double facing = client.player.getYaw();
        Pair<Integer, Integer> renderPos  = transformLocation(playerX,playerZ);
        //draw marker on map
        context.
                drawTexture(MAP_ICON,hudX+renderPos.first()-2,hudY+renderPos.second()-2,58,2,4,4,128,128);

        //todo add direction and scale (can not work out how to rotate)

    }
    private static Pair<Integer, Integer> transformLocation(double x, double z){
        //converts an x and z to a location on the map
        int transformedX = (int)((x-202)/621 * 62);
        int transformedY = (int)((z -202)/621 * 62);
        transformedX = Math.max(0, Math.min(62, transformedX));
        transformedY = Math.max(0, Math.min(62, transformedY));

        return  Pair.of(transformedX,transformedY);
    }

    public static void update() {
        if (client.player == null || client.getNetworkHandler() == null || !SkyblockerConfigManager.get().locations.dwarvenMines.crystalsHud.enabled) {
            visable = false;
            return;
        }
        //get if the player is in the crystals
        visable = Utils.isInCrystals();


    }

}
