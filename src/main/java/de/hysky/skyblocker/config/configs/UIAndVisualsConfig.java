package de.hysky.skyblocker.config.configs;

import de.hysky.skyblocker.skyblock.item.slottext.SlotTextMode;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UIAndVisualsConfig {
	@SerialEntry
	public int nightVisionStrength = 100;

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
    public boolean showEquipmentInInventory = true;

    @SerialEntry
    public boolean cancelComponentUpdateAnimation = true;

    @SerialEntry
    public ChestValue chestValue = new ChestValue();

    @SerialEntry
    public ItemCooldown itemCooldown = new ItemCooldown();

	@SerialEntry
	public SlotText slotText = new SlotText();

    @SerialEntry
    public InventorySearchConfig inventorySearch = new InventorySearchConfig();

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

	public static class SlotText {

		@SerialEntry
		public SlotTextMode slotTextMode = SlotTextMode.ENABLED;

		@SerialEntry
		public Object2BooleanMap<String> textEnabled = new Object2BooleanOpenHashMap<>();

		@SerialEntry
		public boolean slotTextToggled = true;

	}

    public static class InventorySearchConfig {
        @SerialEntry
        public EnableState enabled = EnableState.SKYBLOCK;

        @SerialEntry
        public boolean ctrlK = false;

        @SerialEntry
        public boolean clickableText = false;

        public enum EnableState {
            OFF,
            SKYBLOCK,
            EVERYWHERE;

            @Override
            public String toString() {
                return I18n.translate("skyblocker.config.uiAndVisuals.inventorySearch.state." + this.name());
            }

            public boolean isEnabled() {
                return switch (this) {
                    case OFF -> false;
                    case SKYBLOCK -> de.hysky.skyblocker.utils.Utils.isOnSkyblock();
                    case EVERYWHERE -> true;
                };
            }
        }
    }

    public static class TitleContainer {
        @SerialEntry
        public float titleContainerScale = 100;

        @SerialEntry
        public int x = 540;

        @SerialEntry
        public int y = 10;

        @SerialEntry
        public Direction direction = Direction.VERTICAL;

        @SerialEntry
        public Alignment alignment = Alignment.MIDDLE;

        public float getRenderScale() {
            return titleContainerScale * 0.03f;
        }
    }

    public enum Direction {
        HORIZONTAL, VERTICAL;

        @Override
        public String toString() {
            return I18n.translate("skyblocker.config.uiAndVisuals.titleContainer.direction." + name());
        }
    }

    public enum Alignment {
        LEFT, MIDDLE, RIGHT;

        @Override
        public String toString() {
            return I18n.translate("skyblocker.config.uiAndVisuals.titleContainer.alignment." + name());
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
        public boolean effectsFromFooter = false;

        @SerialEntry
        public ScreenBuilder.DefaultPositioner defaultPositioning = ScreenBuilder.DefaultPositioner.CENTERED;

        @Deprecated
        public boolean plainPlayerNames = false;

        @Deprecated
        public NameSorting nameSorting = NameSorting.DEFAULT;
    }

    @Deprecated
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
        @SuppressWarnings("DeprecatedIsStillUsed")
        @Deprecated
        @SerialEntry
        public LegacyBarPositions barPositions = new LegacyBarPositions();
    }

    /**
     * Backwards compat.
     * <p>
     * Used to load the legacy bar positions, which will not have an effect once the bars are saved in the new format at {@code /skyblocker/status_bars.json}.
     * New bars do not need to be added here.
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public static class LegacyBarPositions {
        @SerialEntry
        public LegacyBarPosition healthBarPosition = LegacyBarPosition.LAYER1;

        @SerialEntry
        public LegacyBarPosition manaBarPosition = LegacyBarPosition.LAYER1;

        @SerialEntry
        public LegacyBarPosition defenceBarPosition = LegacyBarPosition.RIGHT;

        @SerialEntry
        public LegacyBarPosition experienceBarPosition = LegacyBarPosition.LAYER2;
    }

    /**
     * Backwards compat
     */
    public enum LegacyBarPosition {
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

		@SerialEntry
		public boolean closeSignsWithEnter = true;
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
