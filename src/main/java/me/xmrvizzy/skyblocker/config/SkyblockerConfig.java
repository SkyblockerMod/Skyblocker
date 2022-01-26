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

    public static class General {
        public String apiKey;

        @ConfigEntry.Category("bars")
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Bars bars = new Bars();

        @ConfigEntry.Category("RichPresence")
        @ConfigEntry.Gui.CollapsibleObject()
        public RichPresence richPresence = new RichPresence();

        @ConfigEntry.Gui.Excluded
        public List<Integer> lockedSlots = new ArrayList<>();
    }

    public static class Bars {
        public boolean enableBars = true;
    }
    public static class RichPresence {
        @ConfigEntry.Gui.Tooltip()
        public boolean enableRichPresence = true;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public Info info = Info.LOCATION;
        public String customMessage;
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
    }

    public static class Messages {
        public boolean hideAbility = false;
        public boolean hideHeal = false;
        public boolean hideAOTE = false;
        public boolean hideImplosion = false;
        public boolean hideMoltenWave = false;
        public boolean hideAds = false;
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