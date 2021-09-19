package me.xmrvizzy.skyblocker.mixin;

import me.xmrvizzy.skyblocker.chat.ChatParser;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHudListener;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.MessageType;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(ChatHudListener.class)
public class ChatHudListenerMixin {

    private final ChatParser parser = new ChatParser();

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    public void onMessage(MessageType messageType, Text message, UUID senderUuid, CallbackInfo ci) {
        if (!Utils.isSkyblock)
            return;
        if (parser.shouldFilter(message.getString()))
            ci.cancel();

        if(SkyblockerConfig.get().general.autoOpenSlayer) {
            List<Text> siblings = message.getSiblings();
            if (siblings.size() == 3) {
                Text sibling = siblings.get(2);
                ClickEvent clickEvent = sibling.getStyle().getClickEvent();
                if(sibling.asString().equals("§2§l[OPEN MENU]") && clickEvent != null) {
                    ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
                    assert clientPlayerEntity != null;
                    MinecraftClient.getInstance().player.sendChatMessage(clickEvent.getValue());
                }
            }
        }
    }

}
