package de.hysky.skyblocker.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

import java.awt.*;

public class ForagingConfig {

	@SerialEntry
	public Galatea galatea = new Galatea();

	@SerialEntry
	public SweepOverlay sweepOverlay = new SweepOverlay();

	public static class Galatea {
		@SerialEntry
		public boolean enableForestNodeHelper = true;

		@SerialEntry
		public boolean solveForestTemplePuzzle = true;

		@SerialEntry
		public boolean enableLushlilacHighlighter = true;

		@SerialEntry
		public boolean enableSeaLumiesHighlighter = true;

		@SerialEntry
		public int seaLumiesMinimumCount = 3;
	}

	public static class SweepOverlay {
		@SerialEntry
		public boolean enableSweepOverlay = true;

		@SerialEntry
		public boolean enableThrownAbilityOverlay = true;

		@SerialEntry
		public Color sweepOverlayColor = new Color(0x40FF9600, true);
	}
}
