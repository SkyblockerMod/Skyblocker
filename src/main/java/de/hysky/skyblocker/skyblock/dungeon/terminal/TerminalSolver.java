package de.hysky.skyblocker.skyblock.dungeon.terminal;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.container.ContainerSolver;

public sealed interface TerminalSolver permits ColorTerminal, LightsOnTerminal, OrderTerminal, SameColorTerminal, StartsWithTerminal {
	default boolean shouldBlockIncorrectClicks() {
		return SkyblockerConfigManager.get().dungeons.terminals.blockIncorrectClicks;
	}

	default boolean shouldMiddleClick() {
		return SkyblockerConfigManager.get().dungeons.terminals.middleClick;
	}

	default ContainerSolver.SlotClickResult allow() {
		return shouldMiddleClick() ? ContainerSolver.SlotClickResult.ALLOW_MIDDLE_CLICK : ContainerSolver.SlotClickResult.ALLOW;
	}

	default ContainerSolver.SlotClickResult blockOrClick() {
		return shouldBlockIncorrectClicks() ? ContainerSolver.SlotClickResult.CANCEL : allow();
	}
}
