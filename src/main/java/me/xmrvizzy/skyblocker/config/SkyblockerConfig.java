package me.xmrvizzy.skyblocker.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.xmrvizzy.skyblocker.chat.ChatFilterResult;

import java.util.ArrayList;
import java.util.List;

@Config(name = "skyblocker")
public class SkyblockerConfig implements ConfigData {

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.TransitiveObject
    public General general = new General();

    @ConfigEntry.Category("locations")
    @ConfigEntry.Gui.TransitiveObject
    public Locations locations = new Locations();

    @ConfigEntry.Category("messages")
    @ConfigEntry.Gui.TransitiveObject
    public Messages messages = new Messages();

    @ConfigEntry.Category("richPresence")
    @ConfigEntry.Gui.TransitiveObject
    public RichPresence richPresence = new RichPresence();

    public static class General {
        public boolean enableUpdateNotification = true;
        public boolean backpackPreviewWithoutShift = false;

        @ConfigEntry.Gui.Excluded
        public String apiKey;

        @ConfigEntry.Category("bars")
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
        public Bars bars = new Bars();
      
        @ConfigEntry.Category("itemList")
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
        public ItemList itemList = new ItemList();

        @ConfigEntry.Category("quicknav")
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
        public Quicknav quicknav = new Quicknav();

        @ConfigEntry.Category("itemTooltip")
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
        public ItemTooltip itemTooltip = new ItemTooltip();

        @ConfigEntry.Category("hitbox")
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
        public Hitbox hitbox = new Hitbox();

        @ConfigEntry.Gui.Excluded
        public List<Integer> lockedSlots = new ArrayList<>();
    }

    public static class Bars {
        public boolean enableBars = true;
    }

    public static class Hitbox {
        public boolean oldFarmlandHitbox = true;
        public boolean oldLeverHitbox = false;
    }

    public static class RichPresence {
        public boolean enableRichPresence = false;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        @ConfigEntry.Gui.Tooltip()
        public Info info = Info.LOCATION;
        public boolean cycleMode = false;
        public String customMessage;
    }

    public static class ItemList {
        public boolean enableItemList = true;
    }

    public static class Quicknav {
        public boolean enableQuicknav = true;
    }

    public enum Average {
        ONE_DAY,
        THREE_DAY,
        BOTH;

        public String toString() {
            return switch (this) {
                case ONE_DAY -> "1 day price";
                case THREE_DAY -> "3 day price";
                case BOTH -> "Both";
            };
        }
    }

    public static class ItemTooltip {
        public boolean enableNPCPrice = true;
        public boolean enableAvgBIN = true;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        @ConfigEntry.Gui.Tooltip()
        public Average avg = Average.THREE_DAY;
        public boolean enableLowestBIN = true;
        public boolean enableBazaarPrice = true;
        public boolean enableMuseumDate = true;
    }

    public static class Locations {
        @ConfigEntry.Category("dungeons")
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
        public Dungeons dungeons = new Dungeons();

        @ConfigEntry.Category("dwarvenmines")
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
        public DwarvenMines dwarvenMines = new DwarvenMines();
    }

    public static class Dungeons {
        public boolean enableMap = true;
        public boolean solveThreeWeirdos = true;
        public boolean blazesolver = true;
        public boolean solveTrivia = true;
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
        public Terminals terminals = new Terminals();
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
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
        public DwarvenHud dwarvenHud = new DwarvenHud();
    }

    public static class DwarvenHud {
        public boolean enabled = true;
        public boolean enableBackground = true;
        @ConfigEntry.BoundedDiscrete(min = 3, max = 2000)
        public int x = 10;
        @ConfigEntry.BoundedDiscrete(min = 3, max = 2000)
        public int y = 10;
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
        LOCATION
    }

    public static void init() {
        AutoConfig.register(SkyblockerConfig.class, GsonConfigSerializer::new);
    }

    public static SkyblockerConfig get() {
        return AutoConfig.getConfigHolder(SkyblockerConfig.class).getConfig();
    }
}