package me.xmrvizzy.skyblocker.skyblock.dungeon;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.chat.ChatFilterResult;
import me.xmrvizzy.skyblocker.chat.ChatPatternListener;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reparty extends ChatPatternListener {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    public static final Pattern PLAYER = Pattern.compile(" ([a-zA-Z0-9_]{2,16}) ●");
    private static final int BASE_DELAY = 10;

    private String[] players;
    private int playersSoFar;
    private boolean repartying;
    private String partyLeader;

    public Reparty() {
        super("^(?:You are not currently in a party\\." +
                "|Party (?:Membe|Moderato)rs(?: \\(([0-9]+)\\)|:( .*))" +
                "|([\\[A-z+\\]]* )?(?<disband>.*) has disbanded .*" +
                "|.*\n([\\[A-z+\\]]* )?(?<invite>.*) has invited you to join their party!" +
                "\nYou have 60 seconds to accept. Click here to join!\n.*)$");

        this.repartying = false;
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("rp").executes(context -> {
            if (!Utils.isOnSkyblock() || this.repartying || client.player == null) return 0;
            this.repartying = true;
            SkyblockerMod.getInstance().messageScheduler.sendMessageAfterCooldown("/p list");
            return 0;
        })));
    }

    @Override
    public ChatFilterResult state() {
        return (SkyblockerConfig.get().general.acceptReparty || this.repartying) ? ChatFilterResult.FILTER : ChatFilterResult.PASS;
    }

    @Override
    public boolean onMatch(Text message, Matcher matcher) {
        if (matcher.group(1) != null && repartying) {
            this.playersSoFar = 0;
            this.players = new String[Integer.parseInt(matcher.group(1)) - 1];
        } else if (matcher.group(2) != null && repartying) {
            Matcher m = PLAYER.matcher(matcher.group(2));
            while (m.find()) {
                this.players[playersSoFar++] = m.group(1);
            }
        } else if (matcher.group("disband") != null && !matcher.group("disband").equals(client.getSession().getUsername())) {
            partyLeader = matcher.group("disband");
            SkyblockerMod.getInstance().scheduler.schedule(() -> partyLeader = null, 61);
            return false;
        } else if (matcher.group("invite") != null && matcher.group("invite").equals(partyLeader)) {
            String command = "/party accept " + partyLeader;
            sendCommand(command, 0);
            return false;
        } else {
            this.repartying = false;
            return false;
        }
        if (this.playersSoFar == this.players.length) {
            reparty();
        }
        return false;
    }

    private void reparty() {
        ClientPlayerEntity playerEntity = client.player;
        if (playerEntity == null) {
            this.repartying = false;
            return;
        }
        sendCommand("/p disband", 1);
        for (int i = 0; i < this.players.length; ++i) {
            String command = "/p invite " + this.players[i];
            sendCommand(command, i + 2);
        }
        SkyblockerMod.getInstance().scheduler.schedule(() -> this.repartying = false, this.players.length + 2);
    }

    private void sendCommand(String command, int delay) {
        SkyblockerMod.getInstance().messageScheduler.queueMessage(command, delay * BASE_DELAY);
    }
}