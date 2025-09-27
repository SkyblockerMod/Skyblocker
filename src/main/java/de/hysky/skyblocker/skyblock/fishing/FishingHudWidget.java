package de.hysky.skyblocker.skyblock.fishing;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.HelperConfig;
import de.hysky.skyblocker.skyblock.PetCache;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.skyblock.profileviewer.utils.LevelFinder;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.SkyblockTime;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectFloatPair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.List;

@RegisterWidget
public class FishingHudWidget extends ComponentBasedWidget {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Vec3d BARN_LOCATION = new Vec3d(108, 89, -252);

	private static FishingHudWidget instance;

	public static FishingHudWidget getInstance() {
		return instance;
	}

	public FishingHudWidget() {
		super(Text.literal("Fishing").formatted(Formatting.DARK_AQUA, Formatting.BOLD), Formatting.DARK_AQUA.getColorValue(), new Information("hud_fishing", Text.literal("Fishing Hud")));
		instance = this;
	}

	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}

	@Override
	public boolean shouldRender() {
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
		return (SkyblockerConfigManager.get().helpers.fishing.fishingHookDisplay == HelperConfig.Fishing.FishingHookDisplay.HUD) && FishingHookDisplayHelper.fishingHookArmorStand != null;
	}

	@Override
	public void updateContent() {
		//creature counter
		if (SkyblockerConfigManager.get().helpers.fishing.enableSeaCreatureCounter && SeaCreatureTracker.isCreaturesAlive()) { // TODO inner options

			ObjectFloatPair<Text> timer = SeaCreatureTracker.getTimerText(SeaCreatureTracker.getOldestSeaCreatureAge());
			int seaCreatureCap = SeaCreatureTracker.getSeaCreatureCap();
			float seaCreaturePercent = (float) SeaCreatureTracker.seaCreatureCount() / seaCreatureCap * 100;
			addComponent(Components.progressComponent(Ico.TROPICAL_FISH_BUCKET, Text.of("Alive Creatures"), Text.of(SeaCreatureTracker.seaCreatureCount() + "/" + seaCreatureCap), seaCreaturePercent, ColorUtils.percentToColor(100 - seaCreaturePercent)));
			addComponent(Components.progressComponent(Ico.CLOCK, Text.of("Time Left"), timer.left(), timer.rightFloat()));
		}
		//bobber timer
		if (SkyblockerConfigManager.get().helpers.fishing.enableFishingTimer && FishingHelper.startTime != 0) {
			float time = Math.round((System.currentTimeMillis() - FishingHelper.startTime) / 1000f);
			float maxTime;
			PetInfo pet = PetCache.getCurrentPet();
			if (pet != null && pet.type().contains("SLUG")){
				int level = LevelFinder.getLevelInfo("PET_"+pet.tier(), (long) pet.exp()).level;
				maxTime =20 * (1 - (level/200f));
			} else {
				maxTime = 20;
			}
			time = Math.clamp(time, 0, maxTime);
			addComponent(Components.progressComponent(Ico.CLOCK, Text.of("Bobber Time"), SkyblockTime.formatTime(maxTime - time),  100 - (time / maxTime) * 100));
		}
		// rod reel timer
		if (SkyblockerConfigManager.get().helpers.fishing.fishingHookDisplay == HelperConfig.Fishing.FishingHookDisplay.HUD && FishingHookDisplayHelper.fishingHookArmorStand != null) {
			String rodReelTimer = FishingHookDisplayHelper.fishingHookArmorStand.getName().getString();
			addSimpleIcoText(Ico.CLOCK, "Reel Timer: ", rodReelTimer.equals("!!!") ? Formatting.RED : Formatting.YELLOW, rodReelTimer);
		}

	}

	@Override
	protected List<Component> getConfigComponents() {
		return List.of(
				Components.progressComponent(Ico.SALMON_BUCKET, Text.of("Alive Creatures"), Text.of("3/5"), 60, ColorUtils.percentToColor(40)),
				Components.progressComponent(Ico.CLOCK, Text.of("Time Left"), Text.of("1m"), 60f / SkyblockerConfigManager.get().helpers.fishing.timerLength * 100) // TODO move to inner options
		);
	}

	private static boolean isBarnFishing() {
		return CLIENT.player != null && CLIENT.player.squaredDistanceTo(BARN_LOCATION) < 2500;
	}
}
