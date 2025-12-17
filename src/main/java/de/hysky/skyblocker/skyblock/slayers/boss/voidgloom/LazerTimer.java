package de.hysky.skyblocker.skyblock.slayers.boss.voidgloom;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class LazerTimer {
	private static long lastPhaseTime;
	private static double remainingTime;
	private static boolean active;

	@Init
	public static void init() {
		WorldRenderExtractionCallback.EVENT.register(LazerTimer::extractRendering);
	}

	public static void tick() {
		remainingTime -= 0.05;
		active = remainingTime > 0;
	}

	public static void activate() {
		if (System.currentTimeMillis() - lastPhaseTime >= 10000) {
			lastPhaseTime = System.currentTimeMillis();
			remainingTime = 8;
			active = true;
		}
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (active) {
			SlayerManager.BossFight bossFight = SlayerManager.getBossFight();
			if (bossFight != null && bossFight.boss.getVehicle() != null) {
				Text text = Text.literal(String.format("%.1fs", remainingTime)).formatted(Formatting.AQUA);
				collector.submitText(text, bossFight.boss.getEntityPos().add(0, 1.5, 0), 3, true);
			}
		}
	}

	public static boolean isActive() {
		return active;
	}
}
