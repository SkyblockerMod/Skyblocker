package de.hysky.skyblocker.skyblock.fishing;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.HelperConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FishingHookDisplayHelper {
	protected static ArmorStandEntity fishingHookArmorStand;
	private static final Identifier FISHING_HOOK_DISPLAY = Identifier.of(SkyblockerMod.NAMESPACE, "fishing_hook_display");
	static Pattern pattern = Pattern.compile("\\d.\\d");

	@Init
	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> fishingHookArmorStand = null);

		HudLayerRegistrationCallback.EVENT.register(d -> d.attachLayerAfter(IdentifiedLayer.TITLE_AND_SUBTITLE,FISHING_HOOK_DISPLAY,FishingHookDisplayHelper::render));
	}

	public static void render(DrawContext context, RenderTickCounter tickDelta) {
		if (SkyblockerConfigManager.get().helpers.fishing.fishingHookDisplay == HelperConfig.Fishing.FishingHookDisplay.OFF) return;


		// Check if the armor stand is null or invalid
		if (fishingHookArmorStand == null || !fishingHookArmorStand.isAlive() || !fishingHookArmorStand.hasCustomName()) {
			fishingHookArmorStand = null;
			return;
		}


		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		if (player == null || player.fishHook == null || fishingHookArmorStand == null) return;

		// render on the crosshair if enabled
		if (SkyblockerConfigManager.get().helpers.fishing.fishingHookDisplay == HelperConfig.Fishing.FishingHookDisplay.CROSSHAIR) {
			Text armorStandName = fishingHookArmorStand.getName();

			int screenWidth = client.getWindow().getScaledWidth();
			int screenHeight = client.getWindow().getScaledHeight();
			int x = screenWidth / 2; // Center horizontally
			int y = screenHeight / 2; // Position near the top

			// Scale the text by 3x
			context.getMatrices().push();
			context.getMatrices().scale(3.0F, 3.0F, 1.0F);
			context.drawCenteredTextWithShadow(client.textRenderer, armorStandName, (int) (x / 3.0F), (int) (y / 3.0F),0);
			context.getMatrices().pop();
		}
		//else update the tab
		else {
			FishingHudWidget.getInstance().update();
		}
	}

	public static void onArmorStandSpawn(ArmorStandEntity armorStand) {
		if (SkyblockerConfigManager.get().helpers.fishing.fishingHookDisplay == HelperConfig.Fishing.FishingHookDisplay.OFF) return;
		if (fishingHookArmorStand != null) return;

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.player.fishHook == null) return;
		// Check the distance between the armor stand and the player's fishing hook
		double distance = armorStand.getPos().distanceTo(client.player.fishHook.getPos());
		if (distance > 0.1) return; // Checks for a minimum distance of 0.1 blocks

		Matcher matcher = pattern.matcher(armorStand.getName().getString());
		if (armorStand.hasCustomName() && matcher.matches()) {
			fishingHookArmorStand = armorStand;
		} else {
			fishingHookArmorStand = null;
		}
	}
}
