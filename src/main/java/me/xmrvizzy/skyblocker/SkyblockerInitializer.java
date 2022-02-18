package me.xmrvizzy.skyblocker;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.HotbarSlotLock;
import me.xmrvizzy.skyblocker.skyblock.item.PriceInfoTooltip;
import me.xmrvizzy.skyblocker.skyblock.item.WikiLookup;
import me.xmrvizzy.skyblocker.skyblock.itemlist.ItemRegistry;
import net.fabricmc.api.ClientModInitializer;

public class SkyblockerInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SkyblockerConfig.init();
        HotbarSlotLock.init();
        PriceInfoTooltip.init();
        WikiLookup.init();
        ItemRegistry.init();
    }
}
