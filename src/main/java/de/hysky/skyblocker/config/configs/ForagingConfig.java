package de.hysky.skyblocker.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class ForagingConfig {

	@SerialEntry
	public Hunting hunting = new Hunting();

	@SerialEntry
	public Park park = new Park();

	public static class Park {
		@SerialEntry
		public ForagingHud foragingHud = new ForagingHud();

		@SerialEntry
		public boolean highlightConnectedTree = true;

		@SerialEntry
		public int highlightColor = 0x66FFFFFF; // semi-transparent white
	}

	public static class ForagingHud {
		@SerialEntry
		public boolean enableHud = true;

		@SerialEntry
		public int x = 10;

		@SerialEntry
		public int y = 10;
	}

	public static class Hunting {
		// Nothing yet
	}
}
