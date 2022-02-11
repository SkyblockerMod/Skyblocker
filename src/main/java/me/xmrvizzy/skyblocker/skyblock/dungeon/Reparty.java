package me.xmrvizzy.skyblocker.skyblock.dungeon;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.chat.ChatListener;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reparty extends ChatListener {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final SkyblockerMod skyblocker = SkyblockerMod.getInstance();
    public static final Pattern PLAYER = Pattern.compile(" ([a-zA-Z0-9_]{2,16}) ●");
    private static final int BASE_DELAY = 20;

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
            reparty();
        return false;
    }

    private void reparty() {
        ClientPlayerEntity playerEntity = client.player;
        assert playerEntity != null;
        sendCommand(playerEntity, "/p disband", 1);
        StringBuilder sb = new StringBuilder();
        int invites = (players.length - 1) / 5 + 1;
        for(int i = 0; i < invites; i++) {
            sb.setLength(0);
            sb.append("/p invite");
            for(int j = 0; j < 5 && i * 5 + j < players.length; j++) {
                sb.append(' ');
                sb.append(players[i * 5 + j]);
            }
            sendCommand(playerEntity, sb.toString(), i + 2);
        }
        skyblocker.generalScheduler.schedule(() -> repartying = false, invites + 2);
    }

    private void sendCommand(ClientPlayerEntity player, String command, int delay) {
        skyblocker.generalScheduler.schedule(() ->
                player.sendChatMessage(command), delay * BASE_DELAY
        );
    }
}
