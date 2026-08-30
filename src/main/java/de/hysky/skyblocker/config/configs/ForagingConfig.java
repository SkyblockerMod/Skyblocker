package de.hysky.skyblocker.config.configs;

import java.awt.Color;

public class ForagingConfig {

	public boolean enableTreeFelledNotification = false;

	public MoongladeMarsh moongladeMarsh = new MoongladeMarsh();

	public TorrhusCanyon torrhusCanyon = new TorrhusCanyon();

	public SweepOverlay sweepOverlay = new SweepOverlay();

	public static class MoongladeMarsh {
		@Deprecated
		public transient boolean enableForestNodeHelper = true;

		public boolean solveForestTemplePuzzle = true;

		public boolean enableLushlilacHighlighter = true;

		public boolean enableSeaLumiesHighlighter = true;

		public int seaLumiesMinimumCount = 3;

		public boolean enableTreeBreakProgress = true;

		public boolean enableTunerSolver = true;

		@Deprecated
		public transient boolean enableSweepDetailsWidget;
	}

	public static class TorrhusCanyon {
		public boolean solveDesertTemplePuzzles = true;

		public boolean enableRubyVeilshroomHighlighter = true;

		public boolean enableHoneyhiveHighlighter = true;
	}

	public static class SweepOverlay {
		public boolean enableSweepOverlay = true;

		public boolean enableThrownAbilityOverlay = true;

		public Color sweepOverlayColor = new Color(0x40FF9600, true);
	}
}
