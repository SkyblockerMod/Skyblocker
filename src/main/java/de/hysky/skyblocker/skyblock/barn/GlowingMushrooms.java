package de.hysky.skyblocker.skyblock.barn;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.ParticleEvents;
import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.LevelRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
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
	private static final Map<BlockPos, GlowingMushroom> glowingMushrooms = new HashMap<>();
	private static final long HIGHLIGHT_DURATION_MS = 4000L;

	@Init
	public static void init() {
		ParticleEvents.FROM_SERVER.register(GlowingMushrooms::onParticle);
		Scheduler.INSTANCE.scheduleCyclic(GlowingMushrooms::update, 1);
		LevelRenderExtractionCallback.EVENT.register(GlowingMushrooms::extractRendering);
		AttackBlockCallback.EVENT.register((_, _, _, pos, _) -> {
			if (shouldProcess()) glowingMushrooms.remove(pos);
			return InteractionResult.PASS;
		});
		ClientPlayConnectionEvents.JOIN.register((_, _, _) -> reset());
	}

	public static void onParticle(ClientboundLevelParticlesPacket packet) {
		if (!shouldProcess() || client.level == null) return;
		ParticleType<?> type = packet.getParticle().getType();
		if (type != ParticleTypes.ENTITY_EFFECT
				&& type != ParticleTypes.EFFECT
				&& type != ParticleTypes.INSTANT_EFFECT
				&& type != ParticleTypes.WITCH) {
			return;
		}

		BlockPos pos = BlockPos.containing(packet.getX(), packet.getY(), packet.getZ());

		if (!isMushroom(pos)) {
			pos = pos.below();
			if (!isMushroom(pos)) return;
		}

		glowingMushrooms.computeIfAbsent(pos, GlowingMushroom::new).refresh();
	}

	private static void update() {
		if (!shouldProcess()) {
			if (!glowingMushrooms.isEmpty()) glowingMushrooms.clear();
			return;
		}

		long now = System.currentTimeMillis();
		glowingMushrooms.entrySet().removeIf(entry -> !entry.getValue().isActive(now) || !isMushroom(entry.getKey()));
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (!shouldProcess() || client.level == null || glowingMushrooms.isEmpty()) return;

		long now = System.currentTimeMillis();
		for (GlowingMushroom glowingMushroom : glowingMushrooms.values()) {
			if (glowingMushroom.isActive(now) && isMushroom(glowingMushroom.pos)) {
				AABB box = RenderHelper.getBlockBoundingBox(client.level, glowingMushroom.pos);
				if (box == null) {
					box = new AABB(glowingMushroom.pos);
				}
				collector.submitOutlinedBox(box, ColorUtils.getFloatComponents(DyeColor.YELLOW), 3, false);
			}
		}
	}

	private static boolean isMushroom(BlockPos pos) {
		if (client.level == null) return false;
		Block block = client.level.getBlockState(pos).getBlock();
		return block == Blocks.RED_MUSHROOM || block == Blocks.BROWN_MUSHROOM;
	}

	private static boolean shouldProcess() {
		return SkyblockerConfigManager.get().otherLocations.barn.enableGlowingMushroomHelper && Utils.isInFarm() && Utils.getArea() == Area.TheFarmingIslands.GLOWING_MUSHROOM_CAVE;
	}

	private static void reset() {
		glowingMushrooms.clear();
	}

	private static final class GlowingMushroom {
		private final BlockPos pos;
		private long lastSeen;

		private GlowingMushroom(BlockPos pos) {
			this.pos = pos;
			this.lastSeen = System.currentTimeMillis();
		}

		private void refresh() {
			this.lastSeen = System.currentTimeMillis();
		}

		private boolean isActive(long now) {
			return lastSeen + HIGHLIGHT_DURATION_MS > now;
		}
	}
}
