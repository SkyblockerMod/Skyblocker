package me.xmrvizzy.skyblocker.config;

import java.util.ArrayList;
import java.util.List;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.xmrvizzy.skyblocker.chat.ChatFilterResult;

@Config(name = "skyblocker")
public class SkyblockerConfig implements ConfigData {

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.TransitiveObject
    public final General general = new General();

    @ConfigEntry.Category("locations")
    @ConfigEntry.Gui.TransitiveObject
    public final Locations locations = new Locations();

    @ConfigEntry.Category("messages")
    @ConfigEntry.Gui.TransitiveObject
    public final Messages messages = new Messages();

    @ConfigEntry.Category("richPresence")
    @ConfigEntry.Gui.TransitiveObject
    public final RichPresence richPresence = new RichPresence();

    @ConfigEntry.Category("quickNav")
    @ConfigEntry.Gui.TransitiveObject
    public final QuickNav quickNav = new QuickNav();

    public static class QuickNav {
        public final boolean enableQuickNav = true;

        @ConfigEntry.Category("button1")
        @ConfigEntry.Gui.CollapsibleObject()
        public final QuickNavItem button1 = new QuickNavItem(true, new ItemData("diamond_sword"), "Your Skills", "/skills");

        @ConfigEntry.Category("button2")
        @ConfigEntry.Gui.CollapsibleObject()
        public final QuickNavItem button2 = new QuickNavItem(true, new ItemData("painting"), "Collection", "/collection");

        @ConfigEntry.Category("button3")
        @ConfigEntry.Gui.CollapsibleObject()
        public final QuickNavItem button3 = new QuickNavItem(false, new ItemData("air"), "", "");

        @ConfigEntry.Category("button4")
        @ConfigEntry.Gui.CollapsibleObject()
        public final QuickNavItem button4 = new QuickNavItem(true, new ItemData("bone"), "Pets", "/pets");

        @ConfigEntry.Category("button5")
        @ConfigEntry.Gui.CollapsibleObject()
        public final QuickNavItem button5 = new QuickNavItem(true, new ItemData("leather_chestplate", 1, "tag:{display:{color:8991416}}"), "Wardrobe", "/wardrobe");

        @ConfigEntry.Category("button6")
        @ConfigEntry.Gui.CollapsibleObject()
        public final QuickNavItem button6 = new QuickNavItem(true, new ItemData("ender_chest"),  "Storage", "/storage");

        @ConfigEntry.Category("button7")
        @ConfigEntry.Gui.CollapsibleObject()
        public final QuickNavItem button7 = new QuickNavItem(true, new ItemData("player_head", 1, "tag:{SkullOwner:{Id:[I;-300151517,-631415889,-1193921967,-1821784279],Properties:{textures:[{Value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdjYzY2ODc0MjNkMDU3MGQ1NTZhYzUzZTA2NzZjYjU2M2JiZGQ5NzE3Y2Q4MjY5YmRlYmVkNmY2ZDRlN2JmOCJ9fX0=\"}]}}}"), "none", "/hub");

        @ConfigEntry.Category("button8")
        @ConfigEntry.Gui.CollapsibleObject()
        public final QuickNavItem button8 = new QuickNavItem(true, new ItemData("player_head", 1, "tag:{SkullOwner:{Id:[I;1605800870,415127827,-1236127084,15358548],Properties:{textures:[{Value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg5MWQ1YjI3M2ZmMGJjNTBjOTYwYjJjZDg2ZWVmMWM0MGExYjk0MDMyYWU3MWU3NTQ3NWE1NjhhODI1NzQyMSJ9fX0=\"}]}}}"), "none", "/warp dungeon_hub");

        @ConfigEntry.Category("button9")
        @ConfigEntry.Gui.CollapsibleObject()
        public final QuickNavItem button9 = new QuickNavItem(false, new ItemData("air"), "", "");

        @ConfigEntry.Category("button10")
        @ConfigEntry.Gui.CollapsibleObject()
        public final QuickNavItem button10 = new QuickNavItem(true, new ItemData("enchanting_table"), "Enchant", "/etable");

        @ConfigEntry.Category("button11")
        @ConfigEntry.Gui.CollapsibleObject()
        public final QuickNavItem button11 = new QuickNavItem(true, new ItemData("anvil"), "Anvil", "/anvil");

        @ConfigEntry.Category("button12")
        @ConfigEntry.Gui.CollapsibleObject()
        public final QuickNavItem button12 = new QuickNavItem(true, new ItemData("crafting_table"), "Craft Item", "/craft");
    }

    public static class QuickNavItem {
        public QuickNavItem(Boolean render, ItemData itemData, String uiTitle, String clickEvent) {
            this.render = render;
            this.item = itemData;
            this.clickEvent = clickEvent;
            this.uiTitle = uiTitle;
        }

        public final Boolean render;

        @ConfigEntry.Category("item")
        @ConfigEntry.Gui.CollapsibleObject()
        public final ItemData item;

        public final String uiTitle;
        public final String clickEvent;
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

        public final String itemName;
        public final int count;
        public final String nbt;
    }

    public static class General {
        public final boolean enableUpdateNotification = true;
        public final boolean backpackPreviewWithoutShift = false;

        public boolean tabHudEnabled = true;

        @ConfigEntry.Gui.Excluded
        public String apiKey;

        @ConfigEntry.Category("bars")
        @ConfigEntry.Gui.CollapsibleObject()
        public final Bars bars = new Bars();
      
        @ConfigEntry.Category("itemList")
        @ConfigEntry.Gui.CollapsibleObject()
        public final ItemList itemList = new ItemList();

        @ConfigEntry.Category("itemTooltip")
        @ConfigEntry.Gui.CollapsibleObject()
        public final ItemTooltip itemTooltip = new ItemTooltip();

        @ConfigEntry.Category("hitbox")
        @ConfigEntry.Gui.CollapsibleObject()
        public final Hitbox hitbox = new Hitbox();

        @ConfigEntry.Gui.Excluded
        public final List<Integer> lockedSlots = new ArrayList<>();
    }

    public static class Bars {
        public final boolean enableBars = true;

        @ConfigEntry.Category("barpositions")
        @ConfigEntry.Gui.CollapsibleObject()
        public final BarPositions barpositions = new BarPositions();
    }

    public static class BarPositions {
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public final BarPosition healthBarPosition = BarPosition.LAYER1;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public final BarPosition manaBarPosition = BarPosition.LAYER1;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public final BarPosition defenceBarPosition = BarPosition.LAYER1;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public final BarPosition experienceBarPosition = BarPosition.LAYER1;

    }

    public enum BarPosition {
        LAYER1,
        LAYER2,
        RIGHT,
        NONE;

        @Override
		public String toString() {
            return switch (this) {
                case LAYER1 -> "Layer 1";
                case LAYER2 -> "Layer 2";
                case RIGHT -> "Right";
                case NONE -> "Disabled";
            };
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

    public static class Hitbox {
        public final boolean oldFarmlandHitbox = true;
        public final boolean oldLeverHitbox = false;
    }

    public static class RichPresence {
        public final boolean enableRichPresence = false;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        @ConfigEntry.Gui.Tooltip()
        public final Info info = Info.LOCATION;
        public final boolean cycleMode = false;
        public String customMessage;
    }

    public static class ItemList {
        public final boolean enableItemList = true;
    }

    public enum Average {
        ONE_DAY,
        THREE_DAY,
        BOTH;

        @Override
		public String toString() {
            return switch (this) {
                case ONE_DAY -> "1 day price";
                case THREE_DAY -> "3 day price";
                case BOTH -> "Both";
            };
        }
    }

    public static class ItemTooltip {
        public final boolean enableNPCPrice = true;
        public final boolean enableAvgBIN = true;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        @ConfigEntry.Gui.Tooltip()
        public final Average avg = Average.THREE_DAY;
        public final boolean enableLowestBIN = true;
        public final boolean enableBazaarPrice = true;
        public final boolean enableMuseumDate = true;
    }

    public static class Locations {
        @ConfigEntry.Category("dungeons")
        @ConfigEntry.Gui.CollapsibleObject()
        public final Dungeons dungeons = new Dungeons();

        @ConfigEntry.Category("dwarvenmines")
        @ConfigEntry.Gui.CollapsibleObject()
        public final DwarvenMines dwarvenMines = new DwarvenMines();
    }

    public static class Dungeons {
        @ConfigEntry.Gui.Tooltip()
        public final boolean croesusHelper = true;
        public final boolean enableMap = true;
        public final boolean solveThreeWeirdos = true;
        public final boolean blazesolver = true;
        public final boolean solveTrivia = true;
        @ConfigEntry.Gui.CollapsibleObject()
        public final Terminals terminals = new Terminals();
    }

    public static class Terminals {
        public final boolean solveColor = true;
        public final boolean solveOrder = true;
        public final boolean solveStartsWith = true;
    }

    public static class DwarvenMines {
        public final boolean enableDrillFuel = true;
        public final boolean solveFetchur = true;
        public final boolean solvePuzzler = true;
        @ConfigEntry.Gui.CollapsibleObject()
        public final DwarvenHud dwarvenHud = new DwarvenHud();
    }

    public static class DwarvenHud {
        public final boolean enabled = true;
        public final boolean enableBackground = true;
        public int x = 10;
        public int y = 10;
    }

    public static class Messages {
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public final ChatFilterResult hideAbility = ChatFilterResult.PASS;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public final ChatFilterResult hideHeal = ChatFilterResult.PASS;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public final ChatFilterResult hideAOTE = ChatFilterResult.PASS;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public final ChatFilterResult hideImplosion = ChatFilterResult.PASS;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public final ChatFilterResult hideMoltenWave = ChatFilterResult.PASS;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public final ChatFilterResult hideAds = ChatFilterResult.PASS;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public final ChatFilterResult hideTeleportPad = ChatFilterResult.PASS;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public final ChatFilterResult hideCombo = ChatFilterResult.PASS;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public final ChatFilterResult hideAutopet = ChatFilterResult.PASS;
        @ConfigEntry.Gui.Tooltip()
        public final boolean hideMana = false;
    }

    public enum Info {
        PURSE,
        BITS,
        LOCATION
    }

    public static void init() {
        AutoConfig.register(SkyblockerConfig.class, GsonConfigSerializer::new);
    }

    public static SkyblockerConfig get() {
        return AutoConfig.getConfigHolder(SkyblockerConfig.class).getConfig();
    }
}