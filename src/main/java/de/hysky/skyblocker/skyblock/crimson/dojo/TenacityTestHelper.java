package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class TenacityTestHelper {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static final Object2ObjectOpenHashMap<ArmorStand, Vec3> fireBallsWithStartPos = new Object2ObjectOpenHashMap<>();
	private static final Object2ObjectOpenHashMap<ArmorStand, Vec3> particleOffsets = new Object2ObjectOpenHashMap<>();

	protected static void reset() {
		fireBallsWithStartPos.clear();
		particleOffsets.clear();
	}

	protected static void extractRendering(PrimitiveCollector collector) {
		for (ArmorStand fireball : fireBallsWithStartPos.keySet()) {
			Vec3 lineStart = fireBallsWithStartPos.get(fireball).add(particleOffsets.getOrDefault(fireball, Vec3.ZERO));
			Vec3 fireballPos = fireball.position().add(particleOffsets.getOrDefault(fireball, Vec3.ZERO));

			Vec3 distance = fireballPos.subtract(lineStart);
			if (distance.length() > 0.02) { //if big enough gap try from start calculate and show trajectory
				distance = distance.scale(100);
				Vec3 lineEnd = lineStart.add(distance);

				collector.submitLinesFromPoints(new Vec3[]{lineStart, lineEnd}, new float[]{1f, 0f, 0f}, 1, 3, false);

				//get highlighted block
				HitResult hitResult = raycast(lineStart, lineEnd, fireball);
				if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult blockHitResult) {
					collector.submitFilledBox(blockHitResult.getBlockPos(), new float[]{1f, 0f, 0f}, 0.5f, false);
				}
			}
		}
	}

	protected static HitResult raycast(Vec3 start, Vec3 end, ArmorStand fireball) {
		if (CLIENT == null || CLIENT.level == null) {
			return null;
		}
		return CLIENT.level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, fireball));
	}

	/**
	 * If a spawned entity is an armour stand add it to the fireballs map (assuming all armour stands are fireballs)
	 *
	 * @param entity spawned entity
	 */
	protected static void onEntitySpawn(Entity entity) {
		if (entity instanceof ArmorStand armorStand) {
			fireBallsWithStartPos.put(armorStand, armorStand.position());
		}
	}

	protected static void onEntityDespawn(Entity entity) {
		if (entity instanceof ArmorStand armorStand) {
			fireBallsWithStartPos.remove(armorStand);
		}
	}

	/**
	 * Uses the particles spawned with the fireballs to offset from the armour stand position to get a more accurate guess of where it's going
	 *
	 * @param packet particle packet
	 */
	protected static void onParticle(ClientboundLevelParticlesPacket packet) {
		if (!ParticleTypes.FLAME.equals(packet.getParticle().getType())) {
			return;
		}
		//get nearest fireball to particle
		Vec3 particlePos = new Vec3(packet.getX(), packet.getY(), packet.getZ());
		ArmorStand neareastFireball = null;
		double clostestDistance = 50;
		for (ArmorStand fireball : fireBallsWithStartPos.keySet()) {
			double distance = fireball.position().distanceTo(particlePos);
			if (distance < clostestDistance) {
				neareastFireball = fireball;
				clostestDistance = distance;
			}
		}
		if (neareastFireball == null) { //can not find fireball near particle
			return;
		}
		//adjust fireball offset with particle pos
		Vec3 delta = particlePos.subtract(neareastFireball.position());
		//update values
		particleOffsets.put(neareastFireball, delta);
	}
}
