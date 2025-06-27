package de.hysky.skyblocker.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class ForagingConfig {

	@SerialEntry
	public Galatea galatea = new Galatea();

	public static class Galatea {
		@SerialEntry
		public boolean enableForestNodeHelper = true;

		@SerialEntry
		public boolean solveForestTemplePuzzle = true;

		@SerialEntry
		public boolean enableLushlilacHighlighter = true;

		@SerialEntry
		public float lushlilacHighlighterOpacity = 0.5f;

		@SerialEntry
		public boolean enableSeaLumiesHighlighter = true;

		@SerialEntry
		public float seaLumiesHighlighterOpacity = 0.5f;

		@SerialEntry
		public boolean enableTreeBreakProgress = true;

		@SerialEntry
		public int seaLumiesMinimumCount = 3;

		@SerialEntry
		public boolean disableFishingNetPlacement = true;
	}
}
