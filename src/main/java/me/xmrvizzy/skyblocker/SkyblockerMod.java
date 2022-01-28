package me.xmrvizzy.skyblocker;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.container.ContainerSolverManager;
import me.xmrvizzy.skyblocker.discord.DiscordRPCManager;
import me.xmrvizzy.skyblocker.skyblock.dungeon.DungeonBlaze;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;

public class SkyblockerMod {
    public static final String NAMESPACE = "skyblocker";
    private static final SkyblockerMod instance = new SkyblockerMod();
    public final ContainerSolverManager containerSolverManager = new ContainerSolverManager();
    public DiscordRPCManager discordRPCManager = new DiscordRPCManager();
    public static int rpTimer = 0;

    private SkyblockerMod() {
    }

    public static SkyblockerMod getInstance() {
        return instance;
    }

    private int ticks = 0;

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
        if (ticks % 20 == 0) {
            rpTimer++;
            if (rpTimer == 5){
                discordRPCManager.updatePresence();
                rpTimer = 0;
            }
            if (client.world != null && !client.isInSingleplayer())
                Utils.sbChecker();
            if (!discordRPCManager.isConnected && Utils.isSkyblock && SkyblockerConfig.get().general.richPresence.enableRichPresence && client.world != null && !client.isInSingleplayer()) discordRPCManager.start();
            if (discordRPCManager.isConnected && !SkyblockerConfig.get().general.richPresence.enableRichPresence) discordRPCManager.stop();
            if (client.world == null || client.isInSingleplayer() || !Utils.isSkyblock) if (discordRPCManager.isConnected)discordRPCManager.stop();
            ticks = 0;
        }
    }
}