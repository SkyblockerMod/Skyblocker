package de.hysky.skyblocker.skyblock.crimson.dojo;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;

public class ControlTestHelper {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static WitherSkeleton correctWitherSkeleton;
	private static Vec3 lastPos;
	private static long lastUpdate;
	private static Vec3 pingOffset;
	private static Vec3 lastPingOffset;

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
		if (entity instanceof WitherSkeleton witherSkeleton && correctWitherSkeleton == null) {
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
				Vec3 movementVector = correctWitherSkeleton.position().subtract(lastPos).multiply(1, 0.1, 1);
				//adjust the vector to current ping (multiply by 1 + time in second until the next update offset by the players ping)
				pingOffset = movementVector.scale(1 + 3 / 20d + ping);
			}
			lastPos = correctWitherSkeleton.position();
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
			float tickDelta = RenderHelper.getTickCounter().getGameTimeDeltaPartialTick(false);
			//how long until net update
			double updatePercent = (double) (System.currentTimeMillis() - lastUpdate) / 150;
			Vec3 aimPos = correctWitherSkeleton.getEyePosition(tickDelta).add(pingOffset.scale(updatePercent)).add(lastPingOffset.scale(1 - updatePercent));
			AABB targetBox = new AABB(aimPos.add(-0.5, -0.5, -0.5), aimPos.add(0.5, 0.5, 0.5));
			boolean playerLookingAtBox = targetBox.clip(CLIENT.player.getEyePosition(tickDelta), CLIENT.player.getEyePosition(tickDelta).add(CLIENT.player.getViewVector(tickDelta).scale(30))).isPresent();
			float[] boxColor = playerLookingAtBox ? Color.GREEN.getColorComponents(new float[]{0, 0, 0}) : Color.LIGHT_GRAY.getColorComponents(new float[]{0, 0, 0});
			collector.submitOutlinedBox(targetBox, boxColor, 3, true);
		}
	}
}
