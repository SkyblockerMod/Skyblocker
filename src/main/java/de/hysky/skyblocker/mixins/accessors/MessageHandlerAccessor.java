package de.hysky.skyblocker.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.time.Instant;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;

@Mixin(ChatListener.class)
public interface MessageHandlerAccessor {
	@Invoker
	void invokeLogSystemMessage(Component message, Instant timestamp);
}
