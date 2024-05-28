package de.hysky.skyblocker.config.configs;

import de.hysky.skyblocker.utils.waypoint.Waypoint;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UIAndVisualsConfig {
    @SerialEntry
    public boolean compactorDeletorPreview = true;

    @SerialEntry
    public boolean dontStripSkinAlphaValues = true;

    @SerialEntry
    public boolean backpackPreviewWithoutShift = false;

    @SerialEntry
    public boolean hideEmptyTooltips = true;

    @SerialEntry
    public boolean fancyCraftingTable = true;

    @SerialEntry
    public boolean hideStatusEffectOverlay = false;

    @SerialEntry
    public ChestValue chestValue = new ChestValue();

    @SerialEntry
    public ItemCooldown itemCooldown = new ItemCooldown();

    @SerialEntry
    public TitleContainer titleContainer = new TitleContainer();

    @SerialEntry
    public TabHudConf tabHud = new TabHudConf();

    @SerialEntry
    public FancyAuctionHouse fancyAuctionHouse = new FancyAuctionHouse();

    @SerialEntry
    public Bars bars = new Bars();

    @SerialEntry
    public Waypoints waypoints = new Waypoints();

    @SerialEntry
    public TeleportOverlay teleportOverlay = new TeleportOverlay();

    @SerialEntry
    public SearchOverlay searchOverlay = new SearchOverlay();

    @SerialEntry
    public InputCalculator inputCalculator = new InputCalculator();

    @SerialEntry
    public FlameOverlay flameOverlay = new FlameOverlay();

    @SerialEntry
    public CompactDamage compactDamage = new CompactDamage();

    @SerialEntry
    public boolean showEssenceCost = false;

    public static class ChestValue {
        @SerialEntry
        public boolean enableChestValue = true;

        @SerialEntry
        public Formatting color = Formatting.DARK_GREEN;

        @SerialEntry
        public Formatting incompleteColor = Formatting.BLUE;
    }

    public static class ItemCooldown {
        @SerialEntry
        public boolean enableItemCooldowns = true;
    }

    public static class TitleContainer {
        @SerialEntry
        public float titleContainerScale = 100;

        @SerialEntry
        public int x = 540;

        @SerialEntry
        public int y = 10;

        @SerialEntry
        public Direction direction = Direction.HORIZONTAL;

        @SerialEntry
        public Alignment alignment = Alignment.MIDDLE;
    }

    public enum Direction {
        HORIZONTAL, VERTICAL;

        @Override
        public String toString() {
            return switch (this) {
                case HORIZONTAL -> "Horizontal";
                case VERTICAL -> "Vertical";
            };
        }
    }

    public enum Alignment {
        LEFT, RIGHT, MIDDLE;

        @Override
        public String toString() {
            return switch (this) {
                case LEFT -> "Left";
                case RIGHT -> "Right";
                case MIDDLE -> "Middle";
            };
        }
    }

    public static class TabHudConf {
        @SerialEntry
        public boolean tabHudEnabled = true;

        @SerialEntry
        public int tabHudScale = 100;

        @SerialEntry
        public boolean enableHudBackground = true;

        @SerialEntry
        public boolean plainPlayerNames = false;

        @SerialEntry
        public NameSorting nameSorting = NameSorting.DEFAULT;
    }

    public enum NameSorting {
        DEFAULT, ALPHABETICAL;

        @Override
        public String toString() {
            return switch (this) {
                case DEFAULT -> "Default";
                case ALPHABETICAL -> "Alphabetical";
            };
        }
    }

    public static class FancyAuctionHouse {
        @SerialEntry
        public boolean enabled = true;

        @SerialEntry
        public boolean highlightCheapBIN = true;
    }

    public static class Bars {
        @SerialEntry
        public boolean enableBars = true;

        // Kept in for backwards compatibility, remove if needed
        @SerialEntry
        public OldBarPositions barPositions = new OldBarPositions();
    }

    /**
     * Backwards compat
     */
    public static class OldBarPositions {
        @SerialEntry
        public OldBarPosition healthBarPosition = OldBarPosition.LAYER1;

        @SerialEntry
        public OldBarPosition manaBarPosition = OldBarPosition.LAYER1;

        @SerialEntry
        public OldBarPosition defenceBarPosition = OldBarPosition.LAYER1;

        @SerialEntry
        public OldBarPosition experienceBarPosition = OldBarPosition.LAYER1;

    }

    /**
     * Backwards compat
     */
    public enum OldBarPosition {
        LAYER1, LAYER2, RIGHT, NONE
    }

    public static class Waypoints {
        @SerialEntry
        public boolean enableWaypoints = true;

        @SerialEntry
        public Waypoint.Type waypointType = Waypoint.Type.WAYPOINT;
    }

    public static class TeleportOverlay {
        @SerialEntry
        public boolean enableTeleportOverlays = true;

        @SerialEntry
        public boolean enableWeirdTransmission = true;

        @SerialEntry
        public boolean enableInstantTransmission = true;

        @SerialEntry
        public boolean enableEtherTransmission = true;

        @SerialEntry
        public boolean enableSinrecallTransmission = true;

        @SerialEntry
        public boolean enableWitherImpact = true;
    }

    public static class SearchOverlay {
        @SerialEntry
        public boolean enableBazaar = true;

        @SerialEntry
        public boolean enableAuctionHouse = true;

        @SerialEntry
        public boolean keepPreviousSearches = false;

        @SerialEntry
        public int maxSuggestions = 3;

        @SerialEntry
        public int historyLength = 3;

        @SerialEntry
        public boolean enableCommands = false;

        @SerialEntry
        public List<String> bazaarHistory = new ArrayList<>();

        @SerialEntry
        public List<String> auctionHistory = new ArrayList<>();
    }

    public static class InputCalculator {
        @SerialEntry
        public boolean enabled = true;

        @SerialEntry
        public boolean requiresEquals = false;
    }

    public static class FlameOverlay {
        @SerialEntry
        public int flameHeight = 100;

        @SerialEntry
        public int flameOpacity = 100;
    }

    public static class CompactDamage {
        @SerialEntry
        public boolean enabled = true;

        @SerialEntry
        public int precision = 1;

        @SerialEntry
        public Color normalDamageColor = new Color(0xFFFFFF);

        @SerialEntry
        public Color critDamageGradientStart = new Color(0xFFFF55);

        @SerialEntry
        public Color critDamageGradientEnd = new Color(0xFF5555);
    }
}
