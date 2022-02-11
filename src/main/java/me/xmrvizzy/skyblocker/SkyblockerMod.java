package me.xmrvizzy.skyblocker;

import me.xmrvizzy.skyblocker.container.ContainerSolverManager;
import me.xmrvizzy.skyblocker.discord.DiscordRPCManager;
import me.xmrvizzy.skyblocker.skyblock.dungeon.DungeonBlaze;
import me.xmrvizzy.skyblocker.utils.Utils;

public class SkyblockerMod {
    public static final String NAMESPACE = "skyblocker";
    private static final SkyblockerMod instance = new SkyblockerMod();

    public final Scheduler generalScheduler = new Scheduler();
    public final Scheduler sbScheduler = new Scheduler();
    public final ContainerSolverManager containerSolverManager = new ContainerSolverManager();
    public final DiscordRPCManager discordRPCManager = new DiscordRPCManager();

    private SkyblockerMod() {
        generalScheduler.scheduleCyclic(Utils::sbChecker, 20);
        generalScheduler.scheduleCyclic(discordRPCManager::update, 100);
        sbScheduler.scheduleCyclic(DungeonBlaze::update, 4);
    }

    public static SkyblockerMod getInstance() {
        return instance;
    }

    public void onTick() {
        generalScheduler.tick();
        if (Utils.isSkyblock) sbScheduler.tick();
    }
}
