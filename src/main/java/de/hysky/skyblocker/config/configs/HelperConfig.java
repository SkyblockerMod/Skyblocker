package de.hysky.skyblocker.config.configs;

import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.resource.language.I18n;

import java.util.ArrayList;
import java.util.List;

public class HelperConfig {

    @SerialEntry
    public boolean enableNewYearCakesHelper = true;

    @SerialEntry
    public boolean enableBitsTooltip = true;

    @SerialEntry
    public boolean enableWardrobeHelper = true;

	@SerialEntry
	public boolean enableDateCalculator = true;

	@SerialEntry
	public boolean enableCopyUnderbidPrice = false;

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
    public Carnival carnival = new Carnival();

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

		@Deprecated
		@SerialEntry
		public transient boolean enableFishingHookDisplay = true;

        @SerialEntry
        public boolean enableFishingTimer = false;

		@Deprecated
		@SerialEntry
		public transient boolean changeTimerColor = true;

		@Deprecated
		@SerialEntry
		public transient float fishingTimerScale = 1f;

        @SerialEntry
        public boolean hideOtherPlayersRods = false;


		@SerialEntry
		public List<Location> fishingHudEnabledLocations = new ArrayList<>(List.of(Location.values()));

		@SerialEntry
		public boolean enableSeaCreatureCounter = true;


		@SerialEntry
		public FishingHookDisplay fishingHookDisplay = FishingHookDisplay.HUD;

		@SerialEntry
		public boolean onlyShowHudInBarn = true;

		@SerialEntry
		public int timerLength = 340;

		@SerialEntry
		public boolean seaCreatureTimerNotification = true;

		@SerialEntry
		public int seaCreatureCap = 30;

		@SerialEntry
		public boolean seaCreatureCapNotification = true;

		@SerialEntry
		public SkyblockItemRarity minimumNotificationRarity = SkyblockItemRarity.EPIC;

		public enum FishingHookDisplay {
			OFF,
			CROSSHAIR,
			HUD;
			@Override
			public String toString() {
				return I18n.translate("skyblocker.config.helpers.fishing.fishingHookDisplay." + name());
			}
		}
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

    public static class Carnival {
    	@SerialEntry
    	public boolean catchAFishHelper = true;

    	@SerialEntry
    	public boolean zombieShootoutHelper = true;
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
