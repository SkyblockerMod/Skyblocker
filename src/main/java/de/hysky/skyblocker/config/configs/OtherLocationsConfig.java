package de.hysky.skyblocker.config.configs;

import de.hysky.skyblocker.utils.waypoint.Waypoint;

public class OtherLocationsConfig {
	public Barn barn = new Barn();

	public Rift rift = new Rift();

	public TheEnd end = new TheEnd();

	public SpidersDen spidersDen = new SpidersDen();

	public static class Barn {
		public boolean enableGlowingMushroomHelper = true;

		public boolean solveHungryHiker = true;

		public boolean solveTreasureHunter = true;

		public boolean enableCallTrevorMessage = true;

		public boolean enablePeltAnimalHighlighter = true;
	}

	public static class Rift {
		public boolean mirrorverseWaypoints = true;

		public boolean blobbercystGlow = true;

		public boolean enigmaSoulWaypoints = false;

		public boolean highlightFoundEnigmaSouls = true;

		public boolean autoDetectMcGrubber = true;

		public int mcGrubberStacks = 0;
	}

	public static class TheEnd {
		public boolean enableEnderNodeHelper = true;

		public Waypoint.Type enderNodeWaypointType = Waypoint.Type.OUTLINED_HIGHLIGHT;

		@Deprecated
		public transient boolean hudEnabled = true;

		@Deprecated
		public transient boolean zealotKillsEnabled = true;

		@Deprecated
		public transient boolean protectorLocationEnabled = true;

		public boolean waypoint = true;

		public boolean muteEndermanSounds = true;

		@Deprecated
		public transient int x = 10;

		@Deprecated
		public transient int y = 10;
	}

	public static class SpidersDen {
		public Relics relics = new Relics();
	}

	public static class Relics {
		public boolean enableRelicsHelper = false;

		public boolean highlightFoundRelics = true;
	}
}
