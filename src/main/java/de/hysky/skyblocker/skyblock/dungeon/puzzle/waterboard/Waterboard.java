package de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.puzzle.DungeonPuzzle;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeverBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Waterboard extends DungeonPuzzle {
    private static final Logger LOGGER = LoggerFactory.getLogger(Waterboard.class);
    public static final Waterboard INSTANCE = new Waterboard();
	private static JsonObject SOLUTIONS;
	private static final int BOARD_MIN_X = 6;
	private static final int BOARD_MAX_X = 24;
	private static final int BOARD_MIN_Y = 58;
	private static final int BOARD_MAX_Y = 81;
	private static final int BOARD_Z = 26;
	private static final BlockPos FIRST_SWITCH_POSITION = new BlockPos(15, 78, 26);

	private int variant;
	private String doors;
	private Map<LeverType, List<Double>> solution;
	private boolean failed;
	private long waterStartMillis;
	private CompletableFuture<Void> solve;
	private ClientWorld world;
	private Room room;
	private ClientPlayerEntity player;

    private Waterboard() {
        super("waterboard", "water-puzzle");
    }

    @Init
    public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(Waterboard::loadSolutions);
        UseBlockCallback.EVENT.register(INSTANCE::onUseBlock);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("dungeons").then(literal("puzzle").then(literal(INSTANCE.puzzleName)
				.then(literal("reset").executes(context -> {
					INSTANCE.softReset();
					return Command.SINGLE_SUCCESS;
				}))
				.then(literal("setDoors").then(argument("combination", StringArgumentType.string()).executes(context -> {
					String doorCombination = StringArgumentType.getString(context, "combination");
					if (SOLUTIONS.get("1").getAsJsonObject().keySet().contains(doorCombination)) {
						INSTANCE.softReset();
						INSTANCE.doors = doorCombination;
					} else {
						context.getSource().sendError(Constants.PREFIX.get().append("Door combination must be three increasing digits between 0 and 4."));
					}
					return Command.SINGLE_SUCCESS;
				})))
		)))));
    }

	private static void loadSolutions(MinecraftClient client) {
		Identifier solutionsFile = Identifier.of(SkyblockerMod.NAMESPACE, "dungeons/watertimes.json");
		try (BufferedReader reader = client.getResourceManager().openAsReader(solutionsFile)) {
			SOLUTIONS = JsonParser.parseReader(reader).getAsJsonObject();
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Waterboard] Failed to load solutions json", e);
		}
	}

    @Override
    public void tick(MinecraftClient client) {
        if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveWaterboard ||
				!shouldSolve() ||
				client.world == null ||
				!DungeonManager.isCurrentRoomMatched() ||
				solution != null ||
				failed ||
				solve != null && !solve.isDone()) {
            return;
        }

		world = client.world;
		room = DungeonManager.getCurrentRoom();
		player = client.player;
		if (world == null || player == null) {
			LOGGER.error("[Skyblocker Waterboard] world or player was null");
			return;
		}

		solve = CompletableFuture.runAsync(this::solvePuzzle).exceptionally(e -> {
			LOGGER.error("[Skyblocker Waterboard] Encountered an unknown exception while solving waterboard.", e);
			failed = true;
			return null;
		});
    }

	private void solvePuzzle() {
		findDoors();
		if (solution != null) {
			return;
		}

		findVariant();
		checkWater();

		if (!failed) {
			loadSolution();
		}
	}

	private void findVariant() {
		// The waterboard only has four possible layouts, each with a unique pair of
		// toggleable blocks at the entrance. One layout has them a block lower than the others.

		Set<LeverType> firstSwitches = new HashSet<>();
		Box firstSwitchBlocks = Box.enclosing(room.relativeToActual(FIRST_SWITCH_POSITION.add(-1, -1, 0)),
				room.relativeToActual(FIRST_SWITCH_POSITION.add(1, 0, 1)));
		for (BlockState state : world.getStatesInBox(firstSwitchBlocks).toList()) {
			LeverType leverType = LeverType.fromBlock(state.getBlock());
			if (leverType != null) {
				firstSwitches.add(leverType);
			}
		}

		if (firstSwitches.contains(LeverType.GOLD) && firstSwitches.contains(LeverType.TERRACOTTA)) {
			variant = 1;
		} else if (firstSwitches.contains(LeverType.EMERALD) && firstSwitches.contains(LeverType.QUARTZ)) {
			variant = 2;
		} else if (firstSwitches.contains(LeverType.QUARTZ) && firstSwitches.contains(LeverType.DIAMOND)) {
			variant = 3;
		} else if (firstSwitches.contains(LeverType.GOLD) && firstSwitches.contains(LeverType.QUARTZ)) {
			variant = 4;
		} else {
			LOGGER.error("[Skyblocker Waterboard] Unknown waterboard layout.");
			failed = true;
		}
	}

	private void findDoors() {
		if (doors == null) {
			// Determine which doors are closed
			StringBuilder doorBuilder = new StringBuilder();
			BlockPos.Mutable doorPos = new BlockPos.Mutable(15, 57, 19);
			for (int i = 0; i < 5; i++) {
				if (!world.getBlockState(room.relativeToActual(doorPos)).isAir()) {
					doorBuilder.append(i);
				}
				doorPos.move(Direction.NORTH);
			}

			doors = doorBuilder.toString();
		}

		if (doors.isEmpty()) {
			solution = new HashMap<>();
		} else if (doors.length() != 3) {
			player.sendMessage(
					Constants.PREFIX.get().append(
							"Waterboard: doors are in an unrecognized state. " +
							"Make sure exactly three doors are closed, then reset the solver."), false);
			failed = true;
		}
	}

	private void checkWater() {
		// Make sure there is no water currently in the board
		for (int x = BOARD_MIN_X; x <= BOARD_MAX_X; x++) {
			for (int y = BOARD_MIN_Y; y <= BOARD_MAX_Y; y++) {
				BlockPos pos = room.relativeToActual(new BlockPos(x, y, BOARD_Z));
				BlockState state = world.getBlockState(pos);
				if (state.isOf(Blocks.WATER)) {
					player.sendMessage(Constants.PREFIX.get().append(
							"Waterboard: water must be toggled off for the solver to work properly. " +
							"Turn the water off and let it drain, then reset the solver."), false);
					failed = true;
					return;
				}
			}
		}
	}

	private void loadSolution() {
		// Solutions are precalculated according to board variant and initial door combination
		JsonObject data = SOLUTIONS.get(String.valueOf(variant)).getAsJsonObject().get(doors).getAsJsonObject();

		solution = new HashMap<>();
		for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
			LeverType leverType = LeverType.fromName(entry.getKey());
			if (leverType != null) {
				List<Double> times = new ArrayList<>();
				for (JsonElement element : entry.getValue().getAsJsonArray()) {
					times.add(element.getAsDouble());
				}
				solution.put(leverType, times);
			}
		}

		// If the solver was reset after using some levers, make sure they are flipped back to the correct positions
		for (LeverType leverType : LeverType.values()) {
			List<Double> times = solution.computeIfAbsent(leverType, k -> new ArrayList<>());
			if (world.getBlockState(room.relativeToActual(leverType.leverPos)).get(LeverBlock.POWERED, false)) {
				if (times.isEmpty() || times.getFirst() != 0.0) {
					times.addFirst(0.0);
				} else {
					times.removeFirst();
				}
			}
		}
	}

    @Override
    public void render(WorldRenderContext context) {
        if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveWaterboard ||
				room == null ||
				world == null) return;

		try {
			renderWaterPath(context);
			if (solution != null) {
				LeverType nextLever = findNextLever();
				if (nextLever != null) {
					RenderHelper.renderLineFromCursor(context,
							room.relativeToActual(nextLever.leverPos).toCenterPos(),
							ColorUtils.getFloatComponents(DyeColor.LIME), 1f, 2f);
				}
				renderLeverText(context, nextLever);
			}
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Waterboard] Error while rendering", e);
		}
    }

	private LeverType findNextLever() {
		// Determine which lever should be used next
		LeverType nextLever = null;
		double minimumTime = 0.0;

		for (LeverType leverType : LeverType.values()) {
			List<Double> times = solution.computeIfAbsent(leverType, k -> new ArrayList<>());
			if (!times.isEmpty()) {
				double next = times.getFirst();
				if (nextLever == null || next < minimumTime) {
					nextLever = leverType;
					minimumTime = next;
				}
			}
		}

		return nextLever;
	}

	private void renderLeverText(WorldRenderContext context, LeverType nextLever) {
		for (Map.Entry<LeverType, List<Double>> leverData : solution.entrySet()) {
			LeverType lever = leverData.getKey();
			for (int i = 0; i < leverData.getValue().size(); i++) {
				double nextTime = leverData.getValue().get(i);
				long remainingTime = waterStartMillis + (long)(nextTime * 1000) - System.currentTimeMillis();
				String text;

				if (lever == LeverType.WATER && nextTime == 0.0 && nextLever != LeverType.WATER) {
					// Solutions assume levers with a time of 0.0 are used before the water lever
					text = "" + Formatting.RED + Formatting.BOLD + "WAIT";
				} else if (waterStartMillis == 0 && nextTime == 0.0 || waterStartMillis > 0 && remainingTime <= 0.0) {
					text = "" + Formatting.GREEN + Formatting.BOLD + "CLICK";
				} else {
					double timeToShow = waterStartMillis == 0 ? nextTime : remainingTime / 1000.0;
					text = Formatting.YELLOW + String.format("%.2f", timeToShow);
				}

				RenderHelper.renderText(context, Text.of(text),
						room.relativeToActual(lever.leverPos).toCenterPos()
								.offset(Direction.UP, 0.5 * (i + 1)), true);
			}
		}
	}

	private void renderWaterPath(WorldRenderContext context) {
		// Calculate and render path of water through the board (as a fallback and because it looks nice)
		List<Pair<BlockPos, BlockPos>> waterPath = new ArrayList<>();
		waterPath.add(new Pair<>(FIRST_SWITCH_POSITION.up(5), FIRST_SWITCH_POSITION.up(3)));
		findWaterPathVertical(FIRST_SWITCH_POSITION.up(3), waterPath);

		for (Pair<BlockPos, BlockPos> pair : waterPath) {
			Vec3d head = room.relativeToActual(pair.getLeft()).toCenterPos();
			Vec3d tail = room.relativeToActual(pair.getRight()).toCenterPos();
			RenderHelper.renderLinesFromPoints(context, new Vec3d[]{head, tail},
					ColorUtils.getFloatComponents(DyeColor.LIGHT_BLUE), 1f, 3f, true);
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
		return state.isAir() || state.isOf(Blocks.WATER);
	}

    private ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult blockHitResult) {
		if (SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveWaterboard &&
				DungeonManager.isCurrentRoomMatched() &&
				solution != null &&
				blockHitResult.getType() == HitResult.Type.BLOCK) {
			LeverType leverType = LeverType.fromPos(room.actualToRelative(blockHitResult.getBlockPos()));
			if (leverType != null) {
				List<Double> times = solution.computeIfAbsent(leverType, k -> new ArrayList<>());
				if (waterStartMillis == 0 && leverType != LeverType.WATER && (times.isEmpty() || times.getFirst() != 0.0)) {
					// If the incorrect lever was used and the water hasn't started yet, tell the player to move it back
					times.addFirst(0.0);
				} else {
					if (!times.isEmpty()) {
						times.removeFirst();
					}
					if (waterStartMillis == 0 && leverType == LeverType.WATER) {
						waterStartMillis = System.currentTimeMillis();
					}
				}
			}
		}
        return ActionResult.PASS;
    }

    @Override
    public void reset() {
        super.reset();
		softReset();
    }

	private void softReset() {
		// In most cases we want the solver to remain active after resetting,
		// so don't call super.reset()
		solve = null;
		world = null;
		room = null;
		player = null;
		variant = 0;
		doors = null;
		solution = null;
		failed = false;
		waterStartMillis = 0;
	}

	private enum LeverType {
		COAL(Blocks.COAL_BLOCK, new BlockPos(20, 61, 10)),
		GOLD(Blocks.GOLD_BLOCK, new BlockPos(20, 61, 15)),
		QUARTZ(Blocks.QUARTZ_BLOCK, new BlockPos(20, 61, 20)),
		DIAMOND(Blocks.DIAMOND_BLOCK, new BlockPos(10, 61, 20)),
		EMERALD(Blocks.EMERALD_BLOCK, new BlockPos(10, 61, 15)),
		TERRACOTTA(Blocks.TERRACOTTA, new BlockPos(10, 61, 10)),
		WATER(Blocks.WATER, new BlockPos(15, 60, 5));

		public final Block block;
		public final BlockPos leverPos;

		LeverType(Block block, BlockPos leverPos) {
			this.block = block;
			this.leverPos = leverPos;
		}

		public static LeverType fromName(String name) {
			for (LeverType leverType : LeverType.values()) {
				if (leverType.name().equalsIgnoreCase(name)) {
					return leverType;
				}
			}
			return null;
		}

		public static LeverType fromBlock(Block block) {
			for (LeverType leverType : LeverType.values()) {
				if (leverType.block == block) {
					return leverType;
				}
			}
			return null;
		}

		public static LeverType fromPos(BlockPos leverPos) {
			for (LeverType leverType : LeverType.values()) {
				if (leverType.leverPos.isWithinDistance(leverPos, 1)) {
					return leverType;
				}
			}
			return null;
		}
	}
}
