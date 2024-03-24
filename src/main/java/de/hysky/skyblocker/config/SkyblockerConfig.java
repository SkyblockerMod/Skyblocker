package de.hysky.skyblocker.config;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.item.CustomArmorAnimatedDyes;
import de.hysky.skyblocker.skyblock.item.CustomArmorTrims;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class SkyblockerConfig {
	@SerialEntry
	public int version = 1;

	@SerialEntry
	public General general = new General();

	@SerialEntry
	public Locations locations = new Locations();

	@SerialEntry
	public Slayer slayer = new Slayer();

	@SerialEntry
	public QuickNav quickNav = new QuickNav();

	@SerialEntry
	public Messages messages = new Messages();

	@SerialEntry
	public RichPresence richPresence = new RichPresence();

	public static class QuickNav {
		@SerialEntry
		public boolean enableQuickNav = true;

		@SerialEntry
		public QuickNavItem button1 = new QuickNavItem(true, new ItemData("diamond_sword"), "Your Skills", "/skills");

		@SerialEntry
		public QuickNavItem button2 = new QuickNavItem(true, new ItemData("painting"), "Collections", "/collection");

		/* REGEX Explanation
		 * "Pets" : simple match on letters
		 * "(?: \\(\\d+\\/\\d+\\))?" : optional match on the non-capturing group for the page in the format " ($number/$number)"
		 */
		@SerialEntry
		public QuickNavItem button3 = new QuickNavItem(true, new ItemData("bone"), "Pets(:? \\(\\d+\\/\\d+\\))?", "/pets");

		/* REGEX Explanation
		 * "Wardrobe" : simple match on letters
		 * " \\([12]\\/2\\)" : match on the page either " (1/2)" or " (2/2)"
		 */
		@SerialEntry
		public QuickNavItem button4 = new QuickNavItem(true,
				new ItemData("leather_chestplate", 1, "tag:{display:{color:8991416}}"), "Wardrobe \\([12]/2\\)",
				"/wardrobe");

		@SerialEntry
		public QuickNavItem button5 = new QuickNavItem(true, new ItemData("player_head", 1,
				"tag:{SkullOwner:{Id:[I;-2081424676,-57521078,-2073572414,158072763],Properties:{textures:[{Value:\"ewogICJ0aW1lc3RhbXAiIDogMTU5MTMxMDU4NTYwOSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODBhMDc3ZTI0OGQxNDI3NzJlYTgwMDg2NGY4YzU3OGI5ZDM2ODg1YjI5ZGFmODM2YjY0YTcwNjg4MmI2ZWMxMCIKICAgIH0KICB9Cn0=\"}]}}}"),
				"Sack of Sacks", "/sacks");

		/* REGEX Explanation
		 * "(?:Rift )?" : optional match on the non-capturing group "Rift "
		 * "Storage" : simple match on letters
		 * "(?: \\([12]\\/2\\))?" : optional match on the non-capturing group " (1/2)" or " (2/2)"
		 */
		@SerialEntry
		public QuickNavItem button6 = new QuickNavItem(true, new ItemData("ender_chest"),
				"(?:Rift )?Storage(?: \\(1/2\\))?", "/storage");

		@SerialEntry
		public QuickNavItem button7 = new QuickNavItem(true, new ItemData("player_head", 1,
				"tag:{SkullOwner:{Id:[I;-300151517,-631415889,-1193921967,-1821784279],Properties:{textures:[{Value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdjYzY2ODc0MjNkMDU3MGQ1NTZhYzUzZTA2NzZjYjU2M2JiZGQ5NzE3Y2Q4MjY5YmRlYmVkNmY2ZDRlN2JmOCJ9fX0=\"}]}}}"),
				"none", "/hub");

		@SerialEntry
		public QuickNavItem button8 = new QuickNavItem(true, new ItemData("player_head", 1,
				"tag:{SkullOwner:{Id:[I;1605800870,415127827,-1236127084,15358548],Properties:{textures:[{Value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg5MWQ1YjI3M2ZmMGJjNTBjOTYwYjJjZDg2ZWVmMWM0MGExYjk0MDMyYWU3MWU3NTQ3NWE1NjhhODI1NzQyMSJ9fX0=\"}]}}}"),
				"none", "/warp dungeon_hub");

		@SerialEntry
		public QuickNavItem button9 = new QuickNavItem(true, new ItemData("player_head", 1,
				"tag:{SkullOwner:{Id:[I;-562285948,532499670,-1705302742,775653035],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjVkZjU1NTkyNjQzMGQ1ZDc1YWRlZDIxZGQ5NjE5Yjc2YzViN2NhMmM3ZjU0MDE0NDA1MjNkNTNhOGJjZmFhYiJ9fX0=\"}]}}}"),
				"Visit prtl", "/visit prtl");

		@SerialEntry
		public QuickNavItem button10 = new QuickNavItem(true, new ItemData("enchanting_table"), "Enchant Item",
				"/etable");


		@SerialEntry
		public QuickNavItem button11 = new QuickNavItem(true, new ItemData("anvil"), "Anvil", "/anvil");

		@SerialEntry
		public QuickNavItem button12 = new QuickNavItem(true, new ItemData("crafting_table"), "Craft Item", "/craft");
	}

	public static class QuickNavItem {
		public QuickNavItem(Boolean render, ItemData itemData, String uiTitle, String clickEvent) {
			this.render = render;
			this.item = itemData;
			this.clickEvent = clickEvent;
			this.uiTitle = uiTitle;
		}

		@SerialEntry
		public Boolean render;

		@SerialEntry
		public ItemData item;

		@SerialEntry
		public String uiTitle;

		@SerialEntry
		public String clickEvent;
	}

	public static class ItemData {
		public ItemData(String itemName, int count, String nbt) {
			this.itemName = itemName;
			this.count = count;
			this.nbt = nbt;
		}

		public ItemData(String itemName) {
			this.itemName = itemName;
			this.count = 1;
			this.nbt = "";
		}

		@SerialEntry
		public String itemName;

		@SerialEntry
		public int count;

		@SerialEntry
		public String nbt;
	}

	public static class General {
		@SerialEntry
		public boolean enableTips = true;

		@SerialEntry
		public boolean acceptReparty = true;

		@SerialEntry
		public boolean betterPartyFinder = true;

		@SerialEntry
		public boolean fancyCraftingTable = true;

		@SerialEntry
		public boolean backpackPreviewWithoutShift = false;

		@SerialEntry
		public boolean compactorDeletorPreview = true;

		@SerialEntry
		public boolean hideEmptyTooltips = true;

		@SerialEntry
		public boolean hideStatusEffectOverlay = false;

		@SerialEntry
		public boolean dontStripSkinAlphaValues = true;

		@SerialEntry
		public boolean dungeonQuality = true;

		@SerialEntry
		public boolean enableNewYearCakesHelper = true;

		@SerialEntry
		public TabHudConf tabHud = new TabHudConf();

		@SerialEntry
		public Bars bars = new Bars();

		@SerialEntry
		public Experiments experiments = new Experiments();

		@SerialEntry
		public Fishing fishing = new Fishing();

		@SerialEntry
		public FairySouls fairySouls = new FairySouls();

		@SerialEntry
		public MythologicalRitual mythologicalRitual = new MythologicalRitual();

		@SerialEntry
		public ItemCooldown itemCooldown = new ItemCooldown();

		@SerialEntry
		public Shortcuts shortcuts = new Shortcuts();

		@SerialEntry
		public Waypoints waypoints = new Waypoints();

		@SerialEntry
		public QuiverWarning quiverWarning = new QuiverWarning();

		@SerialEntry
		public ItemList itemList = new ItemList();

		@SerialEntry
		public ItemTooltip itemTooltip = new ItemTooltip();

		@SerialEntry
		public ItemInfoDisplay itemInfoDisplay = new ItemInfoDisplay();

		@SerialEntry
		public ItemProtection itemProtection = new ItemProtection();

		@SerialEntry
		public WikiLookup wikiLookup = new WikiLookup();

		@SerialEntry
		public ChestValue chestValue = new ChestValue();

		@SerialEntry
		public SpecialEffects specialEffects = new SpecialEffects();

		@SerialEntry
		public Hitbox hitbox = new Hitbox();

		@SerialEntry
		public TitleContainer titleContainer = new TitleContainer();

		@SerialEntry
		public TeleportOverlay teleportOverlay = new TeleportOverlay();

		@SerialEntry
		public FlameOverlay flameOverlay = new FlameOverlay();

		@SerialEntry
		public SearchOverlay searchOverlay = new SearchOverlay();

		@SerialEntry
		public FancyAuctionHouse fancyAuctionHouse = new FancyAuctionHouse();

		@SerialEntry
		public List<Integer> lockedSlots = new ArrayList<>();

		@SerialEntry
		public ObjectOpenHashSet<String> protectedItems = new ObjectOpenHashSet<>();

		@SerialEntry
		public Object2ObjectOpenHashMap<String, Text> customItemNames = new Object2ObjectOpenHashMap<>();

		@SerialEntry
		public Object2IntOpenHashMap<String> customDyeColors = new Object2IntOpenHashMap<>();

		@SerialEntry
		public Object2ObjectOpenHashMap<String, CustomArmorTrims.ArmorTrimId> customArmorTrims = new Object2ObjectOpenHashMap<>();

		@SerialEntry
		public Object2ObjectOpenHashMap<String, CustomArmorAnimatedDyes.AnimatedDye> customAnimatedDyes = new Object2ObjectOpenHashMap<>();
	}

	public static class FancyAuctionHouse {
		@SerialEntry
		public boolean enabled = true;
		@SerialEntry
		public boolean highlightCheapBIN = true;
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

	public static class Bars {
		@SerialEntry
		public boolean enableBars = true;

		@SerialEntry
		public BarPositions barPositions = new BarPositions();
	}

	public static class BarPositions {
		@SerialEntry
		public BarPosition healthBarPosition = BarPosition.LAYER1;

		@SerialEntry
		public BarPosition manaBarPosition = BarPosition.LAYER1;

		@SerialEntry
		public BarPosition defenceBarPosition = BarPosition.LAYER1;

		@SerialEntry
		public BarPosition experienceBarPosition = BarPosition.LAYER1;

	}

	public enum BarPosition {
		LAYER1, LAYER2, RIGHT, NONE;

		@Override
		public String toString() {
			return I18n.translate("text.autoconfig.skyblocker.option.general.bars.barpositions." + name());
		}

		public int toInt() {
			return switch (this) {
				case LAYER1 -> 0;
				case LAYER2 -> 1;
				case RIGHT -> 2;
				case NONE -> -1;
			};
		}
	}

	public static class Experiments {
		@SerialEntry
		public boolean enableChronomatronSolver = true;

		@SerialEntry
		public boolean enableSuperpairsSolver = true;

		@SerialEntry
		public boolean enableUltrasequencerSolver = true;
	}

	public static class Fishing {
		@SerialEntry
		public boolean enableFishingHelper = true;
	}

	public static class FairySouls {
		@SerialEntry
		public boolean enableFairySoulsHelper = false;

		@SerialEntry
		public boolean highlightFoundSouls = true;

		@SerialEntry
		public boolean highlightOnlyNearbySouls = false;
	}

	public static class MythologicalRitual {
		@SerialEntry
		public boolean enableMythologicalRitualHelper = true;
	}

	public static class ItemCooldown {
		@SerialEntry
		public boolean enableItemCooldowns = true;
	}

	public static class Shortcuts {
		@SerialEntry
		public boolean enableShortcuts = true;

		@SerialEntry
		public boolean enableCommandShortcuts = true;

		@SerialEntry
		public boolean enableCommandArgShortcuts = true;
	}

	public static class Waypoints {
		@SerialEntry
		public boolean enableWaypoints = true;

		@SerialEntry
		public Waypoint.Type waypointType = Waypoint.Type.WAYPOINT;
	}

	public static class QuiverWarning {
		@SerialEntry
		public boolean enableQuiverWarning = true;

		@SerialEntry
		public boolean enableQuiverWarningInDungeons = true;

		@SerialEntry
		public boolean enableQuiverWarningAfterDungeon = true;
	}

	public static class Hitbox {
		@SerialEntry
		public boolean oldFarmlandHitbox = false;

		@SerialEntry
		public boolean oldLeverHitbox = false;
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

	public static class FlameOverlay {
		@SerialEntry
		public int flameHeight = 100;

		@SerialEntry
		public int flameOpacity = 100;
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

	public static class RichPresence {
		@SerialEntry
		public boolean enableRichPresence = false;

		@SerialEntry
		public Info info = Info.LOCATION;

		@SerialEntry
		public boolean cycleMode = false;

		@SerialEntry
		public String customMessage = "Playing Skyblock";
	}

	public static class ItemList {
		@SerialEntry
		public boolean enableItemList = true;
	}

	public enum Average {
		ONE_DAY, THREE_DAY, BOTH;

		@Override
		public String toString() {
			return I18n.translate("text.autoconfig.skyblocker.option.general.itemTooltip.avg." + name());
		}
	}

	public static class ItemTooltip {
		@SerialEntry
		public boolean enableNPCPrice = true;

		@SerialEntry
		public boolean enableMotesPrice = true;

		@SerialEntry
		public boolean enableAvgBIN = true;

		@SerialEntry
		public Average avg = Average.THREE_DAY;

		@SerialEntry
		public boolean enableLowestBIN = true;

		@SerialEntry
		public boolean enableBazaarPrice = true;

		@SerialEntry
		public boolean enableObtainedDate = true;

		@SerialEntry
		public boolean enableMuseumInfo = true;

		@SerialEntry
		public boolean enableExoticTooltip = true;

		@SerialEntry
		public boolean enableAccessoriesHelper = true;
	}

	public static class ItemInfoDisplay {
		@SerialEntry
		public boolean attributeShardInfo = true;

		@SerialEntry
		public boolean itemRarityBackgrounds = false;

		@SerialEntry
		public RarityBackgroundStyle itemRarityBackgroundStyle = RarityBackgroundStyle.CIRCULAR;

		@SerialEntry
		public float itemRarityBackgroundsOpacity = 1f;
	}

	public enum RarityBackgroundStyle {
		CIRCULAR(new Identifier(SkyblockerMod.NAMESPACE, "item_rarity_background_circular")),
		SQUARE(new Identifier(SkyblockerMod.NAMESPACE, "item_rarity_background_square"));

		public final Identifier tex;

		RarityBackgroundStyle(Identifier tex) {
			this.tex = tex;
		}

		@Override
		public String toString() {
			return switch (this) {
				case CIRCULAR -> "Circular";
				case SQUARE -> "Square";
			};
		}
	}

	public static class ItemProtection {
		@SerialEntry
		public SlotLockStyle slotLockStyle = SlotLockStyle.FANCY;
	}

	public enum SlotLockStyle {
		CLASSIC(new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/slot_lock.png")),
		FANCY(new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/fancy_slot_lock.png"));

		public final Identifier tex;

		SlotLockStyle(Identifier tex) {
			this.tex = tex;
		}

		@Override
		public String toString() {
			return switch (this) {
				case CLASSIC -> "Classic";
				case FANCY -> "FANCY";
			};
		}
	}

	public static class WikiLookup {
		@SerialEntry
		public boolean enableWikiLookup = true;

		@SerialEntry
		public boolean officialWiki = true;
	}

	public static class ChestValue {
		@SerialEntry
		public boolean enableChestValue = true;

		@SerialEntry
		public Formatting color = Formatting.DARK_GREEN;

		@SerialEntry
		public Formatting incompleteColor = Formatting.BLUE;
	}

	public static class SpecialEffects {
		@SerialEntry
		public boolean rareDungeonDropEffects = true;
	}

	public static class Locations {
		@SerialEntry
		public Barn barn = new Barn();

		@SerialEntry
		public CrimsonIsle crimsonIsle = new CrimsonIsle();

		@SerialEntry
		public Dungeons dungeons = new Dungeons();

		@SerialEntry
		public DwarvenMines dwarvenMines = new DwarvenMines();

		@SerialEntry
		public Rift rift = new Rift();

		@SerialEntry
		public TheEnd end = new TheEnd();

		@SerialEntry
		public SpidersDen spidersDen = new SpidersDen();

		@SerialEntry
		public Garden garden = new Garden();
	}

	public static class Dungeons {
		@SerialEntry
		public SecretWaypoints secretWaypoints = new SecretWaypoints();

		@SerialEntry
		public DoorHighlight doorHighlight = new DoorHighlight();

		@SerialEntry
		public DungeonScore dungeonScore = new DungeonScore();

		@SerialEntry
		public DungeonChestProfit dungeonChestProfit = new DungeonChestProfit();

		@SerialEntry
		public MimicMessage mimicMessage = new MimicMessage();

		@SerialEntry
		public boolean croesusHelper = true;

		@SerialEntry
		public boolean enableMap = true;

		@SerialEntry
		public float mapScaling = 1f;

		@SerialEntry
		public int mapX = 2;

		@SerialEntry
		public int mapY = 2;

		@SerialEntry
		public boolean playerSecretsTracker = false;

		@SerialEntry
		public boolean starredMobGlow = true;

		@SerialEntry
		public boolean solveThreeWeirdos = true;

		@SerialEntry
		public boolean blazeSolver = true;

		@SerialEntry
		public boolean creeperSolver = true;

		@SerialEntry
		public boolean solveTrivia = true;

		@SerialEntry
		public boolean solveTicTacToe = true;

		@SerialEntry
		public boolean solveWaterboard = true;

		@SerialEntry
		public boolean solveBoulder = true;

		@SerialEntry
		public boolean solveIceFill = true;

		@SerialEntry
		public boolean solveSilverfish = true;

		@SerialEntry
		public boolean fireFreezeStaffTimer = true;

		@SerialEntry
		public boolean floor3GuardianHealthDisplay = true;

		@SerialEntry
		public boolean allowDroppingProtectedItems = false;

		@SerialEntry
		public LividColor lividColor = new LividColor();

		@SerialEntry
		public Terminals terminals = new Terminals();
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
				return switch (this) {
					case HIGHLIGHT -> "Highlight";
					case OUTLINED_HIGHLIGHT -> "Outlined Highlight";
					case OUTLINE -> "Outline";
				};
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

	public static class MimicMessage {
		@SerialEntry
		public boolean sendMimicMessage = true;

		@SerialEntry
		public String mimicMessage = "Mimic dead!";
	}

	public static class LividColor {
		@SerialEntry
		public boolean enableLividColorGlow = true;

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
	}

	public static class DwarvenMines {
		@SerialEntry
		public boolean enableDrillFuel = true;

		@SerialEntry
		public boolean solveFetchur = true;

		@SerialEntry
		public boolean solvePuzzler = true;

		@SerialEntry
		public DwarvenHud dwarvenHud = new DwarvenHud();

		@SerialEntry
		public CrystalsHud crystalsHud = new CrystalsHud();

		@SerialEntry
		public CrystalsWaypoints crystalsWaypoints = new CrystalsWaypoints();
	}

	public static class DwarvenHud {
		@SerialEntry
		public boolean enabledCommissions = true;

		@SerialEntry
		public boolean enabledPowder = true;

		@SerialEntry
		public DwarvenHudStyle style = DwarvenHudStyle.SIMPLE;

		@SerialEntry
		public int x = 10;

		@SerialEntry
		public int y = 10;

		@SerialEntry
		public int powderX = 10;

		@SerialEntry
		public int powderY = 70;
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

	public static class Barn {
		@SerialEntry
		public boolean solveHungryHiker = true;

		@SerialEntry
		public boolean solveTreasureHunter = true;
	}

	public static class CrimsonIsle {
		@SerialEntry
		public Kuudra kuudra = new Kuudra();
	}

	public static class Kuudra {
		@SerialEntry
		public boolean supplyWaypoints = true;

		@SerialEntry
		public boolean fuelWaypoints = true;

		@SerialEntry
		public Waypoint.Type suppliesAndFuelWaypointType = Waypoint.Type.WAYPOINT;

		@SerialEntry
		public boolean ballistaBuildWaypoints = true;

		@SerialEntry
		public boolean safeSpotWaypoints = true;

		@SerialEntry
		public boolean pearlWaypoints = true;

		@SerialEntry
		public boolean noArrowPoisonWarning = true;

		@SerialEntry
		public int arrowPoisonThreshold = 32;
	}

	public static class Rift {
		@SerialEntry
		public boolean mirrorverseWaypoints = true;

		@SerialEntry
		public boolean blobbercystGlow = true;

		@SerialEntry
		public boolean enigmaSoulWaypoints = false;

		@SerialEntry
		public boolean highlightFoundEnigmaSouls = true;

		@SerialEntry
		public int mcGrubberStacks = 0;
	}

	public static class TheEnd {
		@SerialEntry
		public boolean enableEnderNodeHelper = true;

		@SerialEntry
		public boolean hudEnabled = true;

		@SerialEntry
		public boolean waypoint = true;

		@SerialEntry
		public int x = 10;

		@SerialEntry
		public int y = 10;
	}

	public static class SpidersDen {
		@SerialEntry
		public Relics relics = new Relics();
	}

	public static class Relics {
		@SerialEntry
		public boolean enableRelicsHelper = false;

		@SerialEntry
		public boolean highlightFoundRelics = true;
	}

	public static class Garden {
		@SerialEntry
		public FarmingHud farmingHud = new FarmingHud();

		@SerialEntry
		public boolean dicerTitlePrevent = true;

		@SerialEntry
		public boolean visitorHelper = true;

		@SerialEntry
		public boolean lockMouseTool = false;

		@SerialEntry
		public boolean lockMouseGroundOnly = false;
	}

	public static class FarmingHud {
		@SerialEntry
		public boolean enableHud = true;

		@SerialEntry
		public int x;

		@SerialEntry
		public int y;
	}

	public static class Slayer {
		@SerialEntry
		public EndermanSlayer endermanSlayer = new EndermanSlayer();

		@SerialEntry
		public VampireSlayer vampireSlayer = new VampireSlayer();
	}

	public static class EndermanSlayer {
		@SerialEntry
		public boolean enableYangGlyphsNotification = true;

		@SerialEntry
		public boolean highlightBeacons = true;

		@SerialEntry
		public boolean highlightNukekubiHeads = true;
	}

	public static class VampireSlayer {
		@SerialEntry
		public boolean enableEffigyWaypoints = true;

		@SerialEntry
		public boolean compactEffigyWaypoints;

		@SerialEntry
		public int effigyUpdateFrequency = 5;

		@SerialEntry
		public boolean enableHolyIceIndicator = true;

		@SerialEntry
		public int holyIceIndicatorTickDelay = 10;

		@SerialEntry
		public int holyIceUpdateFrequency = 5;

		@SerialEntry
		public boolean enableHealingMelonIndicator = true;

		@SerialEntry
		public float healingMelonHealthThreshold = 4f;

		@SerialEntry
		public boolean enableSteakStakeIndicator = true;

		@SerialEntry
		public int steakStakeUpdateFrequency = 5;

		@SerialEntry
		public boolean enableManiaIndicator = true;

		@SerialEntry
		public int maniaUpdateFrequency = 5;
	}

	public static class Messages {
		@SerialEntry
		public ChatFilterResult hideAbility = ChatFilterResult.PASS;

		@SerialEntry
		public ChatFilterResult hideHeal = ChatFilterResult.PASS;

		@SerialEntry
		public ChatFilterResult hideAOTE = ChatFilterResult.PASS;

		@SerialEntry
		public ChatFilterResult hideImplosion = ChatFilterResult.PASS;

		@SerialEntry
		public ChatFilterResult hideMoltenWave = ChatFilterResult.PASS;

		@SerialEntry
		public ChatFilterResult hideAds = ChatFilterResult.PASS;

		@SerialEntry
		public ChatFilterResult hideTeleportPad = ChatFilterResult.PASS;

		@SerialEntry
		public ChatFilterResult hideCombo = ChatFilterResult.PASS;

		@SerialEntry
		public ChatFilterResult hideAutopet = ChatFilterResult.PASS;

		@SerialEntry
		public ChatFilterResult hideShowOff = ChatFilterResult.PASS;

		@SerialEntry
		public ChatFilterResult hideToggleSkyMall = ChatFilterResult.PASS;

		@SerialEntry
		public ChatFilterResult hideMimicKill = ChatFilterResult.PASS;

		@SerialEntry
		public ChatFilterResult hideDeath = ChatFilterResult.PASS;

		@SerialEntry
		public boolean hideMana = false;

		@SerialEntry
		public ChatFilterResult hideDicer = ChatFilterResult.PASS;

		@SerialEntry
		public ChatRuleConfig chatRuleConfig = new ChatRuleConfig();
	}

	public static class ChatRuleConfig {
		@SerialEntry
		public int announcementLength = 60;
		@SerialEntry
		public int announcementScale = 3;
	}

	public enum Info {
		PURSE, BITS, LOCATION;

		@Override
		public String toString() {
			return I18n.translate("text.autoconfig.skyblocker.option.richPresence.info." + name());
		}
	}
}
