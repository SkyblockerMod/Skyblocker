package de.hysky.skyblocker.config.configs;

import de.hysky.skyblocker.utils.waypoint.Waypoint;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Formatting;

public class DungeonsConfig {
    @SerialEntry
    public boolean fancyPartyFinder = true;

    @SerialEntry
    public boolean croesusHelper = true;

    @SerialEntry
    public boolean playerSecretsTracker = false;

    @SerialEntry
    public boolean starredMobGlow = false;

    @SerialEntry
    public boolean starredMobBoundingBoxes = true;

    @SerialEntry
    public boolean allowDroppingProtectedItems = false;

    @SerialEntry
    public boolean hideSoulweaverSkulls = false;

    @SerialEntry
    public DungeonMap dungeonMap = new DungeonMap();

    @SerialEntry
    public PuzzleSolvers puzzleSolvers = new PuzzleSolvers();

    @SerialEntry
    public TheProfessor theProfessor = new TheProfessor();

    @SerialEntry
    public Livid livid = new Livid();

    @SerialEntry
    public Terminals terminals = new Terminals();

    @SerialEntry
    public Devices devices = new Devices();

    @SerialEntry
    public SecretWaypoints secretWaypoints = new SecretWaypoints();

    @SerialEntry
    public MimicMessage mimicMessage = new MimicMessage();

    @SerialEntry
    public DoorHighlight doorHighlight = new DoorHighlight();

    @SerialEntry
    public DungeonScore dungeonScore = new DungeonScore();

    @SerialEntry
    public DungeonChestProfit dungeonChestProfit = new DungeonChestProfit();

    public static class DungeonMap {
        @SerialEntry
        public boolean enableMap = true;

        @SerialEntry
        public float mapScaling = 1f;

        @SerialEntry
        public int mapX = 2;

        @SerialEntry
        public int mapY = 2;
    }

    public static class PuzzleSolvers {
        @SerialEntry
        public boolean solveTicTacToe = true;

        @SerialEntry
        public boolean solveThreeWeirdos = true;

        @SerialEntry
        public boolean creeperSolver = true;

        @SerialEntry
        public boolean solveWaterboard = true;

        @SerialEntry
        public boolean blazeSolver = true;

        @SerialEntry
        public boolean solveBoulder = true;

        @SerialEntry
        public boolean solveIceFill = true;

        @SerialEntry
        public boolean solveSilverfish = true;

        @SerialEntry
        public boolean solveTrivia = true;
    }

    public static class TheProfessor {
        @SerialEntry
        public boolean fireFreezeStaffTimer = true;

        @SerialEntry
        public boolean floor3GuardianHealthDisplay = true;
    }

    public static class Livid {
        @SerialEntry
        public boolean enableSolidColor = false;

        @SerialEntry
        public boolean enableLividColorGlow = false;

        @SerialEntry
        public boolean enableLividColorBoundingBox = true;

        @SerialEntry
        public boolean enableLividColorText = true;

        @SerialEntry
        public boolean enableLividColorTitle = true;

        @SerialEntry
        public String lividColorText = "The livid color is [color]";
    }

    public static class Terminals {
        @SerialEntry
        public boolean solveColor = true;

        @SerialEntry
        public boolean solveOrder = true;

        @SerialEntry
        public boolean solveStartsWith = true;

        @SerialEntry
        public boolean blockIncorrectClicks = false;
    }

    public static class Devices {
        @SerialEntry
        public boolean solveSimonSays = true;

        @SerialEntry
        public boolean solveLightsOn = true;
    }

    public static class SecretWaypoints {
        @SerialEntry
        public boolean enableRoomMatching = true;

        @SerialEntry
        public boolean enableSecretWaypoints = true;

        @SerialEntry
        public Waypoint.Type waypointType = Waypoint.Type.WAYPOINT;

        @SerialEntry
        public boolean showSecretText = true;

        @SerialEntry
        public boolean enableEntranceWaypoints = true;

        @SerialEntry
        public boolean enableSuperboomWaypoints = true;

        @SerialEntry
        public boolean enableChestWaypoints = true;

        @SerialEntry
        public boolean enableItemWaypoints = true;

        @SerialEntry
        public boolean enableBatWaypoints = true;

        @SerialEntry
        public boolean enableWitherWaypoints = true;

        @SerialEntry
        public boolean enableLeverWaypoints = true;

        @SerialEntry
        public boolean enableFairySoulWaypoints = true;

        @SerialEntry
        public boolean enableStonkWaypoints = true;

        @SerialEntry
        public boolean enableAotvWaypoints = true;

        @SerialEntry
        public boolean enablePearlWaypoints = true;

        @SerialEntry
        public boolean enableDefaultWaypoints = true;
    }

    public static class MimicMessage {
        @SerialEntry
        public boolean sendMimicMessage = true;

        @SerialEntry
        public String mimicMessage = "Mimic dead!";
    }

    public static class DoorHighlight {
        @SerialEntry
        public boolean enableDoorHighlight = true;

        @SerialEntry
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
        @SerialEntry
        public boolean enableDungeonScore270Message = false;

        @SerialEntry
        public boolean enableDungeonScore270Title = false;

        @SerialEntry
        public boolean enableDungeonScore270Sound = false;

        @SerialEntry
        public String dungeonScore270Message = "270 Score Reached!";

        @SerialEntry
        public boolean enableDungeonScore300Message = true;

        @SerialEntry
        public boolean enableDungeonScore300Title = true;

        @SerialEntry
        public boolean enableDungeonScore300Sound = true;

        @SerialEntry
        public String dungeonScore300Message = "300 Score Reached!";

        @SerialEntry
        public boolean enableDungeonCryptsMessage = true;

        @SerialEntry
        public int dungeonCryptsMessageThreshold = 250;

        @SerialEntry
        public String dungeonCryptsMessage = "We only have [crypts] crypts out of 5, we need more!";

        @SerialEntry
        public boolean enableScoreHUD = true;

        @SerialEntry
        public int scoreX = 29;

        @SerialEntry
        public int scoreY = 134;

        @SerialEntry
        public float scoreScaling = 1f;
    }

    public static class DungeonChestProfit {
        @SerialEntry
        public boolean enableProfitCalculator = true;

        @SerialEntry
        public boolean includeKismet = false;

        @SerialEntry
        public boolean includeEssence = true;

        @SerialEntry
        public boolean croesusProfit = true;

        @SerialEntry
        public int neutralThreshold = 1000;

        @SerialEntry
        public Formatting neutralColor = Formatting.DARK_GRAY;

        @SerialEntry
        public Formatting profitColor = Formatting.DARK_GREEN;

        @SerialEntry
        public Formatting lossColor = Formatting.RED;

        @SerialEntry
        public Formatting incompleteColor = Formatting.BLUE;
    }

}
