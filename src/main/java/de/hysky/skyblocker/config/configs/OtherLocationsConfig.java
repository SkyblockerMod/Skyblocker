package de.hysky.skyblocker.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class OtherLocationsConfig {

    @SerialEntry
    public Barn barn = new Barn();

    @SerialEntry
    public Rift rift = new Rift();

    @SerialEntry
    public TheEnd end = new TheEnd();

    @SerialEntry
    public SpidersDen spidersDen = new SpidersDen();

    public static class Barn {
        @SerialEntry
        public boolean solveHungryHiker = true;

        @SerialEntry
        public boolean solveTreasureHunter = true;
    }

    public static class Rift {
        @SerialEntry
        public boolean mirrorverseWaypoints = true;

        @SerialEntry
        public boolean blobbercystGlow = true;

        @SerialEntry
        public boolean enigmaSoulWaypoints = false;

        @SerialEntry
        public boolean highlightFoundEnigmaSouls = true;

        @SerialEntry
        public int mcGrubberStacks = 0;
    }

    public static class TheEnd {
        @SerialEntry
        public boolean enableEnderNodeHelper = true;

        @SerialEntry
        public boolean hudEnabled = true;

        @SerialEntry
        public boolean zealotKillsEnabled = true;

        @SerialEntry
        public boolean protectorLocationEnabled = true;

        @SerialEntry
        public boolean waypoint = true;

		@SerialEntry
		public boolean muteEndermanSounds = false;

        @SerialEntry
        public int x = 10;

        @SerialEntry
        public int y = 10;
    }

    public static class SpidersDen {
        @SerialEntry
        public Relics relics = new Relics();
    }

    public static class Relics {
        @SerialEntry
        public boolean enableRelicsHelper = false;

        @SerialEntry
        public boolean highlightFoundRelics = true;
    }
}
