package me.xmrvizzy.skyblocker.utils;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.RichPresenceButton;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;


public class Discord {
    public static Logger logger = LoggerFactory.getLogger(SkyblockerMod.NAMESPACE);
    public static IPCClient ipcClient = new IPCClient(934607927837356052L);
    public static boolean connected = false;
    public static boolean warned = false;
    public static DecimalFormat dFormat = new DecimalFormat("###,###.##");


    public static void updatePresence(String state, String details){
        logger.debug("updatePresence");
        RichPresence.Builder builder = new RichPresence.Builder();
        RichPresenceButton[] button = new RichPresenceButton[0];
        builder.setState(state)
                .setDetails(details)
                .setButtons(button)
                .setLargeImage("skyblocker-default");
            ipcClient.sendRichPresence(builder.build());
    }

    public static String getInfo(){
        String info = null;
        if (SkyblockerConfig.get().general.richPresence.info == SkyblockerConfig.Info.BITS) info = "Bits: " + Utils.getBits();
        if (SkyblockerConfig.get().general.richPresence.info == SkyblockerConfig.Info.PURSE) info = "Purse: " + dFormat.format(Utils.getPurse());
        if (SkyblockerConfig.get().general.richPresence.info == SkyblockerConfig.Info.LOCATION) info = "⏣ " + Utils.getLocation();
        return info;
    }

    public static void stop(){
        ipcClient.close();
        ipcClient = null;
        connected = false;
    }

    public static void update(){
        if (Utils.isSkyblock && SkyblockerConfig.get().general.richPresence.enableRichPresence){

            if (!connected){
                try {
                    ipcClient = new IPCClient(934607927837356052L);
                    ipcClient.connect();
                    connected = true;

                } catch (Exception e) {
                    if (!warned){
                        if (e.getLocalizedMessage().equals("java.net.SocketException: Connection refused"))
                            logger.warn("Discord client not running");
                        warned = true;
                    }
            }
        }

        ipcClient.setListener(new IPCListener() {
            @Override
            public void onDisconnect(IPCClient client, Throwable t) {
                IPCListener.super.onDisconnect(client, t);
                connected = false;
            }
        });
    }
}}
