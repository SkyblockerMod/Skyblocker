package me.xmrvizzy.skyblocker.mixin;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHudListener;
import net.minecraft.network.MessageType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ChatHudListener.class)
public class ChatHudListenerMixin {
    private static String[] threeWeirdos = {
            "The reward is not in my chest!",
            "At least one of them is lying, and the reward is not in",
            "My chest doesn't have the reward. We are all telling the truth.",
            "My chest has the reward and I'm telling the truth!",
            "The reward isn't in any of our chests.",
            "Both of them are telling the truth. Also,"
    };

    @ModifyVariable(method = "onChatMessage(Lnet/minecraft/network/MessageType;Lnet/minecraft/text/Text;Ljava/util/UUID;)V", at = @At("HEAD"), ordinal = 0)
    public Text modifyMessage(Text message) {
        String msg = message.getString();

        if (SkyblockerConfig.get().dungeons.solveThreeWeirdos && msg.contains("[NPC]")) {
            for (String s : threeWeirdos) {
                if (msg.contains(s)) {
                    return Text.of(msg.replaceFirst("§c", "§a"));
                }
            }
        }

        return message;
    }

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    public void onMessage(MessageType messageType, Text message, UUID senderUuid, CallbackInfo ci) {
        if (SkyblockerConfig.get().messages.hideAbility && message.getString().contains("This ability is currently on cooldown for ") || message.getString().contains("No more charges, next one in "))
            ci.cancel();

        if (SkyblockerConfig.get().messages.hideHeal && message.getString().contains("You healed ") && message.getString().contains(" health!") || message.getString().contains(" healed you for "))
            ci.cancel();

        if (SkyblockerConfig.get().messages.hideAOTE && message.getString().contains("There are blocks in the way!"))
            ci.cancel();

        if (SkyblockerConfig.get().messages.hideImplosion && message.getString().contains("Your Implosion hit "))
            ci.cancel();

        if (SkyblockerConfig.get().messages.hideMoltenWave && message.getString().contains("Your Molten Wave hit "))
            ci.cancel();
    }

}