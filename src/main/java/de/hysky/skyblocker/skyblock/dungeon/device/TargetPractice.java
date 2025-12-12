package de.hysky.skyblocker.skyblock.dungeon.device;

import java.util.ArrayList;
import java.util.List;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.WorldEvents;
import de.hysky.skyblocker.skyblock.dungeon.DungeonBoss;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class TargetPractice {
	private static final BlockPos PRESSURE_PLATE = new BlockPos(63, 127, 35);
	private static final int ACTIVATION_THRESHOLD = 1;
	private static final int UNACTIVATED = 0;
	// In the order you see the grid in the world by row.
	private static final List<BlockPos> POSSIBLE_TARGETS = List.of(
			new BlockPos(68, 130, 50), new BlockPos(66, 130, 50), new BlockPos(64, 130, 50),
			new BlockPos(68, 128, 50), new BlockPos(66, 128, 50), new BlockPos(64, 128, 50),
			new BlockPos(68, 126, 50), new BlockPos(66, 126, 50), new BlockPos(64, 126, 50)
			);
	private static final List<BlockPos> HIT_TARGETS = new ArrayList<>();
	private static final float[] RED = ColorUtils.getFloatComponents(DyeColor.RED);

	@Init
	public static void init() {
		WorldEvents.BLOCK_STATE_UPDATE.register(TargetPractice::onBlockStateUpdate);
		WorldRenderExtractionCallback.EVENT.register(TargetPractice::extractRendering);
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
	}

	private static void onBlockStateUpdate(BlockPos pos, BlockState oldState, BlockState newState) {
		if (!shouldProcess()) return;

		// When the pressure plate deactivates, or the it turns on we want to reset the solver's state.
		if (pos.equals(PRESSURE_PLATE) && newState.getBlock().equals(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE)) {

			// Reset when the new power does not meet the activation threshold of 1 entity on the plate.
			if (newState.getValue(BlockStateProperties.POWER) < ACTIVATION_THRESHOLD) {
				reset();

				return;
			}

			// Reset when the power level was previously zero.
			//
			// This is done despite the above to ensure the solver's state is reset when someone stops doing the device
			// and then it is resumed afterwards.
			if (oldState.getBlock().equals(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE) && oldState.getValue(BlockStateProperties.POWER) == UNACTIVATED) {
				reset();

				return;
			}
		}

		// Handle logic for if a target block changed.
		// The blocks on the grid are initially Blue Terracotta, when the blocks turns into Emerald Block it is the one that the
		// player must shoot, so when that block turns back into Blue Terracotta it has either been successfully shot or the device reset.
		if (POSSIBLE_TARGETS.contains(pos)) {
			if (oldState.getBlock().equals(Blocks.EMERALD_BLOCK) && newState.getBlock().equals(Blocks.BLUE_TERRACOTTA)) {
				// Convert position to immutable since it might be mutable and we can't have it changing
				HIT_TARGETS.add(pos.immutable());
			}
		}
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (!shouldProcess()) return;

		for (BlockPos pos : HIT_TARGETS) {
			collector.submitFilledBox(pos, RED, 0.5f, false);
		}
	}

	private static boolean shouldProcess() {
		return SkyblockerConfigManager.get().dungeons.devices.solveTargetPractice && Utils.isInDungeons() && DungeonManager.isInBoss()
				&& DungeonManager.getBoss() == DungeonBoss.MAXOR;
	}

	private static void reset() {
		HIT_TARGETS.clear();
	}
}
