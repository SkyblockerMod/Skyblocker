package de.hysky.skyblocker.config.configs;

import net.minecraft.client.resource.language.I18n;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MiningConfig {
    public boolean enableDrillFuel = true;

    public DwarvenMines dwarvenMines = new DwarvenMines();

	@Deprecated
    public transient DwarvenHud dwarvenHud = new DwarvenHud();

    public CrystalHollows crystalHollows = new CrystalHollows();

    public CrystalsHud crystalsHud = new CrystalsHud();

    public CrystalsWaypoints crystalsWaypoints = new CrystalsWaypoints();

    public CommissionWaypoints commissionWaypoints = new CommissionWaypoints();

    public Glacite glacite = new Glacite();

    public boolean commissionHighlight = true;

    public static class DwarvenMines {
        public boolean solveFetchur = true;

        public boolean solvePuzzler = true;

	    public boolean enableCarpetHighlighter = true;

	    public Color carpetHighlightColor = new Color(255, 0, 0, 76);
    }

	@Deprecated
    public static class DwarvenHud {
        @Deprecated
        public boolean enabledCommissions = true;

        @Deprecated
        public boolean enabledPowder = true;

        @Deprecated
        public DwarvenHudStyle style = DwarvenHudStyle.SIMPLE;

        @Deprecated
        public int commissionsX = 10;

        @Deprecated
        public int commissionsY = 10;

        @Deprecated
        public int powderX = 10;

        @Deprecated
        public int powderY = 70;
    }

    public static class CrystalHollows {
        public boolean metalDetectorHelper = true;

        public boolean nucleusWaypoints = false;

        public boolean chestHighlighter = true;

        public Color chestHighlightColor = new Color(0, 0, 255, 128);

	    public boolean enablePowderTracker = true;

	    public boolean countNaturalChestsInTracker = true;

	    public List<String> powderTrackerFilter = new ArrayList<>();
    }

    public static class CrystalsHud {
        public boolean enabled = true;

        public boolean showLocations = true;

        public int locationSize = 8;

        public int x = 10;

        public int y = 130;

        public float mapScaling = 1f;
    }

    public static class CrystalsWaypoints {
        public boolean enabled = true;

		@Deprecated
        public transient float textScale = 1;

        public boolean findInChat = true;

        public boolean wishingCompassSolver = true;

		public boolean shareFairyGrotto = true;
    }

    public static class CommissionWaypoints {
        public CommissionWaypointMode mode = CommissionWaypointMode.BOTH;

		@Deprecated
        public transient float textScale = 1;

        public boolean useColor = true;

        public boolean showBaseCamp = false;

        public boolean showEmissary = true;
    }

    public enum CommissionWaypointMode {
        OFF, DWARVEN, GLACITE, BOTH;

        @Override
        public String toString() {
            return I18n.translate("skyblocker.config.mining.commissionWaypoints.mode." + name());
        }
    }

    public static class Glacite {
        public boolean coldOverlay = true;

		public boolean fossilSolver = true;

        public boolean enableCorpseFinder = true;

        public boolean enableParsingChatCorpseFinder = true;

	    public boolean autoShareCorpses = false;

	    public boolean enableCorpseProfitTracker = true;
    }

	/**
	 * @deprecated See {@link UIAndVisualsConfig.TabHudStyle}.
	 */
	@Deprecated
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
