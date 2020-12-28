package me.xmrvizzy.skyblocker.mixin;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
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
    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    public void onChatMessage(MessageType messageType, Text message, UUID senderUuid, CallbackInfo ci) {
        // Ability Cooldown
        if (SkyblockerConfig.get().messages.hideAbility && message.getString().contains("This ability is currently on cooldown for ") || message.getString().contains("No more charges, next one in ")) {
            ci.cancel();
        }

        // Heal Message
        if (SkyblockerConfig.get().messages.hideHeal && message.getString().contains("You healed ") && message.getString().contains(" health!") || message.getString().contains(" healed you for ")) {
            ci.cancel();
        }

        // AOTE
        if (SkyblockerConfig.get().messages.hideAOTE && message.getString().contains("There are blocks in the way!")) {
            ci.cancel();
        }

        // Midas Staff
        if (SkyblockerConfig.get().messages.hideMidasStaff && message.getString().contains("Your Molten Wave hit ")) {
            ci.cancel();
        }
    }

}