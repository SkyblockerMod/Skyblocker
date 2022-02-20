package me.xmrvizzy.skyblocker.mixin;

import me.xmrvizzy.skyblocker.chat.ChatParser;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.gui.hud.ChatHudListener;
import net.minecraft.network.MessageType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ChatHudListener.class)
public class ChatHudListenerMixin {

    private final ChatParser parser = new ChatParser();

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    public void onMessage(MessageType messageType, Text message, UUID senderUuid, CallbackInfo ci) {
        if (!Utils.isOnSkyblock)
            return;
        if (parser.shouldFilter(message.getString()))
            ci.cancel();
    }

}
