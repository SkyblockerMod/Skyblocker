package de.hysky.skyblocker.debug;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class Debug {
	private static final boolean DEBUG_ENABLED = Boolean.parseBoolean(System.getProperty("skyblocker.debug", "false"));

	public static void init() {
		if (DEBUG_ENABLED) {
			ClientCommandRegistrationCallback.EVENT.register(DumpPlayersCommand::register);
		}
	}
}
