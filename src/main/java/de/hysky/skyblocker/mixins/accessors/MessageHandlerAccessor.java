package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.time.Instant;

@Mixin(MessageHandler.class)
public interface MessageHandlerAccessor {
    @Invoker
    void invokeAddToChatLog(Text message, Instant timestamp);
}
