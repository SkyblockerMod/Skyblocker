package me.xmrvizzy.skyblocker;

import me.xmrvizzy.skyblocker.config.SkyblockerConfigWrapper;
import me.xmrvizzy.skyblocker.container.ContainerSolverManager;
import me.xmrvizzy.skyblocker.discord.DiscordRPCManager;
import me.xmrvizzy.skyblocker.skyblock.BackpackPreview;
import me.xmrvizzy.skyblocker.skyblock.StatusBarTracker;
import me.xmrvizzy.skyblocker.skyblock.dungeon.DungeonBlaze;
import me.xmrvizzy.skyblocker.skyblock.dwarven.DwarvenHud;
import me.xmrvizzy.skyblocker.utils.Scheduler;
import me.xmrvizzy.skyblocker.utils.Utils;

public class SkyblockerMod {
    public static final String NAMESPACE = "skyblocker";
    private static final SkyblockerMod instance = new SkyblockerMod();

    public final Scheduler scheduler = new Scheduler();
    public final ContainerSolverManager containerSolverManager = new ContainerSolverManager();
    public final DiscordRPCManager discordRPCManager = new DiscordRPCManager();
    public final StatusBarTracker statusBarTracker = new StatusBarTracker();
    public final SkyblockerConfigWrapper CONFIG = SkyblockerConfigWrapper.createAndLoad();

    private SkyblockerMod() {
        scheduler.scheduleCyclic(Utils::sbChecker, 20);
        scheduler.scheduleCyclic(discordRPCManager::update, 100);
        scheduler.scheduleCyclic(DungeonBlaze::update, 4);
        scheduler.scheduleCyclic(BackpackPreview::tick, 50);
        scheduler.scheduleCyclic(DwarvenHud::update, 40);
    }

    public static SkyblockerMod getInstance() {
        return instance;
    }

    public void onTick() {
        scheduler.tick();
    }
}
