package de.hysky.skyblocker.skyblock.fishing;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

@RegisterWidget
public class FishingHudWidget extends ComponentBasedWidget {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Vec3d BARN_LOCATION = new Vec3d(108, 89, -252);

	private static FishingHudWidget instance;

	public static FishingHudWidget getInstance() {
		return instance;
	}

	public FishingHudWidget() {
		super(Text.literal("Fishing").formatted(Formatting.DARK_AQUA, Formatting.BOLD), Formatting.DARK_AQUA.getColorValue(), "hud_fishing");
		instance = this;
	}

	@Override
	public Set<Location> availableLocations() {
		return Set.of(Location.HUB, Location.CRIMSON_ISLE, Location.CRYSTAL_HOLLOWS, Location.THE_PARK, Location.SPIDERS_DEN);
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		if (enabled) {
			SkyblockerConfigManager.get().helpers.fishing.fishingHudEnabledLocations.add(location);
		} else {
			SkyblockerConfigManager.get().helpers.fishing.fishingHudEnabledLocations.remove(location);
		}
	}

	@Override
	public boolean isEnabledIn(Location location) {
		return SkyblockerConfigManager.get().helpers.fishing.fishingHudEnabledLocations.contains(location);
	}

	@Override
	public boolean shouldRender(Location location) {
		if (super.shouldRender(location) && SeaCreatureTracker.isCreaturesAlive()) {
			if (Utils.getLocation() == Location.HUB && SkyblockerConfigManager.get().helpers.fishing.onlyShowHudInBarn) {
				return isBarnFishing();
			}
			return true;
		}
		return false;
	}

	@Override
	public void updateContent() {
		if (MinecraftClient.getInstance().currentScreen instanceof WidgetsConfigurationScreen) {
			addSimpleIcoText(new ItemStack(Items.SALMON_BUCKET), "Alive Creatures: ", Formatting.WHITE, "3/5");
			addSimpleIcoText(Ico.CLOCK, "Time Left: ", Formatting.DARK_GREEN, "1m");
			return;
		}

		Pair<String, Float> timer = SeaCreatureTracker.getTimerText(SeaCreatureTracker.getOldestSeaCreatureAge());
		int seaCreatureCap = SeaCreatureTracker.getSeaCreatureCap();
		addSimpleIcoText(new ItemStack(Items.TROPICAL_FISH_BUCKET), "Alive Creatures: ", getTextFormating(1 - (float) SeaCreatureTracker.seaCreatureCount() / seaCreatureCap), SeaCreatureTracker.seaCreatureCount() + "/" + seaCreatureCap);
		addSimpleIcoText(Ico.CLOCK, "Time Left: ", getTextFormating(timer.right()), timer.left());

	}

	private static Formatting getTextFormating(float percentage) {
		if (percentage > 0.4) {
			return Formatting.DARK_GREEN;
		} else if (percentage > 0.1) {
			return Formatting.GOLD;
		} else {
			return Formatting.RED;
		}
	}


	@Override
	public Text getDisplayName() {
		return Text.literal("Fishing Hud");
	}


	private static boolean isBarnFishing() {
		return CLIENT.player != null && CLIENT.player.squaredDistanceTo(BARN_LOCATION) < 2500;
	}


}
