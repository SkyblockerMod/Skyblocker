package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.events.ChatEvents;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MessageHandler.class, priority = 600) //Inject before the default of 1000 so it bypasses fabric's injections
public class MessageHandlerMixin {
	@Inject(method = "onGameMessage", at = @At("HEAD"))
	private void skyblocker$monitorGameMessage(Text message, boolean overlay, CallbackInfo ci) {
		String stripped = Formatting.strip(message.getString());
		if (overlay) {
			ChatEvents.RECEIVE_OVERLAY_TEXT.invoker().onMessage(message);
			ChatEvents.RECEIVE_OVERLAY_STRING.invoker().onMessage(stripped);
		} else {
			ChatEvents.RECEIVE_TEXT.invoker().onMessage(message);
			ChatEvents.RECEIVE_STRING.invoker().onMessage(stripped);
		}
	}
}
