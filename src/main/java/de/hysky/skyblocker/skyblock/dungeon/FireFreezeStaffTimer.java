package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.ServerTickCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;

public class FireFreezeStaffTimer {
	private static final Identifier FIRE_FREEZE_STAFF_TIMER = SkyblockerMod.id("fire_freeze_staff_timer");
	private static long fireFreezeTimer;
	private static boolean timerActive = false;

	@Init
	public static void init() {
		HudElementRegistry.attachElementAfter(VanillaHudElements.OVERLAY_MESSAGE, FIRE_FREEZE_STAFF_TIMER, FireFreezeStaffTimer::onDraw);
		ClientReceiveMessageEvents.ALLOW_GAME.register(FireFreezeStaffTimer::onChatMessage);
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> FireFreezeStaffTimer.reset());
		ServerTickCallback.EVENT.register(FireFreezeStaffTimer::onServerTick);
	}

	private static void onServerTick() {
		if (timerActive) fireFreezeTimer -= 50;
	}

	private static void onDraw(GuiGraphics context, DeltaTracker tickCounter) {
		Minecraft client = Minecraft.getInstance();

		if (client.screen != null) return;

		if (SkyblockerConfigManager.get().dungeons.theProfessor.fireFreezeStaffTimer && fireFreezeTimer != 0) {
			if (fireFreezeTimer <= -5000) {
				reset();
				return;
			}

			Component message;
			if (fireFreezeTimer > 0) {
				message = Component.literal("in: ").append(Component.literal(String.format("%.2f", (float) (fireFreezeTimer) / 1000) + "s").withStyle(ChatFormatting.YELLOW));
			} else {
				message = Component.literal("NOW").withStyle(ChatFormatting.RED);
			}

			Font renderer = client.font;
			int width = client.getWindow().getGuiScaledWidth() / 2;
			int height = client.getWindow().getGuiScaledHeight() / 2;

			context.drawCenteredString(renderer, Component.literal("Fire Freeze ").append(message), width, height, CommonColors.WHITE);
		}
	}

	private static void reset() {
		fireFreezeTimer = 0;
		timerActive = false;
	}

	private static boolean onChatMessage(Component text, boolean overlay) {
		if (!overlay && SkyblockerConfigManager.get().dungeons.theProfessor.fireFreezeStaffTimer && ChatFormatting.stripFormatting(text.getString())
				.equals("[BOSS] The Professor: Oh? You found my Guardians' one weakness?")) {
			fireFreezeTimer = 5750L;
			timerActive = true;
		}

		return true;
	}
}
