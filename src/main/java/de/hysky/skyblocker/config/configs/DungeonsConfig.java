package de.hysky.skyblocker.config.configs;

import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Formatting;

import java.awt.Color;

public class DungeonsConfig {
	public boolean fancyPartyFinder = false;

	public boolean croesusHelper = true;

	public boolean salvageHelper = true;

	public boolean onlyHighlightDonatedItems = false;

	public boolean sellableItemsHighlighter = true;

	public boolean bloodCampHelper = false;

	public boolean playerSecretsTracker = false;

	public boolean classBasedPlayerGlow = true;

	public boolean starredMobGlow = true;

	public boolean starredMobBoundingBoxes = false;

	public boolean highlightDoorKeys = true;

	public boolean allowDroppingProtectedItems = false;

	public boolean dungeonSplits = false;

	public boolean hideSoulweaverSkulls = false;

	public DungeonMap dungeonMap = new DungeonMap();

	public SpiritLeapOverlay leapOverlay = new SpiritLeapOverlay();

	public PuzzleSolvers puzzleSolvers = new PuzzleSolvers();

	public TheProfessor theProfessor = new TheProfessor();

	public Livid livid = new Livid();

	public Terminals terminals = new Terminals();

	public Devices devices = new Devices();

	public Goldor goldor = new Goldor();

	public SecretWaypoints secretWaypoints = new SecretWaypoints();

	public MimicMessage mimicMessage = new MimicMessage();

	public PrinceMessage princeMessage = new PrinceMessage();

	public DoorHighlight doorHighlight = new DoorHighlight();

	public DungeonScore dungeonScore = new DungeonScore();

	public DungeonChestProfit dungeonChestProfit = new DungeonChestProfit();

	public static class DungeonMap {
		public boolean enableMap = true;

		public boolean fancyMap = true;

		public boolean showSelfHead = true;

		public boolean showRoomLabels = true;

		public float mapScaling = 1f;

		public int mapX = 2;

		public int mapY = 2;
	}

	public static class SpiritLeapOverlay {
		public boolean enableLeapOverlay = true;

		public boolean leapKeybinds = true;

		public boolean showMap = true;

		public float scale = 1.2f;

		public boolean enableLeapMessage = false;

		public String leapMessage = "Leaped to [name]!";
	}

	public static class PuzzleSolvers {
		public boolean solveTicTacToe = true;

		public boolean solveThreeWeirdos = true;

		public boolean creeperSolver = true;

		@Deprecated
		public transient boolean solveWaterboard = true;

		public boolean waterboardOneFlow = true;

		public boolean previewWaterPath = true;

		public boolean previewLeverEffects = true;

		public boolean blazeSolver = true;

		public boolean solveBoulder = true;

		public boolean solveIceFill = true;

		public boolean solveSilverfish = true;

		public boolean solveTrivia = true;

		public boolean solveTeleportMaze = true;
	}

	public static class TheProfessor {
		public boolean fireFreezeStaffTimer = true;

		public boolean floor3GuardianHealthDisplay = true;
	}

	public static class Livid {
		public boolean enableSolidColor = false;

		public Color customColor = Color.RED;

		public boolean enableLividColorGlow = false;

		public boolean enableLividColorBoundingBox = true;

		public boolean enableLividColorText = true;

		public boolean enableLividColorTitle = true;

		public String lividColorText = "The livid color is [color]";
	}

	public static class Terminals {
		public boolean solveColor = true;

		public boolean solveSameColor = true;

		public boolean solveOrder = true;

		public boolean solveStartsWith = true;

		public boolean blockIncorrectClicks = false;
	}

	public static class Devices {
		public boolean solveSimonSays = true;

		public boolean solveLightsOn = true;

		public boolean solveArrowAlign = true;

		public boolean solveTargetPractice = true;
	}

	public static class Goldor {
		public boolean enableGoldorWaypoints = true;

		public Waypoint.Type waypointType = Waypoint.Type.WAYPOINT;
	}

	public static class SecretWaypoints {
		@Deprecated
		public transient boolean enableRoomMatching = true;

		public boolean enableSecretWaypoints = true;

		public Waypoint.Type waypointType = Waypoint.Type.WAYPOINT;

		public boolean showSecretText = true;

		public boolean enableEntranceWaypoints = true;

		public boolean enableSuperboomWaypoints = true;

		public boolean enableChestWaypoints = true;

		public boolean enableItemWaypoints = true;

		public boolean enableBatWaypoints = true;

		public boolean enableWitherWaypoints = true;

		public boolean enableLeverWaypoints = true;

		public boolean enableFairySoulWaypoints = true;

		public boolean enableStonkWaypoints = true;

		public boolean enableAotvWaypoints = true;

		public boolean enablePearlWaypoints = true;

		public boolean enablePrinceWaypoints = true;

		public boolean enableDefaultWaypoints = true;
	}

	public static class MimicMessage {
		public boolean sendMimicMessage = true;

		@Deprecated
		public transient String mimicMessage = "Mimic dead!";
	}

	public static class PrinceMessage {
		public boolean sendPrinceMessage = true;

		@Deprecated
		public transient String princeMessage = "Prince dead!";
	}

	public static class DoorHighlight {
		public boolean enableDoorHighlight = true;

		public Type doorHighlightType = Type.OUTLINED_HIGHLIGHT;

		public enum Type {
			HIGHLIGHT,
			OUTLINED_HIGHLIGHT,
			OUTLINE;

			@Override
			public String toString() {
				return I18n.translate("skyblocker.config.dungeons.doorHighlight.doorHighlightType.type." + name());
			}
		}
	}

	public static class DungeonScore {
		public boolean enableDungeonScore270Message = false;

		public boolean enableDungeonScore270Title = false;

		public boolean enableDungeonScore270Sound = false;

		public String dungeonScore270Message = "270 Score Reached!";

		public boolean enableDungeonScore300Message = true;

		public boolean enableDungeonScore300Title = true;

		public boolean enableDungeonScore300Sound = true;

		public String dungeonScore300Message = "300 Score Reached!";

		public boolean enableDungeonCryptsMessage = true;

		public int dungeonCryptsMessageThreshold = 250;

		public String dungeonCryptsMessage = "Crypts: [crypts]/5";

		public boolean enableScoreHUD = true;

		public int scoreX = 29;

		public int scoreY = 134;

		public float scoreScaling = 1f;
	}

	public static class DungeonChestProfit {
		public boolean enableProfitCalculator = true;

		public boolean includeKismet = false;

		public boolean includeEssence = true;

		public boolean croesusProfit = true;

		public int neutralThreshold = 1000;

		public Formatting neutralColor = Formatting.DARK_GRAY;

		public Formatting profitColor = Formatting.DARK_GREEN;

		public Formatting lossColor = Formatting.RED;

		public Formatting incompleteColor = Formatting.BLUE;
	}

}
