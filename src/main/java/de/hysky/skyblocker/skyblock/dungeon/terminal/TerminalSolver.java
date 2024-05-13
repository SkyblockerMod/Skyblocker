package de.hysky.skyblocker.skyblock.dungeon.terminal;

import de.hysky.skyblocker.config.SkyblockerConfigManager;

public interface TerminalSolver {

	default boolean shouldBlockIncorrectClicks() {
		return SkyblockerConfigManager.get().dungeons.terminals.blockIncorrectClicks;
	}
}
