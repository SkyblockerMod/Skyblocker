package me.xmrvizzy.skyblocker.mixin;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.dungeon.DungeonPuzzles;
import me.xmrvizzy.skyblocker.skyblock.dwarven.Puzzler;
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

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    public void onMessage(MessageType messageType, Text message, UUID senderUuid, CallbackInfo ci) {
        String msg = message.getString();

        if (Utils.isDungeons) {
            if (SkyblockerConfig.get().locations.dungeons.solveThreeWeirdos && msg.contains("[NPC]"))
                DungeonPuzzles.threeWeirdos(msg);
        }

        if (Utils.isSkyblock) {
            if (SkyblockerConfig.get().locations.dwarvenMines.solvePuzzler && msg.contains("[NPC]") && msg.contains("Puzzler"))
                Puzzler.puzzler(msg);

            if (SkyblockerConfig.get().messages.hideAbility && msg.contains("This ability is currently on cooldown for ") || msg.contains("No more charges, next one in "))
                ci.cancel();

            if (SkyblockerConfig.get().messages.hideHeal && msg.contains("You healed ") && msg.contains(" health!") || msg.contains(" healed you for "))
                ci.cancel();

            if (SkyblockerConfig.get().messages.hideAOTE && msg.contains("There are blocks in the way!"))
                ci.cancel();

            if (SkyblockerConfig.get().messages.hideImplosion && msg.contains("Your Implosion hit "))
                ci.cancel();

            if (SkyblockerConfig.get().messages.hideMoltenWave && msg.contains("Your Molten Wave hit "))
                ci.cancel();
        }
    }

}