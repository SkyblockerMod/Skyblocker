package de.hysky.skyblocker.skyblock.galatea;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.ParticleEvents;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

public class ForestNodes {
	private static final Minecraft client = Minecraft.getInstance();
	private static final Map<BlockPos, ForestNode> forestNodes = new HashMap<>();

	@Init
	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(ForestNodes::update, 20);
		WorldRenderExtractionCallback.EVENT.register(ForestNodes::extractRendering);
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			if (!shouldProcess()) {
				return InteractionResult.PASS;
			}
			forestNodes.remove(pos);
			return InteractionResult.PASS;
		});
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!shouldProcess()) {
				return InteractionResult.PASS;
			}
			BlockPos pos = hitResult.getBlockPos();
			forestNodes.remove(pos);
			return InteractionResult.PASS;
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
		ParticleEvents.FROM_SERVER.register(ForestNodes::onParticle);
	}

	private static void onParticle(ClientboundLevelParticlesPacket packet) {
		if (!shouldProcess()) {
			return;
		}

		ParticleType<?> particleType = packet.getParticle().getType();
		if (!ParticleTypes.HAPPY_VILLAGER.getType().equals(particleType)) {
			return;
		}

		double x = packet.getX();
		double y = packet.getY() - 1;
		double z = packet.getZ();
		BlockPos pos = BlockPos.containing(x, y, z);

		// Check for three ItemDisplayEntity with minecraft:string in the same block
		if (client.level != null) {
			int stringItemCount = countStringItemDisplays(pos);
			if (stringItemCount == 3) {
				forestNodes.computeIfAbsent(pos, ForestNode::new);
			}
		}
	}

	private static int countStringItemDisplays(BlockPos pos) {
		ClientLevel world = client.level;
		if (world == null) {
			return 0;
		}

		// Get all ItemDisplayEntity within the same block
		List<Display.ItemDisplay> entities = world.getEntitiesOfClass(
				Display.ItemDisplay.class,
				AABB.ofSize(pos.getCenter(), 1.0, 1.0, 1.0),
				entity -> true
		);

		// Count those with minecraft:string
		return (int) entities.stream()
				.filter(entity -> {
					ItemStack stack = entity.getItemStack();
					return !stack.isEmpty() && stack.getItem().equals(Items.STRING);
				})
				.count();
	}

	private static void update() {
		if (!shouldProcess()) {
			return;
		}
		Iterator<Map.Entry<BlockPos, ForestNode>> iterator = forestNodes.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<BlockPos, ForestNode> entry = iterator.next();
			ForestNode forestNode = entry.getValue();
			forestNode.updateWaypoint();
			if (!forestNode.shouldRender()) {
				iterator.remove();
			}
		}
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (!shouldProcess()) {
			return;
		}
		for (ForestNode forestNode : forestNodes.values()) {
			if (forestNode.shouldRender()) {
				forestNode.extractRendering(collector);
			}
		}
	}

	private static boolean shouldProcess() {
		return SkyblockerConfigManager.get().foraging.galatea.enableForestNodeHelper && Utils.isInGalatea();
	}

	private static void reset() {
		forestNodes.clear();
	}

	public static class ForestNode extends Waypoint {
		private long lastConfirmed;

		private ForestNode(BlockPos pos) {
			super(pos,
					() -> Type.HIGHLIGHT,
					ColorUtils.getFloatComponents(DyeColor.ORANGE),
					DEFAULT_HIGHLIGHT_ALPHA,
					DEFAULT_LINE_WIDTH,
					false
			);
			this.lastConfirmed = System.currentTimeMillis();
		}

		private void updateWaypoint() {
			long currentTimeMillis = System.currentTimeMillis();
			if (lastConfirmed + 2000 > currentTimeMillis || client.level == null) {
				return;
			}
			int stringItemCount = countStringItemDisplays(pos);
			if (stringItemCount == 3) {
				lastConfirmed = System.currentTimeMillis();
			}
		}

		@Override
		public boolean shouldRender() {
			return super.shouldRender() && lastConfirmed + 5000 > System.currentTimeMillis();
		}
	}
}
