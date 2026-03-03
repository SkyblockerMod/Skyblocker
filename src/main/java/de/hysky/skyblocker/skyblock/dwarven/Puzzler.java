package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import java.util.regex.Matcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

public class Puzzler extends ChatPatternListener {
	public Puzzler() {
		super("^\\[NPC] Puzzler: ((?:▲|▶|◀|▼){10})$");
	}

	@Override
	public ChatFilterResult state() {
		return SkyblockerConfigManager.get().mining.dwarvenMines.solvePuzzler ? null : ChatFilterResult.PASS;
	}

	@Override
	public boolean onMatch(Component message, Matcher matcher) {
		int x = 181;
		int z = 135;
		for (char c : matcher.group(1).toCharArray()) {
			if (c == '▲') z++;
			else if (c == '▼') z--;
			else if (c == '◀') x++;
			else if (c == '▶') x--;
		}
		ClientLevel world = Minecraft.getInstance().level;
		if (world != null)
			world.setBlockAndUpdate(new BlockPos(x, 195, z), Blocks.CRIMSON_PLANKS.defaultBlockState());
		return false;
	}
}
