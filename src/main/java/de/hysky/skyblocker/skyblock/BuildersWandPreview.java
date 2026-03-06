package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.BlockPosSet;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import java.util.ArrayDeque;
import java.util.Queue;

public class BuildersWandPreview {
	private static final int MAX_BLOCKS = 241;
	public static final int PLOT_SIZE = 96;
	public static final int PLOT_OFFSET = 48;
	private static final float[] RED = {1.0f, 0.0f, 0.0f};
	public static final boolean SODIUM_LOADED = FabricLoader.getInstance().isModLoaded("sodium");
	private static final Minecraft client = Minecraft.getInstance();

	@Init
	public static void init() {
		WorldRenderExtractionCallback.EVENT.register(collector -> {
			if (!SkyblockerConfigManager.get().helpers.enableBuildersWandPreview || !Utils.isOnSkyblock() || client.player == null) return;
			if (!Utils.isInPrivateIsland() && !Utils.isInGarden()) return;
			if (!(client.hitResult instanceof BlockHitResult blockHitResult) || blockHitResult.getType() != HitResult.Type.BLOCK) return;
			ItemStack stack = client.player.getMainHandItem();
			if (!stack.is(Items.BLAZE_ROD)) return;
			switch (stack.getSkyblockId()) {
				case "BUILDERS_WAND" -> extractBuildersWandPreview(collector, blockHitResult);
				case "BUILDERS_RULER" -> extractBuildersRulerPreview(collector, blockHitResult);
			}
		});
	}

	private static void extractBuildersWandPreview(PrimitiveCollector collector, BlockHitResult hitResult) {
		if (client.level == null) return;
		BlockPos hitPos = hitResult.getBlockPos();
		Direction side = hitResult.getDirection();
		if (!client.level.getBlockState(hitPos.relative(side)).isAir()) return;
		BlockState state = client.level.getBlockState(hitPos);
		for (BlockPos pos : findConnectedFaces(client.level, hitPos, side, state).destroyAndIterateMut()) {
			extractBlockPreview(collector, pos.relative(side), state);
		}
	}

	private static BlockPosSet findConnectedFaces(Level world, BlockPos pos, Direction side, BlockState state) {
		// bfs connected block faces
		Queue<BlockPos> q = new ArrayDeque<>();
		BlockPosSet visited = new BlockPosSet();
		q.add(pos);
		visited.add(pos);

		while (!q.isEmpty()) {
			BlockPos current = q.poll();
			BlockPos.MutableBlockPos mutable = current.mutable();
			for (Direction dir : Direction.values()) {
				// Only search in the 4 directions perpendicular to the hit side
				if (dir == side || dir == side.getOpposite()) continue;
				mutable.move(dir);
				// We nest to make sure mutable is moved back to the original position after checking each direction
				if (!visited.contains(mutable) && world.getBlockState(mutable).equals(state)) {
					if (world.getBlockState(mutable.move(side)).isAir()) {
						BlockPos neighbor = mutable.move(side.getOpposite()).immutable();
						q.add(neighbor);
						visited.add(neighbor);
					} else {
						mutable.move(side.getOpposite());
					}
				}
				mutable.move(dir.getOpposite());
			}

			if (visited.size() > MAX_BLOCKS) return new BlockPosSet();
		}

		return visited;
	}

	private static void extractBuildersRulerPreview(PrimitiveCollector collector, BlockHitResult hitResult) {
		if (client.player == null || client.level == null || !Utils.isInGarden()) return;
		BlockPos startPos = hitResult.getBlockPos();
		boolean isSneaking = client.player.isShiftKeyDown();
		Block startBlock = Blocks.AIR;
		// Render the blocks we're about to remove if we're sneaking
		// Render the blocks we're about to place if we're not sneaking
		if (!isSneaking) startPos = startPos.relative(hitResult.getDirection());
		// Save the starting block state since only the same blocks can be removed
		else startBlock = client.level.getBlockState(startPos).getBlock();

		BlockPos.MutableBlockPos pos = startPos.mutable();
		for (int i = 0; i < MAX_BLOCKS && checkPos(startPos, pos, client.level.getBlockState(pos), isSneaking, startBlock); i++) {
			if (isSneaking) collector.submitFilledBox(pos, RED, 0.5f, true);
			else extractBlockPreview(collector, pos, Blocks.DIRT.defaultBlockState());
			pos.move(client.player.getDirection());
		}
	}

	private static boolean checkPos(BlockPos plotPos, BlockPos.MutableBlockPos pos, BlockState state, boolean isSneaking, Block startBlock) {
		return isInPlot(plotPos, pos) && (isSneaking ? state.is(startBlock) : state.isAir());
	}

	private static boolean isInPlot(BlockPos plotPos, BlockPos pos) {
		return Math.floorDiv(plotPos.getX() + PLOT_OFFSET, PLOT_SIZE) == Math.floorDiv(pos.getX() + PLOT_OFFSET, PLOT_SIZE) && Math.floorDiv(plotPos.getZ() + PLOT_OFFSET, PLOT_SIZE) == Math.floorDiv(pos.getZ() + PLOT_OFFSET, PLOT_SIZE);
	}

	private static void extractBlockPreview(PrimitiveCollector collector, BlockPos pos, BlockState state) {
		collector.submitBlockHologram(pos, state);
	}
}
