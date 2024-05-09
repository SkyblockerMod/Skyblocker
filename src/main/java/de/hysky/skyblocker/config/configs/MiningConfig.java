package de.hysky.skyblocker.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class MiningConfig {
    @SerialEntry
    public boolean enableDrillFuel = true;

    @SerialEntry
    public DwarvenMines dwarvenMines = new DwarvenMines();

    @SerialEntry
    public DwarvenHud dwarvenHud = new DwarvenHud();

    @SerialEntry
    public CrystalHollows crystalHollows = new CrystalHollows();

    @SerialEntry
    public CrystalsHud crystalsHud = new CrystalsHud();

    @SerialEntry
    public CrystalsWaypoints crystalsWaypoints = new CrystalsWaypoints();

    public static class DwarvenMines {
        @SerialEntry
        public boolean solveFetchur = true;

        @SerialEntry
        public boolean solvePuzzler = true;
    }

    public static class DwarvenHud {
        @SerialEntry
        public boolean enabledCommissions = true;

        @SerialEntry
        public boolean enabledPowder = true;

        @SerialEntry
        public DwarvenHudStyle style = DwarvenHudStyle.SIMPLE;

        @SerialEntry
        public int commissionsX = 10;

        @SerialEntry
        public int commissionsY = 10;

        @SerialEntry
        public int powderX = 10;

        @SerialEntry
        public int powderY = 70;
    }

    public static class CrystalHollows {
        @SerialEntry
        public boolean metalDetectorHelper = true;
    }

    public static class CrystalsHud {
        @SerialEntry
        public boolean enabled = true;

        @SerialEntry
        public boolean showLocations = true;

        @SerialEntry
        public int locationSize = 8;

        @SerialEntry
        public int x = 10;

        @SerialEntry
        public int y = 130;

        @SerialEntry
        public float mapScaling = 1f;
    }

    public static class CrystalsWaypoints {
        @SerialEntry
        public boolean enabled = true;

        @SerialEntry
        public boolean findInChat = true;
    }

    public enum DwarvenHudStyle {
        SIMPLE, FANCY, CLASSIC;

        @Override
        public String toString() {
            return switch (this) {
                case SIMPLE -> "Simple";
                case FANCY -> "Fancy";
                case CLASSIC -> "Classic";
            };
        }
    }
}
