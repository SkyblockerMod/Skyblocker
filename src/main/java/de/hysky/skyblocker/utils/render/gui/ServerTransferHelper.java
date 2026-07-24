package de.hysky.skyblocker.utils.render.gui;

// Set when the window loses focus during a Hypixel server transfer, so the cursor is released instead of staying hidden. Reset once we're back in game.
public class ServerTransferHelper {
	private static boolean interrupted = false;

	public static boolean isInterrupted() {
		return interrupted;
	}

	public static void setInterrupted(boolean interrupted) {
		ServerTransferHelper.interrupted = interrupted;
	}
}
