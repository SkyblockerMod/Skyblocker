package de.hysky.skyblocker.skyblock.fishing;

import java.util.Objects;

import it.unimi.dsi.fastutil.objects.ObjectFloatPair;
import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.phys.Vec3;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.HelperConfig;
import de.hysky.skyblocker.skyblock.PetCache;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.skyblock.profileviewer.utils.LevelFinder;
import de.hysky.skyblocker.skyblock.tabhud.config.OptionWidgetCollector;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ElementBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.ElementCollector;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.JsonValueInput;
import de.hysky.skyblocker.utils.JsonValueOutput;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.time.SkyblockTime;

@RegisterWidget
public class FishingHudWidget extends ElementBasedWidget {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Vec3 BARN_LOCATION = new Vec3(108, 89, -252);

	private static @Nullable FishingHudWidget instance;

	private boolean showCreatureCounter = true;
	private boolean showFishingTimer = true;

	public static FishingHudWidget getInstance() {
		return Objects.requireNonNull(instance, "FishingHudWidget not initialized");
	}

	public FishingHudWidget() {
		super(Component.literal("Fishing").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), TextColor.DARK_AQUA.getValue(), new Information("hud_fishing", Component.literal("Fishing HUD")));
		instance = this;
	}

	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}

	@Override
	public boolean shouldRender() {
		// sea creature tracker
		if (showCreatureCounter && SeaCreatureTracker.isCreaturesAlive()) {
			if (Utils.getLocation() == Location.HUB && SkyblockerConfigManager.get().helpers.fishing.onlyShowHudInBarn) {
				return isBarnFishing();
			}
			return true;

		}
		//bobber timer
		if (showFishingTimer && FishingHelper.startTime != 0) {
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
		//creature counter
		if (showCreatureCounter && SeaCreatureTracker.isCreaturesAlive()) {

			ObjectFloatPair<Component> timer = SeaCreatureTracker.getTimerText(SeaCreatureTracker.getOldestSeaCreatureAge());
			int seaCreatureCap = SeaCreatureTracker.SEA_CREATURE_CAP;
			float seaCreaturePercent = (float) SeaCreatureTracker.seaCreatureCount() / seaCreatureCap * 100;
			addElement(Elements.progressComponent(Ico.TROPICAL_FISH_BUCKET, Component.nullToEmpty("Alive Creatures"), Component.nullToEmpty(SeaCreatureTracker.seaCreatureCount() + "/" + seaCreatureCap), seaCreaturePercent, ColorUtils.percentToColor(100 - seaCreaturePercent)));
			addElement(Elements.progressComponent(Ico.CLOCK, Component.nullToEmpty("Time Left"), timer.left(), timer.rightFloat()));
		}
		//bobber timer
		if (showFishingTimer && FishingHelper.startTime != 0) {
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
			addElement(Elements.progressComponent(Ico.CLOCK, Component.nullToEmpty("Bobber Time"), SkyblockTime.formatTime(maxTime - time),  100 - (time / maxTime) * 100));
		}
		// rod reel timer
		if (SkyblockerConfigManager.get().helpers.fishing.fishingHookDisplay == HelperConfig.Fishing.FishingHookDisplay.HUD && FishingHookDisplayHelper.fishingHookArmorStand != null) {
			String rodReelTimer = FishingHookDisplayHelper.fishingHookArmorStand.getName().getString();
			addSimpleIcoText(Ico.CLOCK, "Reel Timer: ", rodReelTimer.equals("!!!") ? ChatFormatting.RED : ChatFormatting.YELLOW, rodReelTimer);
		}

	}

	@Override
	protected void updateConfigContent(ElementCollector collector) {
		if (showCreatureCounter) {
			collector.addElement(Elements.progressComponent(Ico.SALMON_BUCKET, Component.nullToEmpty("Alive Creatures"), Component.nullToEmpty("3/5"), 60, ColorUtils.percentToColor(40)));
			collector.addElement(Elements.progressComponent(Ico.CLOCK, Component.nullToEmpty("Time Left"), Component.nullToEmpty("1m"), 60f / SkyblockerConfigManager.get().helpers.fishing.timerLength * 100));
		}
		if (showFishingTimer) {
			collector.addElement(Elements.progressComponent(Ico.CLOCK, Component.nullToEmpty("Bobber Time"), SkyblockTime.formatTime(15),  75));
		}
		if (SkyblockerConfigManager.get().helpers.fishing.fishingHookDisplay == HelperConfig.Fishing.FishingHookDisplay.HUD) {
			collector.addSimpleIcoText(Ico.CLOCK, "Reel Timer: ", ChatFormatting.YELLOW, "???");
		}
	}

	@Override
	public void getOptionWidgets(OptionWidgetCollector collector) {
		super.getOptionWidgets(collector);
		collector.yesNoButton(Component.translatable("skyblocker.config.helpers.fishing.hud.enableSeaCreatureCounter"), b -> showCreatureCounter = b, showCreatureCounter, Component.translatable("skyblocker.config.helpers.fishing.hud.enableSeaCreatureCounter.@Tooltip"));
		collector.yesNoButton(Component.translatable("skyblocker.config.helpers.fishing.enableFishingTimer"), b -> showFishingTimer = b, showFishingTimer, Component.translatable("skyblocker.config.helpers.fishing.enableFishingTimer.@Tooltip"));
		collector.addWidget(CycleButton.builder(d -> Component.literal(d.toString()), SkyblockerConfigManager.get().helpers.fishing.fishingHookDisplay)
				.withTooltip(_ -> Tooltip.create(Component.translatable("skyblocker.config.helpers.fishing.fishingHookDisplay.@Tooltip")))
				.withValues(HelperConfig.Fishing.FishingHookDisplay.values())
				.create(
						Component.translatable("skyblocker.config.helpers.fishing.fishingHookDisplay"),
						(_, value) -> SkyblockerConfigManager.updateOnly(config -> config.helpers.fishing.fishingHookDisplay = value)
				)
		);
	}

	@Override
	public void load(JsonValueInput input) {
		super.load(input);
		showCreatureCounter = input.readBooleanOr("creature_counter", true);
		showFishingTimer = input.readBooleanOr("fishing_timer", true);
	}

	@Override
	public void save(JsonValueOutput output) {
		super.save(output);
		output.writeBool("creature_counter", showCreatureCounter);
		output.writeBool("fishing_timer", showFishingTimer);
	}

	private static boolean isBarnFishing() {
		return CLIENT.player != null && CLIENT.player.distanceToSqr(BARN_LOCATION) < 2500;
	}
}
