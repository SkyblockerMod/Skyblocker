package me.xmrvizzy.skyblocker.skyblock.dwarven;

import me.xmrvizzy.skyblocker.chat.ChatListener;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class Puzzler extends ChatListener {
    public Puzzler() {
        super("^§e\\[NPC] §dPuzzler§f: ((?:§d▲|§5▶|§b◀|§a▼){10})$");
    }

    @Override
    public boolean isEnabled() {
        return SkyblockerConfig.get().locations.dwarvenMines.solvePuzzler;
    }

    @Override
    public boolean onMessage(String[] groups) {
        int x = 0;
        int z = 0;
        for (char c : groups[1].toCharArray()) {
            switch (c) {
                case '▲': z += 1;
                case '▶': x -= 1;
                case '▼': z -= 1;
                case '◀': x += 1;
            }
        }
        StringBuilder message = new StringBuilder("§e[NPC] §dPuzzler§f: ");
        if(z > 0) {
            message.append("§d");
            message.append(z);
            message.append("▲");
        }
        else if (z < 0) {
            message.append("§a▼");
            message.append(-z);
            message.append("▼");
        }
        if(x > 0) {
            message.append("§b");
            message.append(x);
            message.append("◀");
        }
        else if (x < 0) {
            message.append("§5▶");
            message.append(-x);
            message.append("▶");
        }

        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        client.player.sendMessage(Text.of(message.toString()), false);
        return true;
    }
}