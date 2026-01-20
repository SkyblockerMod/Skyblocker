package de.hysky.skyblocker.config.configs;

import net.minecraft.client.resources.language.I18n;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class MiningConfig {
	public boolean enableDrillFuel = true;

	public boolean commissionHighlight = true;

	public boolean callMismyla = true;

	public boolean redialOnBadSignal = true;

	/**
	 * TODO: Move into {@link PickobulusHelper} in next config version.
	 */
	public boolean enablePickobulusHelper = true;

	public PickobulusHelper pickobulusHelper = new PickobulusHelper();

	public DwarvenMines dwarvenMines = new DwarvenMines();

	@Deprecated
	public transient DwarvenHud dwarvenHud = new DwarvenHud();

	public CrystalHollows crystalHollows = new CrystalHollows();

	public CrystalsHud crystalsHud = new CrystalsHud();

	public CrystalsWaypoints crystalsWaypoints = new CrystalsWaypoints();

	public CommissionWaypoints commissionWaypoints = new CommissionWaypoints();

	public Glacite glacite = new Glacite();

	public BlockBreakPrediction blockBreakPrediction = new BlockBreakPrediction();

	public static class PickobulusHelper {
		public boolean enablePickobulusHud = true;

		public boolean hideHudOnCooldown = false;
	}

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

		public boolean hideEmissaryOnPigeon = true;
	}

	public enum CommissionWaypointMode {
		OFF, DWARVEN, GLACITE, BOTH;

		@Override
		public String toString() {
			return I18n.get("skyblocker.config.mining.commissionWaypoints.mode." + name());
		}
	}

	public static class Glacite {
		public boolean coldOverlay = true;

		public boolean fossilSolver = true;

		public boolean enableCorpseFinder = true;

		public boolean enableParsingChatCorpseFinder = true;

		public boolean autoShareCorpses = false;

		public boolean enableCorpseProfitTracker = true;

		public boolean forceEnglishCorpseProfitTracker = true;
	}

	public static class BlockBreakPrediction {
		public boolean enabled = false;

		public boolean playSound = false;


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
