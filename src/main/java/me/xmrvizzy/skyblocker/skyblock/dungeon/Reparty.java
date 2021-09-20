package me.xmrvizzy.skyblocker.skyblock.dungeon;

import me.xmrvizzy.skyblocker.chat.ChatListener;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reparty extends ChatListener {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Logger logger = LogManager.getLogger(Reparty.class.getName());
    public static final Pattern PLAYER = Pattern.compile(" ([a-zA-Z0-9_]{2,16}) ●");

    private String[] players;
    private int playersSoFar;
    private boolean repartying;

    public Reparty() {
        super("^(?:You are not currently in a party\\.|Party (?:Membe|Moderato)rs(?: \\(([0-9]+)\\)|:( .*)))$");
        repartying = false;
        ClientCommandManager.DISPATCHER.register(
            ClientCommandManager.literal("rp").executes(context -> {
                if(!Utils.isSkyblock)
                    return 0;
                assert client.player != null;
                repartying = true;
                client.player.sendChatMessage("/p list");
                return 0;
            })
        );
    }

    @Override
    public boolean isEnabled() {
        return repartying;
    }

    @Override
    public boolean onMessage(String[] groups) {
        if(groups[1] != null) {
            playersSoFar = 0;
            players = new String[Integer.parseInt(groups[1]) - 1];
        }
        else if(groups[2] != null) {
            Matcher m = PLAYER.matcher(groups[2]);
            while(m.find()) {
                players[playersSoFar++] = m.group(1);
            }
        }
        else
            repartying = false;
        if(playersSoFar == players.length)
            client.execute(this::reparty);
        return false;
    }

    private void reparty() {
        ClientPlayerEntity playerEntity = client.player;
        assert playerEntity != null;
        playerEntity.sendChatMessage("/p disband");
        for(String player : players) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                logger.info("[Skyblocker] sleep while repartying interupted!");
            }
            playerEntity.sendChatMessage("/p invite " + player);
        }
        repartying = false;
    }
}
