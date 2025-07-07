package de.hysky.skyblocker.config.configs;

public class OtherLocationsConfig {
    public Barn barn = new Barn();

    public Rift rift = new Rift();

    public TheEnd end = new TheEnd();

    public SpidersDen spidersDen = new SpidersDen();

    public static class Barn {
        public boolean solveHungryHiker = true;

        public boolean solveTreasureHunter = true;
    }

    public static class Rift {
        public boolean mirrorverseWaypoints = true;

        public boolean blobbercystGlow = true;

        public boolean enigmaSoulWaypoints = false;

        public boolean highlightFoundEnigmaSouls = true;

        public int mcGrubberStacks = 0;
    }

    public static class TheEnd {
        public boolean enableEnderNodeHelper = true;

        public boolean hudEnabled = true;

        public boolean zealotKillsEnabled = true;

        public boolean protectorLocationEnabled = true;

        public boolean waypoint = true;

		public boolean muteEndermanSounds = false;

        public int x = 10;

        public int y = 10;
    }

    public static class SpidersDen {
        public Relics relics = new Relics();
    }

    public static class Relics {
        public boolean enableRelicsHelper = false;

        public boolean highlightFoundRelics = true;
    }
}
