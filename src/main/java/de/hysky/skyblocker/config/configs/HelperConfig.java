package de.hysky.skyblocker.config.configs;

import de.hysky.skyblocker.utils.waypoint.Waypoint;
import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class HelperConfig {

    @SerialEntry
    public boolean enableNewYearCakesHelper = true;

    @SerialEntry
    public MythologicalRitual mythologicalRitual = new MythologicalRitual();

    @SerialEntry
    public Jerry jerry = new Jerry();

    @SerialEntry
    public Experiments experiments = new Experiments();

    @SerialEntry
    public Fishing fishing = new Fishing();

    @SerialEntry
    public FairySouls fairySouls = new FairySouls();

    @SerialEntry
    public ChocolateFactory chocolateFactory = new ChocolateFactory();

    @SerialEntry
    public Bazaar bazaar = new Bazaar();

    @SerialEntry
    public ItemPrice itemPrice = new ItemPrice();

    public static class MythologicalRitual {
        @SerialEntry
        public boolean enableMythologicalRitualHelper = true;
    }

    public static class Jerry {
        @SerialEntry
        public boolean enableJerryTimer = false;
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

        @SerialEntry
        public boolean enableFishingTimer = false;

        @SerialEntry
        public boolean changeTimerColor = true;

        @SerialEntry
        public float fishingTimerScale = 1f;

        @SerialEntry
        public boolean hideOtherPlayersRods = false;
    }

    public static class FairySouls {
        @SerialEntry
        public boolean enableFairySoulsHelper = false;

        @SerialEntry
        public boolean highlightFoundSouls = true;

        @SerialEntry
        public boolean highlightOnlyNearbySouls = false;
    }

    public static class ChocolateFactory {
        @SerialEntry
        public boolean enableChocolateFactoryHelper = true;

        @SerialEntry
        public boolean enableEggFinder = true;

        @SerialEntry
        public boolean sendEggFoundMessages = true;

        @SerialEntry
        public Waypoint.Type waypointType = Waypoint.Type.WAYPOINT;

        @SerialEntry
        public boolean enableTimeTowerReminder = true;

        @SerialEntry
        public boolean straySound = true;
    }

    public static class Bazaar {
        @SerialEntry
        public boolean enableBazaarHelper = true;
    }

    public static class ItemPrice {
        @SerialEntry
        public boolean enableItemPriceLookup = true;

        @SerialEntry
        public boolean enableItemPriceRefresh = true;
    }
}
