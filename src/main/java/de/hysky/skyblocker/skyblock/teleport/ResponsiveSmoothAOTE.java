package de.hysky.skyblocker.skyblock.teleport;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;

import static de.hysky.skyblocker.skyblock.teleport.PredictiveSmoothAOTE.getItemDistance;

public class ResponsiveSmoothAOTE {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private static long startTime;
	private static Vec3d lastPosition;
	private static double lastProgress;

	public static void PlayerGoingToTeleport() {
		if (CLIENT.player == null) return;
		//make sure teleport is enabled for held item
		ItemStack heldItem = CLIENT.player.getMainHandStack();
		String itemId = heldItem.getSkyblockId();
		NbtCompound customData = ItemUtils.getCustomData(heldItem);
		int distance = getItemDistance(itemId, customData);
		if (distance == -1) {
			return;
		}
		lastPosition = CLIENT.player.getEyePos();
		lastProgress = 0;
		startTime = System.currentTimeMillis();
	}

	public static Vec3d getInterpolatedPos() {
		if (lastPosition == null || CLIENT.player == null) return null;
		double progress = (double) (System.currentTimeMillis() - startTime) / SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.maximumAddedLag;
		//end if finished
		if (progress > 1) {
			lastPosition = null;
			lastProgress = 0;
			return null;
		}
		//find vector between last position and current pos
		Vec3d teleportVector = CLIENT.player.getEyePos().subtract(lastPosition);
		double progressDiff = progress - lastProgress;
		double relativeProgress = progressDiff / (1 - lastProgress);


		//return interpolated pos
		if (lastPosition == null) return null; // some who still a problem here
		lastPosition = lastPosition.add(teleportVector.multiply(relativeProgress));
		lastProgress = progress;
		return lastPosition;

	}
}
