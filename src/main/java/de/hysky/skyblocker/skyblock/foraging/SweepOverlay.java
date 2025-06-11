package de.hysky.skyblocker.skyblock.foraging;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SweepOverlay {
	private static final Logger LOGGER = LoggerFactory.getLogger(SweepOverlay.class);
	private static final MinecraftClient client = MinecraftClient.getInstance();
	private static float[] colorComponents;
	private static final int DEFAULT_MAX_WOOD = 0;
	private static final Pattern SWEEP_VALUE_PATTERN = Pattern.compile("Sweep:\\s*(?:∮|§[0-9a-fk-or])*(\\d+)");
	private static final HashMap<Block, Float> TOUGHNESS_MAP = new HashMap<>();
	private static final Set<Block> LOG_BLOCKS = Set.of(
			Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG,
			Blocks.ACACIA_LOG, Blocks.CHERRY_LOG, Blocks.DARK_OAK_LOG, Blocks.PALE_OAK_LOG,
			Blocks.MANGROVE_LOG, Blocks.MANGROVE_ROOTS, Blocks.MUDDY_MANGROVE_ROOTS,
			Blocks.STRIPPED_SPRUCE_LOG, Blocks.STRIPPED_BIRCH_LOG, Blocks.STRIPPED_JUNGLE_LOG,
			Blocks.STRIPPED_ACACIA_LOG, Blocks.STRIPPED_CHERRY_LOG, Blocks.STRIPPED_DARK_OAK_LOG,
			Blocks.STRIPPED_PALE_OAK_LOG, Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_MANGROVE_LOG,
			Blocks.STRIPPED_BAMBOO_BLOCK, Blocks.OAK_WOOD, Blocks.SPRUCE_WOOD, Blocks.BIRCH_WOOD,
			Blocks.JUNGLE_WOOD, Blocks.ACACIA_WOOD, Blocks.CHERRY_WOOD, Blocks.DARK_OAK_WOOD,
			Blocks.MANGROVE_WOOD, Blocks.STRIPPED_OAK_WOOD, Blocks.STRIPPED_SPRUCE_WOOD,
			Blocks.STRIPPED_BIRCH_WOOD, Blocks.STRIPPED_JUNGLE_WOOD, Blocks.STRIPPED_ACACIA_WOOD,
			Blocks.STRIPPED_CHERRY_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD, Blocks.STRIPPED_PALE_OAK_WOOD,
			Blocks.STRIPPED_MANGROVE_WOOD
	);
	private static final Set<String> VALID_AXES = Set.of(
			"JUNGLE_AXE", "TREECAPITATOR_AXE", "FIG_AXE", "FIGSTONE_AXE",
			"ROOKIE_AXE", "PROMISING_AXE", "SWEET_AXE", "EFFICIENT_AXE"
	);
	private static final Map<String, Float> FALLBACK_SWEEP_VALUES = Map.of(
			"JUNGLE_AXE", 10.0f,
			"TREECAPITATOR_AXE", 35.0f
	);
	private static final BlockPos[] NEIGHBOR_OFFSETS = {
			new BlockPos(-1, -1, -1), new BlockPos(-1, -1, 0), new BlockPos(-1, -1, 1),
			new BlockPos(-1, 0, -1),  new BlockPos(-1, 0, 0),  new BlockPos(-1, 0, 1),
			new BlockPos(-1, 1, -1),  new BlockPos(-1, 1, 0),  new BlockPos(-1, 1, 1),
			new BlockPos(0, -1, -1),  new BlockPos(0, -1, 0),  new BlockPos(0, -1, 1),
			new BlockPos(0, 0, -1),   new BlockPos(0, 0, 1),
			new BlockPos(0, 1, -1),   new BlockPos(0, 1, 0),   new BlockPos(0, 1, 1),
			new BlockPos(1, -1, -1),  new BlockPos(1, -1, 0),  new BlockPos(1, -1, 1),
			new BlockPos(1, 0, -1),   new BlockPos(1, 0, 0),   new BlockPos(1, 0, 1),
			new BlockPos(1, 1, -1),   new BlockPos(1, 1, 0),   new BlockPos(1, 1, 1)
	};

	static {
		TOUGHNESS_MAP.put(Blocks.STRIPPED_SPRUCE_LOG, 7.0f);
		TOUGHNESS_MAP.put(Blocks.STRIPPED_SPRUCE_WOOD, 7.0f);
		TOUGHNESS_MAP.put(Blocks.MANGROVE_LOG, 50.0f);
		TOUGHNESS_MAP.put(Blocks.MANGROVE_WOOD, 50.0f);
	}

	@Init
	public static void init() {
		configCallback(SkyblockerConfigManager.get().foraging.sweepOverlay.sweepOverlayColor);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(SweepOverlay::render);
	}

	private static boolean isValidLocation() {
		return Utils.isInGalatea() || Utils.isInPark() || Utils.isInHub() || Utils.isInPrivateIsland();
	}

	/**
	 * Renders an overlay for logs that can be chopped when holding a valid axe.
	 * If the crosshair doesn't target a block, casts a ray up to 50 blocks and highlights logs with half the sweep value and a modified color.
	 *
	 * @param wrc the world render context
	 */
	private static void render(WorldRenderContext wrc) {
		var config = SkyblockerConfigManager.get().foraging.sweepOverlay;
		if (!isValidLocation() || !config.enableSweepOverlay || client.player == null || client.world == null) {
			return;
		}

		ItemStack heldItem = client.player.getMainHandStack();
		String itemId = ItemUtils.getItemId(heldItem);
		if (!VALID_AXES.contains(itemId)) {
			return;
		}

		BlockHitResult blockHitResult = null;
		boolean isThrown = false;

		if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.BLOCK
				&& client.crosshairTarget instanceof BlockHitResult hitResult) {
			blockHitResult = hitResult;
		} else {
			// Cast a ray up to 50 blocks
			Vec3d start = client.player.getCameraPosVec(1.0f);
			Vec3d look = client.player.getRotationVec(1.0f);
			Vec3d end = start.add(look.multiply(50.0));
			RaycastContext context = new RaycastContext(
					start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, client.player
			);
			HitResult hitResult = client.world.raycast(context);
			if (hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult rayHitResult) {
				blockHitResult = rayHitResult;
				isThrown = true;
			}
		}

		if (blockHitResult != null) {
			BlockState state = client.world.getBlockState(blockHitResult.getBlockPos());
			if (isLog(state.getBlock())) {
				renderConnectedLogs(wrc, blockHitResult, state, isThrown);
			}
		}
	}

	/**
	 * Checks if a block is a log or wood type that can be chopped.
	 *
	 * @param block the block to check
	 * @return true if the block is a valid log or wood type
	 */
	private static boolean isLog(Block block) {
		return LOG_BLOCKS.contains(block);
	}

	/**
	 * Retrieves the Sweep stat from the player list or falls back to item-based values.
	 *
	 * @return the Sweep stat as a float
	 */
	private static float getSweepStat() {
		if (client.player == null) {
			return 0.0f;
		}

		List<String> playerList = PlayerListManager.getPlayerStringList();
		if (playerList != null) {
			for (String entry : playerList) {
				Matcher matcher = SWEEP_VALUE_PATTERN.matcher(entry);
				if (matcher.find()) {
					try {
						return Float.parseFloat(matcher.group(1));
					} catch (NumberFormatException e) {
						LOGGER.warn("Failed to parse Sweep stat from tab list: {}. Error: {}", entry, e.getMessage());
					}
				}
			}
		}

		String itemId = client.player.getMainHandStack().getSkyblockId();
		return FALLBACK_SWEEP_VALUES.getOrDefault(itemId, 0.0f);
	}

	/**
	 * Calculates the maximum number of logs that can be chopped based on Sweep stat and toughness.
	 *
	 * @param sweepStat the player's Sweep stat
	 * @param toughness the toughness of the log
	 * @return the maximum number of logs
	 */
	private static int calculateMaxWood(float sweepStat, float toughness) {
		if (toughness <= 0) return DEFAULT_MAX_WOOD;
		return (int) Math.floor(sweepStat / toughness);
	}

	/**
	 * Gets the toughness value for a given block state.
	 *
	 * @param state the block state
	 * @return the toughness value, defaulting to 1.0 if not specified
	 */
	private static float getToughness(BlockState state) {
		return TOUGHNESS_MAP.getOrDefault(state.getBlock(), 1.0f);
	}

	/**
	 * Renders an overlay highlighting connected logs that can be chopped based on the player's Sweep stat.
	 * For ray-cast hits, uses half the sweep stat and a dimmer color.
	 *
	 * @param wrc           the world render context
	 * @param blockHitResult the block hit result from the crosshair or ray cast
	 * @param state         the block state of the targeted block
	 * @param isThrown      true if the hit comes from a ray cast
	 */
	private static void renderConnectedLogs(WorldRenderContext wrc, BlockHitResult blockHitResult, BlockState state, boolean isThrown) {
		BlockPos startPos = blockHitResult.getBlockPos();
		World world = client.world;
		float sweepStat = getSweepStat();
		if (isThrown) {
			sweepStat *= 0.5f; // Halve the sweep stat for ray-cast hits
		}
		if (sweepStat <= 0) return;

		// Adjust color for ray-cast hits (dimmer: multiply RGB by 0.7, keep alpha)
		float[] renderColor = colorComponents;
		if (isThrown) {
			renderColor = new float[] {
					colorComponents[0] * 0.7f,
					colorComponents[1] * 0.7f,
					colorComponents[2] * 0.7f,
					colorComponents[3]
			};
		}

		HashSet<BlockPos> visited = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		int woodCount = 0;
		float toughness = getToughness(state);
		int maxWood = calculateMaxWood(sweepStat, toughness);

		queue.add(startPos);
		visited.add(startPos);

		while (woodCount < maxWood && !queue.isEmpty()) {
			BlockPos pos = queue.poll();
			BlockState currentState = world.getBlockState(pos);
			if (!isLog(currentState.getBlock())) continue;

			woodCount++;
			RenderHelper.renderFilled(wrc, pos, renderColor, renderColor[3], false);

			for (BlockPos offset : NEIGHBOR_OFFSETS) {
				BlockPos neighbor = pos.add(offset);
				if (visited.contains(neighbor) || queue.contains(neighbor)) continue;

				if (isLog(world.getBlockState(neighbor).getBlock())) {
					queue.add(neighbor);
					visited.add(neighbor);
				}
			}
		}
	}


	public static void configCallback(Color color) {
		colorComponents = color.getRGBComponents(null);
	}
}
