package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class TenacityTestHelper {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private static final Object2ObjectOpenHashMap<ArmorStandEntity, Vec3d> fireBallsWithStartPos = new Object2ObjectOpenHashMap<>();
	private static final Object2ObjectOpenHashMap<ArmorStandEntity, Vec3d> particleOffsets = new Object2ObjectOpenHashMap<>();

	protected static void reset() {
		fireBallsWithStartPos.clear();
		particleOffsets.clear();
	}

	protected static void extractRendering(PrimitiveCollector collector) {
		for (ArmorStandEntity fireball : fireBallsWithStartPos.keySet()) {
			Vec3d lineStart = fireBallsWithStartPos.get(fireball).add(particleOffsets.getOrDefault(fireball, Vec3d.ZERO));
			Vec3d fireballPos = fireball.getEntityPos().add(particleOffsets.getOrDefault(fireball, Vec3d.ZERO));

			Vec3d distance = fireballPos.subtract(lineStart);
			if (distance.length() > 0.02) { //if big enough gap try from start calculate and show trajectory
				distance = distance.multiply(100);
				Vec3d lineEnd = lineStart.add(distance);

				collector.submitLinesFromPoints(new Vec3d[]{lineStart, lineEnd}, new float[]{1f, 0f, 0f}, 1, 3, false);

				//get highlighted block
				HitResult hitResult = raycast(lineStart, lineEnd, fireball);
				if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult blockHitResult) {
					collector.submitFilledBox(blockHitResult.getBlockPos(), new float[]{1f, 0f, 0f}, 0.5f, false);
				}
			}
		}
	}

	protected static HitResult raycast(Vec3d start, Vec3d end, ArmorStandEntity fireball) {
		if (CLIENT == null || CLIENT.world == null) {
			return null;
		}
		return CLIENT.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.ANY, fireball));
	}

	/**
	 * If a spawned entity is an armour stand add it to the fireballs map (assuming all armour stands are fireballs)
	 *
	 * @param entity spawned entity
	 */
	protected static void onEntitySpawn(Entity entity) {
		if (entity instanceof ArmorStandEntity armorStand) {
			fireBallsWithStartPos.put(armorStand, armorStand.getEntityPos());
		}
	}

	protected static void onEntityDespawn(Entity entity) {
		if (entity instanceof ArmorStandEntity armorStand) {
			fireBallsWithStartPos.remove(armorStand);
		}
	}

	/**
	 * Uses the particles spawned with the fireballs to offset from the armour stand position to get a more accurate guess of where it's going
	 *
	 * @param packet particle packet
	 */
	protected static void onParticle(ParticleS2CPacket packet) {
		if (!ParticleTypes.FLAME.equals(packet.getParameters().getType())) {
			return;
		}
		//get nearest fireball to particle
		Vec3d particlePos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
		ArmorStandEntity neareastFireball = null;
		double clostestDistance = 50;
		for (ArmorStandEntity fireball : fireBallsWithStartPos.keySet()) {
			double distance = fireball.getEntityPos().distanceTo(particlePos);
			if (distance < clostestDistance) {
				neareastFireball = fireball;
				clostestDistance = distance;
			}
		}
		if (neareastFireball == null) { //can not find fireball near particle
			return;
		}
		//adjust fireball offset with particle pos
		Vec3d delta = particlePos.subtract(neareastFireball.getEntityPos());
		//update values
		particleOffsets.put(neareastFireball, delta);
	}
}
