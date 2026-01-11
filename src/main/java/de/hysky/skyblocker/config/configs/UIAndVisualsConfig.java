package de.hysky.skyblocker.config.configs;

import de.hysky.skyblocker.skyblock.GyroOverlay;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextMode;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.language.I18n;

public class UIAndVisualsConfig {

	public boolean swingOnAbilities = false;

	public int nightVisionStrength = 100;

	public boolean compactorDeletorPreview = true;

	public boolean dontStripSkinAlphaValues = true;

	public boolean backpackPreviewWithoutShift = false;

	public boolean hideEmptyTooltips = true;

	public boolean fancyCraftingTable = true;

	public boolean hideStatusEffectOverlay = true;

	public boolean showEquipmentInInventory = true;

	public boolean cancelComponentUpdateAnimation = true;

	public boolean showCustomizeButton = true;

	public boolean showConfigButton = false;

	public boolean trueQuiverCount = true;

	public ChestValue chestValue = new ChestValue();

	public ItemCooldown itemCooldown = new ItemCooldown();

	public boolean museumOverlay = true;

	public SlotText slotText = new SlotText();

	public RadialMenu radialMenu = new RadialMenu();

	public InventorySearchConfig inventorySearch = new InventorySearchConfig();

	public TitleContainer titleContainer = new TitleContainer();

	public TabHudConf tabHud = new TabHudConf();

	public FancyAuctionHouse fancyAuctionHouse = new FancyAuctionHouse();

	public Bars bars = new Bars();

	public Waypoints waypoints = new Waypoints();

	public TeleportOverlay teleportOverlay = new TeleportOverlay();

	public SmoothAOTE smoothAOTE = new SmoothAOTE();

	public SearchOverlay searchOverlay = new SearchOverlay();

	public BazaarQuickQuantities bazaarQuickQuantities = new BazaarQuickQuantities();

	public InputCalculator inputCalculator = new InputCalculator();

	public FlameOverlay flameOverlay = new FlameOverlay();

	public CompactDamage compactDamage = new CompactDamage();

	public HealthBars healthBars = new HealthBars();

	public GyrokineticWandOverlay gyroOverlay = new GyrokineticWandOverlay();

	public ItemPickup itemPickup = new ItemPickup();

	public static class ChestValue {
		public boolean enableChestValue = true;

		public ChatFormatting color = ChatFormatting.DARK_GREEN;

		public ChatFormatting incompleteColor = ChatFormatting.BLUE;
	}

	public static class ItemCooldown {
		public boolean enableItemCooldowns = true;
	}

	public static class SlotText {
		public SlotTextMode slotTextMode = SlotTextMode.ENABLED;

		public Object2BooleanOpenHashMap<String> textEnabled = new Object2BooleanOpenHashMap<>();

		public boolean slotTextToggled = true;

	}

	public static class RadialMenu {
		public boolean enabled = false;

		public boolean tooltipsWithoutShift = false;

		public Object2BooleanOpenHashMap<String> enabledMenus = new Object2BooleanOpenHashMap<>();


	}

	public static class InventorySearchConfig {
		public EnableState enabled = EnableState.SKYBLOCK;

		public boolean ctrlK = false;

		public boolean clickableText = false;

		public enum EnableState {
			OFF,
			SKYBLOCK,
			EVERYWHERE;

			@Override
			public String toString() {
				return I18n.get("skyblocker.config.uiAndVisuals.inventorySearch.state." + this.name());
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
		public float titleContainerScale = 100;

		public int x = -1;

		public int y = -1;

		public Direction direction = Direction.VERTICAL;

		public Alignment alignment = Alignment.MIDDLE;

		public float getRenderScale() {
			return titleContainerScale * 0.03f;
		}
	}

	public enum Direction {
		HORIZONTAL, VERTICAL;

		@Override
		public String toString() {
			return I18n.get("skyblocker.config.uiAndVisuals.titleContainer.direction." + name());
		}
	}

	public enum Alignment {
		LEFT, MIDDLE, RIGHT;

		@Override
		public String toString() {
			return I18n.get("skyblocker.config.uiAndVisuals.titleContainer.alignment." + name());
		}
	}

	public static class TabHudConf {
		public boolean tabHudEnabled = true;

		public int tabHudScale = 100;

		public boolean showVanillaTabByDefault = false;

		public TabHudStyle style = TabHudStyle.FANCY;

		public boolean displayIcons = true;

		public boolean compactWidgets = false;

		public boolean enableHudBackground = true;

		public boolean effectsFromFooter = false;

		public ScreenBuilder.DefaultPositioner defaultPositioning = ScreenBuilder.DefaultPositioner.CENTERED;

		@Deprecated
		public transient boolean plainPlayerNames = false;

		public NameSorting nameSorting = NameSorting.DEFAULT;
	}

	/**
	 * @implNote Currently, there are no "decorations", meaning that there is no difference between the SIMPLE and CLASSIC styles.
	 */
	public enum TabHudStyle {
		/**
		 * The minimal style, with no decorations nor custom components,
		 * rendered in a minimal rectangle background,
		 * or no background at all if {@link TabHudConf#enableHudBackground} is false.
		 */
		MINIMAL,
		/**
		 * The simple style, with no decorations nor custom components.
		 */
		SIMPLE,
		/**
		 * The classic style, with decorations but no custom components.
		 */
		CLASSIC,
		/**
		 * The default style, with all custom components and decorations in use.
		 */
		FANCY;

		public boolean isMinimal() {
			return this == MINIMAL;
		}

		@Override
		public String toString() {
			return I18n.get("skyblocker.config.uiAndVisuals.tabHud.style." + name());
		}
	}

	public enum NameSorting {
		DEFAULT,
		ALPHABETICAL(Comparator.comparing(ple -> matchPlayerName(ple.getTabListDisplayName().getString(), "name").orElse(""), String.CASE_INSENSITIVE_ORDER)),
		SKYBLOCK_LEVEL(Comparator.<PlayerInfo>comparingInt(ple -> matchPlayerName(ple.getTabListDisplayName().getString(), "level").map(Integer::parseInt).orElse(0)).reversed());

		public final Comparator<PlayerInfo> comparator;

		NameSorting() {
			this(null);
		}

		NameSorting(Comparator<PlayerInfo> comparator) {
			this.comparator = comparator;
		}

		private static Optional<String> matchPlayerName(String name, String group) {
			Matcher matcher = PlayerListManager.PLAYER_NAME_PATTERN.matcher(name);
			return matcher.matches() ? Optional.of(matcher.group(group)) : Optional.empty();
		}

		@Override
		public String toString() {
			return switch (this) {
				case DEFAULT -> "Default";
				case ALPHABETICAL -> "Alphabetical";
				case SKYBLOCK_LEVEL -> "Skyblock Level";
			};
		}
	}

	public static class FancyAuctionHouse {
		public boolean enabled = false;

		public boolean highlightCheapBIN = true;
	}

	public static class Bars {
		public boolean enableBars = true;

		public boolean enableVanillaStyleManaBar = false;

		public IntelligenceDisplay intelligenceDisplay = IntelligenceDisplay.ORIGINAL;

		// Kept in for backwards compatibility, remove if needed
		@SuppressWarnings("DeprecatedIsStillUsed")
		@Deprecated
		public LegacyBarPositions barPositions = new LegacyBarPositions();
	}

	public enum IntelligenceDisplay {
		ORIGINAL,
		ACCURATE,
		IN_FRONT
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
		public LegacyBarPosition healthBarPosition = LegacyBarPosition.LAYER1;

		public LegacyBarPosition manaBarPosition = LegacyBarPosition.LAYER1;

		public LegacyBarPosition defenceBarPosition = LegacyBarPosition.RIGHT;

		public LegacyBarPosition experienceBarPosition = LegacyBarPosition.LAYER2;
	}

	/**
	 * Backwards compat
	 */
	public enum LegacyBarPosition {
		LAYER1, LAYER2, RIGHT, NONE
	}

	public static class Waypoints {
		public boolean enableWaypoints = true;

		public Waypoint.Type waypointType = Waypoint.Type.WAYPOINT;

		public boolean renderLine = true;

		public Color lineColor = new Color(0, 255, 0, 255);

		public float lineWidth = 5f;

		public boolean allowSkippingWaypoints = true;

		public boolean allowGoingBackwards = true;

		public boolean enableChatWaypoints = true;
	}

	public static class TeleportOverlay {
		public boolean enableTeleportOverlays = true;

		public boolean showWhenInAir = false;

		public Color teleportOverlayColor = new Color(0x7F761594, true);

		public boolean enableWeirdTransmission = false;

		public boolean enableInstantTransmission = false;

		public boolean enableEtherTransmission = true;

		public boolean enableSinrecallTransmission = false;

		public boolean enableWitherImpact = false;
	}

	public static class SmoothAOTE {
		public boolean predictive = false;

		public boolean enableWeirdTransmission = false;

		public boolean enableInstantTransmission = false;

		public boolean enableEtherTransmission = false;

		public boolean enableSinrecallTransmission = false;

		public boolean enableWitherImpact = false;

		public int maximumAddedLag = 100;
	}

	public static class SearchOverlay {
		public boolean enableBazaar = true;

		public boolean enableAuctionHouse = true;

		public boolean enableMuseum = true;

		public boolean keepPreviousSearches = false;

		public int maxSuggestions = 3;

		public int historyLength = 3;

		public boolean enableCommands = false;

		public List<String> bazaarHistory = new ArrayList<>();

		public List<String> auctionHistory = new ArrayList<>();

		public List<String> museumHistory = new ArrayList<>();
	}

	public static class BazaarQuickQuantities {
		public boolean enabled = false;

		public boolean closeSignOnUse = false;

		public int slot1Quantity = 28;

		public int slot2Quantity = 2240;

		public int slot3Quantity = 256;
	}

	public static class InputCalculator {
		public boolean enabled = true;

		public boolean requiresEquals = false;

		public boolean closeSignsWithEnter = true;
	}

	public static class FlameOverlay {
		public int flameHeight = 100;

		public int flameOpacity = 100;
	}

	public static class CompactDamage {
		public boolean enabled = true;

		@Deprecated
		public transient int precision = 1;

		public int maxPrecision = 4;

		public Color normalDamageColor = new Color(0xFFFFFF);

		public Color critDamageGradientStart = new Color(0xFFFF55);

		public Color critDamageGradientEnd = new Color(0xFF5555);
	}

	public static class HealthBars {
		public boolean enabled = false;

		public float scale = 1.5f;

		public boolean removeHealthFromName = true;

		public boolean removeMaxHealthFromName = true;

		public boolean applyToHealthOnlyMobs = true;

		public boolean hideFullHealth = false;

		public Color fullBarColor = new Color(0x00FF00);

		public Color halfBarColor = new Color(0xFFFF00);

		public Color emptyBarColor = new Color(0xFF0000);
	}

	public static class GyrokineticWandOverlay {
		public GyroOverlay.Mode gyroOverlayMode = GyroOverlay.Mode.OFF;

		public Color gyroOverlayColor = new Color(0x7F761594, true);
	}

	public static class ItemPickup {
		public boolean enabled = false;

		public boolean sackNotifications = false;

		public boolean showItemName = true;

		public int lifeTime = 3;

		public boolean splitNotifications = false;
	}
}
