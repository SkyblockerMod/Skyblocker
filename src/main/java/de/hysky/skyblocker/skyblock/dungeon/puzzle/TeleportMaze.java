package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TeleportMaze extends DungeonPuzzle {
	private static final float[] LIME = ColorUtils.getFloatComponents(DyeColor.LIME);
	private static final float[] RED = ColorUtils.getFloatComponents(DyeColor.RED);
	private static final Set<BlockPos> ROOM_CENTERS = Set.of(
			new BlockPos(7, 68, 9),
			new BlockPos(23, 68, 9),
			new BlockPos(7, 68, 17),
			new BlockPos(23, 68, 17),
			new BlockPos(7, 68, 25),
			new BlockPos(15, 68, 25),
			new BlockPos(23, 68, 25)
	);
	private static final Set<BlockPos> TELEPORT_PADS = Set.of(
			new BlockPos(15, 69, 12), // Start
			new BlockPos(4, 69, 6),
			new BlockPos(10, 69, 6),
			new BlockPos(20, 69, 6),
			new BlockPos(26, 69, 6),
			new BlockPos(4, 69, 12),
			new BlockPos(10, 69, 12),
			new BlockPos(20, 69, 12),
			new BlockPos(26, 69, 12),
			new BlockPos(4, 69, 14),
			new BlockPos(10, 69, 14),
			new BlockPos(20, 69, 14),
			new BlockPos(26, 69, 14),
			new BlockPos(4, 69, 20),
			new BlockPos(10, 69, 20),
			new BlockPos(20, 69, 20),
			new BlockPos(26, 69, 20),
			new BlockPos(4, 69, 22),
			new BlockPos(10, 69, 22),
			new BlockPos(12, 69, 22),
			new BlockPos(18, 69, 22),
			new BlockPos(20, 69, 22),
			new BlockPos(26, 69, 22),
			new BlockPos(4, 69, 28),
			new BlockPos(10, 69, 28),
			new BlockPos(12, 69, 28),
			new BlockPos(18, 69, 28),
			new BlockPos(20, 69, 28),
			new BlockPos(26, 69, 28),
			new BlockPos(15, 69, 14) // End
	);
	public static final TeleportMaze INSTANCE = new TeleportMaze();
	/**
	 * The actual coordinate of pads that have been detected and the room type they teleport to.
	 */
	private final Map<BlockPos, RoomType> pads = new HashMap<>();
	/**
	 * The actual coordinate of the final pad.
	 */
	@Nullable
	private BlockPos finalPad;

	private TeleportMaze() {
		super("teleport-maze", "teleport-pad-room");
	}

	public void onTeleport(MinecraftClient client, BlockPos from, BlockPos to) {
		if (!shouldSolve() || !DungeonManager.isCurrentRoomMatched() || client.player == null || client.world == null) return;

		BlockPos prevPlayer = DungeonManager.getCurrentRoom().actualToRelative(from.withY(69));
		BlockPos player = DungeonManager.getCurrentRoom().actualToRelative(to.withY(69));
		if (prevPlayer.equals(player) || !TELEPORT_PADS.contains(prevPlayer)) return;

		// Process the teleport from the previous pad to the current pad
		processTeleport(client.world, prevPlayer, player);
		// Find the pad closest to the player, which is the current pad they teleported to
		BlockPos nearestPad = TELEPORT_PADS.stream().min(Comparator.comparingDouble(pad -> pad.getSquaredDistance(player))).orElse(null);
		// Also process the teleport from the current pad to the previous pad
		processTeleport(client.world, nearestPad, prevPlayer);
	}

	private void processTeleport(World world, BlockPos from, BlockPos to) {
		getRoomType(world, to).ifPresent(type -> pads.put(DungeonManager.getCurrentRoom().relativeToActual(from), type));
	}

	private Optional<RoomType> getRoomType(World world, BlockPos pos) {
		// Special processing for the entrance
		if (pos.getX() == 15 && pos.getZ() == 12) return Optional.of(RoomType.ENTRANCE);
		// Check if the position is in a room and return the room type by checking the ore block
		return ROOM_CENTERS.stream().filter(center -> center.getX() - 3 <= pos.getX() && pos.getX() <= center.getX() + 3 &&
						center.getZ() - 3 <= pos.getZ() && pos.getZ() <= center.getZ() + 3).findAny()
				.map(DungeonManager.getCurrentRoom()::relativeToActual).map(world::getBlockState).map(BlockState::getBlock).flatMap(RoomType::fromBlock);
	}

	@Override
	public void tick(MinecraftClient client) {
		if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveTeleportMaze || !shouldSolve() || finalPad != null) return;
		// Mark the last unused pad that's not the start or the end as the final pad
		List<BlockPos> finalPads = TELEPORT_PADS.stream().filter(pad -> pad.getX() != 15) // Filter out the start and end pads
				.map(DungeonManager.getCurrentRoom()::relativeToActual).filter(pad -> !pads.containsKey(pad)).toList(); // Filter out used pads
		if (finalPads.size() == 1) finalPad = finalPads.getFirst(); // If there's only one left, mark it as the final pad
	}

	@Override
	public void render(WorldRenderContext context) {
		if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveTeleportMaze || !shouldSolve()) return;
		boolean debug = Debug.debugEnabled();
		for (Map.Entry<BlockPos, RoomType> entry : pads.entrySet()) {
			// Only use the real room color in debug mode to present the solution to users in a simpler manner for now.
			// Should be revisited eventually.
			float[] color = debug ? entry.getValue().colorComponents : RED;
			RenderHelper.renderFilled(context, entry.getKey(), color, 0.5f, debug);
		}
		if (finalPad != null) {
			RenderHelper.renderFilled(context, finalPad, LIME, 1f, true);
			RenderHelper.renderLineFromCursor(context, Vec3d.ofCenter(finalPad), LIME, 1f, 2f);
		}
	}

	@Override
	public void reset() {
		super.reset();
		pads.clear();
		finalPad = null;
	}

	private enum RoomType {
		ENTRANCE(Blocks.BARRIER, DyeColor.GRAY),
		COAL(Blocks.COAL_ORE, DyeColor.BLACK),
		IRON(Blocks.IRON_ORE, DyeColor.LIGHT_GRAY),
		REDSTONE(Blocks.REDSTONE_ORE, DyeColor.RED),
		LAPIS(Blocks.LAPIS_ORE, DyeColor.BLUE),
		GOLD(Blocks.GOLD_ORE, DyeColor.YELLOW),
		DIAMOND(Blocks.DIAMOND_ORE, DyeColor.CYAN),
		EMERALD(Blocks.EMERALD_ORE, DyeColor.LIME);

		private final Block block;
		private final float[] colorComponents;

		RoomType(Block block, DyeColor dyeColor) {
			this.block = block;
			this.colorComponents = ColorUtils.getFloatComponents(dyeColor);
		}

		private static Optional<RoomType> fromBlock(Block block) {
			return Arrays.stream(values()).filter(type -> type.block == block).findFirst();
		}
	}
}
