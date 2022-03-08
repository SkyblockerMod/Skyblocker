package me.xmrvizzy.skyblocker.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

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

        @ConfigEntry.Gui.Excluded
        public String apiKey;

        @ConfigEntry.Category("bars")
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Bars bars = new Bars();
      
        @ConfigEntry.Category("itemList")
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public ItemList itemList = new ItemList();

        @ConfigEntry.Category("quicknav")
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Quicknav quicknav = new Quicknav();

        public boolean enableUpdateNotification = true;

        @ConfigEntry.Category("itemTooltip")
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public ItemTooltip itemTooltip = new ItemTooltip();

        @ConfigEntry.Gui.Excluded
        public List<Integer> lockedSlots = new ArrayList<>();
    }

    public static class Bars {
        public boolean enableBars = true;
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
        BOTH
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
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Dungeons dungeons = new Dungeons();

        @ConfigEntry.Category("dwarvenmines")
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public DwarvenMines dwarvenMines = new DwarvenMines();
    }

    public static class Dungeons {
        public boolean enableMap = true;
        public boolean solveThreeWeirdos = true;
        public boolean blazesolver = true;
        public boolean solveTrivia = true;
        public boolean oldLevers = false;
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
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
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public DwarvenHud dwarvenHud = new DwarvenHud();
    }

    public static class DwarvenHud {
        public boolean enabled = true;
        public boolean enableBackground = true;
        @ConfigEntry.BoundedDiscrete(min = 3, max = 700)
        public int x = 10;
        @ConfigEntry.BoundedDiscrete(min = 3, max = 427)
        public int y = 10;
    }

    public static class Messages {
        public boolean hideAbility = false;
        public boolean hideHeal = false;
        public boolean hideAOTE = false;
        public boolean hideImplosion = false;
        public boolean hideMoltenWave = false;
        public boolean hideAds = false;
        public boolean hideTeleportPad = false;
        public boolean hideCombo = false;
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