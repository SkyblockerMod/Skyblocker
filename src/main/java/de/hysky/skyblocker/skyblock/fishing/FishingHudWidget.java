package de.hysky.skyblocker.skyblock.fishing;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.HelperConfig;
import de.hysky.skyblocker.skyblock.PetCache;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.skyblock.profileviewer.utils.LevelFinder;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.SkyblockTime;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectFloatPair;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

@RegisterWidget
public class FishingHudWidget extends ComponentBasedWidget {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Vec3 BARN_LOCATION = new Vec3(108, 89, -252);

	private static FishingHudWidget instance;

	public static FishingHudWidget getInstance() {
		return instance;
	}

	public FishingHudWidget() {
		super(Component.literal("Fishing").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), ChatFormatting.DARK_AQUA.getColor(), "hud_fishing");
		instance = this;
	}

	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}

	@Override
	public Set<Location> availableLocations() {
		return ALL_LOCATIONS;
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		SkyblockerConfigManager.get().helpers.fishing.enableFishingHud = enabled;
	}

	@Override
	public boolean isEnabledIn(Location location) {
		return SkyblockerConfigManager.get().helpers.fishing.enableFishingHud;
	}

	@Override
	public boolean shouldRender(Location location) {
		if (!super.shouldRender(location)) {
			return false;
		}
		// sea creature tracker
		if (SkyblockerConfigManager.get().helpers.fishing.enableSeaCreatureCounter && SeaCreatureTracker.isCreaturesAlive()) {
			if (Utils.getLocation() == Location.HUB && SkyblockerConfigManager.get().helpers.fishing.onlyShowHudInBarn) {
				return isBarnFishing();
			}
			return true;

		}
		//bobber timer
		if (SkyblockerConfigManager.get().helpers.fishing.enableFishingTimer && FishingHelper.startTime != 0) {
			return true;
		}
		//rod timer
		if ((SkyblockerConfigManager.get().helpers.fishing.fishingHookDisplay == HelperConfig.Fishing.FishingHookDisplay.HUD) && FishingHookDisplayHelper.fishingHookArmorStand != null) {
			return true;
		}
		return false;
	}

	@Override
	public void updateContent() {
		if (Minecraft.getInstance().screen instanceof WidgetsConfigurationScreen) {
			addComponent(Components.progressComponent(Ico.SALMON_BUCKET, Component.nullToEmpty("Alive Creatures"), Component.nullToEmpty("3/5"), 60, ColorUtils.percentToColor(40)));
			addComponent(Components.progressComponent(Ico.CLOCK, Component.nullToEmpty("Time Left"), Component.nullToEmpty("1m"), 60f / SkyblockerConfigManager.get().helpers.fishing.timerLength * 100));
			return;
		}
		//creature counter
		if (SkyblockerConfigManager.get().helpers.fishing.enableSeaCreatureCounter && SeaCreatureTracker.isCreaturesAlive()) {

			ObjectFloatPair<Component> timer = SeaCreatureTracker.getTimerText(SeaCreatureTracker.getOldestSeaCreatureAge());
			int seaCreatureCap = SeaCreatureTracker.getSeaCreatureCap();
			float seaCreaturePercent = (float) SeaCreatureTracker.seaCreatureCount() / seaCreatureCap * 100;
			addComponent(Components.progressComponent(Ico.TROPICAL_FISH_BUCKET, Component.nullToEmpty("Alive Creatures"), Component.nullToEmpty(SeaCreatureTracker.seaCreatureCount() + "/" + seaCreatureCap), seaCreaturePercent, ColorUtils.percentToColor(100 - seaCreaturePercent)));
			addComponent(Components.progressComponent(Ico.CLOCK, Component.nullToEmpty("Time Left"), timer.left(), timer.rightFloat()));
		}
		//bobber timer
		if (SkyblockerConfigManager.get().helpers.fishing.enableFishingTimer && FishingHelper.startTime != 0) {
			float time = Math.round((System.currentTimeMillis() - FishingHelper.startTime) / 1000f);
			float maxTime;
			PetInfo pet = PetCache.getCurrentPet();
			if (pet != null && pet.type().contains("SLUG")) {
				int level = LevelFinder.getLevelInfo("PET_"+pet.tier(), (long) pet.exp()).level;
				maxTime = 20 * (1 - (level/200f));
			} else {
				maxTime = 20;
			}
			time = Math.clamp(time, 0, maxTime);
			addComponent(Components.progressComponent(Ico.CLOCK, Component.nullToEmpty("Bobber Time"), SkyblockTime.formatTime(maxTime - time),  100 - (time / maxTime) * 100));
		}
		// rod reel timer
		if (SkyblockerConfigManager.get().helpers.fishing.fishingHookDisplay == HelperConfig.Fishing.FishingHookDisplay.HUD && FishingHookDisplayHelper.fishingHookArmorStand != null) {
			String rodReelTimer = FishingHookDisplayHelper.fishingHookArmorStand.getName().getString();
			addSimpleIcoText(Ico.CLOCK, "Reel Timer: ", rodReelTimer.equals("!!!") ? ChatFormatting.RED : ChatFormatting.YELLOW, rodReelTimer);
		}

	}

	@Override
	public Component getDisplayName() {
		return Component.literal("Fishing Hud");
	}

	private static boolean isBarnFishing() {
		return CLIENT.player != null && CLIENT.player.distanceToSqr(BARN_LOCATION) < 2500;
	}
}
