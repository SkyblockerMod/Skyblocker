package me.xmrvizzy.skyblocker;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.container.ContainerSolverManager;
import me.xmrvizzy.skyblocker.skyblock.dungeon.DungeonBlaze;
import me.xmrvizzy.skyblocker.utils.Discord;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;

public class SkyblockerMod {
    public static final String NAMESPACE = "skyblocker";
    private static final SkyblockerMod instance = new SkyblockerMod();
    public final ContainerSolverManager containerSolverManager = new ContainerSolverManager();

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
            if (client.world != null && !client.isInSingleplayer())
                Utils.sbChecker();
            Discord.update();
            if (Discord.connected){
                if (SkyblockerConfig.get().general.richPresence.enableRichPresence) Discord.updatePresence(Discord.getInfo(), SkyblockerConfig.get().general.richPresence.customMessage);
                if (!SkyblockerConfig.get().general.richPresence.enableRichPresence || !Utils.isSkyblock || client.world == null) Discord.stop();
            }
            ticks = 0;
        }
    }
}