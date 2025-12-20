package de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.puzzle.DungeonPuzzle;
import de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard.Waterboard.LeverType;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard.Waterboard.BOARD_MAX_X;
import static de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard.Waterboard.BOARD_MAX_Y;
import static de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard.Waterboard.BOARD_MIN_X;
import static de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard.Waterboard.BOARD_MIN_Y;
import static de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard.Waterboard.BOARD_Z;
import static de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard.Waterboard.WATER_ENTRANCE_POSITION;

public class WaterboardPreviewer extends DungeonPuzzle {
	private static final Logger LOGGER = LoggerFactory.getLogger(WaterboardPreviewer.class);
	public static final WaterboardPreviewer INSTANCE = new WaterboardPreviewer();

	private LeverType prospective;
	private ClientLevel world;
	private Room room;
	private LocalPlayer player;

	private WaterboardPreviewer() {
		super("waterboard", "water-puzzle");
	}

	@Init
	public static void init() {}

	@Override
	public void tick(Minecraft client) {}

	@Override
	public void extractRendering(PrimitiveCollector collector) {
		if (!shouldSolve() || Minecraft.getInstance().level == null || Minecraft.getInstance().player == null || !DungeonManager.isCurrentRoomMatched()) {
			return;
		}

		world = Minecraft.getInstance().level;
		room = DungeonManager.getCurrentRoom();
		player = Minecraft.getInstance().player;

		try {
			findProspective();
			extractWaterPath(collector);
			extractProspectiveChanges(collector);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Waterboard] Error while rendering previews", e);
		}
	}

	private void extractWaterPath(PrimitiveCollector collector) {
		if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.previewWaterPath) {
			return;
		}

		// Calculate and render path of water through the board
		// If there is a prospective lever, instead find the path for if that lever was used
		List<Tuple<BlockPos, BlockPos>> waterPath = new ArrayList<>();
		waterPath.add(new Tuple<>(WATER_ENTRANCE_POSITION.above(5), WATER_ENTRANCE_POSITION.above(3)));
		findWaterPathVertical(WATER_ENTRANCE_POSITION.above(3), waterPath);

		for (Tuple<BlockPos, BlockPos> pair : waterPath) {
			Vec3 head = room.relativeToActual(pair.getA()).getCenter();
			Vec3 tail = room.relativeToActual(pair.getB()).getCenter();

			List<Vec3[]> lines = new ArrayList<>();
			if (prospective == null) {
				lines.add(new Vec3[]{head, tail});
			} else {
				Vec3 forward = tail.subtract(head).normalize();
				double distance = head.distanceTo(tail);
				for (int i = 0; i < distance; i++) {
					lines.add(new Vec3[]{head, head.add(forward.scale(0.3))});
					lines.add(new Vec3[]{head.add(forward.scale(0.7)), head.add(forward)});
					head = head.add(forward);
				}
			}

			for (Vec3[] line : lines) {
				collector.submitLinesFromPoints(line,
						ColorUtils.getFloatComponents(LeverType.WATER.color), 1f, 3f, true);
			}
		}
	}

	private void findWaterPathVertical(BlockPos root, List<Tuple<BlockPos, BlockPos>> waterPath) {
		if (isWaterPassable(root.below())) {
			BlockPos.MutableBlockPos tail = new BlockPos.MutableBlockPos().set(root.below());
			while (isWaterPassable(tail.below())) {
				tail.move(Direction.DOWN);
			}
			waterPath.add(new Tuple<>(root, new BlockPos(tail)));
			findWaterPathHorizontal(tail, waterPath);
		}
	}

	private void findWaterPathHorizontal(BlockPos root, List<Tuple<BlockPos, BlockPos>> waterPath) {
		if (!isWaterPassable(root.below())) {
			BlockPos.MutableBlockPos left = new BlockPos.MutableBlockPos().set(root);
			int leftSteps = 0;
			while (isWaterPassable(left.east()) && !isWaterPassable(left.below()) && leftSteps < 7) {
				left.move(Direction.EAST);
				leftSteps++;
			}
			BlockPos.MutableBlockPos right = new BlockPos.MutableBlockPos().set(root);
			int rightSteps = 0;
			while (isWaterPassable(right.west()) && !isWaterPassable(right.below()) && rightSteps < 7) {
				right.move(Direction.WEST);
				rightSteps++;
			}

			// If one side has an air block closer to the source than the other side, the water will only flow in that direction.
			// Skyblock only looks up to 5 blocks away when determining if there is an air block.
			// If no air is found, the water flows in both directions up to a maximum of 7 blocks away.
			if (isWaterPassable(left.below()) && leftSteps <= 5 && (leftSteps < rightSteps || !isWaterPassable(right.below()))) {
				waterPath.add(new Tuple<>(root, new BlockPos(left)));
				findWaterPathVertical(left, waterPath);
			} else if (isWaterPassable(right.below()) && rightSteps <= 5 && (rightSteps < leftSteps || !isWaterPassable(left.below()))) {
				waterPath.add(new Tuple<>(root, new BlockPos(right)));
				findWaterPathVertical(right, waterPath);
			} else {
				if (leftSteps > 0) {
					waterPath.add(new Tuple<>(root, new BlockPos(left)));
					findWaterPathVertical(left, waterPath);
				}
				if (rightSteps > 0) {
					waterPath.add(new Tuple<>(root, new BlockPos(right)));
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
		BlockState behindState = world.getBlockState(room.relativeToActual(pos.relative(Direction.SOUTH)));
		boolean open = state.isAir() || state.is(Blocks.WATER);
		if (prospective == null) {
			return open;
		} else {
			return open && !behindState.is(prospective.block) || state.is(prospective.block);
		}
	}

	private void findProspective() {
		if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.previewLeverEffects) {
			return;
		}

		// If the player is looking at a toggleable block in the board, show what would happen if that block was toggled
		Vec3 camera = room.actualToRelative(player.getEyePosition());
		Vec3 look = room.actualToRelative(player.getEyePosition().add(player.getLookAngle())).subtract(camera);
		double t1 = (BOARD_Z - 1.5 - camera.z()) / look.z();
		double t2 = (BOARD_Z + 1.5 - camera.z()) / look.z();
		Vec3 start = camera.add(look.scale(t1));
		Vec3 end = camera.add(look.scale(t2));

		Direction behind = switch (room.getDirection()) {
			case NW -> Direction.SOUTH;
			case NE -> Direction.WEST;
			case SW -> Direction.EAST;
			case SE -> Direction.NORTH;
		};

		prospective = BlockGetter.traverseBlocks(room.relativeToActual(start), room.relativeToActual(end), null, (ctx, pos) -> {
			if (room.actualToRelative(pos).getZ() != BOARD_Z) {
				return null;
			}
			LeverType leverType = LeverType.fromBlock(world.getBlockState(pos).getBlock());
			if (leverType == null) {
				BlockPos alternatePos = pos.relative(behind);
				leverType = LeverType.fromBlock(world.getBlockState(alternatePos).getBlock());
			}
			return leverType;
		}, (ctx) -> null);
	}

	private void extractProspectiveChanges(PrimitiveCollector collector) {
		if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.previewLeverEffects || prospective == null) {
			return;
		}

		// Render filled shapes for blocks that would be extended and outlines for blocks that would be retracted
		for (int x = BOARD_MIN_X; x <= BOARD_MAX_X; x++) {
			for (int y = BOARD_MIN_Y; y <= BOARD_MAX_Y; y++) {
				BlockPos activePos = room.relativeToActual(new BlockPos(x, y, BOARD_Z));
				BlockPos inactivePos = room.relativeToActual(new BlockPos(x, y, BOARD_Z + 1));
				if (world.getBlockState(activePos).is(prospective.block)) {
					collector.submitOutlinedBox(activePos, ColorUtils.getFloatComponents(prospective.color), 2f, true);
				} else if (world.getBlockState(inactivePos).is(prospective.block)) {
					collector.submitFilledBox(activePos, ColorUtils.getFloatComponents(prospective.color), 0.8f, true);
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
