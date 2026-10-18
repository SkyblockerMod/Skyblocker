package de.hysky.skyblocker.skyblock.foraging.torrhus;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.phys.Vec3;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.LevelRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.scheduler.Scheduler;

public class DesertTemplePuzzle {
	private static final Minecraft MINECRAFT = Minecraft.getInstance();
	private static final Map<DyeColor, BlockPos> BUTTON_LOCATIONS = Map.ofEntries(
			Map.entry(DyeColor.BROWN, new BlockPos(-611, 49, 232)),
			Map.entry(DyeColor.RED, new BlockPos(-612, 49, 232)),
			Map.entry(DyeColor.ORANGE, new BlockPos(-613, 49, 232)),
			Map.entry(DyeColor.YELLOW, new BlockPos(-614, 48, 232)),
			Map.entry(DyeColor.LIME, new BlockPos(-614, 47, 232)),
			Map.entry(DyeColor.GREEN, new BlockPos(-614, 46, 232)),
			Map.entry(DyeColor.CYAN, new BlockPos(-613, 45, 232)),
			Map.entry(DyeColor.LIGHT_BLUE, new BlockPos(-612, 45, 232)),
			Map.entry(DyeColor.BLUE, new BlockPos(-611, 45, 232)),
			Map.entry(DyeColor.PURPLE, new BlockPos(-610, 46, 232)),
			Map.entry(DyeColor.MAGENTA, new BlockPos(-610, 47, 232)),
			Map.entry(DyeColor.PINK, new BlockPos(-610, 48, 232))
			);
	private static final BlockPos GLASS_FLOOR_CENTRE = new BlockPos(-612, 42, 227);
	private static final Vector3ic[] NEIGHBOUR_OFFSETS = new Vector3ic[] {
			// North                // East                // South               // West
			new Vector3i(0, 0, -1), new Vector3i(1, 0, 0), new Vector3i(0, 0, 1), new Vector3i(-1, 0, 0)
	};
	private static final BlockPos GLASS_WHEEL_CENTRE = new BlockPos(-612, 47, 233);
	private static final Map<BlockPos, BlockPos> STATUE_ARROWS = Map.ofEntries(
			// Bird
			Map.entry(new BlockPos(-618, 46, 248), new BlockPos(-616, 46, 248)),
			// Snake
			Map.entry(new BlockPos(-607, 46, 249), new BlockPos(-607, 46, 247)),
			// Frog
			Map.entry(new BlockPos(-606, 46, 240), new BlockPos(-608, 46, 240)),
			// Fly
			Map.entry(new BlockPos(-617, 46, 241), new BlockPos(-617, 46, 239))
			);
	private static final float[] WHITE = { 1f, 1f, 1f };
	private static final float LINE_WIDTH = 5f;
	private static List<DyeColor> buttonOrder = List.of();

	@Init
	public static void init() {
		// Update solution every 5 seconds
		Scheduler.INSTANCE.scheduleCyclic(DesertTemplePuzzle::calculateSolution, 20 * 5);
		LevelRenderExtractionCallback.EVENT.register(DesertTemplePuzzle::extractRendering);
		ClientPlayConnectionEvents.JOIN.register((_, _, _) -> reset());
	}

	private static boolean shouldProcess() {
		// The scoreboard shows "Torrhus Springs" outside and "Desert Temple" inside
		boolean inArea = Utils.getArea() == Area.TorrhusCanyon.TORRHUS_SPRINGS || Utils.getArea() == Area.TorrhusCanyon.DESERT_TEMPLE;

		return SkyblockerConfigManager.get().foraging.torrhusCanyon.solveDesertTemplePuzzles && Utils.isInTorrhusCanyon() && inArea && MINECRAFT.level != null;
	}

	private static void calculateSolution() {
		if (shouldProcess()) {
			buttonOrder = solve(MINECRAFT.level);
		}
	}

	private static List<DyeColor> solve(ClientLevel level) {
		// The solution may always be the same, but better to be safe than sorry!
		// (also an excuse to write a cool algorithm!)
		List<DyeColor> colours = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();
		Queue<BlockPos> queue = new ArrayDeque<>();

		queue.add(GLASS_FLOOR_CENTRE);

		// Flood fill using Breadth-First Searching to collect all glass blocks
		while (!queue.isEmpty()) {
			BlockPos current = queue.remove();

			if (level.getBlockState(current).getBlock() instanceof StainedGlassBlock glassBlock) {
				colours.add(glassBlock.getColor());
			}

			for (Vector3ic offset : NEIGHBOUR_OFFSETS) {
				BlockPos neighbour = current.offset(offset.x(), offset.y(), offset.z());

				if (visited.contains(neighbour) || queue.contains(neighbour)) {
					continue;
				} else if (level.getBlockState(neighbour).getBlock() instanceof StainedGlassBlock) {
					visited.add(neighbour);
					queue.add(neighbour);
				}
			}
		}

		// Determine Solution
		Map<DyeColor, Long> frequencyMap = colours.stream()
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		// Determine Order - it goes based on lowest number of glass blocks -> highest number of glass blocks
		List<DyeColor> buttonOrder = frequencyMap.entrySet().stream()
				.sorted(Map.Entry.<DyeColor, Long>comparingByValue())
				.map(Map.Entry::getKey)
				.toList();

		return buttonOrder;
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (shouldProcess()) {
			// Colour Floor Puzzle - if the solution is uncomputed or the door is open don't show the numbers
			if (!buttonOrder.isEmpty() && !MINECRAFT.level.getBlockState(GLASS_WHEEL_CENTRE).isAir()) {
				for (int i = 0; i < buttonOrder.size(); i++) {
					DyeColor colour = buttonOrder.get(i);
					BlockPos pos = BUTTON_LOCATIONS.get(colour);

					// Likely not possible but better to be safe about it
					if (pos != null) {
						collector.submitText(Component.literal(String.valueOf(i + 1)), Vec3.atBottomCenterOf(pos).add(0d, 0.2d, 0.2d), false);
					}
				}
			}

			// Statue Moving
			for (Map.Entry<BlockPos, BlockPos> entry : STATUE_ARROWS.entrySet()) {
				Vec3 start = Vec3.atCenterOf(entry.getKey());
				Vec3 end = Vec3.atCenterOf(entry.getValue());

				// Arrow drawing code from Gizmos

				collector.submitLinesFromPoints(new Vec3[] { start, end }, WHITE, 1f, LINE_WIDTH, false);

				Quaternionf rotation = new Quaternionf().rotationTo(new Vector3f(1.0f, 0.0f, 0.0f), end.subtract(start).toVector3f().normalize());
				float len = (float) Mth.clamp(end.distanceTo(start) * 0.1f, 0.1f, 1.0);
				Vector3f[] tips = new Vector3f[]{
						rotation.transform(-len, len, 0.0f, new Vector3f()),
						rotation.transform(-len, 0.0f, len, new Vector3f()),
						rotation.transform(-len, -len, 0.0f, new Vector3f()),
						rotation.transform(-len, 0.0f, -len, new Vector3f())
				};

				for (Vector3f tip : tips) {
					collector.submitLinesFromPoints(new Vec3[] { end.add(tip.x(), tip.y(), tip.z()), end }, WHITE, 1f, LINE_WIDTH, false);
				}
			}
		}
	}

	private static void reset() {
		buttonOrder = List.of();
	}
}
