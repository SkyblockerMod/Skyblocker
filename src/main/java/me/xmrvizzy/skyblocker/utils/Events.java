package me.xmrvizzy.skyblocker.utils;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Events {
    public static MinecraftClient client = MinecraftClient.getInstance();
    public static Logger logger = LoggerFactory.getLogger(SkyblockerMod.NAMESPACE);

    public static void onSkyblockJoin(){
        Utils.isOnSkyblock = true;
        logger.info("Joined Skyblock");
        if (UpdateChecker.shouldUpdate()){
            TranslatableText linkMessage = new TranslatableText("skyblocker.update.update_message");
            TranslatableText linkMessageEnding = new TranslatableText("skyblocker.update.update_message_end");
            TranslatableText link = new TranslatableText("skyblocker.update.update_link");
            TranslatableText hoverText = new TranslatableText("skyblocker.update.hover_text");
            linkMessage.append(link.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/mod/skyblocker-liap/versions")).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)))).append(linkMessageEnding);

            client.player.sendMessage(linkMessage, false);
        }
    }

    public static void onSkyblockDisconnect(){
        logger.info("Disconnected from Skyblock");
        SkyblockerMod.getInstance().discordRPCManager.stop();
        Utils.isOnSkyblock = false;
        Utils.isInDungeons = false;
    }
}
