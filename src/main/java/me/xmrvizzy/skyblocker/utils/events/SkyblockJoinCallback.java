package me.xmrvizzy.skyblocker.utils.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

public interface SkyblockJoinCallback {
    Event<SkyblockJoinCallback> EVENT = EventFactory.createArrayBacked(SkyblockJoinCallback.class,
            (listeners) -> () -> {
                for (SkyblockJoinCallback listener : listeners) {
                    ActionResult result = listener.join();

                    if(result != ActionResult.PASS) {
                        return result;
                    }
                }
                return ActionResult.PASS;
            });

    ActionResult join();
}
