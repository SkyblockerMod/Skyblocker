package de.hysky.skyblocker.skyblock.slayers.boss.voidgloom;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class LazerTimer {
	public static double remainingTime = 0;
	private static boolean isRiding = false;

	@Init
	public static void init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(LazerTimer::render);
	}

	public static void updateTimer() {
		if (isRiding) {
			if (!SlayerManager.isBossSpawned()) {
				isRiding = false;
				return;
			}

			remainingTime -= 0.05;
			if (remainingTime <= 0) {
				isRiding = false;
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
			Entity boss = SlayerManager.getSlayerBoss();
			if (boss != null) {
				String timeText = String.format("%.2fs", remainingTime);
				Text renderText = Text.literal("Lazer: ").formatted(Formatting.WHITE)
						.append(Text.literal(timeText).formatted(Formatting.GREEN).formatted(Formatting.BOLD));

				RenderHelper.renderText(context, renderText, boss.getPos().add(0, 2, 0), true);

			}
		}
	}
}
