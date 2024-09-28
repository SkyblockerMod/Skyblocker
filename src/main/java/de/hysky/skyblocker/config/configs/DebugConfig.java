package de.hysky.skyblocker.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class DebugConfig {
	@SerialEntry
	public int dumpRange = 5;

	@SerialEntry
	public boolean showInvisibleArmorStands = false;

	@SerialEntry
	public boolean webSocketDebug = false;
}
