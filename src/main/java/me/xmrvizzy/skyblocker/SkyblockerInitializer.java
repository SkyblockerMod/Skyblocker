package me.xmrvizzy.skyblocker;

import me.xmrvizzy.skyblocker.chat.ChatMessageListener;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.discord.DiscordRPCManager;
import me.xmrvizzy.skyblocker.skyblock.HotbarSlotLock;
import me.xmrvizzy.skyblocker.skyblock.api.RepositoryUpdate;
import me.xmrvizzy.skyblocker.skyblock.api.StatsCommand;
import me.xmrvizzy.skyblocker.skyblock.dwarven.DwarvenHud;
import me.xmrvizzy.skyblocker.skyblock.item.PriceInfoTooltip;
import me.xmrvizzy.skyblocker.skyblock.item.WikiLookup;
import me.xmrvizzy.skyblocker.skyblock.itemlist.ItemRegistry;
import me.xmrvizzy.skyblocker.utils.UpdateChecker;
import net.fabricmc.api.ClientModInitializer;

public class SkyblockerInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HotbarSlotLock.init();
        SkyblockerConfig.init();
        PriceInfoTooltip.init();
        WikiLookup.init();
        ItemRegistry.init();
        RepositoryUpdate.init();
        StatsCommand.init();
        DwarvenHud.init();
        ChatMessageListener.init();
        UpdateChecker.init();
        DiscordRPCManager.init();
    }
}
