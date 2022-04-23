package me.xmrvizzy.skyblocker.mixin;

import me.xmrvizzy.skyblocker.chat.ChatFilterResult;
import me.xmrvizzy.skyblocker.chat.ChatMessageListener;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHudListener;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.MessageType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ChatHudListener.class)
public class ChatHudListenerMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    public void onMessage(MessageType messageType, Text message, UUID senderUuid, CallbackInfo ci) {
        if (!Utils.isOnSkyblock)
            return;
        String asString = message.getString();
        ChatFilterResult result = ChatMessageListener.EVENT.invoker().onMessage(message, asString);
        switch (result) {
            case ACTION_BAR:
                ClientPlayerEntity player = client.player;
                if (player != null)
                    player.sendMessage(message, true);
            case FILTER:
                ci.cancel();
        }
    }

}
