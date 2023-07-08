package me.xmrvizzy.skyblocker.config;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.chat.ChatFilterResult;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.resource.language.I18n;

import java.util.ArrayList;
import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@Config(name = "skyblocker")
public class SkyblockerConfig implements ConfigData {

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.TransitiveObject
    public General general = new General();

    @ConfigEntry.Category("locations")
    @ConfigEntry.Gui.TransitiveObject
    public Locations locations = new Locations();

    @ConfigEntry.Category("slayer")
    @ConfigEntry.Gui.TransitiveObject
    public Slayer slayer = new Slayer();

    @ConfigEntry.Category("quickNav")
    @ConfigEntry.Gui.TransitiveObject
    public QuickNav quickNav = new QuickNav();

    @ConfigEntry.Category("messages")
    @ConfigEntry.Gui.TransitiveObject
    public Messages messages = new Messages();

    @ConfigEntry.Category("richPresence")
    @ConfigEntry.Gui.TransitiveObject
    public RichPresence richPresence = new RichPresence();

    public static class QuickNav {
        public boolean enableQuickNav = true;

        @ConfigEntry.Category("button1")
        @ConfigEntry.Gui.CollapsibleObject()
        public QuickNavItem button1 = new QuickNavItem(true, new ItemData("diamond_sword"), "Your Skills", "/skills");

        @ConfigEntry.Category("button2")
        @ConfigEntry.Gui.CollapsibleObject()
        public QuickNavItem button2 = new QuickNavItem(true, new ItemData("painting"), "Collections", "/collection");

        @ConfigEntry.Category("button3")
        @ConfigEntry.Gui.CollapsibleObject()
        public QuickNavItem button3 = new QuickNavItem(true, new ItemData("bone"), "\\(\\d+/\\d+\\) Pets", "/pets");

        @ConfigEntry.Category("button4")
        @ConfigEntry.Gui.CollapsibleObject()
        public QuickNavItem button4 = new QuickNavItem(true, new ItemData("leather_chestplate", 1, "tag:{display:{color:8991416}}"), "Wardrobe \\([12]/2\\)", "/wardrobe");

        @ConfigEntry.Category("button5")
        @ConfigEntry.Gui.CollapsibleObject()
        public QuickNavItem button5 = new QuickNavItem(true, new ItemData("player_head", 1, "tag:{SkullOwner:{Id:[I;-2081424676,-57521078,-2073572414,158072763],Properties:{textures:[{Value:\"ewogICJ0aW1lc3RhbXAiIDogMTU5MTMxMDU4NTYwOSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODBhMDc3ZTI0OGQxNDI3NzJlYTgwMDg2NGY4YzU3OGI5ZDM2ODg1YjI5ZGFmODM2YjY0YTcwNjg4MmI2ZWMxMCIKICAgIH0KICB9Cn0=\"}]}}}"), "Sack of Sacks", "/sacks");

        @ConfigEntry.Category("button6")
        @ConfigEntry.Gui.CollapsibleObject()
        public QuickNavItem button6 = new QuickNavItem(true, new ItemData("ender_chest"), "Storage", "/storage");

        @ConfigEntry.Category("button7")
        @ConfigEntry.Gui.CollapsibleObject()
        public QuickNavItem button7 = new QuickNavItem(true, new ItemData("player_head", 1, "tag:{SkullOwner:{Id:[I;-300151517,-631415889,-1193921967,-1821784279],Properties:{textures:[{Value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdjYzY2ODc0MjNkMDU3MGQ1NTZhYzUzZTA2NzZjYjU2M2JiZGQ5NzE3Y2Q4MjY5YmRlYmVkNmY2ZDRlN2JmOCJ9fX0=\"}]}}}"), "none", "/hub");

        @ConfigEntry.Category("button8")
        @ConfigEntry.Gui.CollapsibleObject()
        public QuickNavItem button8 = new QuickNavItem(true, new ItemData("player_head", 1, "tag:{SkullOwner:{Id:[I;1605800870,415127827,-1236127084,15358548],Properties:{textures:[{Value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg5MWQ1YjI3M2ZmMGJjNTBjOTYwYjJjZDg2ZWVmMWM0MGExYjk0MDMyYWU3MWU3NTQ3NWE1NjhhODI1NzQyMSJ9fX0=\"}]}}}"), "none", "/warp dungeon_hub");

        @ConfigEntry.Category("button9")
        @ConfigEntry.Gui.CollapsibleObject()
        public QuickNavItem button9 = new QuickNavItem(true, new ItemData("player_head", 1, "tag:{SkullOwner:{Id:[I;-562285948,532499670,-1705302742,775653035],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjVkZjU1NTkyNjQzMGQ1ZDc1YWRlZDIxZGQ5NjE5Yjc2YzViN2NhMmM3ZjU0MDE0NDA1MjNkNTNhOGJjZmFhYiJ9fX0=\"}]}}}"), "Visit prtl", "/visit prtl");

        @ConfigEntry.Category("button10")
        @ConfigEntry.Gui.CollapsibleObject()
        public QuickNavItem button10 = new QuickNavItem(true, new ItemData("enchanting_table"), "Enchant Item", "/etable");

        @ConfigEntry.Category("button11")
        @ConfigEntry.Gui.CollapsibleObject()
        public QuickNavItem button11 = new QuickNavItem(true, new ItemData("anvil"), "Anvil", "/anvil");

        @ConfigEntry.Category("button12")
        @ConfigEntry.Gui.CollapsibleObject()
        public QuickNavItem button12 = new QuickNavItem(true, new ItemData("crafting_table"), "Craft Item", "/craft");
    }

    public static class QuickNavItem {
        public QuickNavItem(Boolean render, ItemData itemData, String uiTitle, String clickEvent) {
            this.render = render;
            this.item = itemData;
            this.clickEvent = clickEvent;
            this.uiTitle = uiTitle;
        }

        public Boolean render;

        @ConfigEntry.Category("item")
        @ConfigEntry.Gui.CollapsibleObject()
        public ItemData item;

        public String uiTitle;
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

        public String itemName;
        public int count;
        public String nbt;
    }

    public static class General {
        public boolean enableUpdateNotification = true;
        public boolean acceptReparty = true;
        public boolean backpackPreviewWithoutShift = false;
        public boolean hideEmptyTooltips = true;

        @ConfigEntry.Category("tabHud")
        @ConfigEntry.Gui.CollapsibleObject()
        public TabHudConf tabHud = new TabHudConf();

        @ConfigEntry.Gui.Excluded
        public String apiKey;

        @ConfigEntry.Category("bars")
        @ConfigEntry.Gui.CollapsibleObject()
        public Bars bars = new Bars();

        @ConfigEntry.Category("experiments")
        @ConfigEntry.Gui.CollapsibleObject()
        public Experiments experiments = new Experiments();

        @ConfigEntry.Category("fishing")
        @ConfigEntry.Gui.CollapsibleObject()
        public Fishing fishing = new Fishing();

        @ConfigEntry.Category("fairySouls")
        @ConfigEntry.Gui.CollapsibleObject()
        public FairySouls fairySouls = new FairySouls();

        @ConfigEntry.Category("itemList")
        @ConfigEntry.Gui.CollapsibleObject()
        public ItemList itemList = new ItemList();

        @ConfigEntry.Category("itemTooltip")
        @ConfigEntry.Gui.CollapsibleObject()
        public ItemTooltip itemTooltip = new ItemTooltip();

        @ConfigEntry.Category("hitbox")
        @ConfigEntry.Gui.CollapsibleObject()
        public Hitbox hitbox = new Hitbox();

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.Category("titleContainer")
        @ConfigEntry.Gui.CollapsibleObject()
        public TitleContainer titleContainer = new TitleContainer();

        @ConfigEntry.Gui.Excluded
        public List<Integer> lockedSlots = new ArrayList<>();
    }

    public static class TabHudConf {
        public boolean tabHudEnabled = true;

        @ConfigEntry.BoundedDiscrete(min = 10, max = 200)
        @ConfigEntry.Gui.Tooltip()
        public int tabHudScale = 100;
    }

    public static class Bars {
        public boolean enableBars = true;

        @ConfigEntry.Category("barpositions")
        @ConfigEntry.Gui.CollapsibleObject()
        public BarPositions barpositions = new BarPositions();
    }

    public static class BarPositions {
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public BarPosition healthBarPosition = BarPosition.LAYER1;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public BarPosition manaBarPosition = BarPosition.LAYER1;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public BarPosition defenceBarPosition = BarPosition.LAYER1;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public BarPosition experienceBarPosition = BarPosition.LAYER1;

    }

    public enum BarPosition {
        LAYER1,
        LAYER2,
        RIGHT,
        NONE;

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
        public boolean enableChronomatronSolver = true;
        public boolean enableSuperpairsSolver = true;
        public boolean enableUltrasequencerSolver = true;
    }

    public static class Fishing {
        public boolean enableFishingHelper = true;
    }

    public static class FairySouls {
        public boolean enableFairySoulsHelper = false;
    }

    public static class Hitbox {
        public boolean oldFarmlandHitbox = true;
        public boolean oldLeverHitbox = false;
    }

    public static class TitleContainer {
        @ConfigEntry.BoundedDiscrete(min = 30, max = 140)
        public float titleContainerScale = 100;
        public int x = 540;
        public int y = 10;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public Direction direction = Direction.HORIZONTAL;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.DROPDOWN)
        public Alignment alignment = Alignment.MIDDLE;
    }

    public enum Direction {
        HORIZONTAL,
        VERTICAL;

        @Override
        public String toString() {
            return switch (this) {
                case HORIZONTAL -> "Horizontal";
                case VERTICAL -> "Vertical";
            };
        }
    }

    public enum Alignment {
        LEFT,
        RIGHT,
        MIDDLE;

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
        public boolean enableRichPresence = false;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        @ConfigEntry.Gui.Tooltip()
        public Info info = Info.LOCATION;
        public boolean cycleMode = false;
        public String customMessage = "Playing Skyblock";
    }

    public static class ItemList {
        public boolean enableItemList = true;
    }

    public enum Average {
        ONE_DAY,
        THREE_DAY,
        BOTH;

        @Override
        public String toString() {
            return I18n.translate("text.autoconfig.skyblocker.option.general.itemTooltip.avg." + name());
        }
    }

    public static class ItemTooltip {
        public boolean enableNPCPrice = true;
        @ConfigEntry.Gui.Tooltip
        public boolean enableMotesPrice = true;
        public boolean enableAvgBIN = true;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        @ConfigEntry.Gui.Tooltip()
        public Average avg = Average.THREE_DAY;
        public boolean enableLowestBIN = true;
        public boolean enableBazaarPrice = true;
        public boolean enableMuseumDate = true;
    }

    public static class Locations {
        @ConfigEntry.Category("barn")
        @ConfigEntry.Gui.CollapsibleObject()
        public Barn barn = new Barn();

        @ConfigEntry.Category("dungeons")
        @ConfigEntry.Gui.CollapsibleObject()
        public Dungeons dungeons = new Dungeons();

        @ConfigEntry.Category("dwarvenmines")
        @ConfigEntry.Gui.CollapsibleObject()
        public DwarvenMines dwarvenMines = new DwarvenMines();

        @ConfigEntry.Category("rift")
        @ConfigEntry.Gui.CollapsibleObject()
        public Rift rift = new Rift();
    }

    public static class Dungeons {
        @ConfigEntry.Gui.Tooltip()
        public boolean croesusHelper = true;
        public boolean enableMap = true;
        public float mapScaling = 1f;
        public int mapX = 2;
        public int mapY = 2;
        public boolean solveThreeWeirdos = true;
        public boolean blazesolver = true;
        public boolean solveTrivia = true;
        @ConfigEntry.Gui.CollapsibleObject
        public LividColor lividColor = new LividColor();
        @ConfigEntry.Gui.CollapsibleObject()
        public Terminals terminals = new Terminals();
    }

    public static class LividColor {
        @ConfigEntry.Gui.Tooltip()
        public boolean enableLividColor = true;
        @ConfigEntry.Gui.Tooltip()
        public String lividColorText = "The livid color is [color]";
    }

    public static class Terminals {
        public boolean solveColor = true;
        public boolean solveOrder = true;
        public boolean solveStartsWith = true;
    }

    public static class DwarvenMines {
        public boolean enableDrillFuel = true;
        public boolean solveFetchur = true;
        public boolean solvePuzzler = true;
        @ConfigEntry.Gui.CollapsibleObject()
        public DwarvenHud dwarvenHud = new DwarvenHud();
    }

    public static class DwarvenHud {
        public boolean enabled = true;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        @ConfigEntry.Gui.Tooltip(count = 3)
        public Style style = Style.SIMPLE;
        public boolean enableBackground = true;
        public int x = 10;
        public int y = 10;
    }

    public enum Style {
        SIMPLE,
        FANCY,
        CLASSIC;

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
        public boolean solveHungryHiker = true;
        public boolean solveTreasureHunter = true;
    }

    public static class Rift {
        public boolean mirrorverseWaypoints = true;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 5)
        @ConfigEntry.Gui.Tooltip
        public int mcGrubberStacks = 0;
    }

    public static class Slayer {
        @ConfigEntry.Category("vampire")
        @ConfigEntry.Gui.CollapsibleObject()
        public VampireSlayer vampireSlayer = new VampireSlayer();
    }

    public static class VampireSlayer {
        public boolean enableEffigyWaypoints = true;
        public boolean compactEffigyWaypoints;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        @ConfigEntry.Gui.Tooltip()
        public int effigyUpdateFrequency = 5;
        public boolean enableHolyIceIndicator = true;
        public int holyIceIndicatorTickDelay = 10;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        @ConfigEntry.Gui.Tooltip()
        public int holyIceUpdateFrequency = 5;
        public boolean enableHealingMelonIndicator = true;
        public float healingMelonHealthThreshold = 4F;
        public boolean enableSteakStakeIndicator = true;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        @ConfigEntry.Gui.Tooltip()
        public int steakStakeUpdateFrequency = 5;
        public boolean enableManiaIndicator = true;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        @ConfigEntry.Gui.Tooltip()
        public int maniaUpdateFrequency = 5;
    }

    public static class Messages {
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ChatFilterResult hideAbility = ChatFilterResult.PASS;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ChatFilterResult hideHeal = ChatFilterResult.PASS;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ChatFilterResult hideAOTE = ChatFilterResult.PASS;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ChatFilterResult hideImplosion = ChatFilterResult.PASS;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ChatFilterResult hideMoltenWave = ChatFilterResult.PASS;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ChatFilterResult hideAds = ChatFilterResult.PASS;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ChatFilterResult hideTeleportPad = ChatFilterResult.PASS;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ChatFilterResult hideCombo = ChatFilterResult.PASS;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ChatFilterResult hideAutopet = ChatFilterResult.PASS;
        @ConfigEntry.Gui.Tooltip()
        public boolean hideMana = false;
    }

    public enum Info {
        PURSE,
        BITS,
        LOCATION;

        @Override
        public String toString() {
            return I18n.translate("text.autoconfig.skyblocker.option.richPresence.info." + name());
        }
    }

    /**
     * Registers the config to AutoConfig and register commands to open the config screen.
     */
    public static void init() {
        AutoConfig.register(SkyblockerConfig.class, GsonConfigSerializer::new);
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(literal("skyblocker").then(optionsLiteral("config")).then(optionsLiteral("options")))));
    }

    /**
     * Registers an options command with the given name. Used for registering both options and config as valid commands.
     *
     * @param name the name of the command node
     * @return the command builder
     */
    private static LiteralArgumentBuilder<FabricClientCommandSource> optionsLiteral(String name) {
        return literal(name).executes(context -> {
            // Don't immediately open the next screen as it will be closed by ChatScreen right after this command is executed
            SkyblockerMod.getInstance().scheduler.queueOpenScreen(AutoConfig.getConfigScreen(SkyblockerConfig.class, null));
            return Command.SINGLE_SUCCESS;
        });
    }

    public static SkyblockerConfig get() {
        return AutoConfig.getConfigHolder(SkyblockerConfig.class).getConfig();
    }
}
