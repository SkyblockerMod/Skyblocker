package me.xmrvizzy.skyblocker.utils.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.ActionResult;

public interface SkyblockJoinCallback {
    Event<SkyblockJoinCallback> EVENT = EventFactory.createArrayBacked(SkyblockJoinCallback.class,
            (listeners) -> (handler, sender, client) -> {
                for (SkyblockJoinCallback listener : listeners) {
                    ActionResult result = listener.join(handler, sender, client);

                    if(result != ActionResult.PASS) {
                        return result;
                    }
                }
                return ActionResult.PASS;
            });

    ActionResult join(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client);
}
