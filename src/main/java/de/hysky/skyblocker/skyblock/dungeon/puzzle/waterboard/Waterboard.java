package de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.puzzle.DungeonPuzzle;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeverBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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

	private Map<LeverType, List<Double>> solution;
	private boolean failed;
	private double waterStartMillis;
	private CompletableFuture<Void> solve;

    private Waterboard() {
        super("waterboard", "water-puzzle");
    }

    @Init
    public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(Waterboard::loadSolutions);
        UseBlockCallback.EVENT.register(INSTANCE::onUseBlock);
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
				client.world == null ||
				!DungeonManager.isCurrentRoomMatched() ||
				solution != null ||
				failed ||
				solve != null && !solve.isDone()) {
            return;
        }
		solve = CompletableFuture.runAsync(() -> solvePuzzle(client)).exceptionally(e -> {
			LOGGER.error("[Skyblocker Waterboard] Encountered an unknown exception while solving waterboard.", e);
			failed = true;
			return null;
		});
    }

	private void solvePuzzle(MinecraftClient client) {
		Room room = DungeonManager.getCurrentRoom();
		ClientWorld world = client.world;
		if (world == null) throw new RuntimeException("Unreachable");
		if (client.player == null) throw new RuntimeException("Unreachable");

		Set<LeverType> firstSwitches = new HashSet<>();
		Box firstSwitchBlocks = Box.enclosing(room.relativeToActual(FIRST_SWITCH_POSITION.add(-1, -1, 0)),
				room.relativeToActual(FIRST_SWITCH_POSITION.add(1, 0, 1)));
		for (BlockState state : world.getStatesInBox(firstSwitchBlocks).toList()) {
			LeverType leverType = LeverType.fromBlock(state.getBlock());
			if (leverType != null) {
				firstSwitches.add(leverType);
			}
		}

		int variant;
		if (firstSwitches.contains(LeverType.GOLD) && firstSwitches.contains(LeverType.TERRACOTTA)) {
			variant = 0;
		} else if (firstSwitches.contains(LeverType.EMERALD) && firstSwitches.contains(LeverType.QUARTZ)) {
			variant = 1;
		} else if (firstSwitches.contains(LeverType.QUARTZ) && firstSwitches.contains(LeverType.DIAMOND)) {
			variant = 2;
		} else if (firstSwitches.contains(LeverType.GOLD) && firstSwitches.contains(LeverType.QUARTZ)) {
			variant = 3;
		} else {
			LOGGER.error("[Skyblocker Waterboard] Unknown waterboard layout.");
			return;
		}

		StringBuilder doors = new StringBuilder();
		BlockPos.Mutable doorPos = new BlockPos.Mutable(15, 57, 19);
		for (int i = 0; i < 5; i++) {
			if (!world.getBlockState(room.relativeToActual(doorPos)).isAir()) {
				doors.append(i);
			}
			doorPos.move(Direction.NORTH);
		}

		if (doors.isEmpty()) {
			solution = new HashMap<>();
			return;
		} else if (doors.toString().length() != 3) {
			client.player.sendMessage(
					Constants.PREFIX.get().append(
							"Waterboard: doors are in an unrecognized state. " +
							"Make sure exactly three doors are closed, then reset the solver."), false);
			failed = true;
		}

		for (int x = BOARD_MIN_X; x <= BOARD_MAX_X; x++) {
			for (int y = BOARD_MIN_Y; y <= BOARD_MAX_Y; y++) {
				BlockPos pos = room.relativeToActual(new BlockPos(x, y, BOARD_Z));
				BlockState state = world.getBlockState(pos);
				if (state.getBlock() == Blocks.WATER) {
					client.player.sendMessage(Constants.PREFIX.get().append(
							"Waterboard: water must be toggled off for the solver to work properly. " +
									"Turn the water off and let it drain, then reset the solver."), false);
					failed = true;
					break;
				}
			}
		}

		if (failed) {
			return;
		}

		JsonObject data = SOLUTIONS.get(String.valueOf(variant)).getAsJsonObject().get(doors.toString()).getAsJsonObject();

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

		for (LeverType leverType : LeverType.values()) {
			List<Double> times = solution.get(leverType);
			if (world.getBlockState(room.relativeToActual(leverType.leverPos)).get(LeverBlock.POWERED, false)) {
				if (times == null) {
					times = new ArrayList<>();
					solution.put(leverType, times);
				}
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
				!DungeonManager.isCurrentRoomMatched() ||
				solution == null) return;
		Room room = DungeonManager.getCurrentRoom();

		LeverType nextLever = null;
		double minimumTime = 0;
		for (LeverType leverType : LeverType.values()) {
			List<Double> times = solution.get(leverType);
			if (times != null && times.size() > leverType.timesUsed) {
				double next = times.get(leverType.timesUsed);
				if (nextLever == null || next < minimumTime) {
					nextLever = leverType;
					minimumTime = next;
				}
			}
		}

		for (Map.Entry<LeverType, List<Double>> leverData : solution.entrySet()) {
			LeverType lever = leverData.getKey();
			for (int i = lever.timesUsed; i < leverData.getValue().size(); i++) {
				double nextTime = leverData.getValue().get(i);
				double remainingTime = waterStartMillis + nextTime * 1000 - System.currentTimeMillis();
				String text;

				if (lever == LeverType.WATER && i == 0 && nextLever != LeverType.WATER) {
					text = "" + Formatting.RED + Formatting.BOLD + "WAIT";
				} else if (waterStartMillis == 0 && nextTime == 0 || waterStartMillis > 0 && remainingTime <= 0) {
					text = "" + Formatting.GREEN + Formatting.BOLD + "CLICK";
				} else {
					double timeToShow = waterStartMillis == 0 ? nextTime : remainingTime / 1000.0;
					text = Formatting.YELLOW + String.format("%.2f", timeToShow);
				}

				RenderHelper.renderText(context, Text.of(text),
						room.relativeToActual(lever.leverPos).toCenterPos()
								.offset(Direction.UP, 0.5 * (1 + i - lever.timesUsed)), true);
			}

			if (nextLever != null) {
				RenderHelper.renderLineFromCursor(context,
						room.relativeToActual(nextLever.leverPos).toCenterPos(),
						ColorUtils.getFloatComponents(DyeColor.LIME), 1f, 2f);
			}
		}
    }

    private ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult blockHitResult) {
		if (SkyblockerConfigManager.get().dungeons.puzzleSolvers.solveWaterboard &&
				DungeonManager.isCurrentRoomMatched() &&
				solution != null &&
				blockHitResult.getType() == HitResult.Type.BLOCK) {
			Room room = DungeonManager.getCurrentRoom();
			LeverType leverType = LeverType.fromPos(room.actualToRelative(blockHitResult.getBlockPos()));
			if (leverType != null) {
				leverType.timesUsed++;
				if (leverType == LeverType.WATER && waterStartMillis == 0) {
					waterStartMillis = System.currentTimeMillis();
				}
			}
		}
        return ActionResult.PASS;
    }

    @Override
    public void reset() {
        super.reset();
        solve = null;
		solution = null;
		failed = false;
		waterStartMillis = 0;
		for (LeverType leverType : LeverType.values()) {
			leverType.timesUsed = 0;
		}
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
		public int timesUsed;

		LeverType(Block block, BlockPos leverPos) {
			this.block = block;
			this.leverPos = leverPos;
			timesUsed = 0;
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
