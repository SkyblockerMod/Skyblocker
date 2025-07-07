package de.hysky.skyblocker.config.configs;

import java.awt.*;

public class ForagingConfig {

	public Galatea galatea = new Galatea();

	public SweepOverlay sweepOverlay = new SweepOverlay();

	public static class Galatea {
		public boolean enableForestNodeHelper = true;

		public boolean solveForestTemplePuzzle = true;

		public boolean enableLushlilacHighlighter = true;

		public boolean enableSeaLumiesHighlighter = true;

		public int seaLumiesMinimumCount = 3;
	}

	public static class SweepOverlay {
		public boolean enableSweepOverlay = true;

		public boolean enableThrownAbilityOverlay = true;

		public Color sweepOverlayColor = new Color(0x40FF9600, true);
	}
}
