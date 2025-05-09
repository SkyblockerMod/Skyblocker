package de.hysky.skyblocker.skyblock.fishing;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FishingHookDisplayHelper {
	private static ArmorStandEntity fishingHookArmorStand;

	@Init
	public static void init() {
		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (!Utils.isOnSkyblock()) {
				return ActionResult.PASS;
			}
			return ActionResult.PASS;
		});
		HudRenderCallback.EVENT.register(FishingHookDisplayHelper::render);
	}

	public static void render(DrawContext context, RenderTickCounter tickDelta) {
		if (!SkyblockerConfigManager.get().helpers.fishing.enableFishingHookDisplay) return;


		// Check if the armor stand is null or invalid
		if (fishingHookArmorStand == null || !fishingHookArmorStand.isAlive() || !fishingHookArmorStand.hasCustomName()) {
			fishingHookArmorStand = null;
			return;
		}


		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		if (player == null || player.fishHook == null) return;

		// Proceed only if fishingHookArmorStand is not null
		if (fishingHookArmorStand != null) {
//			MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("[DEBUG] Rendering"));

			String armorStandName = fishingHookArmorStand.getName().getString();

			int screenWidth = client.getWindow().getScaledWidth();
			int screenHeight = client.getWindow().getScaledHeight();
			int x = screenWidth / 2; // Center horizontally
			int y = screenHeight / 2; // Position near the top

			// Scale the text by 3x
			context.getMatrices().push();
			context.getMatrices().scale(3.0F, 3.0F, 1.0F);
			context.drawCenteredTextWithShadow(client.textRenderer, armorStandName, (int) (x / 3.0F), (int) (y / 3.0F), 0xFFFF00);
			context.getMatrices().pop();
		}
	}

	public static void onArmorStandSpawn(ArmorStandEntity armorStand) {
		if (!SkyblockerConfigManager.get().helpers.fishing.enableFishingHookDisplay) return;
		if (fishingHookArmorStand != null) return;

		Pattern pattern = Pattern.compile("\\b\\d\\.\\d\\b");
		Matcher matcher = pattern.matcher(armorStand.getName().getString());
		if (armorStand.hasCustomName() && matcher.find()) {
			fishingHookArmorStand = armorStand;
			var message = "ArmorStand spawned: " + fishingHookArmorStand.getName().getString();
//			MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("[DEBUG] " + message));
		} else {
			fishingHookArmorStand = null;
		}
	}
}
