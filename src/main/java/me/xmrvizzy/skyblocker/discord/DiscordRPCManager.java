package me.xmrvizzy.skyblocker.discord;

import com.google.gson.JsonObject;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.time.OffsetDateTime;

public class DiscordRPCManager implements IPCListener{
    public static long startTimestamp;
    public static IPCClient client;
    public boolean isConnected;
    public static final Logger logger = LoggerFactory.getLogger("Skyblocker DiscordRPC");
    public static DecimalFormat dFormat = new DecimalFormat("###,###.##");

    public void start(){
        try {
            logger.info("Starting...");
            startTimestamp = OffsetDateTime.now().toEpochSecond();
            client = new IPCClient(934607927837356052L);
            client.setListener(this);
            try {
                client.connect();
            } catch (Exception e) {
                logger.warn("Failed to connect: " + e.getMessage());
            }
        } catch (Throwable ex) {
            logger.error("unexpected error occurred while trying to start...");
            ex.printStackTrace();
        }
    }

    public void updatePresence(){
        RichPresence presence = new RichPresence.Builder()
                .setState(SkyblockerConfig.get().general.richPresence.customMessage)
                .setDetails(getInfo())
                .setStartTimestamp(startTimestamp)
                .setLargeImage("skyblocker-default")
                .build();
        if (client != null && isConnected) client.sendRichPresence(presence);
    }

    public String getInfo(){
        String info = null;
        if (SkyblockerConfig.get().general.richPresence.info == SkyblockerConfig.Info.BITS) info = "Bits: " + dFormat.format(Utils.getBits());
        if (SkyblockerConfig.get().general.richPresence.info == SkyblockerConfig.Info.PURSE) info = "Purse: " + dFormat.format(Utils.getPurse());
        if (SkyblockerConfig.get().general.richPresence.info == SkyblockerConfig.Info.LOCATION) info = "⏣ " + Utils.getLocation();
        return info;
    }

    public void stop(){
        logger.info("Closing...");
        isConnected = false;
        client.close();
        client = null;
    }

    @Override
    public void onReady(IPCClient client) {
        logger.info("Started!");
        isConnected = true;
    }

    @Override
    public void onClose(IPCClient client, JsonObject json) {
        logger.info("Closed");
        isConnected = false;
    }

}
