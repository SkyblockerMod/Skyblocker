package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.events.ChatEvents;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MessageHandler.class, priority = 400) //Inject before the default of 1000 so it bypasses fabric's injections and some from other mods
public class MessageHandlerMixin {
	@Inject(method = "onGameMessage", at = @At("HEAD"))
	private void skyblocker$monitorGameMessage(Text message, boolean overlay, CallbackInfo ci) {
		if (overlay) return; //Can add overlay-specific events in the future or incorporate it into the existing events. For now, it's not necessary.
		ChatEvents.RECEIVE_TEXT.invoker().onMessage(message);
		ChatEvents.RECEIVE_STRING.invoker().onMessage(message.getString());
	}
}
