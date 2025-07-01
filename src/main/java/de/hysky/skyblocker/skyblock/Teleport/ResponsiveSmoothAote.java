package de.hysky.skyblocker.skyblock.Teleport;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;

import static de.hysky.skyblocker.skyblock.Teleport.PredictiveSmoothAOTE.getItemDistance;

public class ResponsiveSmoothAote {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static Vec3d startPosition;
	private static Vec3d teleportVector;
	private static long startTime;

	public static void playerTeleported() {
		if (startPosition == null || CLIENT.player == null) return;
		teleportVector = CLIENT.player.getEyePos().subtract(startPosition);
		startTime = System.currentTimeMillis();
	}

	public static void PlayerGoingToTeleport() {
		if (CLIENT.player == null) return;
		//make sure teleport is enabled for held item
		ItemStack heldItem = CLIENT.player.getMainHandStack();
		String itemId = heldItem.getSkyblockId();
		NbtCompound customData = ItemUtils.getCustomData(heldItem);
		int distance = getItemDistance(itemId, customData);
		if(distance == -1) {
			return;
		}
		startPosition = CLIENT.player.getEyePos();
	}

	public static Vec3d getInterpolatedPos() {
		if (startPosition == null || teleportVector == null || CLIENT.player == null) return null;
		double progress = (double) (System.currentTimeMillis() - startTime) / SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.maximumAddedLag;
		//end if finished
		if (progress > 1) {
			startPosition = null;
			teleportVector = null;
			return null;
		}

		//return interpolated pos
		return startPosition.add(teleportVector.multiply(progress));

	}
}
