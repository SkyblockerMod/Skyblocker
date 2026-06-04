package de.hysky.skyblocker.skyblock.teleport;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

import static de.hysky.skyblocker.skyblock.teleport.PredictiveSmoothAOTE.getItemDistance;

public class ResponsiveSmoothAOTE {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static long startTime;
	@Nullable
	private static Vec3 lastPosition;
	private static double lastProgress;
	@Nullable
	private static Vec3 cameraStartPos;

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
		// make sure the camera is not in 3rd person if disabled
		if (CLIENT.options.getCameraType() != CameraType.FIRST_PERSON && !SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.thirdPerson) {
			return;
		}

		Vec3 currentIntermediatePosition = getInterpolatedPos(RenderHelper.getCamera().position());
		lastPosition = currentIntermediatePosition == null ? RenderHelper.getCamera().position() : currentIntermediatePosition;
		lastProgress = 0;
		startTime = System.currentTimeMillis();
	}

	@Nullable
	public static Vec3 getInterpolatedPos(Vec3 original) {
		if (lastPosition == null || CLIENT.player == null) return null;
		cameraStartPos = original;
		double progress = (double) (System.currentTimeMillis() - startTime) / SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.maximumAddedLag;
		//end if finished
		if (progress >= 1) {
			lastPosition = null;
			lastProgress = 0;
			return null;
		}
		//find vector between last position and current pos
		Vec3 teleportVector = original.subtract(lastPosition);
		double progressDiff = progress - lastProgress;
		double relativeProgress = progressDiff / (1 - lastProgress);


		//return interpolated pos
		if (lastPosition == null) return null; // some who still a problem here
		lastPosition = lastPosition.add(teleportVector.scale(relativeProgress));
		lastProgress = progress;
		return lastPosition;
	}

	@Nullable
	public static Vec3 getInterpolatedPlayerPos(float partialTicks) {
		if (CLIENT.player == null || cameraStartPos == null) return null;
		Vec3 diff = CLIENT.player.getPosition(partialTicks).subtract(cameraStartPos);
		Vec3 camara = getInterpolatedPos(cameraStartPos);
		if (camara == null) return null;
		return camara.add(diff);
	}
}
