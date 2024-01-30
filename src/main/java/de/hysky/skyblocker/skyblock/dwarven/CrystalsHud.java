package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
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

public class CrystalsHud {
    public static final MinecraftClient client = MinecraftClient.getInstance();

    protected static final Identifier MAP_TEXTURE = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/crystals_map.png"); //todo is this the right place to store file

    private static final Identifier MAP_ICON = new Identifier("textures/map/map_icons.png");

    public static boolean visable  = false;




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
        //draw player on map
        if (client.player == null || client.getNetworkHandler() == null) {
            return;
        }
        //get player location
        double playerX = client.player.getX();
        double playerZ = client.player.getZ();
        double facing = client.player.getYaw();
        //map location to map
        int renderX = (int)((playerX-202)/621 * 62);
        int renderY = (int)((playerZ -202)/621 * 62);
        int renderAngle = (int)(facing %360);
        if (renderAngle < 0){//make sure the angle is always correct between 0 and 360
            renderAngle = 360 + renderAngle;
        }
        //clamp location to map
        renderX = Math.max(0, Math.min(62, renderX));
        renderY = Math.max(0, Math.min(62, renderY));
        //draw marker on map
        context.
                drawTexture(MAP_ICON,hudX+renderX,hudY+renderY,2,0,5,7,128,128);

        //todo add direction and scale (could be wrong drawing methods) and offset to center on player

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
