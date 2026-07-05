package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.ParticleEvents;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.LevelRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class InvisibugHighlighter {
	private static final Minecraft client = Minecraft.getInstance();
	private static final Object2LongOpenHashMap<Vec3> invisibugLocations = new Object2LongOpenHashMap<>();

	private static final long PARTICLE_EXPIRY = 500;
	private static final float[] COLOR = new float[3];
	private static final float ALPHA = 0.20f;
	private static final Vec3 HIGHLIGHT_SIZE = new Vec3(0.5f, 0.5f, 0.5f);

	@Init
	public static void init() {
		LevelRenderExtractionCallback.EVENT.register(InvisibugHighlighter::extractRendering);
		ClientPlayConnectionEvents.JOIN.register((_, _, _) -> invisibugLocations.clear());
		ParticleEvents.FROM_SERVER.register(InvisibugHighlighter::onParticle);
	}

	private static boolean isActive() {
		return SkyblockerConfigManager.get().hunting.huntingMobs.highlightInvisibug && Utils.isInGalatea();
	}

	/**
	 * Filters incoming crit particles and forwards valid ones to {@link #handleInvisibugParticle}.
	 * Particles are ignored if a non-player {@link LivingEntity} is at the spawn position,
	 * preventing false positives from striking certain NPCs.
	 *
	 * @param packet the incoming particle packet from the server
	 */
	private static void onParticle(ClientboundLevelParticlesPacket packet) {
		if (isActive() && ParticleTypes.CRIT.equals(packet.getParticle().getType()) && client.level != null) {
			Vec3 pos = new Vec3(packet.getX(), packet.getY(), packet.getZ());
			// Exclude particles that are emitted from striking an NPC
			if (client.level.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(pos, 1, 1, 1), e -> !(e instanceof Player)).isEmpty()) {
				handleInvisibugParticle(pos);
			}
		}
	}

	/**
	 * Records the particle position as an active invisibug location and removes nearby locations to handle movement.
	 *
	 * @param pos A vector representing the position of the invisibug particle
	 */
	private static void handleInvisibugParticle(Vec3 pos) {
		// As the invisibug moves, remove the previous location
		invisibugLocations.object2LongEntrySet().removeIf(e -> e.getKey().distanceToSqr(pos) <= 1);
		// Add the new location to the map
		invisibugLocations.put(pos, System.currentTimeMillis());
	}

	/**
	 * Renders a highlight box at each active invisibug position and removes expired locations where there is no longer an invisibug.
	 *
	 * @param collector the primitive collector to submit render geometry to
	 */
	public static void extractRendering(PrimitiveCollector collector) {
		if (!isActive() || invisibugLocations.isEmpty() || client.player == null) return;

		long now = System.currentTimeMillis();
		invisibugLocations.object2LongEntrySet().removeIf(e -> now - e.getLongValue() > PARTICLE_EXPIRY);

		SkyblockerConfigManager.get().hunting.huntingMobs.invisibugGlowColor.getRGBColorComponents(COLOR);
		for (Vec3 pos : invisibugLocations.keySet()) {
			collector.submitFilledBox(pos.subtract(HIGHLIGHT_SIZE.scale(0.5)), HIGHLIGHT_SIZE, COLOR, ALPHA, false);
		}
	}
}
