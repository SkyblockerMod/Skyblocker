package de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.puzzle.DungeonPuzzle;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard.Waterboard.*;

public class WaterboardPreviewer extends DungeonPuzzle {
	private static final Logger LOGGER = LoggerFactory.getLogger(WaterboardPreviewer.class);
	public static final WaterboardPreviewer INSTANCE = new WaterboardPreviewer();

	private LeverType prospective;
	private ClientWorld world;
	private Room room;
	private ClientPlayerEntity player;

	private WaterboardPreviewer() {
		super("waterboard", "water-puzzle");
	}

	@Init
	public static void init() {}

	@Override
	public void tick(MinecraftClient client) {}

	@Override
	public void render(WorldRenderContext context) {
		if (!shouldSolve() || MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null || !DungeonManager.isCurrentRoomMatched()) {
			return;
		}

		world = MinecraftClient.getInstance().world;
		room = DungeonManager.getCurrentRoom();
		player = MinecraftClient.getInstance().player;

		try {
			findProspective();
			renderWaterPath(context);
			renderProspectiveChanges(context);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Waterboard] Error while rendering previews", e);
		}
	}

	private void renderWaterPath(WorldRenderContext context) {
		if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.previewWaterPath) {
			return;
		}

		// Calculate and render path of water through the board
		// If there is a prospective lever, instead find the path for if that lever was used
		List<Pair<BlockPos, BlockPos>> waterPath = new ArrayList<>();
		waterPath.add(new Pair<>(WATER_ENTRANCE_POSITION.up(5), WATER_ENTRANCE_POSITION.up(3)));
		findWaterPathVertical(WATER_ENTRANCE_POSITION.up(3), waterPath);

		for (Pair<BlockPos, BlockPos> pair : waterPath) {
			Vec3d head = room.relativeToActual(pair.getLeft()).toCenterPos();
			Vec3d tail = room.relativeToActual(pair.getRight()).toCenterPos();

			List<Vec3d[]> lines = new ArrayList<>();
			if (prospective == null) {
				lines.add(new Vec3d[]{head, tail});
			} else {
				Vec3d forward = tail.subtract(head).normalize();
				double distance = head.distanceTo(tail);
				for (int i = 0; i < distance; i++) {
					lines.add(new Vec3d[]{head, head.add(forward.multiply(0.3))});
					lines.add(new Vec3d[]{head.add(forward.multiply(0.7)), head.add(forward)});
					head = head.add(forward);
				}
			}

			for (Vec3d[] line : lines) {
				RenderHelper.renderLinesFromPoints(context, line,
						ColorUtils.getFloatComponents(LeverType.WATER.color), 1f, 3f, true);
			}
		}
	}

	private void findWaterPathVertical(BlockPos root, List<Pair<BlockPos, BlockPos>> waterPath) {
		if (isWaterPassable(root.down())) {
			BlockPos.Mutable tail = new BlockPos.Mutable().set(root.down());
			while (isWaterPassable(tail.down())) {
				tail.move(Direction.DOWN);
			}
			waterPath.add(new Pair<>(root, new BlockPos(tail)));
			findWaterPathHorizontal(tail, waterPath);
		}
	}

	private void findWaterPathHorizontal(BlockPos root, List<Pair<BlockPos, BlockPos>> waterPath) {
		if (!isWaterPassable(root.down())) {
			BlockPos.Mutable left = new BlockPos.Mutable().set(root);
			int leftSteps = 0;
			while (isWaterPassable(left.east()) && !isWaterPassable(left.down()) && leftSteps < 7) {
				left.move(Direction.EAST);
				leftSteps++;
			}
			BlockPos.Mutable right = new BlockPos.Mutable().set(root);
			int rightSteps = 0;
			while (isWaterPassable(right.west()) && !isWaterPassable(right.down()) && rightSteps < 7) {
				right.move(Direction.WEST);
				rightSteps++;
			}

			// If one side has an air block closer to the source than the other side, the water will only flow in that direction.
			// Skyblock only looks up to 5 blocks away when determining if there is an air block.
			// If no air is found, the water flows in both directions up to a maximum of 7 blocks away.
			if (isWaterPassable(left.down()) && leftSteps <= 5 && (leftSteps < rightSteps || !isWaterPassable(right.down()))) {
				waterPath.add(new Pair<>(root, new BlockPos(left)));
				findWaterPathVertical(left, waterPath);
			} else if (isWaterPassable(right.down()) && rightSteps <= 5 && (rightSteps < leftSteps || !isWaterPassable(left.down()))) {
				waterPath.add(new Pair<>(root, new BlockPos(right)));
				findWaterPathVertical(right, waterPath);
			} else {
				if (leftSteps > 0) {
					waterPath.add(new Pair<>(root, new BlockPos(left)));
					findWaterPathVertical(left, waterPath);
				}
				if (rightSteps > 0) {
					waterPath.add(new Pair<>(root, new BlockPos(right)));
					findWaterPathVertical(right, waterPath);
				}
			}
		}
	}

	private boolean isWaterPassable(BlockPos pos) {
		if (pos.getX() < BOARD_MIN_X || pos.getX() > BOARD_MAX_X ||
				pos.getY() < BOARD_MIN_Y || pos.getY() > BOARD_MAX_Y ||
				pos.getZ() != BOARD_Z) {
			return false;
		}
		BlockState state = world.getBlockState(room.relativeToActual(pos));
		BlockState behindState = world.getBlockState(room.relativeToActual(pos.offset(Direction.SOUTH)));
		boolean open = state.isAir() || state.isOf(Blocks.WATER);
		if (prospective == null) {
			return open;
		} else {
			return open && !behindState.isOf(prospective.block) || state.isOf(prospective.block);
		}
	}

	private void findProspective() {
		if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.previewLeverEffects) {
			return;
		}

		// If the player is looking at a toggleable block in the board, show what would happen if that block was toggled
		Vec3d camera = room.actualToRelative(player.getEyePos());
		Vec3d look = room.actualToRelative(player.getEyePos().add(player.getRotationVector())).subtract(camera);
		double t1 = (BOARD_Z - 1.5 - camera.getZ()) / look.getZ();
		double t2 = (BOARD_Z + 1.5 - camera.getZ()) / look.getZ();
		Vec3d start = camera.add(look.multiply(t1));
		Vec3d end = camera.add(look.multiply(t2));

		Direction behind = switch (room.getDirection()) {
			case NW -> Direction.SOUTH;
			case NE -> Direction.WEST;
			case SW -> Direction.EAST;
			case SE -> Direction.NORTH;
		};

		prospective = BlockView.raycast(room.relativeToActual(start), room.relativeToActual(end), null, (ctx, pos) -> {
			if (room.actualToRelative(pos).getZ() != BOARD_Z) {
				return null;
			}
			LeverType leverType = LeverType.fromBlock(world.getBlockState(pos).getBlock());
			if (leverType == null) {
				BlockPos alternatePos = pos.offset(behind);
				leverType = LeverType.fromBlock(world.getBlockState(alternatePos).getBlock());
			}
			return leverType;
		}, (ctx) -> null);
	}

	private void renderProspectiveChanges(WorldRenderContext context) {
		if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.previewLeverEffects || prospective == null) {
			return;
		}

		// Render filled shapes for blocks that would be extended and outlines for blocks that would be retracted
		for (int x = BOARD_MIN_X; x <= BOARD_MAX_X; x++) {
			for (int y = BOARD_MIN_Y; y <= BOARD_MAX_Y; y++) {
				BlockPos activePos = room.relativeToActual(new BlockPos(x, y, BOARD_Z));
				BlockPos inactivePos = room.relativeToActual(new BlockPos(x, y, BOARD_Z + 1));
				if (world.getBlockState(activePos).isOf(prospective.block)) {
					RenderHelper.renderOutline(context, activePos, ColorUtils.getFloatComponents(prospective.color), 2f, true);
				} else if (world.getBlockState(inactivePos).isOf(prospective.block)) {
					RenderHelper.renderFilled(context, activePos, ColorUtils.getFloatComponents(prospective.color), 0.8f, true);
				}
			}
		}
	}

	@Override
	public void reset() {
		super.reset();
		world = null;
		room = null;
		player = null;
		prospective = null;
	}
}
