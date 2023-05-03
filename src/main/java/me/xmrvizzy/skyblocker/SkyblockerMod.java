package me.xmrvizzy.skyblocker;

import me.xmrvizzy.skyblocker.gui.ContainerSolverManager;
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
    public final StatusBarTracker statusBarTracker = new StatusBarTracker();

    private SkyblockerMod() {
        scheduler.scheduleCyclic(Utils::sbChecker, 20);
        scheduler.scheduleCyclic(DiscordRPCManager::update, 100);
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
