package me.xmrvizzy.skyblocker.skyblock.dwarven;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class Puzzler {
    public static void puzzler(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null && client.world == null) return;

        int x = 181;
        int y = 195;
        int z = 135;

        String path = Formatting.strip(message);
        path = path.substring(path.indexOf(":") + 2);
        String check = path
                .replaceAll("▲", "").replaceAll("▶", "")
                .replaceAll("▼", "").replaceAll("◀", "");

        if (check.isEmpty()) {
            for (char c : path.toCharArray()) {
                if (c == '▲') z += 1;
                if (c == '▶') x -= 1;
                if (c == '▼') z -= 1;
                if (c == '◀') x += 1;
            }

            client.world.setBlockState(new BlockPos(x, y, z), Blocks.EMERALD_BLOCK.getDefaultState());
        }
    }
}