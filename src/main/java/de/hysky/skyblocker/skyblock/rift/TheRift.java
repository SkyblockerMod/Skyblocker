package de.hysky.skyblocker.skyblock.rift;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.ChatEvents;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class TheRift {
    @Init
    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(MirrorverseWaypoints::render);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(EffigyWaypoints::render);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(EnigmaSouls::render);
        ClientLifecycleEvents.CLIENT_STARTED.register(MirrorverseWaypoints::load);
        ClientLifecycleEvents.CLIENT_STARTED.register(EnigmaSouls::load);
        ClientLifecycleEvents.CLIENT_STOPPING.register(EnigmaSouls::save);
        ChatEvents.RECEIVE_STRING.register(EnigmaSouls::onMessage);
        ClientCommandRegistrationCallback.EVENT.register(EnigmaSouls::registerCommands);
        Scheduler.INSTANCE.scheduleCyclic(EffigyWaypoints::updateEffigies, SkyblockerConfigManager.get().slayers.vampireSlayer.effigyUpdateFrequency);
    }
}
