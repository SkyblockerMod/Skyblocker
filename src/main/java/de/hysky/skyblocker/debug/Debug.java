package de.hysky.skyblocker.debug;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;

public class Debug {
	private static final boolean DEBUG_ENABLED = Boolean.parseBoolean(System.getProperty("skyblocker.debug", "false"));

	public static void init() {
		if (DEBUG_ENABLED || FabricLoader.getInstance().isDevelopmentEnvironment()) {
			ClientCommandRegistrationCallback.EVENT.register(DumpPlayersCommand::register);
		}
	}
}
