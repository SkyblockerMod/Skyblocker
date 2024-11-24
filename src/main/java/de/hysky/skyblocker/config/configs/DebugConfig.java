package de.hysky.skyblocker.config.configs;

import de.hysky.skyblocker.debug.Debug;
import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class DebugConfig {
	@SerialEntry
	public int dumpRange = 5;

	@SerialEntry
	public Debug.DumpFormat dumpFormat = Debug.DumpFormat.SNBT;

	@SerialEntry
	public boolean showInvisibleArmorStands = false;

	@SerialEntry
	public boolean webSocketDebug = false;

	@SerialEntry
	public boolean corpseFinderDebug = false;
}
