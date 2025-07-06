package de.hysky.skyblocker.skyblock.fishing;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.HelperConfig;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.title.Title;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

public class FishingHelper {
	private static final Title title = new Title("skyblocker.fishing.reelNow", Formatting.GREEN);
	protected static long startTime;
	@Init
	public static void init() {
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (!Utils.isOnSkyblock()) {
				return ActionResult.PASS;
			}
			if (stack.getItem() instanceof FishingRodItem) {
				if (player.fishHook == null) {
					start(player);
				} else {
					reset();
				}
			}
			return ActionResult.PASS;
		});
	}

	public static void start(PlayerEntity player) {
		startTime = System.currentTimeMillis();
		float yawRad = player.getYaw() * 0.017453292F;
	}

	public static void reset() {
		startTime = 0;
		//once amour stand is gone reset rod real timer
	}



	// Sends a title notification if a fish is caught
	public static void checkIfFishWasCaught(ArmorStandEntity armorStand) {
		if (Utils.isOnSkyblock() && (SkyblockerConfigManager.get().helpers.fishing.enableFishingHelper || SkyblockerConfigManager.get().helpers.fishing.fishingHookDisplay == HelperConfig.Fishing.FishingHookDisplay.HUD)) {
			if (!armorStand.isInvisible() || !armorStand.hasCustomName() || !armorStand.isCustomNameVisible()) return;

			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if (player != null && player.fishHook != null) {
				String name = armorStand.getCustomName().getString();
				if (name.equals("!!!") && player.fishHook.getBoundingBox().expand(4D).contains(armorStand.getPos())) {
					if (SkyblockerConfigManager.get().helpers.fishing.enableFishingHelper) {
						RenderHelper.displayInTitleContainerAndPlaySound(title, 10);
					}
				}
			}
		}
	}


}
