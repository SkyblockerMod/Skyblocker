package de.hysky.skyblocker.skyblock.foraging.torrhus;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.LevelRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;

public class CritterCapsuleHelper {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	@Nullable
	public static Entity highlighted = null;

	@Init
	public static void init() {
		LevelRenderExtractionCallback.EVENT.register(CritterCapsuleHelper::extractRendering);
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (CLIENT.player == null || CLIENT.level == null || !SkyblockerConfigManager.get().hunting.safari.CritterCapsuleHelper || !Utils.isInSafari()) return;

		//check holding
		ItemStack heldItem = CLIENT.player.getMainHandItem();
		String itemId = heldItem.getSkyblockId();
		if (!itemId.equals("CRITTER_CAPSULE") && !itemId.equals("MASTERFUL_CRITTER_CAPSULE")) return;

		//find capsules path
		float tickDelta = RenderHelper.getTickCounter().getGameTimeDeltaPartialTick(false);
		Vec3 start = CLIENT.player.getEyePosition(tickDelta);
		Vec3 look = CLIENT.player.getViewVector(tickDelta);

		HitResult hitResult = throwLine(collector, start, look);
		if (hitResult instanceof BlockHitResult block) {
			collector.submitFilledBox(block.getBlockPos(), SkyblockerConfigManager.get().hunting.safari.CritterCapsuleHelperColor.getComponents(new float[4]), 0.5f, false);
		}
		if (hitResult instanceof EntityHitResult entity) {
			highlighted = entity.getEntity();
		} else {
			highlighted = null;
		}
	}

	private static @Nullable HitResult throwLine(PrimitiveCollector collector, Vec3 start, Vec3 look) {
		if (CLIENT.level == null) return null;
		int max_distance = 50;
		Vec3 lastPos = start;
		for (int i = 0; i < max_distance; i++) {
			Vec3 pos = start.add(look.scale(i));
			double AccumulatedGravity;

			AccumulatedGravity = (-0.85614) * Math.exp(-0.29696 * 0.093157 * i) * Math.sin(0.093157 * Math.sqrt(1 - 0.29696 * 0.29696) * i + 3.6507) - 36.584 + 35.962 * Math.sin((-0.027059) * i + 14.087);
			pos = pos.add(0, AccumulatedGravity, 0);

			//draw line
			if (i > 1) {
				Vec3 offset = look.cross(Vec3.Y_AXIS).normalize().scale(0.05);
				collector.submitLinesFromPoints(new Vec3[]{lastPos.add(offset), pos.add(offset)}, SkyblockerConfigManager.get().hunting.safari.CritterCapsuleHelperColor.getComponents(new float[4]), 0.5f, 1 + 4 * (max_distance - i) / ((float) max_distance), false);
			}

			//check for block hit
			BlockPos block = BlockPos.containing(pos);
			BlockState state = CLIENT.level.getBlockState(block);
			if (!state.isAir() && !state.is(Blocks.WATER)) {
				return new BlockHitResult(pos, Direction.DOWN, block, true);
			}

			//check for mob hits
			AABB detectionBox = new AABB(lastPos, pos).inflate(0.05);
			for (Entity entity : CLIENT.level.getEntities(null, detectionBox)) {
				if (entity instanceof Player) {
					continue;
				}
				return new EntityHitResult(entity, pos);
			}
			lastPos = pos;
		}
		return null;

	}
}
