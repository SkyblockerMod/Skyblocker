package de.hysky.skyblocker.skyblock.slayers.boss.voidgloom;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class LazerTimer {

	public static UUID bossUUID = null;
	public static Vec3d bossLocation = null;

	public static double remainingTime = 0;
	private static boolean isRiding = false;

	@Init
	public static void init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(LazerTimer::render);
		Scheduler.INSTANCE.scheduleCyclic(LazerTimer::updateTimer, 1);
	}

	private static void updateTimer() {
		if (isRiding) {
			if (!SlayerManager.isBossSpawned()) {//in case player died in lazer phase
				isRiding = false;
				remainingTime = 0;
				return;
			}

			remainingTime -= 0.05;
			if (remainingTime <= 0) {
				remainingTime = 0;
				isRiding = false;
				bossUUID = null;
				bossLocation = null;
			}
		}
	}

	public static void resetTimer() {
		remainingTime = 8.0;
	}

	public static boolean isRiding() {
		return isRiding;
	}

	public static void setRiding(boolean riding) {
		isRiding = riding;
	}

	private static void render(WorldRenderContext context) {
		if (isRiding) {
			String timeText = String.format("%.2fsc", remainingTime);
			Text renderText = Text.literal("Lazer: ").formatted(Formatting.WHITE)
					.append(Text.literal(timeText).formatted(Formatting.GREEN).formatted(Formatting.BOLD));

			RenderHelper.renderText(context, renderText, bossLocation.add(0, 2, 0), true);
		}
	}
}
