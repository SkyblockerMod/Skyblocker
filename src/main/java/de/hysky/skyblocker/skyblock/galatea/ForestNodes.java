package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForestNodes {
	private static final Logger LOGGER = LoggerFactory.getLogger(ForestNodes.class);
	private static final MinecraftClient client = MinecraftClient.getInstance();
	private static final Map<BlockPos, ForestNode> forestNodes = new HashMap<>();

	@Init
	public static void init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(ForestNodes::render);
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			forestNodes.remove(pos);
			return ActionResult.PASS;
		});
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			BlockPos pos = hitResult.getBlockPos();
			forestNodes.remove(pos);
			return ActionResult.PASS;
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
	}

	public static void onParticle(ParticleS2CPacket packet) {
		if (!shouldProcess()) {
			return;
		}

		ParticleType<?> particleType = packet.getParameters().getType();
		if (!ParticleTypes.HAPPY_VILLAGER.getType().equals(particleType)) {
			return;
		}

		double x = packet.getX();
		double y = packet.getY() - 1;
		double z = packet.getZ();
		BlockPos pos = BlockPos.ofFloored(x, y, z);

		// Check for three ItemDisplayEntity with minecraft:string in the same block
		if (client.world != null) {
			int stringItemCount = countStringItemDisplays(pos);
			if (stringItemCount == 3) {
				forestNodes.computeIfAbsent(pos, ForestNode::new);
			}
		}
	}

	private static int countStringItemDisplays(BlockPos pos) {
		ClientWorld world = client.world;
		if (world == null) {
			return 0;
		}

		// Get all ItemDisplayEntity within the same block
		List<DisplayEntity.ItemDisplayEntity> entities = world.getEntitiesByClass(
				DisplayEntity.ItemDisplayEntity.class,
				new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1),
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

	private static void render(WorldRenderContext context) {
		if (!shouldProcess()) {
			return;
		}
		for (ForestNode forestNode : forestNodes.values()) {
			if (forestNode.shouldRender()) {
				forestNode.render(context);
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

		private ForestNode(BlockPos pos) {
			super(pos, () -> SkyblockerConfigManager.get().uiAndVisuals.waypoints.waypointType, ColorUtils.getFloatComponents(DyeColor.LIME));
		}
	}
}
