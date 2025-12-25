package de.hysky.skyblocker.skyblock.fishing;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.HelperConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ArmorStand;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FishingHookDisplayHelper {
	protected static ArmorStand fishingHookArmorStand;
	private static final ResourceLocation FISHING_HOOK_DISPLAY = SkyblockerMod.id("fishing_hook_display");
	static Pattern pattern = Pattern.compile("\\d.\\d");

	@Init
	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> fishingHookArmorStand = null);
		HudElementRegistry.attachElementAfter(VanillaHudElements.TITLE_AND_SUBTITLE, FISHING_HOOK_DISPLAY, FishingHookDisplayHelper::render);
	}

	public static void render(GuiGraphics context, DeltaTracker tickDelta) {
		if (SkyblockerConfigManager.get().helpers.fishing.fishingHookDisplay == HelperConfig.Fishing.FishingHookDisplay.OFF) return;


		// Check if the armor stand is null or invalid
		if (fishingHookArmorStand == null || !fishingHookArmorStand.isAlive() || !fishingHookArmorStand.hasCustomName()) {
			fishingHookArmorStand = null;
			return;
		}


		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null || player.fishing == null || fishingHookArmorStand == null) return;

		// render on the crosshair if enabled
		if (SkyblockerConfigManager.get().helpers.fishing.fishingHookDisplay == HelperConfig.Fishing.FishingHookDisplay.CROSSHAIR) {
			Component armorStandName = fishingHookArmorStand.getName();

			int screenWidth = client.getWindow().getGuiScaledWidth();
			int screenHeight = client.getWindow().getGuiScaledHeight();
			int x = screenWidth / 2; // Center horizontally
			int y = screenHeight / 2; // Position near the top

			// Scale the text by 3x
			context.pose().pushMatrix();
			context.pose().scale(3.0F, 3.0F);
			context.drawCenteredString(client.font, armorStandName, (int) (x / 3.0F), (int) (y / 3.0F), 0xFFFFFF00);
			context.pose().popMatrix();
		}
		//else update the tab
		else {
			FishingHudWidget.getInstance().update();
		}
	}

	public static void onArmorStandSpawn(ArmorStand armorStand) {
		if (SkyblockerConfigManager.get().helpers.fishing.fishingHookDisplay == HelperConfig.Fishing.FishingHookDisplay.OFF) return;
		if (fishingHookArmorStand != null) return;

		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.player.fishing == null) return;
		// Check the distance between the armor stand and the player's fishing hook
		double distance = armorStand.position().distanceTo(client.player.fishing.position());
		if (distance > 0.1) return; // Checks for a minimum distance of 0.1 blocks

		Matcher matcher = pattern.matcher(armorStand.getName().getString());
		if (armorStand.hasCustomName() && matcher.matches()) {
			fishingHookArmorStand = armorStand;
		} else {
			fishingHookArmorStand = null;
		}
	}
}
