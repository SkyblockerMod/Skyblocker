package me.xmrvizzy.skyblocker.skyblock.dwarven;

import me.xmrvizzy.skyblocker.chat.ChatListener;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

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
        int x = 181;
        int z = 135;
        for (char c : groups[1].toCharArray()) {
            if (c == '▲') z++;
            else if (c == '▼') z--;
            else if (c == '◀') x++;
            else if (c == '▶') x--;
        }
        ClientWorld world = MinecraftClient.getInstance().world;
        assert world != null;
        world.setBlockStateWithoutNeighborUpdates(new BlockPos(x, 195, z), Blocks.CRIMSON_PLANKS.getDefaultState());
        return false;
    }
}