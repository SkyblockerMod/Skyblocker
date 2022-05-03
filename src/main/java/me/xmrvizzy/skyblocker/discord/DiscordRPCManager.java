package me.xmrvizzy.skyblocker.discord;

import com.google.gson.JsonObject;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.pipe.PipeStatus;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.SkyblockEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.time.OffsetDateTime;

public class DiscordRPCManager implements IPCListener{
    public static long startTimestamp;
    public static IPCClient client;
    public boolean isConnected;
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordRPCManager.class.getName());
    public static DecimalFormat dFormat = new DecimalFormat("###,###.##");
    public int cycleCount = 0;

    public void update() {
        if (!SkyblockerConfig.get().richPresence.enableRichPresence || !Utils.isOnSkyblock) {
            if (isConnected) stop();
            return;
        }
        if (!isConnected) start();
        if (SkyblockerConfig.get().richPresence.cycleMode)
            cycleCount = (cycleCount + 1) % 3;
        updatePresence();
    }

    public void start(){
        try {
            LOGGER.info("[Skyblocker DiscordRPC] Starting...");
            startTimestamp = OffsetDateTime.now().toEpochSecond();
            client = new IPCClient(934607927837356052L);
            client.setListener(this);
            try {
                client.connect();
            } catch (Exception e) {
                LOGGER.warn("Failed to connect: " + e.getMessage());
            }
        } catch (Throwable ex) {
            LOGGER.error("[Skyblocker DiscordRPC] unexpected error occurred while trying to start...");
            ex.printStackTrace();
        }
    }

    public void updatePresence(){
        RichPresence presence = new RichPresence.Builder()
                .setState(SkyblockerConfig.get().richPresence.customMessage)
                .setDetails(getInfo())
                .setStartTimestamp(startTimestamp)
                .setLargeImage("skyblocker-default")
                .build();
        if (client != null && isConnected) client.sendRichPresence(presence);
    }

    public String getInfo(){
        String info = null;
        if (!SkyblockerConfig.get().richPresence.cycleMode){
            switch (SkyblockerConfig.get().richPresence.info){
                case BITS -> info = "Bits: " + dFormat.format(Utils.getBits());
                case PURSE -> info = "Purse: " + dFormat.format(Utils.getPurse());
                case LOCATION -> info = "⏣ " + Utils.getLocation();
            }
        } else if (SkyblockerConfig.get().richPresence.cycleMode){
            switch (cycleCount){
                case 0 -> info = "Bits: " + dFormat.format(Utils.getBits());
                case 1 -> info = "Purse: " + dFormat.format(Utils.getPurse());
                case 2 -> info = "⏣ " + Utils.getLocation();
            }
        }
        return info;
    }

    public void stop(){
        if (client != null && client.getStatus() == PipeStatus.CONNECTED) {
            LOGGER.info("[Skyblocker DiscordRPC] Closing...");
            isConnected = false;
            client.close();
            client = null;
        }
    }

    @Override
    public void onReady(IPCClient client) {
        LOGGER.info("[Skyblocker DiscordRPC] Started!");
        isConnected = true;
    }

    @Override
    public void onClose(IPCClient client, JsonObject json) {
        LOGGER.info("[Skyblocker DiscordRPC] Closed");
        isConnected = false;
    }

    public void init(){
        SkyblockEvents.LEAVE.register(this::stop);
    }

}
