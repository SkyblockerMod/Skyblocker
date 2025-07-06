package de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.dungeon.puzzle.DungeonPuzzle;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.render.RenderHelper;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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

import static de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard.Waterboard.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/*
Benchmark times for solutions in watertimes.json (for anyone trying to improve the solutions)
Time starts when the water lever is turned on and stops when the last door opens, use the toggleTimer command

--- Variant 1 ---
012: 11.85s
013: 14.24s
014: 14.35s
023: 14.16s
024: 10.81s
034: 16.11s
123: 12.36s
124: 11.94s
134: 12.01s
234: 11.29s

--- Variant 2 ---
012: 12.26s
013: 13.79s
014: 14.51s
023: 14.44s
024: 12.66s
034: 11.91s
123: 12.89s
124: 13.26s
134: 12.69s
234: 12.50s

--- Variant 3 ---
012: 12.45s
013: 12.86s
014: 11.66s
023: 13.04s
024: 11.96s
034: 13.71s
123: 13.66s
124: 10.94s
134: 12.30s
234: 12.65s

--- Variant 4 ---
012: 13.29s
013: 12.15s
014: 12.25s
023: 11.10s
024: 13.11s
034: 16.59s
123: 11.20s
124: 13.61s
134: 14.21s
234: 13.94s
*/

public class WaterboardOneFlow extends DungeonPuzzle {
	private static final Logger LOGGER = LoggerFactory.getLogger(WaterboardOneFlow.class);
	public static final WaterboardOneFlow INSTANCE = new WaterboardOneFlow();
	private static final Identifier WATER_TIMES = Identifier.of(SkyblockerMod.NAMESPACE, "dungeons/watertimes.json");
	private static final Text WAIT_TEXT = Text.literal("WAIT").formatted(Formatting.RED, Formatting.BOLD);
	private static final Text CLICK_TEXT = Text.literal("CLICK").formatted(Formatting.GREEN, Formatting.BOLD);
	private static JsonObject SOLUTIONS;

	private boolean timerEnabled;
	private final List<Mark> marks = new ArrayList<>();
	private ClientWorld world;
	private Room room;
	private ClientPlayerEntity player;

	private int variant;
	private String doors;
	private String initialDoors;
	private EnumMap<LeverType, DoubleList> solution;
	private boolean finished;
	private long waterStartMillis;
	private CompletableFuture<Void> solve;

    private WaterboardOneFlow() {
        super("waterboard", "water-puzzle");
    }

    @Init
    public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(WaterboardOneFlow::loadSolutions);
        UseBlockCallback.EVENT.register(INSTANCE::onUseBlock);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("dungeons").then(literal("puzzle").then(literal(INSTANCE.puzzleName)
						.then(literal("reset").executes(context -> {
							INSTANCE.softReset();
							return Command.SINGLE_SUCCESS;
						}))
		)))));

		if (Debug.debugEnabled()) {
			ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("dungeons").then(literal("puzzle").then(literal(INSTANCE.puzzleName)
					.then(literal("setDoors").then(argument("combination", StringArgumentType.string()).executes(context -> {
						String doorCombination = StringArgumentType.getString(context, "combination");
						if (SOLUTIONS.get("1").getAsJsonObject().keySet().contains(doorCombination)) {
							INSTANCE.softReset();
							INSTANCE.doors = doorCombination;
						} else {
							context.getSource().sendError(Constants.PREFIX.get().append("Door combination must be three increasing digits between 0 and 4"));
						}
						return Command.SINGLE_SUCCESS;
					})))
					.then(literal("toggleTimer").executes((context) -> {
						INSTANCE.timerEnabled = !INSTANCE.timerEnabled;
						context.getSource().sendFeedback(Constants.PREFIX.get().append(
								INSTANCE.timerEnabled ? "Timer enabled." : "Timer disabled."));
						return Command.SINGLE_SUCCESS;
					}))
					.then(literal("modifyLever").then(argument("leverType", LeverType.LeverTypeArgumentType.leverType()).then(argument("times", StringArgumentType.greedyString()).executes((context) -> {
						LeverType leverType = LeverType.LeverTypeArgumentType.getLeverType(context, "leverType");
						if (leverType == null) {
							context.getSource().sendError(Constants.PREFIX.get().append("Invalid lever type"));
						} else if (INSTANCE.solution == null) {
							context.getSource().sendError(Constants.PREFIX.get().append("No existing solution"));
						} else {
							try {
								DoubleList times = new DoubleArrayList();
								for (String time : StringArgumentType.getString(context, "times").split(" ")) {
									times.add(Double.parseDouble(time));
								}
								INSTANCE.solution.put(leverType, times);
							} catch (NumberFormatException e) {
								context.getSource().sendError(Constants.PREFIX.get().append("Times must be valid numbers or decimals"));
							}
						}
						return Command.SINGLE_SUCCESS;
					}))))
					.then(literal("addMark").executes((context) -> {
						if (INSTANCE.world == null || INSTANCE.room == null || INSTANCE.player == null) {
							context.getSource().sendError(Constants.PREFIX.get().append("Solver not active"));
							return Command.SINGLE_SUCCESS;
						}

						Vec3d camera = INSTANCE.room.actualToRelative(INSTANCE.player.getEyePos());
						Vec3d look = INSTANCE.room.actualToRelative(INSTANCE.player.getEyePos()
								.add(INSTANCE.player.getRotationVector())).subtract(camera);
						double t = (BOARD_Z + 0.5 - camera.getZ()) / look.getZ();
						Vec3d vec = camera.add(look.multiply(t));
						double x = MathHelper.floor(vec.x);
						double y = MathHelper.floor(vec.y);
						double z = MathHelper.floor(vec.z);

						if (x < BOARD_MIN_X || x > BOARD_MAX_X || y < BOARD_MIN_Y || y > BOARD_MAX_Y || z != BOARD_Z) {
							context.getSource().sendError(Constants.PREFIX.get().append("Mark is not inside the board"));
							return Command.SINGLE_SUCCESS;
						}
						BlockPos pos = BlockPos.ofFloored(INSTANCE.room.relativeToActual(vec));

						if (!INSTANCE.world.getBlockState(pos).isAir()) {
							context.getSource().sendError(Constants.PREFIX.get().append("Marks can only be placed on air"));
							return Command.SINGLE_SUCCESS;
						}

						for (Mark mark : INSTANCE.marks) {
							if (mark.pos.equals(pos)) {
								context.getSource().sendError(Constants.PREFIX.get().append("There is already a mark at that position"));
								return Command.SINGLE_SUCCESS;
							}
						}

						INSTANCE.marks.add(new Mark(INSTANCE.marks.size() + 1, pos));
						return Command.SINGLE_SUCCESS;
					}))
					.then(literal("clearMarks").executes((context) -> {
						INSTANCE.marks.clear();
						return Command.SINGLE_SUCCESS;
					}))
			)))));
		}
    }

	private static void loadSolutions(MinecraftClient client) {
		try (BufferedReader reader = client.getResourceManager().openAsReader(WATER_TIMES)) {
			SOLUTIONS = JsonParser.parseReader(reader).getAsJsonObject();
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Waterboard] Failed to load solutions json", e);
		}
	}

    @Override
    public void tick(MinecraftClient client) {
        if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.waterboardOneFlow ||
				!shouldSolve() ||
				client.world == null ||
				client.player == null ||
				!DungeonManager.isCurrentRoomMatched()) {
            return;
        }

		world = client.world;
		room = DungeonManager.getCurrentRoom();
		player = client.player;

		if (solution == null && !finished && solve == null) {
			solve = CompletableFuture.runAsync(this::solvePuzzle).exceptionally(e -> {
				LOGGER.error("[Skyblocker Waterboard] Encountered an unknown exception while solving waterboard.", e);
				finished = true;
				return null;
			});
		}

		if (!finished && isPuzzleSolved()) {
			finished = true;
			if (timerEnabled) {
				double elapsed = (System.currentTimeMillis() - waterStartMillis) / 1000.0;
				player.sendMessage(Constants.PREFIX.get().append("Puzzle solved in ")
						.append(Text.literal(String.format("%.2f", elapsed)).formatted(Formatting.GREEN))
						.append(Formatting.RESET.toString()).append(" seconds."), false);
			}
		}

		if (waterStartMillis > 0) {
			for (Mark mark : marks) {
				if (!mark.reached && world.getBlockState(mark.pos).isOf(Blocks.WATER)) {
					mark.reached = true;
					double elapsed = (System.currentTimeMillis() - waterStartMillis) / 1000.0;
					player.sendMessage(Constants.PREFIX.get().append(String.format("Mark %d reached in ", mark.index))
							.append(Text.literal(String.format("%.2f", elapsed)).formatted(Formatting.GREEN))
							.append(Formatting.RESET.toString()).append(" seconds."), false);
				}
			}
		}
    }

	private void solvePuzzle() {
		variant = findVariant();
		if (variant == 0) {
			finished = true;
			return;
		}

		initialDoors = findDoors();
		if (doors == null) {
			// Assume we want to open all doors, unless manually testing a different solution
			doors = initialDoors;
			if (doors.isEmpty()) {
				solution = makeEmptySolution();
				finished = true;
				return;
			} else if (doors.length() != 3) {
				player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.puzzle.waterboard.invalidDoors")), false);
				finished = true;
				return;
			}
		}

		if (!checkWater()) {
			player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.puzzle.waterboard.waterFound")), false);
			finished = true;
			return;
		}

		if (!finished) {
			// Solutions are precalculated according to board variant and initial door combination (in watertimes.json)
			JsonObject data = SOLUTIONS.get(String.valueOf(variant)).getAsJsonObject().get(doors).getAsJsonObject();
			solution = setupSolution(data);
		}
	}

	private EnumMap<LeverType, DoubleList> makeEmptySolution() {
		EnumMap<LeverType, DoubleList> solution = new EnumMap<>(LeverType.class);
		for (LeverType leverType : LeverType.values()) {
			solution.put(leverType, new DoubleArrayList());
		}
		return solution;
	}

	private int findVariant() {
		// The waterboard only has four possible layouts, each with a unique pair of
		// toggleable blocks at the entrance. They are a block lower on the first layout.

		Set<LeverType> firstSwitches = new HashSet<>();
		Box firstSwitchBlocks = Box.enclosing(room.relativeToActual(WATER_ENTRANCE_POSITION.add(-1, -1, 0)),
				room.relativeToActual(WATER_ENTRANCE_POSITION.add(1, 0, 1)));
		for (BlockState state : world.getStatesInBox(firstSwitchBlocks).toList()) {
			LeverType leverType = LeverType.fromBlock(state.getBlock());
			if (leverType != null) {
				firstSwitches.add(leverType);
			}
		}

		if (firstSwitches.contains(LeverType.GOLD) && firstSwitches.contains(LeverType.TERRACOTTA)) {
			return 1;
		} else if (firstSwitches.contains(LeverType.EMERALD) && firstSwitches.contains(LeverType.QUARTZ)) {
			return 2;
		} else if (firstSwitches.contains(LeverType.QUARTZ) && firstSwitches.contains(LeverType.DIAMOND)) {
			return 3;
		} else if (firstSwitches.contains(LeverType.GOLD) && firstSwitches.contains(LeverType.QUARTZ)) {
			return 4;
		}

		LOGGER.error("[Skyblocker Waterboard] Unknown waterboard layout. Detected switches: [{}]",
				String.join(", ", firstSwitches.stream().map(LeverType::asString).toList()));

		return 0;
	}

	private String findDoors() {
		// Determine which doors are closed
		StringBuilder doorBuilder = new StringBuilder();
		BlockPos.Mutable doorPos = new BlockPos.Mutable(15, 57, 19);
		for (int i = 0; i < 5; i++) {
			if (!world.getBlockState(room.relativeToActual(doorPos)).isAir()) {
				doorBuilder.append(i);
			}
			doorPos.move(Direction.NORTH);
		}
		return doorBuilder.toString();
	}

	private boolean checkWater() {
		// Make sure there is no water currently in the board
		for (int x = BOARD_MIN_X; x <= BOARD_MAX_X; x++) {
			for (int y = BOARD_MIN_Y; y <= BOARD_MAX_Y; y++) {
				BlockPos pos = room.relativeToActual(new BlockPos(x, y, BOARD_Z));
				BlockState state = world.getBlockState(pos);
				if (state.isOf(Blocks.WATER)) {
					return false;
				}
			}
		}
		return true;
	}

	private EnumMap<LeverType, DoubleList> setupSolution(JsonObject data) {
		EnumMap<LeverType, DoubleList> solution = makeEmptySolution();
		for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
			LeverType leverType = LeverType.fromName(entry.getKey());
			if (leverType != null) {
				DoubleList times = new DoubleArrayList();
				for (JsonElement element : entry.getValue().getAsJsonArray()) {
					times.add(element.getAsDouble());
				}
				solution.put(leverType, times);
			}
		}

		// If the solver was reset after using some levers, make sure they are flipped back to the correct positions
		for (LeverType leverType : LeverType.values()) {
			DoubleList times = solution.get(leverType);
			if (leverType != LeverType.WATER && isLeverActive(leverType)) {
				if (times.isEmpty() || times.getFirst() != 0.0) {
					times.addFirst(0.0);
				} else {
					times.removeFirst();
				}
			}
		}

		return solution;
	}

	private boolean isLeverActive(LeverType leverType) {
		BlockPos offset = leverType.initialPositions[variant - 1];
		if (offset == null) {
			return false;
		}
		return !world.getBlockState(room.relativeToActual(WATER_ENTRANCE_POSITION.add(offset))).isOf(leverType.block);
	}

	private boolean isPuzzleSolved() {
		if (doors == null || initialDoors == null || waterStartMillis == 0) {
			return false;
		}
		String currentDoors = findDoors();
		for (int i = 0; i < 5; i++) {
			String s = String.valueOf(i);
			// If the door was toggled, initialDoors... == currentDoors... will be false.
			// If the door should have been toggled, doors... will be true.
			// If the puzzle is solved, doors will only be toggled when they should be,
			// so the two will not be equal. If they are equal, it means the puzzle isn't solved.
			if (initialDoors.contains(s) == currentDoors.contains(s) == doors.contains(s)) {
				return false;
			}
		}
		return true;
	}

    @Override
    public void render(WorldRenderContext context) {
		if (!SkyblockerConfigManager.get().dungeons.puzzleSolvers.waterboardOneFlow ||
				world == null || room == null || player == null) return;

		try {
			for (Mark mark : marks) {
				float[] components = ColorUtils.getFloatComponents(mark.reached ? DyeColor.LIME : DyeColor.WHITE);
				RenderHelper.renderFilled(context, mark.pos, components, 0.5f, true);
				RenderHelper.renderText(context, Text.of(String.format("Mark %d", mark.index)),
						mark.pos.toCenterPos().offset(Direction.UP, 0.2), true);
			}

			if (solution != null) {
				List<ObjectDoublePair<LeverType>> sortedTimes = solution.entrySet().stream()
						.flatMap((entry) -> entry.getValue().doubleStream().mapToObj((time) -> ObjectDoublePair.of(entry.getKey(), time)))
						// Sort by next use time, then by lever type
						.sorted(Comparator
								.<ObjectDoublePair<LeverType>>comparingDouble(p -> p.rightDouble() + (p.left() == LeverType.WATER ? 0.001 : 0.0))
								.thenComparingInt(p -> p.left().ordinal())
						).toList();
				LeverType nextLever = sortedTimes.isEmpty() ? null : sortedTimes.getFirst().left();
				LeverType nextNextLever = sortedTimes.size() < 2 ? null : sortedTimes.get(1).left();

				if (nextLever != null) {
					RenderHelper.renderLineFromCursor(context,
							room.relativeToActual(nextLever.leverPos).toCenterPos(),
							ColorUtils.getFloatComponents(DyeColor.LIME), 1f, 2f);
					if (nextNextLever != null) {
						RenderHelper.renderLinesFromPoints(context, new Vec3d[]{
								room.relativeToActual(nextLever.leverPos).toCenterPos(),
								room.relativeToActual(nextNextLever.leverPos).toCenterPos()
						}, ColorUtils.getFloatComponents(DyeColor.WHITE), 0.5f, 1f, true);
					}
				}

				renderLeverText(context, nextLever);
			}
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Waterboard] Error while rendering one flow", e);
		}
	}

	private void renderLeverText(WorldRenderContext context, LeverType nextLever) {
		for (Map.Entry<LeverType, DoubleList> leverData : solution.entrySet()) {
			LeverType lever = leverData.getKey();
			for (int i = 0; i < leverData.getValue().size(); i++) {
				double nextTime = leverData.getValue().getDouble(i);
				long remainingTime = waterStartMillis + (long) (nextTime * 1000) - System.currentTimeMillis();
				Text text;

				if (lever == LeverType.WATER && nextTime == 0.0 && nextLever != LeverType.WATER) {
					// Solutions assume levers with a time of 0.0 are used before the water lever
					text = WAIT_TEXT;
				} else if (waterStartMillis == 0 && nextTime == 0.0 || waterStartMillis > 0 && remainingTime <= 0.0) {
					text = CLICK_TEXT;
				} else {
					double timeToShow = waterStartMillis == 0 ? nextTime : remainingTime / 1000.0;
					text = Text.literal(String.format("%.2f", timeToShow)).formatted(Formatting.YELLOW);
				}

				RenderHelper.renderText(context, text,
						room.relativeToActual(lever.leverPos).toCenterPos()
								.offset(Direction.UP, 0.5 * (i + 1)), true);
			}
		}
	}

    private ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult blockHitResult) {
		try {
			if (SkyblockerConfigManager.get().dungeons.puzzleSolvers.waterboardOneFlow &&
					solution != null && blockHitResult.getType() == HitResult.Type.BLOCK) {
				LeverType leverType = LeverType.fromPos(room.actualToRelative(blockHitResult.getBlockPos()));
				if (leverType != null) {
					List<Double> times = solution.get(leverType);
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
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Waterboard] Exception in onUseBlock", e);
		}
        return ActionResult.PASS;
    }

    @Override
    public void reset() {
        super.reset();
		softReset();
    }

	private void softReset() {
		// In most cases we want the solver to remain active after resetting, so don't call super.reset()
		solve = null;
		variant = 0;
		doors = null;
		initialDoors = null;
		solution = null;
		finished = false;
		waterStartMillis = 0;
		for (Mark mark : marks) {
			mark.reached = false;
		}
	}

	// Can be added to the board to time how long it takes the water to reach certain locations.
	// Use the addMarks command while looking at the spot where the mark should go.
	private static class Mark {
		private final int index;
		private final BlockPos pos;
		private boolean reached;

		private Mark(int index, BlockPos pos) {
			this.index = index;
			this.pos = pos;
			this.reached = false;
		}
	}
}
