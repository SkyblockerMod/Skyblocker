package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class BuildersWandPreview {
	private static final int MAX_BLOCKS = 241;
	public static final int PLOT_SIZE = 96;
	public static final int PLOT_OFFSET = 48;
	private static final float[] RED = {1.0f, 0.0f, 0.0f};
	public static final boolean SODIUM_LOADED = FabricLoader.getInstance().isModLoaded("sodium");
	private static final MinecraftClient client = MinecraftClient.getInstance();

	@Init
	public static void init() {
		WorldRenderExtractionCallback.EVENT.register(collector -> {
			if (!SkyblockerConfigManager.get().helpers.enableBuildersWandPreview || !Utils.isOnSkyblock() || client.player == null) return;
			if (!Utils.isInPrivateIsland() && !Utils.isInGarden()) return;
			if (!(client.crosshairTarget instanceof BlockHitResult blockHitResult) || blockHitResult.getType() != HitResult.Type.BLOCK) return;
			ItemStack stack = client.player.getMainHandStack();
			if (!stack.isOf(Items.BLAZE_ROD)) return;
			switch (stack.getSkyblockId()) {
				case "BUILDERS_WAND" -> extractBuildersWandPreview(collector, blockHitResult);
				case "BUILDERS_RULER" -> extractBuildersRulerPreview(collector, blockHitResult);
			}
		});
	}

	private static void extractBuildersWandPreview(PrimitiveCollector collector, BlockHitResult hitResult) {
		if (client.world == null) return;
		BlockPos hitPos = hitResult.getBlockPos();
		BlockState state = client.world.getBlockState(hitPos);
		Direction side = hitResult.getSide();
		for (BlockPos pos : findConnectedFaces(client.world, hitPos, side, state)) {
			extractBlockPreview(collector, pos.offset(side), state);
		}
	}

	private static Set<BlockPos> findConnectedFaces(World world, BlockPos pos, Direction side, BlockState state) {
		// bfs connected block faces
		Queue<BlockPos> q = new ArrayDeque<>();
		Set<BlockPos> visited = new HashSet<>();
		q.add(pos);
		visited.add(pos);

		while (!q.isEmpty()) {
			BlockPos current = q.poll();
			BlockPos.Mutable mutable = current.mutableCopy();
			for (Direction dir : Direction.values()) {
				// Only search in the 4 directions perpendicular to the hit side
				if (dir == side || dir == side.getOpposite()) continue;
				mutable.move(dir);
				// We nest to make sure mutable is moved back to the original position after checking each direction
				if (!visited.contains(mutable) && world.getBlockState(mutable).equals(state)) {
					if (world.getBlockState(mutable.move(side)).isAir()) {
						BlockPos neighbor = mutable.move(side.getOpposite()).toImmutable();
						q.add(neighbor);
						visited.add(neighbor);
					} else {
						mutable.move(side.getOpposite());
					}
				}
				mutable.move(dir.getOpposite());
			}

			if (visited.size() > MAX_BLOCKS) return Set.of();
		}

		return visited;
	}

	private static void extractBuildersRulerPreview(PrimitiveCollector collector, BlockHitResult hitResult) {
		if (client.player == null || client.world == null || !Utils.isInGarden()) return;
		BlockPos startPos = hitResult.getBlockPos();
		boolean isSneaking = client.player.isSneaking();
		Block startBlock = Blocks.AIR;
		// Render the blocks we're about to remove if we're sneaking
		// Render the blocks we're about to place if we're not sneaking
		if (!isSneaking) startPos = startPos.offset(hitResult.getSide());
		// Save the starting block state since only the same blocks can be removed
		else startBlock = client.world.getBlockState(startPos).getBlock();

		BlockPos.Mutable pos = startPos.mutableCopy();
		for (int i = 0; i < MAX_BLOCKS && checkPos(startPos, pos, client.world.getBlockState(pos), isSneaking, startBlock); i++) {
			if (isSneaking) collector.submitFilledBox(pos, RED, 0.5f, true);
			else extractBlockPreview(collector, pos, Blocks.DIRT.getDefaultState());
			pos.move(client.player.getHorizontalFacing());
		}
	}

	private static boolean checkPos(BlockPos plotPos, BlockPos.Mutable pos, BlockState state, boolean isSneaking, Block startBlock) {
		return isInPlot(plotPos, pos) && (isSneaking ? state.isOf(startBlock) : state.isAir());
	}

	private static boolean isInPlot(BlockPos plotPos, BlockPos pos) {
		return Math.floorDiv(plotPos.getX() + PLOT_OFFSET, PLOT_SIZE) == Math.floorDiv(pos.getX() + PLOT_OFFSET, PLOT_SIZE) && Math.floorDiv(plotPos.getZ() + PLOT_OFFSET, PLOT_SIZE) == Math.floorDiv(pos.getZ() + PLOT_OFFSET, PLOT_SIZE);
	}

	private static void extractBlockPreview(PrimitiveCollector collector, BlockPos pos, BlockState state) {
		collector.submitBlockHologram(pos, state);
	}
}
