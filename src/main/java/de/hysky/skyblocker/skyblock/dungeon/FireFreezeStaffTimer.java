package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;

public class FireFreezeStaffTimer {
	private static final ResourceLocation FIRE_FREEZE_STAFF_TIMER = SkyblockerMod.id("fire_freeze_staff_timer");
	private static long fireFreezeTimer;

	@Init
	public static void init() {
		HudElementRegistry.attachElementAfter(VanillaHudElements.OVERLAY_MESSAGE, FIRE_FREEZE_STAFF_TIMER, FireFreezeStaffTimer::onDraw);
		ClientReceiveMessageEvents.ALLOW_GAME.register(FireFreezeStaffTimer::onChatMessage);
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> FireFreezeStaffTimer.reset());
	}

	private static void onDraw(GuiGraphics context, DeltaTracker tickCounter) {
		Minecraft client = Minecraft.getInstance();

		if (client.screen != null) return;

		if (SkyblockerConfigManager.get().dungeons.theProfessor.fireFreezeStaffTimer && fireFreezeTimer != 0) {
			long now = System.currentTimeMillis();

			if (now >= fireFreezeTimer + 5000) {
				reset();
				return;
			}

			String message =
					fireFreezeTimer > now
							? String.format("%.2f", (float) (fireFreezeTimer - now) / 1000) + "s"
							: "NOW";

			Font renderer = client.font;
			int width = client.getWindow().getGuiScaledWidth() / 2;
			int height = client.getWindow().getGuiScaledHeight() / 2;

			context.drawCenteredString(
					renderer, "Fire freeze in: " + message, width, height, CommonColors.WHITE);
		}
	}

	private static void reset() {
		fireFreezeTimer = 0;
	}

	private static boolean onChatMessage(Component text, boolean overlay) {
		if (!overlay && SkyblockerConfigManager.get().dungeons.theProfessor.fireFreezeStaffTimer && ChatFormatting.stripFormatting(text.getString())
				.equals("[BOSS] The Professor: Oh? You found my Guardians' one weakness?")) {
			fireFreezeTimer = System.currentTimeMillis() + 5000L;
		}

		return true;
	}
}
