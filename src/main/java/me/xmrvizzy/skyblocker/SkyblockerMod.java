package me.xmrvizzy.skyblocker;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.container.ContainerSolverManager;
import me.xmrvizzy.skyblocker.discord.DiscordRPCManager;
import me.xmrvizzy.skyblocker.skyblock.dungeon.DungeonBlaze;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.Objects;

public class SkyblockerMod {
    public static final String NAMESPACE = "skyblocker";
    private static final SkyblockerMod instance = new SkyblockerMod();
    public final ContainerSolverManager containerSolverManager = new ContainerSolverManager();
    public DiscordRPCManager discordRPCManager = new DiscordRPCManager();

    private SkyblockerMod() {
    }

    public static SkyblockerMod getInstance() {
        return instance;
    }

    private int ticks = 0;
    private int rpTimer = 0;

    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        ticks++;
        if (ticks % 4 == 0)
            try {
                if (Utils.isDungeons) {
                    DungeonBlaze.DungeonBlaze();
                }
            } catch (Exception e) {
                //System.out.println("Blazesolver: " + e);
            }
        if (ticks % 20 == 0 ) {
            rpTimer++;
            if (rpTimer == 5){
                discordRPCManager.updatePresence();
                discordRPCManager.cycleCount++;
                if (discordRPCManager.cycleCount == 3) discordRPCManager.cycleCount = 0;
                rpTimer = 0;
            }
            if (client.world != null && !client.isInSingleplayer())
                Utils.sbChecker();
            if (!discordRPCManager.isConnected && Utils.isSkyblock && SkyblockerConfig.get().general.richPresence.enableRichPresence && onHypxiel()) discordRPCManager.start();
            if (discordRPCManager.isConnected && !SkyblockerConfig.get().general.richPresence.enableRichPresence) discordRPCManager.stop();
            if (client.world == null || client.isInSingleplayer() || !Utils.isSkyblock || !onHypxiel()) if (discordRPCManager.isConnected)discordRPCManager.stop();
            ticks = 0;
        }
    }
    public static MinecraftClient client() {
        try {
            return MinecraftClient.getInstance();
        }
        catch(NullPointerException e) {
            return null;
        }
    }
    public static boolean onHypxiel() {
        try {
            return client() != null && !client().isInSingleplayer() && client().getCurrentServerEntry().address != null && client().getCurrentServerEntry().address.contains("hypixel.net");
        } catch (NullPointerException exception) {
            return false;
        }
    }
}