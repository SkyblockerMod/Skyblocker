package me.xmrvizzy.skyblocker.skyblock.dwarven;

import me.xmrvizzy.skyblocker.chat.ChatListener;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public class Puzzler extends ChatListener {

    @Override
    public boolean isEnabled() {
        return SkyblockerConfig.get().locations.dwarvenMines.solvePuzzler;
    }

    @Override
    public boolean onMessage(String message) {
        if (message.contains("§e[NPC] §dPuzzler§f: ")) {
            if (message.contains("Nice!") || message.contains("tomorrow")) {
                return false;
            }

            message = message.replace("§e[NPC] §dPuzzler§f: ", "");

            int x = 181;
            int z = 135;

            for (char c : message.toCharArray()) {
                if (c == '▲') z++;
                else if (c == '▼') z--;
                else if (c == '◀') x++;
                else if (c == '▶') x--;
            }

            ClientWorld world = MinecraftClient.getInstance().world;
            if (world == null) {
                throw new RuntimeException("[Skyblocker] world cannot be null!");
            }

            world.setBlockStateWithoutNeighborUpdates(new BlockPos(x, 195, z), Blocks.CRIMSON_PLANKS.getDefaultState());
        }
        return false;
    }
}