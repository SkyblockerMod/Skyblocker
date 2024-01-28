package de.hysky.skyblocker.skyblock.garden;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import dev.architectury.event.events.client.ClientPlayerEvent;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sprayonator {

	private static final Pattern SPRAYONATOR_PATTERN = Pattern.compile("SPRAYONATOR! You sprayed Plot - (?<plot>\\d+) with (?<matter>[^!]+)!");
	private static final SprayData[] sprayedPlots = SkyblockerConfigManager.get().locations.garden.sprayedPlots; //This represents plots in 1 dimension with index+1 being the plot number
	private static final byte[][] plots = { //Bytes, to save memory and just why not.
			{21, 13, 9, 14, 22},
			{15, 5, 1, 6, 16},
			{10, 2, 0, 3, 11},
			{17, 7, 4, 8, 18},
			{23, 19, 12, 20, 24}
	};

	private Sprayonator() {
	}

	public static void init() {
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if (overlay || !Utils.isInGarden()) return;
			onGameMessage(message.getString());
		});
		Scheduler.INSTANCE.scheduleCyclic(Sprayonator::tick, 20);
		ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(s -> {
			SkyblockerConfigManager.get().locations.garden.sprayedPlots = sprayedPlots;
			SkyblockerConfigManager.save();
		});
	}

	public static void tick() {
		if (!Utils.isInGarden()) return;
		for (int i = 0; i < sprayedPlots.length; i++) {
			SprayData sprayData = sprayedPlots[i];
			if (sprayData == null) continue;
			if (System.currentTimeMillis() - sprayData.time <= 1800000) continue; // 30 minutes in milliseconds
			sprayedPlots[i] = null;
		}
	}

	public static void onGameMessage(String message) {
		Matcher matcher = SPRAYONATOR_PATTERN.matcher(message);
		if (!matcher.matches()) return;
		sprayedPlots[Integer.parseInt(matcher.group("plot")) - 1] = new SprayData(matcher.group("matter"));
	}

	/**
	 * @return The plot the player is in currently, 0 if not in a plot or -1 if the operation fails.
	 */
	public static byte getCurrentPlot() {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null) return -1;
		int plotX = (player.getBlockX() + 240) / 96; // +240 is to move origin to (-240,-240) so that we can do division to figure out the plot index
		int plotZ = (player.getBlockZ() + 240) / 96; // 96 is the size of a plot in the garden, this allows us to figure out the index of the plot
		return plots[plotZ][plotX];
	}

	public static byte[][] getPlots() {
		return plots;
	}

	public static SprayData getSprayData(int index) {
		return sprayedPlots[index];
	}

	public record SprayData(String matter, long time) {
		private SprayData(String matter) {
			this(matter, System.currentTimeMillis());
		}
	}
}
