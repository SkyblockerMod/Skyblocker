package me.xmrvizzy.skyblocker.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import dev.isxander.yacl3.config.ConfigEntry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.xmrvizzy.skyblocker.utils.chat.ChatFilterResult;
import me.xmrvizzy.skyblocker.skyblock.item.CustomArmorTrims;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ConfigModel {
	@ConfigEntry
	public General general = new General();

	@ConfigEntry
	public Locations locations = new Locations();

	@ConfigEntry
	public Slayer slayer = new Slayer();

	@ConfigEntry
	public QuickNav quickNav = new QuickNav();

	@ConfigEntry
	public Messages messages = new Messages();

	@ConfigEntry
	public RichPresence richPresence = new RichPresence();

	public static class QuickNav {
		@ConfigEntry
		public boolean enableQuickNav = true;

		@ConfigEntry
		public QuickNavItem button1 = new QuickNavItem(true, new ItemData("diamond_sword"), "Your Skills", "/skills");

		@ConfigEntry
		public QuickNavItem button2 = new QuickNavItem(true, new ItemData("painting"), "Collections", "/collection");

		/* REGEX Explanation
		 * "Pets" : simple match on letters
         * "(?: \\(\\d+\\/\\d+\\))?" : optional match on the non-capturing group for the page in the format " ($number/$number)"
         */
		@ConfigEntry
		public QuickNavItem button3 = new QuickNavItem(true, new ItemData("bone"), "Pets(:? \\(\\d+\\/\\d+\\))?", "/pets");

		/* REGEX Explanation
		 * "Wardrobe" : simple match on letters
		 * " \\([12]\\/2\\)" : match on the page either " (1/2)" or " (2/2)"
		 */
		@ConfigEntry
		public QuickNavItem button4 = new QuickNavItem(true,
				new ItemData("leather_chestplate", 1, "tag:{display:{color:8991416}}"), "Wardrobe \\([12]/2\\)",
				"/wardrobe");

		@ConfigEntry
		public QuickNavItem button5 = new QuickNavItem(true, new ItemData("player_head", 1,
				"tag:{SkullOwner:{Id:[I;-2081424676,-57521078,-2073572414,158072763],Properties:{textures:[{Value:\"ewogICJ0aW1lc3RhbXAiIDogMTU5MTMxMDU4NTYwOSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODBhMDc3ZTI0OGQxNDI3NzJlYTgwMDg2NGY4YzU3OGI5ZDM2ODg1YjI5ZGFmODM2YjY0YTcwNjg4MmI2ZWMxMCIKICAgIH0KICB9Cn0=\"}]}}}"),
				"Sack of Sacks", "/sacks");

		/* REGEX Explanation
		 * "(?:Rift )?" : optional match on the non-capturing group "Rift "
		 * "Storage" : simple match on letters
		 * "(?: \\([12]\\/2\\))?" : optional match on the non-capturing group " (1/2)" or " (2/2)"
		 */
		@ConfigEntry
		public QuickNavItem button6 = new QuickNavItem(true, new ItemData("ender_chest"),
				"(?:Rift )?Storage(?: \\(1/2\\))?", "/storage");

		@ConfigEntry
		public QuickNavItem button7 = new QuickNavItem(true, new ItemData("player_head", 1,
				"tag:{SkullOwner:{Id:[I;-300151517,-631415889,-1193921967,-1821784279],Properties:{textures:[{Value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdjYzY2ODc0MjNkMDU3MGQ1NTZhYzUzZTA2NzZjYjU2M2JiZGQ5NzE3Y2Q4MjY5YmRlYmVkNmY2ZDRlN2JmOCJ9fX0=\"}]}}}"),
				"none", "/hub");

		@ConfigEntry
		public QuickNavItem button8 = new QuickNavItem(true, new ItemData("player_head", 1,
				"tag:{SkullOwner:{Id:[I;1605800870,415127827,-1236127084,15358548],Properties:{textures:[{Value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg5MWQ1YjI3M2ZmMGJjNTBjOTYwYjJjZDg2ZWVmMWM0MGExYjk0MDMyYWU3MWU3NTQ3NWE1NjhhODI1NzQyMSJ9fX0=\"}]}}}"),
				"none", "/warp dungeon_hub");

		@ConfigEntry
		public QuickNavItem button9 = new QuickNavItem(true, new ItemData("player_head", 1,
				"tag:{SkullOwner:{Id:[I;-562285948,532499670,-1705302742,775653035],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjVkZjU1NTkyNjQzMGQ1ZDc1YWRlZDIxZGQ5NjE5Yjc2YzViN2NhMmM3ZjU0MDE0NDA1MjNkNTNhOGJjZmFhYiJ9fX0=\"}]}}}"),
				"Visit prtl", "/visit prtl");

		@ConfigEntry
		public QuickNavItem button10 = new QuickNavItem(true, new ItemData("enchanting_table"), "Enchant Item",
				"/etable");

		@ConfigEntry
		public QuickNavItem button11 = new QuickNavItem(true, new ItemData("anvil"), "Anvil", "/anvil");

		@ConfigEntry
		public QuickNavItem button12 = new QuickNavItem(true, new ItemData("crafting_table"), "Craft Item", "/craft");
	}

	public static class QuickNavItem {
		public QuickNavItem(Boolean render, ItemData itemData, String uiTitle, String clickEvent) {
			this.render = render;
			this.item = itemData;
			this.clickEvent = clickEvent;
			this.uiTitle = uiTitle;
		}

		@ConfigEntry
		public Boolean render;

		@ConfigEntry
		public ItemData item;

		@ConfigEntry
		public String uiTitle;
		
		@ConfigEntry
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

		@ConfigEntry
		public String itemName;
		
		@ConfigEntry
		public int count;
		
		@ConfigEntry
		public String nbt;
	}

	public static class General {
		@ConfigEntry
		public boolean acceptReparty = true;
		
		@ConfigEntry
		public boolean backpackPreviewWithoutShift = false;
		
		@ConfigEntry
		public boolean compactorDeletorPreview = true;
		
		@ConfigEntry
		public boolean hideEmptyTooltips = true;
		
		@ConfigEntry
		public boolean hideStatusEffectOverlay = false;

		@ConfigEntry
		public TabHudConf tabHud = new TabHudConf();

		@ConfigEntry
		public Bars bars = new Bars();

		@ConfigEntry
		public Experiments experiments = new Experiments();

		@ConfigEntry
		public Fishing fishing = new Fishing();

		@ConfigEntry
		public FairySouls fairySouls = new FairySouls();

		@ConfigEntry
		public Shortcuts shortcuts = new Shortcuts();
		
		@ConfigEntry
		public QuiverWarning quiverWarning = new QuiverWarning();

		@ConfigEntry
		public ItemList itemList = new ItemList();

		@ConfigEntry
		public ItemTooltip itemTooltip = new ItemTooltip();

		@ConfigEntry
		public ItemInfoDisplay itemInfoDisplay = new ItemInfoDisplay();
		
		@ConfigEntry
		public SpecialEffects specialEffects = new SpecialEffects();

		@ConfigEntry
		public Hitbox hitbox = new Hitbox();

		@ConfigEntry
		public TitleContainer titleContainer = new TitleContainer();

		@ConfigEntry
		public TeleportOverlay teleportOverlay = new TeleportOverlay();

		@ConfigEntry
		public List<Integer> lockedSlots = new ArrayList<>();

		@ConfigEntry
		public Object2ObjectOpenHashMap<String, Text> customItemNames = new Object2ObjectOpenHashMap<>();

		@ConfigEntry
		public Object2IntOpenHashMap<String> customDyeColors = new Object2IntOpenHashMap<>();

		@ConfigEntry
		public Object2ObjectOpenHashMap<String, CustomArmorTrims.ArmorTrimId> customArmorTrims = new Object2ObjectOpenHashMap<>();
	}

	public static class TabHudConf {
		@ConfigEntry
		public boolean tabHudEnabled = true;

		@ConfigEntry
		public int tabHudScale = 100;
		
		@ConfigEntry
		public boolean plainPlayerNames = false;
		
		@ConfigEntry
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
		@ConfigEntry
		public boolean enableBars = true;

		@ConfigEntry
		public BarPositions barpositions = new BarPositions();
	}

	public static class BarPositions {
		@ConfigEntry
		public BarPosition healthBarPosition = BarPosition.LAYER1;
		
		@ConfigEntry
		public BarPosition manaBarPosition = BarPosition.LAYER1;
		
		@ConfigEntry
		public BarPosition defenceBarPosition = BarPosition.LAYER1;
		
		@ConfigEntry
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
		@ConfigEntry
		public boolean enableChronomatronSolver = true;
		
		@ConfigEntry
		public boolean enableSuperpairsSolver = true;
		
		@ConfigEntry
		public boolean enableUltrasequencerSolver = true;
	}

	public static class Fishing {
		@ConfigEntry
		public boolean enableFishingHelper = true;
	}

	public static class FairySouls {
		@ConfigEntry
		public boolean enableFairySoulsHelper = false;
		
		@ConfigEntry
		public boolean highlightFoundSouls = true;
		
		@ConfigEntry
		public boolean highlightOnlyNearbySouls = false;
	}

	public static class Shortcuts {
		@ConfigEntry
		public boolean enableShortcuts = true;
		
		@ConfigEntry
		public boolean enableCommandShortcuts = true;
		
		@ConfigEntry
		public boolean enableCommandArgShortcuts = true;
	}
	
    public static class QuiverWarning {
    	@ConfigEntry
        public boolean enableQuiverWarning = true;
    	
    	@ConfigEntry
        public boolean enableQuiverWarningInDungeons = true;
    	
    	@ConfigEntry
        public boolean enableQuiverWarningAfterDungeon = true;
    }

	public static class Hitbox {
		@ConfigEntry
		public boolean oldFarmlandHitbox = true;
		
		@ConfigEntry
		public boolean oldLeverHitbox = false;
	}

	public static class TitleContainer {
		@ConfigEntry
		public float titleContainerScale = 100;
		
		@ConfigEntry
		public int x = 540;
		
		@ConfigEntry
		public int y = 10;
		
		@ConfigEntry
		public Direction direction = Direction.HORIZONTAL;
		
		@ConfigEntry
		public Alignment alignment = Alignment.MIDDLE;
	}

	public static class TeleportOverlay {
		@ConfigEntry
		public boolean enableTeleportOverlays = true;
		
		@ConfigEntry
		public boolean enableWeirdTransmission = true;
		
		@ConfigEntry
		public boolean enableInstantTransmission = true;
		
		@ConfigEntry
		public boolean enableEtherTransmission = true;
		
		@ConfigEntry
		public boolean enableSinrecallTransmission = true;
		
		@ConfigEntry
		public boolean enableWitherImpact = true;
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

	public static class RichPresence {
		@ConfigEntry
		public boolean enableRichPresence = false;

		@ConfigEntry
		public Info info = Info.LOCATION;
		
		@ConfigEntry
		public boolean cycleMode = false;
		
		@ConfigEntry
		public String customMessage = "Playing Skyblock";
	}

	public static class ItemList {
		@ConfigEntry
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
		@ConfigEntry
		public boolean enableNPCPrice = true;
		
		@ConfigEntry
		public boolean enableMotesPrice = true;
		
		@ConfigEntry
		public boolean enableAvgBIN = true;
		
		@ConfigEntry
		public Average avg = Average.THREE_DAY;
		
		@ConfigEntry
		public boolean enableLowestBIN = true;
		
		@ConfigEntry
		public boolean enableBazaarPrice = true;
		
		@ConfigEntry
		public boolean enableMuseumDate = true;
	}

	public static class ItemInfoDisplay {
		@ConfigEntry
		public boolean attributeShardInfo = true;
	}
	
	public static class SpecialEffects {
		@ConfigEntry
		public boolean rareDungeonDropEffects = true;
	}

	public static class Locations {
		@ConfigEntry
		public Barn barn = new Barn();

		@ConfigEntry
		public Dungeons dungeons = new Dungeons();

		@ConfigEntry
		public DwarvenMines dwarvenMines = new DwarvenMines();

		@ConfigEntry
		public Rift rift = new Rift();
		
		@ConfigEntry
		public SpidersDen spidersDen = new SpidersDen();
	}

	public static class Dungeons {
		@ConfigEntry
		public SecretWaypoints secretWaypoints = new SecretWaypoints();
		
		@ConfigEntry
		public DungeonChestProfit dungeonChestProfit = new DungeonChestProfit();
		
		@ConfigEntry
		public boolean croesusHelper = true;
		
		@ConfigEntry
		public boolean enableMap = true;
		
		@ConfigEntry
		public float mapScaling = 1f;
		
		@ConfigEntry
		public int mapX = 2;
		
		@ConfigEntry
		public int mapY = 2;
		
		@ConfigEntry
		public boolean starredMobGlow = true;
		
		@ConfigEntry
		public boolean solveThreeWeirdos = true;
		
		@ConfigEntry
		public boolean blazesolver = true;
		
		@ConfigEntry
		public boolean solveTrivia = true;
		
		@ConfigEntry
		public boolean solveTicTacToe = true;
		
		@ConfigEntry
		public LividColor lividColor = new LividColor();
		
		@ConfigEntry
		public Terminals terminals = new Terminals();
	}

	public static class SecretWaypoints {
		@ConfigEntry
		public boolean enableSecretWaypoints = true;
		
		@ConfigEntry
		public boolean noInitSecretWaypoints = false;
		
		@ConfigEntry
		public boolean enableEntranceWaypoints = true;
		
		@ConfigEntry
		public boolean enableSuperboomWaypoints = true;
		
		@ConfigEntry
		public boolean enableChestWaypoints = true;
		
		@ConfigEntry
		public boolean enableItemWaypoints = true;
		
		@ConfigEntry
		public boolean enableBatWaypoints = true;
		
		@ConfigEntry
		public boolean enableWitherWaypoints = true;
		
		@ConfigEntry
		public boolean enableLeverWaypoints = true;
		
		@ConfigEntry
		public boolean enableFairySoulWaypoints = true;
		
		@ConfigEntry
		public boolean enableStonkWaypoints = true;
		
		@ConfigEntry
		public boolean enableDefaultWaypoints = true;
	}
	
	public static class DungeonChestProfit {
		@ConfigEntry
		public boolean enableProfitCalculator = true;
		
		@ConfigEntry
		public boolean includeKismet = false;
		
		@ConfigEntry
		public boolean includeEssence = true;
		
		@ConfigEntry
		public int neutralThreshold = 1000;
		
		@ConfigEntry
		public FormattingOption neutralColor = FormattingOption.DARK_GRAY;
		
		@ConfigEntry
		public FormattingOption profitColor = FormattingOption.DARK_GREEN;
		
		@ConfigEntry
		public FormattingOption lossColor = FormattingOption.RED;
		
		@ConfigEntry
		public FormattingOption incompleteColor = FormattingOption.BLUE;
		
	}
	
	public enum FormattingOption {
		BLACK(Formatting.BLACK),
		DARK_BLUE(Formatting.DARK_BLUE),
		DARK_GREEN(Formatting.DARK_GREEN),
		DARK_AQUA(Formatting.DARK_AQUA),
		DARK_RED(Formatting.DARK_RED),
		DARK_PURPLE(Formatting.DARK_PURPLE),
		GOLD(Formatting.GOLD),
		GRAY(Formatting.GRAY),
		DARK_GRAY(Formatting.DARK_GRAY),
		BLUE(Formatting.BLUE),
		GREEN(Formatting.GREEN),
		AQUA(Formatting.AQUA),
		RED(Formatting.RED),
		LIGHT_PURPLE(Formatting.LIGHT_PURPLE),
		YELLOW(Formatting.YELLOW),
		WHITE(Formatting.WHITE),
		OBFUSCATED(Formatting.OBFUSCATED),
		BOLD(Formatting.BOLD),
		STRIKETHROUGH(Formatting.STRIKETHROUGH),
		UNDERLINE(Formatting.UNDERLINE),
		ITALIC(Formatting.ITALIC),
		RESET(Formatting.RESET);
		
		public final Formatting formatting;
		
		
		FormattingOption(Formatting formatting) {
			this.formatting = formatting;
		}
		
		@Override
		public String toString() {
			return StringUtils.capitalize(formatting.getName().replaceAll("_", " "));
		}
	}

	public static class LividColor {
		@ConfigEntry
		public boolean enableLividColor = true;
		
		@ConfigEntry
		public String lividColorText = "The livid color is [color]";
	}

	public static class Terminals {
		@ConfigEntry
		public boolean solveColor = true;
		
		@ConfigEntry
		public boolean solveOrder = true;
		
		@ConfigEntry
		public boolean solveStartsWith = true;
	}

	public static class DwarvenMines {
		@ConfigEntry
		public boolean enableDrillFuel = true;
		
		@ConfigEntry
		public boolean solveFetchur = true;
		
		@ConfigEntry
		public boolean solvePuzzler = true;
		
		@ConfigEntry
		public DwarvenHud dwarvenHud = new DwarvenHud();
	}

	public static class DwarvenHud {
		@ConfigEntry
		public boolean enabled = true;
		
		@ConfigEntry
		public DwarvenHudStyle style = DwarvenHudStyle.SIMPLE;
		
		@ConfigEntry
		public boolean enableBackground = true;
		
		@ConfigEntry
		public int x = 10;
		
		@ConfigEntry
		public int y = 10;
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
		@ConfigEntry
		public boolean solveHungryHiker = true;
		
		@ConfigEntry
		public boolean solveTreasureHunter = true;
	}

	public static class Rift {
		@ConfigEntry
		public boolean mirrorverseWaypoints = true;
		
		@ConfigEntry
		public int mcGrubberStacks = 0;
	}
	
	public static class SpidersDen {
		@ConfigEntry
		public Relics relics = new Relics();
	}
	
	public static class Relics {
		@ConfigEntry
		public boolean enableRelicsHelper = false;
		
		@ConfigEntry
		public boolean highlightFoundRelics = true;
	}

	public static class Slayer {
		@ConfigEntry
		public VampireSlayer vampireSlayer = new VampireSlayer();
	}

	public static class VampireSlayer {
		@ConfigEntry
		public boolean enableEffigyWaypoints = true;
		
		@ConfigEntry
		public boolean compactEffigyWaypoints;
		
		@ConfigEntry
		public int effigyUpdateFrequency = 5;
		
		@ConfigEntry
		public boolean enableHolyIceIndicator = true;
		
		@ConfigEntry
		public int holyIceIndicatorTickDelay = 10;

		@ConfigEntry
		public int holyIceUpdateFrequency = 5;
		
		@ConfigEntry
		public boolean enableHealingMelonIndicator = true;
		
		@ConfigEntry
		public float healingMelonHealthThreshold = 4f;
		
		@ConfigEntry
		public boolean enableSteakStakeIndicator = true;

		@ConfigEntry
		public int steakStakeUpdateFrequency = 5;
		
		@ConfigEntry
		public boolean enableManiaIndicator = true;

		@ConfigEntry
		public int maniaUpdateFrequency = 5;
	}

	public static class Messages {
		@ConfigEntry
		public ChatFilterResult hideAbility = ChatFilterResult.PASS;
		
		@ConfigEntry
		public ChatFilterResult hideHeal = ChatFilterResult.PASS;
		
		@ConfigEntry
		public ChatFilterResult hideAOTE = ChatFilterResult.PASS;
		
		@ConfigEntry
		public ChatFilterResult hideImplosion = ChatFilterResult.PASS;
		
		@ConfigEntry
		public ChatFilterResult hideMoltenWave = ChatFilterResult.PASS;
		
		@ConfigEntry
		public ChatFilterResult hideAds = ChatFilterResult.PASS;
		
		@ConfigEntry
		public ChatFilterResult hideTeleportPad = ChatFilterResult.PASS;
		
		@ConfigEntry
		public ChatFilterResult hideCombo = ChatFilterResult.PASS;
		
		@ConfigEntry
		public ChatFilterResult hideAutopet = ChatFilterResult.PASS;
		
		@ConfigEntry
		public ChatFilterResult hideShowOff = ChatFilterResult.PASS;
		
		@ConfigEntry
		public boolean hideMana = false;
	}

	public enum Info {
		PURSE, BITS, LOCATION;

		@Override
		public String toString() {
			return I18n.translate("text.autoconfig.skyblocker.option.richPresence.info." + name());
		}
	}
}
