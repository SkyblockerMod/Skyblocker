package de.hysky.skyblocker.skyblock.barn;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.ParticleEvents;
import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import de.hysky.skyblocker.utils.waypoint.SeenWaypoint;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import java.util.HashMap;
import java.util.Map;

public class GlowingMushrooms {
	private static final Minecraft client = Minecraft.getInstance();
	private static final Map<BlockPos, GlowingMushrooms.GlowingMushroom> glowingMushrooms = new HashMap<>();

	@Init
	public static void init() {
		ParticleEvents.FROM_SERVER.register(GlowingMushrooms::onParticle);
		Scheduler.INSTANCE.scheduleCyclic(GlowingMushrooms::update, 1);
		WorldRenderExtractionCallback.EVENT.register(GlowingMushrooms::extractRendering);
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			if (shouldProcess()) glowingMushrooms.remove(pos);
			return InteractionResult.PASS;
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
	}

	public static void onParticle(ClientboundLevelParticlesPacket packet) {
		if (!shouldProcess() || client.level == null) return;
		if (!ParticleTypes.ENTITY_EFFECT.equals(packet.getParticle().getType())) return;

		BlockPos pos = BlockPos.containing(packet.getX(), packet.getY(), packet.getZ());

		Block block = client.level.getBlockState(pos).getBlock();
		if (block != Blocks.RED_MUSHROOM && block != Blocks.BROWN_MUSHROOM) return;

		GlowingMushroom mushroom = glowingMushrooms.computeIfAbsent(pos, GlowingMushroom::new);
		mushroom.addParticle();
	}

	private static void update() {
		if (!shouldProcess()) return;
		for (GlowingMushroom glowingMushroom : glowingMushrooms.values()) {
			glowingMushroom.updateWaypoint();
		}
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (!shouldProcess() || client.level == null) return;
		for (GlowingMushroom glowingMushroom : glowingMushrooms.values()) {
			if (!glowingMushroom.shouldRender()) continue;

			Block block = client.level.getBlockState(glowingMushroom.pos).getBlock();
			if (block != Blocks.RED_MUSHROOM && block != Blocks.BROWN_MUSHROOM) continue;

			AABB boundingBox = RenderHelper.getBlockBoundingBox(client.level, glowingMushroom.pos);
			collector.submitOutlinedBox(boundingBox, ColorUtils.getFloatComponents(DyeColor.YELLOW), 3, glowingMushroom.shouldRenderThroughWalls());
		}
	}

	private static boolean shouldProcess() {
		return SkyblockerConfigManager.get().otherLocations.barn.enableGlowingMushroomHelper && Utils.isInFarm() && Utils.getArea() == Area.GLOWING_MUSHROOM_CAVE;
	}

	private static void reset() {
		glowingMushrooms.clear();
	}

	public static class GlowingMushroom extends SeenWaypoint {
		private int particleCount = 0;
		private long lastConfirmed;

		private GlowingMushroom(BlockPos pos) {
			super(pos, () -> SkyblockerConfigManager.get().uiAndVisuals.waypoints.waypointType, ColorUtils.getFloatComponents(DyeColor.CYAN));
		}

		private void updateWaypoint() {
			super.tick(client);

			if (particleCount < 1) return;

			long now = System.currentTimeMillis();
			if (lastConfirmed + 2000 > now) return;

			lastConfirmed = now;
			particleCount = 0;
		}

		public void addParticle() {
			particleCount++;
		}

		@Override
		public boolean shouldRender() {
			return super.shouldRender() && lastConfirmed + 5000 > System.currentTimeMillis();
		}
	}
}
