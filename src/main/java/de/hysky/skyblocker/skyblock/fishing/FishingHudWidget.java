package de.hysky.skyblocker.skyblock.fishing;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.HelperConfig;
import de.hysky.skyblocker.skyblock.PetCache;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.skyblock.profileviewer.utils.LevelFinder;
import de.hysky.skyblocker.skyblock.tabhud.config.option.BooleanOption;
import de.hysky.skyblocker.skyblock.tabhud.config.option.WidgetOption;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.SkyblockTime;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectFloatPair;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

@RegisterWidget
public class FishingHudWidget extends ComponentBasedWidget {
	public static final String ID = "hud_fishing";
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Vec3 BARN_LOCATION = new Vec3(108, 89, -252);

	private static FishingHudWidget instance;

	public static FishingHudWidget getInstance() {
		return instance;
	}

	public FishingHudWidget() {
		super(Component.literal("Fishing").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), ChatFormatting.DARK_AQUA.getColor(), new Information(ID, Component.literal("Fishing Hud")));
		instance = this;
	}

	private boolean seaCreatureCounter = true;
	private boolean fishingTimer = false;
	private boolean onlyShowInBarn = true;

	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}

	@Override
	public boolean shouldRender() {
		// sea creature tracker
		if (seaCreatureCounter && SeaCreatureTracker.isCreaturesAlive()) {
			if (Utils.getLocation() == Location.HUB && onlyShowInBarn) {
				return isBarnFishing();
			}
			return true;

		}
		//bobber timer
		if (fishingTimer && FishingHelper.startTime != 0) {
			return true;
		}
		//rod timer
		return (SkyblockerConfigManager.get().helpers.fishing.fishingHookDisplay == HelperConfig.Fishing.FishingHookDisplay.HUD) && FishingHookDisplayHelper.fishingHookArmorStand != null;
	}

	@Override
	public void updateContent() {
		//creature counter
		if (seaCreatureCounter && SeaCreatureTracker.isCreaturesAlive()) { // TODO inner options

			ObjectFloatPair<Component> timer = SeaCreatureTracker.getTimerText(SeaCreatureTracker.getOldestSeaCreatureAge());
			int seaCreatureCap = SeaCreatureTracker.getSeaCreatureCap();
			float seaCreaturePercent = (float) SeaCreatureTracker.seaCreatureCount() / seaCreatureCap * 100;
			addComponent(Components.progressComponent(Ico.TROPICAL_FISH_BUCKET, Component.nullToEmpty("Alive Creatures"), Component.nullToEmpty(SeaCreatureTracker.seaCreatureCount() + "/" + seaCreatureCap), seaCreaturePercent, ColorUtils.percentToColor(100 - seaCreaturePercent)));
			addComponent(Components.progressComponent(Ico.CLOCK, Component.nullToEmpty("Time Left"), timer.left(), timer.rightFloat()));
		}
		//bobber timer
		if (fishingTimer && FishingHelper.startTime != 0) {
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
	protected List<de.hysky.skyblocker.skyblock.tabhud.widget.component.Component> getConfigComponents() {
		List<de.hysky.skyblocker.skyblock.tabhud.widget.component.Component> components = new ArrayList<>(4);
		if (seaCreatureCounter) {
			components.add(Components.progressComponent(Ico.TROPICAL_FISH_BUCKET, Component.nullToEmpty("Alive Creatures"), Component.nullToEmpty("1/5"), 20, ColorUtils.percentToColor(80)));
			components.add(Components.progressComponent(Ico.CLOCK, Component.nullToEmpty("Time Left"), Component.literal("1:23"), 50));
		}
		if (fishingTimer) {
			components.add(Components.progressComponent(Ico.CLOCK, Component.nullToEmpty("Bobber Time"), Component.literal("50s"), 60));
		}
		if (SkyblockerConfigManager.get().helpers.fishing.fishingHookDisplay == HelperConfig.Fishing.FishingHookDisplay.HUD) {
			components.add(Components.iconTextComponent(Ico.CLOCK, Component.literal("Reel Timer: ").append(Component.literal("!!!").withStyle(ChatFormatting.RED))));
		}
		return components;
	}

	@Override
	public void getOptions(List<WidgetOption<?>> options) {
		super.getOptions(options);
		options.add(new BooleanOption("sea_creature_counter", Component.literal("Sea Creature Tracker"), () -> seaCreatureCounter, b -> seaCreatureCounter = b, true));
		options.add(new BooleanOption("fishing_timer", Component.literal("Fishing Timer"), () -> fishingTimer, b -> fishingTimer = b, false));
		options.add(new BooleanOption("only_show_in_barn", Component.literal("Only show in Barn in The HUB"), () -> onlyShowInBarn, b -> onlyShowInBarn = b, true));
	}

	private static boolean isBarnFishing() {
		return CLIENT.player != null && CLIENT.player.distanceToSqr(BARN_LOCATION) < 2500;
	}
}
