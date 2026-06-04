package de.hysky.skyblocker.skyblock.teleport;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import static de.hysky.skyblocker.skyblock.teleport.PredictiveSmoothAOTE.getItemDistance;

public class ResponsiveSmoothAOTE {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static long startTime;
	private static Vec3 lastPosition;
	private static double lastProgress;

	public static void playerGoingToTeleport() {
		if (CLIENT.player == null || SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.maximumAddedLag == 0) return;
		//make sure teleport is enabled for held item
		ItemStack heldItem = CLIENT.player.getMainHandItem();
		String itemId = heldItem.getSkyblockId();
		CompoundTag customData = ItemUtils.getCustomData(heldItem);
		int distance = getItemDistance(itemId, customData);
		if (distance == -1) {
			return;
		}

		Vec3 currentIntermediatePosition = getInterpolatedPos();
		lastPosition = currentIntermediatePosition == null ? CLIENT.player.getEyePosition() : currentIntermediatePosition;
		lastProgress = 0;
		startTime = System.currentTimeMillis();
	}

	public static Vec3 getInterpolatedPos() {
		if (lastPosition == null || CLIENT.player == null) return null;
		double progress = (double) (System.currentTimeMillis() - startTime) / SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.maximumAddedLag;
		//end if finished
		if (progress >= 1) {
			lastPosition = null;
			lastProgress = 0;
			return null;
		}
		//find vector between last position and current pos
		Vec3 teleportVector = CLIENT.player.getEyePosition().subtract(lastPosition);
		double progressDiff = progress - lastProgress;
		double relativeProgress = progressDiff / (1 - lastProgress);


		//return interpolated pos
		if (lastPosition == null) return null; // some who still a problem here
		lastPosition = lastPosition.add(teleportVector.scale(relativeProgress));
		lastProgress = progress;
		return lastPosition;

	}
}
