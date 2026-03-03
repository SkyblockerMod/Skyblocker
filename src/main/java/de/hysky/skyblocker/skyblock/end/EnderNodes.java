package de.hysky.skyblocker.skyblock.end;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.ParticleEvents;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import de.hysky.skyblocker.utils.waypoint.SeenWaypoint;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import java.util.HashMap;
import java.util.Map;

public class EnderNodes {
	private static final Minecraft client = Minecraft.getInstance();
	private static final Map<BlockPos, EnderNode> enderNodes = new HashMap<>();

	@Init
	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(EnderNodes::update, 20);
		WorldRenderExtractionCallback.EVENT.register(EnderNodes::extractRendering);
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			enderNodes.remove(pos);
			return InteractionResult.PASS;
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
		ParticleEvents.FROM_SERVER.register(EnderNodes::onParticle);
	}

	private static void onParticle(ClientboundLevelParticlesPacket packet) {
		if (!shouldProcess()) return;
		ParticleType<?> particleType = packet.getParticle().getType();
		if (!ParticleTypes.PORTAL.getType().equals(particleType) && !ParticleTypes.WITCH.getType().equals(particleType))
			return;

		double x = packet.getX();
		double y = packet.getY();
		double z = packet.getZ();
		double xFrac = Mth.positiveModulo(x, 1);
		double yFrac = Mth.positiveModulo(y, 1);
		double zFrac = Mth.positiveModulo(z, 1);
		BlockPos pos;
		Direction direction;
		if (yFrac == 0.25) {
			pos = BlockPos.containing(x, y - 1, z);
			direction = Direction.UP;
		} else if (yFrac == 0.75) {
			pos = BlockPos.containing(x, y + 1, z);
			direction = Direction.DOWN;
		} else if (xFrac == 0.25) {
			pos = BlockPos.containing(x - 1, y, z);
			direction = Direction.EAST;
		} else if (xFrac == 0.75) {
			pos = BlockPos.containing(x + 1, y, z);
			direction = Direction.WEST;
		} else if (zFrac == 0.25) {
			pos = BlockPos.containing(x, y, z - 1);
			direction = Direction.SOUTH;
		} else if (zFrac == 0.75) {
			pos = BlockPos.containing(x, y, z + 1);
			direction = Direction.NORTH;
		} else {
			return;
		}

		EnderNode enderNode = enderNodes.computeIfAbsent(pos, EnderNode::new);
		IntIntPair particles = enderNode.particles.get(direction);
		if (ParticleTypes.PORTAL.getType().equals(particleType)) {
			particles.left(particles.leftInt() + 1);
		} else if (ParticleTypes.WITCH.getType().equals(particleType)) {
			particles.right(particles.rightInt() + 1);
		}
	}

	private static void update() {
		if (shouldProcess()) {
			for (EnderNode enderNode : enderNodes.values()) {
				enderNode.updateWaypoint();
			}
		}
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (shouldProcess()) {
			for (EnderNode enderNode : enderNodes.values()) {
				if (enderNode.shouldRender()) {
					enderNode.extractRendering(collector);
				}
			}
		}
	}

	private static boolean shouldProcess() {
		return SkyblockerConfigManager.get().otherLocations.end.enableEnderNodeHelper && Utils.isInTheEnd();
	}

	private static void reset() {
		enderNodes.clear();
	}

	public static class EnderNode extends SeenWaypoint {
		private final Map<Direction, IntIntPair> particles = Map.of(
				Direction.UP, new IntIntMutablePair(0, 0),
				Direction.DOWN, new IntIntMutablePair(0, 0),
				Direction.EAST, new IntIntMutablePair(0, 0),
				Direction.WEST, new IntIntMutablePair(0, 0),
				Direction.SOUTH, new IntIntMutablePair(0, 0),
				Direction.NORTH, new IntIntMutablePair(0, 0)
		);
		private long lastConfirmed;

		private EnderNode(BlockPos pos) {
			super(pos, () -> SkyblockerConfigManager.get().otherLocations.end.enderNodeWaypointType, ColorUtils.getFloatComponents(DyeColor.CYAN));
		}

		private void updateWaypoint() {
			tick(client);
			long currentTimeMillis = System.currentTimeMillis();
			if (lastConfirmed + 2000 > currentTimeMillis || client.level == null || !particles.entrySet().stream().allMatch(entry -> entry.getValue().leftInt() >= 5 && entry.getValue().rightInt() >= 5 || !client.level.getBlockState(pos.relative(entry.getKey())).isAir())) return;
			lastConfirmed = currentTimeMillis;
			for (Map.Entry<Direction, IntIntPair> entry : particles.entrySet()) {
				entry.getValue().left(0);
				entry.getValue().right(0);
			}
		}

		@Override
		public boolean shouldRender() {
			return super.shouldRender() && lastConfirmed + 5000 > System.currentTimeMillis();
		}
	}
}
