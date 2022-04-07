package me.xmrvizzy.skyblocker.skyblock.dwarven;

import me.xmrvizzy.skyblocker.chat.ChatFilterResult;
import me.xmrvizzy.skyblocker.chat.ChatPatternListener;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.regex.Matcher;

public class Puzzler extends ChatPatternListener {
    public Puzzler() {
        super("^§e\\[NPC] §dPuzzler§f: ((?:§d▲|§5▶|§b◀|§a▼){10})$");
    }

    @Override
    public ChatFilterResult state() {
        return SkyblockerConfig.get().locations.dwarvenMines.solvePuzzler ? null : ChatFilterResult.PASS;
    }

    @Override
    public boolean onMatch(Text message, Matcher matcher) {
        int x = 181;
        int z = 135;
        for (char c : matcher.group(1).toCharArray()) {
            if (c == '▲') z++;
            else if (c == '▼') z--;
            else if (c == '◀') x++;
            else if (c == '▶') x--;
        }
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world != null)
            world.setBlockStateWithoutNeighborUpdates(new BlockPos(x, 195, z), Blocks.CRIMSON_PLANKS.getDefaultState());
        return false;
    }
}