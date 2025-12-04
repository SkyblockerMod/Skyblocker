package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class ControlTestHelper {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private static WitherSkeletonEntity correctWitherSkeleton;
	private static Vec3d lastPos;
	private static long lastUpdate;
	private static Vec3d pingOffset;
	private static Vec3d lastPingOffset;

	protected static void reset() {
		correctWitherSkeleton = null;
		lastPos = null;
		lastUpdate = -1;
		pingOffset = null;
		lastPingOffset = null;
	}

	/**
	 * Find the correct WitherSkeleton entity when it spawns to start tracking it
	 *
	 * @param entity spawned entity
	 */
	protected static void onEntitySpawn(Entity entity) {
		if (entity instanceof WitherSkeletonEntity witherSkeleton && correctWitherSkeleton == null) {
			correctWitherSkeleton = witherSkeleton;
		}
	}

	/**
	 * Finds where to look in 3 ticks effected by ping
	 */
	protected static void update() {
		if (correctWitherSkeleton != null) {
			//smoothly adjust the ping throughout the test
			if (lastPos != null) {
				lastPingOffset = pingOffset;
				double ping = DojoManager.ping / 1000d;
				//find distance between last position and current position of skeleton
				Vec3d movementVector = correctWitherSkeleton.getEntityPos().subtract(lastPos).multiply(1, 0.1, 1);
				//adjust the vector to current ping (multiply by 1 + time in second until the next update offset by the players ping)
				pingOffset = movementVector.multiply(1 + 3 / 20d + ping);
			}
			lastPos = correctWitherSkeleton.getEntityPos();
			lastUpdate = System.currentTimeMillis();
		}
	}

	/**
	 * Renders an outline around where the player should aim (assumes values are updated every 3 ticks)
	 *
	 * @param context render context
	 */
	protected static void extractRendering(PrimitiveCollector collector) {
		if (CLIENT.player != null && correctWitherSkeleton != null && pingOffset != null && lastPingOffset != null) {
			float tickDelta = RenderHelper.getTickCounter().getTickProgress(false);
			//how long until net update
			double updatePercent = (double) (System.currentTimeMillis() - lastUpdate) / 150;
			Vec3d aimPos = correctWitherSkeleton.getCameraPosVec(tickDelta).add(pingOffset.multiply(updatePercent)).add(lastPingOffset.multiply(1 - updatePercent));
			Box targetBox = new Box(aimPos.add(-0.5, -0.5, -0.5), aimPos.add(0.5, 0.5, 0.5));
			boolean playerLookingAtBox = targetBox.raycast(CLIENT.player.getCameraPosVec(tickDelta), CLIENT.player.getCameraPosVec(tickDelta).add(CLIENT.player.getRotationVec(tickDelta).multiply(30))).isPresent();
			float[] boxColor = playerLookingAtBox ? Color.GREEN.getColorComponents(new float[]{0, 0, 0}) : Color.LIGHT_GRAY.getColorComponents(new float[]{0, 0, 0});
			collector.submitOutlinedBox(targetBox, boxColor, 3, true);
		}
	}
}
