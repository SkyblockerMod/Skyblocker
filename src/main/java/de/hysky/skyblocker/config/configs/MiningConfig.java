package de.hysky.skyblocker.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.resource.language.I18n;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MiningConfig {
    @SerialEntry
    public boolean enableDrillFuel = true;

    @SerialEntry
    public DwarvenMines dwarvenMines = new DwarvenMines();

	@Deprecated
    public DwarvenHud dwarvenHud = new DwarvenHud();

    @SerialEntry
    public CrystalHollows crystalHollows = new CrystalHollows();

    @SerialEntry
    public CrystalsHud crystalsHud = new CrystalsHud();

    @SerialEntry
    public CrystalsWaypoints crystalsWaypoints = new CrystalsWaypoints();

    @SerialEntry
    public CommissionWaypoints commissionWaypoints = new CommissionWaypoints();

    @SerialEntry
    public Glacite glacite = new Glacite();

    @SerialEntry
    public boolean commissionHighlight = true;

    public static class DwarvenMines {
        @SerialEntry
        public boolean solveFetchur = true;

        @SerialEntry
        public boolean solvePuzzler = true;

		@SerialEntry
	    public boolean enableCarpetHighlighter = true;

		@SerialEntry
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
        @SerialEntry
        public boolean metalDetectorHelper = true;

        @SerialEntry
        public boolean nucleusWaypoints = false;

        @SerialEntry
        public boolean chestHighlighter = true;

        @SerialEntry
        public Color chestHighlightColor = new Color(0, 0, 255, 128);

	    @SerialEntry
	    public boolean enablePowderTracker = true;

	    @SerialEntry
	    public boolean countNaturalChestsInTracker = true;

		@SerialEntry
	    public List<String> powderTrackerFilter = new ArrayList<>();
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

		@Deprecated
        @SerialEntry
        public float textScale = 1;

        @SerialEntry
        public boolean findInChat = true;

        @SerialEntry
        public boolean wishingCompassSolver = true;

		@SerialEntry
		public boolean shareFairyGrotto = true;
    }

    public static class CommissionWaypoints {
        @SerialEntry
        public CommissionWaypointMode mode = CommissionWaypointMode.BOTH;

		@Deprecated
        @SerialEntry
        public float textScale = 1;

        @SerialEntry
        public boolean useColor = true;

        @SerialEntry
        public boolean showBaseCamp = false;

        @SerialEntry
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
        @SerialEntry
        public boolean coldOverlay = true;

        @SerialEntry
        public boolean enableCorpseFinder = true;

        @SerialEntry
        public boolean enableParsingChatCorpseFinder = true;

		@SerialEntry
	    public boolean autoShareCorpses = false;
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
