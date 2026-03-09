package de.hysky.skyblocker.skyblock.fishing;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.HelperConfig;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;

public class FishingHelper {
	private static final Title title = new Title("skyblocker.fishing.reelNow", ChatFormatting.GREEN);
	protected static long startTime;

	@Init
	public static void init() {
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (!Utils.isOnSkyblock()) {
				return InteractionResult.PASS;
			}
			if (stack.getItem() instanceof FishingRodItem) {
				if (player.fishing == null) {
					start();
				} else {
					reset();
				}
			}
			return InteractionResult.PASS;
		});
		ClientPlayConnectionEvents.JOIN.register((_cPL, _pS, _mc) -> reset());
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
