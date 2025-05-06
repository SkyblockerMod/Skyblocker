package de.hysky.skyblocker.skyblock.fishing;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class FishingHookDisplayHelper {
	private static ArmorStandEntity fishingHookArmorStand;

	@Init
	public static void init() {
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (!Utils.isOnSkyblock()) {
				return ActionResult.PASS;
			}
			return ActionResult.PASS;
		});
		WorldRenderEvents.AFTER_TRANSLUCENT.register(FishingHookDisplayHelper::render);
	}

	public static void render(WorldRenderContext context) {
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
			Vec3d playerEyePos = player.getPos().add(0, player.getEyeHeight(player.getPose()), 0);
			Vec3d lookVec = player.getRotationVec(1.0F).normalize();
			Vec3d displayPos = playerEyePos.add(lookVec.multiply(3.0)); // 2 blocks in front of the player

			String armorStandName = fishingHookArmorStand.getName().getString();
			RenderHelper.renderText(context, Text.literal(armorStandName), displayPos, true);
		}
	}

	public static void onArmorStandSpawn(ArmorStandEntity armorStand) {
		if (!SkyblockerConfigManager.get().helpers.fishing.enableFishingHookDisplay) return;

		if (armorStand.hasCustomName() && armorStand.getName().getString().matches("\\d+(\\.\\d+)?")) {
			fishingHookArmorStand = armorStand;
			var message = "ArmorStand spawned: " + fishingHookArmorStand.getName().getString();
			MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("[DEBUG] " + message));
		} else {
			fishingHookArmorStand = null;
		}
	}
}
