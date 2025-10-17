package de.hysky.skyblocker.skyblock.dungeon.device;

import com.mojang.brigadier.Command;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.DungeonBoss;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ArrowPath {
	private static final BlockPos LEFT_TOP = new BlockPos(-2, 124, 79);
	private static final Box FRAMES_AREA = Box.enclosing(LEFT_TOP, new BlockPos(-3, 120, 75));
	private static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Arrow Path Solver");

	private static int[] currentSolution = null;
	private static boolean noSolution = false;

	@Init
	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
		WorldRenderExtractionCallback.EVENT.register(ArrowPath::extractRendering);
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("dungeons").then(literal("device").then(literal("arrow-path")
				.then(literal("solve").executes(context -> {
					findSolution();
					return Command.SINGLE_SUCCESS;
				}))
		)))));
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (shouldProcess() && MinecraftClient.getInstance().player.getPos().squaredDistanceTo(FRAMES_AREA.getCenter()) < 64) {
			if (currentSolution == null && !noSolution) {
				findSolution();
			}
			if (!noSolution) {
				List<ItemFrameEntity> frameEntitiesList = MinecraftClient.getInstance().world.getEntitiesByClass(ItemFrameEntity.class, FRAMES_AREA, frame -> true);
				for (ItemFrameEntity frameEntity : frameEntitiesList) {
					int now = frameEntity.getRotation();
					int expect = currentSolution[getSolutionIndex(frameEntity.getBlockPos())];
					if (expect >= 0) {
						int remaining = (expect + 8 - now) % 8;
						if (remaining > 0) {
							collector.submitText(Text.literal(String.valueOf(remaining)).withColor(ColorUtils.interpolate(0xFF00FF00, 0xFFFF0000, remaining / 8d)), frameEntity.getPos().add(0.3, 0, 0), false);
						}
					}
				}
			}
		}
	}

	private static int getSolutionIndex(BlockPos pos) {
		return (LEFT_TOP.getY() - pos.getY()) * 5 + LEFT_TOP.getZ() - pos.getZ();
	}

	private static void findSolution() {
		World world = MinecraftClient.getInstance().world;
		List<ItemFrameEntity> frameEntitiesList = world.getEntitiesByClass(ItemFrameEntity.class, FRAMES_AREA, frame -> true);

		Optional<int[]> solution = Path.SOLUTIONS.stream()
				.filter(rotations -> {
					for (ItemFrameEntity itemFrame : frameEntitiesList) {
						switch (rotations[getSolutionIndex(itemFrame.getBlockPos())]) {
							case Path.X -> {
								return false;
							}
							case Path.S -> {
								if (!itemFrame.getHeldItemStack().isOf(Items.LIME_WOOL)) return false;
							}
							case Path.E -> {
								if (!itemFrame.getHeldItemStack().isOf(Items.RED_WOOL)) return false;
							}
							default -> {
								if (!itemFrame.getHeldItemStack().isOf(Items.ARROW)) return false;
							}
						}
					}
					return true;
				})
				.findAny();

		currentSolution = solution.orElse(null);
		noSolution = solution.isEmpty();
		if (noSolution) {
			LOGGER.error("Failed to find a solution for Arrow Path device!");
		}
	}

	private static boolean shouldProcess() {
		return SkyblockerConfigManager.get().dungeons.devices.solveArrowPath &&
				Utils.isInDungeons() && DungeonManager.isInBoss() && DungeonManager.getBoss() == DungeonBoss.MAXOR;
	}

	private static void reset() {
		currentSolution = null;
		noSolution = false;
	}

	private static class Path {
		private static final int X = -1; // missing
		private static final int S = -2; // start
		private static final int E = -3; // end
		private static final int U = 7; // up
		private static final int D = 3; // down
		private static final int L = 5; // left
		private static final int R = 1; // right

		private static final List<int[]> SOLUTIONS = List.of(
				new int[]{
						R, R, D, X, X,
						U, X, D, X, X,
						S, X, D, X, E,
						X, X, D, X, U,
						X, X, R, R, U},
				new int[]{
						R, R, E, L, L,
						U, X, X, X, U,
						U, L, X, R, U,
						X, U, X, U, X,
						X, S, X, S, X},
				new int[]{
						D, L, L, X, X,
						D, X, U, X, X,
						E, X, S, X, E,
						X, X, D, X, U,
						X, X, R, R, U},
				new int[]{
						S, X, X, X, S,
						D, X, X, X, D,
						D, X, E, X, D,
						D, X, U, X, D,
						R, R, U, L, L},
				new int[]{
						R, R, R, R, D,
						U, X, X, X, D,
						U, X, E, X, D,
						U, X, U, X, D,
						S, X, U, L, L},
				new int[]{
						R, R, D, X, E,
						U, X, D, X, U,
						U, X, D, X, U,
						U, X, D, X, U,
						S, X, R, R, U},
				new int[]{
						S, R, R, R, E,
						X, X, X, X, X,
						S, R, R, R, E,
						X, X, X, X, X,
						S, R, R, R, E},
				new int[]{
						X, R, R, D, X,
						X, U, X, D, X,
						X, U, X, D, X,
						X, U, X, D, X,
						S, U, X, R, E},
				new int[]{
						S, R, D, X, X,
						X, X, R, R, E,
						S, R, U, X, X,
						X, X, R, R, E,
						S, R, U, X, X}
		);
	}
}
