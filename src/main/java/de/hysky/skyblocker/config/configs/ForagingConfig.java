package de.hysky.skyblocker.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class ForagingConfig {

	@SerialEntry
	public Galatea galatea = new Galatea();

	@SerialEntry
	public Hunting hunting = new Hunting();

	public static class Galatea {
		@SerialEntry
		public boolean enableForestNodeHelper = true;

		@SerialEntry
		public boolean solveForestTemplePuzzle = true;
	}

	public static class Hunting {
		
	}
}
