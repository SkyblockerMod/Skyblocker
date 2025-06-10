package de.hysky.skyblocker.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

import java.awt.*;

public class ForagingConfig {

	@SerialEntry
	public Galatea galatea = new Galatea();

	@SerialEntry
	public Hunting hunting = new Hunting();

	@SerialEntry
	public SweepOverlay sweepOverlay = new SweepOverlay();

	public static class Galatea {
		@SerialEntry
		public boolean enableForestNodeHelper = true;
	}

	public static class Hunting {
		
	}

	public static class SweepOverlay {
		@SerialEntry
		public boolean enableSweepOverlay = true;

		@SerialEntry
		public Color sweepOverlayColor = new Color(0x40FF9600, true);
	}
}
