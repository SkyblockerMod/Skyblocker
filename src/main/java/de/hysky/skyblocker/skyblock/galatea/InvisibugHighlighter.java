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
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
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

	private static void onParticle(ClientboundLevelParticlesPacket packet) {
		if (isActive() && packet.getParticle().getType() instanceof ParticleType<?> type && (ParticleTypes.CRIT.equals(type))) {
			handleInvisibugParticle(packet);
		}
	}

	/**
	 * Records the particle position as an active invisibug location and removes nearby locations to handle movement.
	 *
	 * @param packet the incoming particle packet containing the spawn position
	 */
	private static void handleInvisibugParticle(ClientboundLevelParticlesPacket packet) {
		if (Minecraft.getInstance().level == null) return;
		Vec3 pos = new Vec3(packet.getX(), packet.getY(), packet.getZ());
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
