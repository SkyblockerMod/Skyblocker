package me.xmrvizzy.skyblocker;

import me.xmrvizzy.skyblocker.chat.ChatMessageListener;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.discord.DiscordRPCManager;
import me.xmrvizzy.skyblocker.gui.ContainerSolverManager;
import me.xmrvizzy.skyblocker.skyblock.BackpackPreview;
import me.xmrvizzy.skyblocker.skyblock.HotbarSlotLock;
import me.xmrvizzy.skyblocker.skyblock.StatusBarTracker;
import me.xmrvizzy.skyblocker.skyblock.api.RepositoryUpdate;
import me.xmrvizzy.skyblocker.skyblock.api.StatsCommand;
import me.xmrvizzy.skyblocker.skyblock.dungeon.DungeonBlaze;
import me.xmrvizzy.skyblocker.skyblock.dwarven.DwarvenHud;
import me.xmrvizzy.skyblocker.skyblock.item.PriceInfoTooltip;
import me.xmrvizzy.skyblocker.skyblock.item.WikiLookup;
import me.xmrvizzy.skyblocker.skyblock.itemlist.ItemRegistry;
import me.xmrvizzy.skyblocker.skyblock.quicknav.QuickNav;
import me.xmrvizzy.skyblocker.utils.Scheduler;
import me.xmrvizzy.skyblocker.utils.UpdateChecker;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class SkyblockerMod implements ClientModInitializer {
    public static final String NAMESPACE = "skyblocker";
    private static SkyblockerMod INSTANCE;

    public final Scheduler scheduler = new Scheduler();
    public final ContainerSolverManager containerSolverManager = new ContainerSolverManager();
    public final StatusBarTracker statusBarTracker = new StatusBarTracker();

    public SkyblockerMod() {
        INSTANCE = this;
    }

    public static SkyblockerMod getInstance() {
        return INSTANCE;
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        HotbarSlotLock.init();
        SkyblockerConfig.init();
        PriceInfoTooltip.init();
        WikiLookup.init();
        ItemRegistry.init();
        RepositoryUpdate.init();
        ContainerSolverManager.init();
        BackpackPreview.init();
        QuickNav.init();
        StatsCommand.init();
        DwarvenHud.init();
        ChatMessageListener.init();
        UpdateChecker.init();
        DiscordRPCManager.init();
        scheduler.scheduleCyclic(Utils::sbChecker, 20);
        scheduler.scheduleCyclic(DiscordRPCManager::update, 100);
        scheduler.scheduleCyclic(DungeonBlaze::update, 4);
        scheduler.scheduleCyclic(BackpackPreview::tick, 50);
        scheduler.scheduleCyclic(DwarvenHud::update, 40);
    }

    public void tick(MinecraftClient client) {
        scheduler.tick();
    }
}
