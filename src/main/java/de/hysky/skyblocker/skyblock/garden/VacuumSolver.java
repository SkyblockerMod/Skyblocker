package de.hysky.skyblocker.skyblock.garden;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class VacuumSolver {
	private static final long PARTICLES_MAX_DELAY = 500;
	private static final double PARTICLES_MAX_DISTANCE = 4.0;
	private static final List<Vec3d> particleTrail = new LinkedList<>();
	private static BlockPos fixedDestination = null;
	private static long lastParticleUpdate = System.currentTimeMillis();
	private static final Map<ArmorStandEntity, BlockPos> linkedMarkers = new HashMap<>();

	@Init
	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
		WorldRenderEvents.AFTER_TRANSLUCENT.register(VacuumSolver::renderTrail);
	}

	private static void reset() {
		particleTrail.clear();
		lastParticleUpdate = System.currentTimeMillis();
		linkedMarkers.clear();
	}

	public static void onParticle(ParticleS2CPacket packet) {
		if (!Utils.isInGarden() || !SkyblockerConfigManager.get().farming.garden.vacuumSolver || !ParticleTypes.ANGRY_VILLAGER.equals(packet.getParameters().getType())) {
			return;
		}

		if (!isHoldingVacuum()) {
			return;
		}

		Vec3d particlePos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
		long currentTime = System.currentTimeMillis();

		if (currentTime - lastParticleUpdate > PARTICLES_MAX_DELAY) {
			particleTrail.clear();
		}

		if (!particleTrail.isEmpty() && !(particleTrail.getLast().distanceTo(particlePos) <= PARTICLES_MAX_DISTANCE)) {
			particleTrail.clear();
		}

		particleTrail.add(particlePos);
		fixedDestination = BlockPos.ofFloored(particlePos);

		lastParticleUpdate = currentTime;
		linkMarkerWithDestination();
	}

	private static boolean isHoldingVacuum() {
		MinecraftClient client = MinecraftClient.getInstance();

		if (client.player == null || client.player.getMainHandStack().isEmpty()) {
			return false;
		}

		List<Text> tooltip = client.player.getMainHandStack().getTooltip(Item.TooltipContext.DEFAULT, client.player, TooltipType.BASIC);

		return tooltip.stream().anyMatch(text -> text.getString().contains("Pest Tracker"));
	}

	// Highlight flickers when no pest is nearby? it should render regardless
	private static void linkMarkerWithDestination() {
		if (fixedDestination == null || MinecraftClient.getInstance().world == null) return;

		for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {
			if (entity instanceof ArmorStandEntity armorStand
					&& armorStand.isMarker()
					&& armorStand.hasCustomName()
					&& armorStand.getCustomName() != null
					&& armorStand.getCustomName().getString().startsWith("ൠ")) {
				linkedMarkers.put(armorStand, fixedDestination);
				break;
			}
		}
	}

	private static void renderTrail(WorldRenderContext context) {
		if (fixedDestination == null) {
			return;
		}

		float[] color = {1f, 0f, 0f};

		RenderHelper.renderFilled(context, fixedDestination, color, 1, false);

		linkedMarkers.entrySet().removeIf(entry -> entry.getKey().isRemoved());

		for (BlockPos pos : linkedMarkers.values()) {
			RenderHelper.renderFilled(context, pos, color, 1, false);
		}

		if (linkedMarkers.isEmpty() && fixedDestination != null) {
			RenderHelper.renderFilled(context, fixedDestination, color, 1, false);
		}
	}
}
