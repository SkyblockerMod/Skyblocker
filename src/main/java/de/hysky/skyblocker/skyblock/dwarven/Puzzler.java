package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.regex.Matcher;

public class Puzzler extends ChatPatternListener {
    public Puzzler() {
        super("^\\[NPC] Puzzler: ((?:▲|▶|◀|▼){10})$");
    }

    @Override
    public ChatFilterResult state() {
        return SkyblockerConfigManager.get().locations.dwarvenMines.solvePuzzler ? null : ChatFilterResult.PASS;
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
            world.setBlockState(new BlockPos(x, 195, z), Blocks.CRIMSON_PLANKS.getDefaultState());
        return false;
    }
}