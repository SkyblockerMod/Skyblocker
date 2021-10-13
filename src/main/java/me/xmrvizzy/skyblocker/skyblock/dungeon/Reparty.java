package me.xmrvizzy.skyblocker.skyblock.dungeon;

import me.xmrvizzy.skyblocker.chat.ChatListener;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reparty extends ChatListener {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    public static final Pattern PLAYER = Pattern.compile(" ([a-zA-Z0-9_]{2,16}) ●");
    private static final int SLEEP_TIME = 600;

    private String[] players;
    private int playersSoFar;
    private boolean repartying;

    public Reparty() {
        super("^(?:You are not currently in a party\\.|Party (?:Membe|Moderato)rs(?: \\(([0-9]+)\\)|:( .*)))$");
        repartying = false;
        ClientCommandManager.DISPATCHER.register(
                ClientCommandManager.literal("rp").executes(context -> {
                    if (!Utils.isSkyblock || repartying)
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
        if (groups[1] != null) {
            playersSoFar = 0;
            players = new String[Integer.parseInt(groups[1]) - 1];
        } else if (groups[2] != null) {
            Matcher m = PLAYER.matcher(groups[2]);
            while (m.find()) {
                players[playersSoFar++] = m.group(1);
            }
        } else {
            repartying = false;
            return false;
        }
        if (playersSoFar == players.length)
            new Thread(this::reparty).start();
        return false;
    }

    private void reparty() {
        ClientPlayerEntity playerEntity = client.player;
        assert playerEntity != null;
        StringBuilder sb = new StringBuilder("/p disband");
        for (int i = 0; i < players.length; i++) {
            if (i % 5 == 0) {
                sleep();
                playerEntity.sendChatMessage(sb.toString());
                sb.setLength(0);
                sb.append("/p invite");
            }
            sb.append(' ');
            sb.append(players[i]);
        }
        if (players.length % 5 != 0) {
            sleep();
            playerEntity.sendChatMessage(sb.toString());
        }
        repartying = false;
    }

    private void sleep() {
        long sleepStart = System.currentTimeMillis();
        boolean interrupted = false;
        long sleepLeft = SLEEP_TIME;
        do {
            if (interrupted) {
                sleepLeft = sleepStart + SLEEP_TIME - System.currentTimeMillis();
                interrupted = false;
            }
            try {
                Thread.sleep(sleepLeft);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        } while (interrupted);
    }
}
