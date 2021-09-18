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
        if (Utils.isSkyblock && parser.shouldFilter(message.getString()))
            ci.cancel();

        /*
        String msg = message.getString();

        if (Utils.isDungeons) {
            if (SkyblockerConfig.get().locations.dungeons.solveThreeWeirdos && msg.contains("[NPC]"))
                DungeonPuzzles.threeWeirdos(msg);
            
            DungeonPuzzles.trivia(msg, ci);
        }

        if (Utils.isSkyblock) {
            if (msg.contains("[OPEN MENU]")) {
                List<Text> siblings = message.getSiblings();
                for (Text sibling : siblings) {
                    if (sibling.getString().contains("[OPEN MENU]")) {
                        this.client.player.sendChatMessage(sibling.getStyle().getClickEvent().getValue());
                    }
                }
            }

            if (msg.contains("[NPC]")) {
                if (SkyblockerConfig.get().locations.dwarvenMines.solveFetchur &&
                        msg.contains("Fetchur")) {
                    Fetchur.solve(msg, ci);
                }

                if (SkyblockerConfig.get().locations.dwarvenMines.solvePuzzler &&
                        msg.contains("Puzzler"))
                    Puzzler.solve(msg);
            }

            if(filter.shouldFilter(msg))
                ci.cancel();
        }
        */
    }

}
