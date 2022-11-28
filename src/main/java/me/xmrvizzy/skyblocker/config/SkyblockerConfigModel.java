package me.xmrvizzy.skyblocker.config;

import io.wispforest.owo.config.annotation.*;
import me.xmrvizzy.skyblocker.chat.ChatFilterResult;

import java.util.ArrayList;
import java.util.List;


@Modmenu(modId = "skyblocker")
@Config(name = "skyblocker/skyblocker-config", wrapperName = "SkyblockerConfigWrapper")
public class SkyblockerConfigModel {
    @Nest
    public General general = new General();

    @Nest
    public Dungeons dungeons = new Dungeons();

    @Nest
    public DwarvenMines dwarvenMines = new DwarvenMines();

    @Nest
    public Messages messages = new Messages();

    @Nest
    public RichPresence richPresence = new RichPresence();

    public static class RichPresence {
        public boolean enableRichPresence = false;
        public RichPresenceInfo richPresenceInfo = RichPresenceInfo.LOCATION;
        public boolean cycleMode = false;
        public String customMessage = "";
    }

    public enum RichPresenceInfo {
        PURSE,
        BITS,
        LOCATION
    }

    public static class Messages {

        public ChatFilterResult hideAbility = ChatFilterResult.PASS;

        public ChatFilterResult hideHeal = ChatFilterResult.PASS;
        public ChatFilterResult hideAOTE = ChatFilterResult.PASS;
        public ChatFilterResult hideImplosion = ChatFilterResult.PASS;
        public ChatFilterResult hideMoltenWave = ChatFilterResult.PASS;
        public ChatFilterResult hideAds = ChatFilterResult.PASS;
        public ChatFilterResult hideTeleportPad = ChatFilterResult.PASS;
        public ChatFilterResult hideCombo = ChatFilterResult.PASS;
        public ChatFilterResult hideAutopet = ChatFilterResult.PASS;
        public boolean hideMana = false;
    }

    public static class General {
        public boolean enableUpdateNotification = true;
        public boolean backpackPreviewWithoutShift = false;

        @ExcludeFromScreen
        public String apiKey = "";

        @Nest
        public Bars bars = new Bars();
        @Nest
        public ItemList itemList = new ItemList();

        @Nest
        public Quicknav quicknav = new Quicknav();

        @Nest
        public ItemTooltip itemTooltip = new ItemTooltip();

        @Nest
        public Hitbox hitbox = new Hitbox();

        @ExcludeFromScreen
        public List<Integer> lockedSlots = new ArrayList<>();
    }

    public static class DwarvenMines {
        public boolean enableDrillFuel = true;
        public boolean solveFetchur = true;
        public boolean solvePuzzler = true;
        @Nest
        public DwarvenHud dwarvenHud = new DwarvenHud();
    }

    public static class DwarvenHud {
        public boolean enabled = true;
        public boolean enableBackground = true;
        public int x = 10;
        public int y = 10;
    }

    public static class Dungeons {
        public boolean croesusHelper = true;
        public boolean enableMap = true;
        public boolean solveThreeWeirdos = true;
        public boolean blazesolver = true;
        public boolean solveTrivia = true;
        @Nest
        public Terminals terminals = new Terminals();
    }

    public static class Terminals {
        public boolean solveColor = true;
        public boolean solveOrder = true;
        public boolean solveStartsWith = true;
    }

    public static class Bars {
        public boolean enableBars = true;

        @Nest
        public BarPositions barpositions = new BarPositions();
    }

    public static class BarPositions {
        public BarPosition healthBarPosition = BarPosition.LAYER1;

        public BarPosition manaBarPosition = BarPosition.LAYER1;

        public BarPosition defenceBarPosition = BarPosition.LAYER1;

        public BarPosition experienceBarPosition = BarPosition.LAYER1;

    }

    public enum BarPosition {
        LAYER1,
        LAYER2,
        RIGHT,
        NONE;

        public int toInt() {
            return switch (this) {
                case LAYER1 -> 0;
                case LAYER2 -> 1;
                case RIGHT -> 2;
                case NONE -> -1;
            };
        }
    }

    public static class ItemList {
        public boolean enableItemList = true;
    }

    public static class Quicknav {
        public boolean enableQuicknav = true;
    }

    public static class ItemTooltip {
        public boolean enableNPCPrice = true;
        public boolean enableAvgBIN = true;
        public Average avg = Average.THREE_DAY;
        public boolean enableLowestBIN = true;
        public boolean enableBazaarPrice = true;
        public boolean enableMuseumDate = true;
    }

    public enum Average {
        ONE_DAY,
        THREE_DAY,
        BOTH;
    }

    public static class Hitbox {
        public boolean oldFarmlandHitbox = true;
        public boolean oldLeverHitbox = false;
    }
}
