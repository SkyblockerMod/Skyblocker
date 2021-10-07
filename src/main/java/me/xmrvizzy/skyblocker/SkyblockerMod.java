package me.xmrvizzy.skyblocker;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.HotbarSlotLock;
import me.xmrvizzy.skyblocker.skyblock.dungeon.DungeonBlaze;
import me.xmrvizzy.skyblocker.skyblock.item.PriceInfoTooltip;
import me.xmrvizzy.skyblocker.utils.RenderUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public class SkyblockerMod implements ClientModInitializer {
    public static final String NAMESPACE = "skyblocker";
    private static int TICKS = 0;

    @Override
    public void onInitializeClient() {
        HotbarSlotLock.init();
        SkyblockerConfig.init();
        PriceInfoTooltip.init();
    }


    public static void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        TICKS++;
        if (TICKS % 4 == 0)
            try {
                if (Utils.isDungeons) {
                    DungeonBlaze.DungeonBlaze();
                }
            } catch (Exception e) {
                //System.out.println("Blazesolver: " + e);
            }
        if (TICKS % 20 == 0) {
            if (client.world != null && !client.isInSingleplayer())
                Utils.sbChecker();

            TICKS = 0;
        }
    }
}