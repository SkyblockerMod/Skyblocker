package de.hysky.skyblocker.config.configs;

import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.minecraft.client.resource.language.I18n;

import java.util.ArrayList;
import java.util.List;

public class HelperConfig {
	public boolean enableNewYearCakesHelper = true;

	public boolean enableBitsTooltip = true;

	public boolean enableWardrobeHelper = true;

	public boolean enableDateCalculator = true;

	public boolean enableCopyUnderbidPrice = false;

	public MythologicalRitual mythologicalRitual = new MythologicalRitual();

	public Jerry jerry = new Jerry();

	public Experiments experiments = new Experiments();

	public Fishing fishing = new Fishing();

	public FairySouls fairySouls = new FairySouls();

	public ChocolateFactory chocolateFactory = new ChocolateFactory();

	public Carnival carnival = new Carnival();

	public Bazaar bazaar = new Bazaar();

	public ItemPrice itemPrice = new ItemPrice();

	public static class MythologicalRitual {
		public boolean enableMythologicalRitualHelper = true;
	}

	public static class Jerry {
		public boolean enableJerryTimer = false;
	}

	public static class Experiments {
		public boolean enableChronomatronSolver = true;

		public boolean enableSuperpairsSolver = true;

		public boolean enableUltrasequencerSolver = true;

		public boolean blockIncorrectClicks = false;
	}

	public static class Fishing {
		public boolean enableFishingHelper = true;

		@Deprecated
		public transient boolean enableFishingHookDisplay = true;

		public boolean enableFishingTimer = false;

		@Deprecated
		public transient boolean changeTimerColor = true;

		@Deprecated
		public transient float fishingTimerScale = 1f;

		public boolean hideOtherPlayersRods = false;

		public boolean enableFishingHud = false;

		public List<Location> fishingHudEnabledLocations = new ArrayList<>(List.of(Location.values()));

		public boolean enableSeaCreatureCounter = true;

		public FishingHookDisplay fishingHookDisplay = FishingHookDisplay.HUD;

		public boolean onlyShowHudInBarn = true;

		public int timerLength = 340;

		public boolean seaCreatureTimerNotification = true;

		public int seaCreatureCap = 30;

		public boolean seaCreatureCapNotification = true;

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
		public boolean enableFairySoulsHelper = false;

		public boolean highlightFoundSouls = true;

		public boolean highlightOnlyNearbySouls = false;
	}

	public static class ChocolateFactory {
		public boolean enableChocolateFactoryHelper = true;

		public boolean enableEggFinder = true;

		public boolean sendEggFoundMessages = true;

		public Waypoint.Type waypointType = Waypoint.Type.WAYPOINT;

		public boolean enableTimeTowerReminder = true;

		public boolean straySound = true;
	}

	public static class Carnival {
		public boolean catchAFishHelper = true;

		public boolean zombieShootoutHelper = true;
	}

	public static class Bazaar {
		public boolean enableBazaarHelper = true;

		public boolean enableReorderHelper = true;
	}

	public static class ItemPrice {
		public boolean enableItemPriceLookup = true;

		public boolean enableItemPriceRefresh = true;
	}
}
