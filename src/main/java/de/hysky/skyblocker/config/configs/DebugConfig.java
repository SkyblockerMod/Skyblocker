package de.hysky.skyblocker.config.configs;

import de.hysky.skyblocker.debug.Debug;

public class DebugConfig {
	public int dumpRange = 5;

	public Debug.DumpFormat dumpFormat = Debug.DumpFormat.SNBT;

	public boolean showInvisibleArmorStands = false;

	public boolean webSocketDebug = false;

	public boolean corpseFinderDebug = false;
}
