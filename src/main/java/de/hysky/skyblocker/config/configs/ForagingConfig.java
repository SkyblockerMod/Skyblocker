package de.hysky.skyblocker.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class ForagingConfig {

	@SerialEntry
	public Hunting hunting = new Hunting();

	/** All Foraging‐related settings for “Modern Foraging Island” (Location.PARK) */
	@SerialEntry
	public Park park = new Park();

	/** (Optional) If you later want a separate section for another area, e.g. “Forest” */
	@SerialEntry
	public Forest forest = new Forest();

	/* ────────────────────────────────────────────────────────────────────────── */

	public static class Park {
		/** HUD toggle + position for the Foraging overlay */
		@SerialEntry
		public ForagingHud foragingHud = new ForagingHud();

		/** Highlights all connected logs of a tree */
		@SerialEntry
		public boolean highlightConnectedTree = true;

		/** Color of the tree highlight (ARGB format) */
		@SerialEntry
		public int highlightColor = 0x66FFFFFF; // semi-transparent white
	}

	public static class Forest {
		/** Example: separate HUD for a Forest area, if you ever add that */
		@SerialEntry
		public ForagingHud foragingHud = new ForagingHud();
	}

	public static class ForagingHud {
		/** Shows or hides the Foraging HUD when in Location.PARK (or Location.FOREST) */
		@SerialEntry
		public boolean enableHud = true;

		/** X‐coordinate (pixels) of the HUD on screen */
		@SerialEntry
		public int x = 10;

		/** Y‐coordinate (pixels) of the HUD on screen */
		@SerialEntry
		public int y = 10;
	}

	public static class Hunting {
		
	}
}
