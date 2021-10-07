package me.xmrvizzy.skyblocker;

import me.xmrvizzy.skyblocker.skyblock.dungeon.DungeonBlaze;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;

public class SkyblockerMod {
    public static final String NAMESPACE = "skyblocker";
    private static final SkyblockerMod instance = new SkyblockerMod();

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

            ticks = 0;
        }
    }
}