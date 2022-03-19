package me.xmrvizzy.skyblocker.utils.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

public interface SkyblockLeaveCallback {
    Event<SkyblockLeaveCallback> EVENT = EventFactory.createArrayBacked(SkyblockLeaveCallback.class,
            (listeners) -> () -> {
                for (SkyblockLeaveCallback listener : listeners) {
                    ActionResult result = listener.leave();

                    if(result != ActionResult.PASS) {
                        return result;
                    }
                }
                return ActionResult.PASS;
            });

    ActionResult leave();
}
