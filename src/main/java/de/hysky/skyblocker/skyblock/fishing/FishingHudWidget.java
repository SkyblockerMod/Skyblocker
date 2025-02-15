package de.hysky.skyblocker.skyblock.fishing;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.utils.Location;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.MinecraftClient;
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
		super(Text.literal("Fishing").formatted(Formatting.DARK_AQUA, Formatting.BOLD), Formatting.DARK_AQUA.getColorValue(), "hud_fishing");//todo better color
		instance = this;
	}

	@Override
	public Set<Location> availableLocations() {
		return Set.of(Location.HUB);
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {

	}

	@Override
	public boolean isEnabledIn(Location location) {
		return true;//todo
	}

	@Override
	public boolean shouldRender(Location location) {
		return super.shouldRender(location) && SeaCreatureTracker.isCreaturesAlive(); //todo coifig
	}

	@Override
	public void updateContent() {
		if (MinecraftClient.getInstance().currentScreen instanceof WidgetsConfigurationScreen) {
			addSimpleIcoText(Ico.WATER, "Alive Creatures: ", Formatting.WHITE, "3/5");
			addSimpleIcoText(Ico.CLOCK, "Time Left: ", Formatting.DARK_GREEN, "1m");
			return;
		}

		Pair<String, Formatting> timer = SeaCreatureTracker.getTimerText(360000, SeaCreatureTracker.getOldestSeaCreatureAge());
		int seaCreatureCap = SeaCreatureTracker.getSeaCreatureCap();
		addSimpleIcoText(Ico.WATER, "Alive Creatures: ", Formatting.WHITE, SeaCreatureTracker.seaCreatureCount()+"/"+seaCreatureCap);
		addSimpleIcoText(Ico.CLOCK, "Time Left: ", timer.right(), timer.left());

	}

	@Override
	public Text getDisplayName() {
		return Text.literal("Fishing Hud");
	}


	private static boolean isBarnFishing() {
		return CLIENT.player != null && CLIENT.player.squaredDistanceTo(BARN_LOCATION) < 2500;
	}


}
