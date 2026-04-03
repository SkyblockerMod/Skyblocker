package de.hysky.skyblocker.skyblock.fishing;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.HelperConfig;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

public class FishingHelper {
	private static final Title title = new Title("skyblocker.fishing.reelNow", ChatFormatting.GREEN);
	protected static long startTime;

	private static final int BOBBER_TIMEOUT_TICKS = 10;
	private static int waitingForBobberTicks = 0;

	@Init
	public static void init() {
		// Initiate wait for bobber to appear server-side upon cast
		UseItemCallback.EVENT.register((player, _, hand) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (!Utils.isOnSkyblock()) {
				return InteractionResult.PASS;
			}
			if (stack.is(Items.FISHING_ROD)) {
				if (player.fishing == null) {
					waitingForBobberTicks = BOBBER_TIMEOUT_TICKS;
				} else {
					waitingForBobberTicks = 0;
					reset();
				}
			}
			return InteractionResult.PASS;
		});

		// Start fishing bobber timer and remove timer when no bobber detected after server grace period
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null) {
				if (waitingForBobberTicks > 0) {
					if (client.player.fishing != null) {
						waitingForBobberTicks = 0;
						start(); // only start timer when bobber confirmed
					} else {
						waitingForBobberTicks--;
					}
				} else if (client.player.fishing == null) reset();
			}
		});
	}

	public static void start() {
		startTime = System.currentTimeMillis();
	}

	public static void reset() {
		startTime = 0;
	}

	// Sends a title notification if a fish is caught
	public static void checkIfFishWasCaught(ArmorStand armorStand) {
		if (Utils.isOnSkyblock() && (SkyblockerConfigManager.get().helpers.fishing.enableFishingHelper || SkyblockerConfigManager.get().helpers.fishing.fishingHookDisplay == HelperConfig.Fishing.FishingHookDisplay.HUD)) {
			if (!armorStand.isInvisible() || !armorStand.hasCustomName() || !armorStand.isCustomNameVisible()) return;

			LocalPlayer player = Minecraft.getInstance().player;
			if (player != null && player.fishing != null) {
				String name = armorStand.getCustomName().getString();
				if (name.equals("!!!") && player.fishing.getBoundingBox().inflate(4D).contains(armorStand.position())) {
					if (SkyblockerConfigManager.get().helpers.fishing.enableFishingHelper) {
						TitleContainer.addTitleAndPlaySound(title, 10);
					}
				}
			}
		}
	}
}
