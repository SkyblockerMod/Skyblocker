package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.device.SimonSays;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

public class GhostBlock {

	private static final ArrayList<BlockPos> ghostPositions = new ArrayList<>();

	@Init
	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(GhostBlock::tick, 1);
		UseBlockCallback.EVENT.register(GhostBlock::onBlockInteract);
		ClientPlayerBlockBreakEvents.AFTER.register(GhostBlock::afterBlockBreak);
	}

	/**
	 * allows user to click a block to recover ghost block state, just like vanilla
	 */
	private static ActionResult onBlockInteract(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
		final BlockPos pos = hitResult.getBlockPos().add(hitResult.getSide().getVector());

		ghostPositions.remove(pos);

		return ActionResult.PASS;
	}

	private static void afterBlockBreak(ClientWorld world, ClientPlayerEntity player, BlockPos pos, BlockState state) {
		if (shouldProcess() && isHeldPickaxe(player)) {
			ghostPositions.add(pos);
		}
	}

	private static void tick() {
		if (!shouldProcess()) {
			ghostPositions.clear();
			return;
		}

		final PlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null) return;

		final ClientWorld world = MinecraftClient.getInstance().world;
		if (world == null) return;

		if (isHeldPickaxe(player)) {
			for (BlockPos position : ghostPositions) {
				world.setBlockState(position, Blocks.AIR.getDefaultState());
			}
		} else {
			ghostPositions.clear();
		}
	}

	private static boolean isHeldPickaxe(PlayerEntity player) {
		final ItemStack heldItem = player.getMainHandStack();

		return heldItem.isIn(ItemTags.PICKAXES);
	}


	private static boolean shouldProcess() {
		return SkyblockerConfigManager.get().dungeons.ghostBlocks &&
				Utils.isInDungeons();
	}
}
