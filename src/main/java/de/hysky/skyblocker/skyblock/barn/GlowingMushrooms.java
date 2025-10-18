package de.hysky.skyblocker.skyblock.barn;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.ParticleEvents;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import de.hysky.skyblocker.utils.waypoint.SeenWaypoint;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.HashMap;
import java.util.Map;

public class GlowingMushrooms {
	private static final MinecraftClient client = MinecraftClient.getInstance();
	private static final Map<BlockPos, GlowingMushrooms.GlowingMushroom> glowingMushrooms = new HashMap<>();

	@Init
	public static void init() {
		ParticleEvents.FROM_SERVER.register(GlowingMushrooms::onParticle);
		Scheduler.INSTANCE.scheduleCyclic(GlowingMushrooms::update, 1);
		WorldRenderExtractionCallback.EVENT.register(GlowingMushrooms::extractRendering);
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			if (shouldProcess()) glowingMushrooms.remove(pos);
			return ActionResult.PASS;
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
	}

	public static void onParticle(ParticleS2CPacket packet) {
		if (!shouldProcess() || client.world == null) return;
		if (!ParticleTypes.ENTITY_EFFECT.equals(packet.getParameters().getType())) return;

		BlockPos pos = BlockPos.ofFloored(packet.getX(), packet.getY(), packet.getZ());

		Block block = client.world.getBlockState(pos).getBlock();
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
		if (!shouldProcess() || client.world == null) return;
		for (GlowingMushroom glowingMushroom : glowingMushrooms.values()) {
			if (!glowingMushroom.shouldRender()) continue;

			Block block = client.world.getBlockState(glowingMushroom.pos).getBlock();
			if (block != Blocks.RED_MUSHROOM && block != Blocks.BROWN_MUSHROOM) continue;

			Box boundingBox = RenderHelper.getBlockBoundingBox(client.world, glowingMushroom.pos);
			collector.submitOutlinedBox(boundingBox, ColorUtils.getFloatComponents(DyeColor.YELLOW), 3, glowingMushroom.shouldRenderThroughWalls());
		}
	}

	private static boolean shouldProcess() {
		return SkyblockerConfigManager.get().otherLocations.barn.enableGlowingMushroomHelper && Utils.isInFarm() && Utils.getIslandArea().equals("⏣ Glowing Mushroom Cave");
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
