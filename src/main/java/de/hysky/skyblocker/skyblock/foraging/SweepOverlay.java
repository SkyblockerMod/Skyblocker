package de.hysky.skyblocker.skyblock.foraging;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.ItemCooldowns;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static float[] colorComponents;
	private static final int MAX_WOOD_CAP = 35;
	private static final Pattern SWEEP_VALUE_PATTERN = Pattern.compile("Sweep:\\s*(?:∮|§[0-9a-fk-or])*(\\d+)");
	private static boolean sweepStatNoticeShown = false;
	private static final Set<String> VALID_AXES = Set.of(
			"JUNGLE_AXE", "TREECAPITATOR_AXE", "FIG_AXE", "FIGSTONE_AXE",
			"ROOKIE_AXE", "PROMISING_AXE", "SWEET_AXE", "EFFICIENT_AXE"
	);
	private static final Set<String> THROWABLE_AXES = Set.of(
			"FIG_AXE", "FIGSTONE_AXE", "JUNGLE_AXE", "TREECAPITATOR_AXE"
	);

	private static final BlockPos[] NEIGHBOR_OFFSETS = {
			new BlockPos(-1, -1, -1), new BlockPos(-1, -1, 0), new BlockPos(-1, -1, 1),
			new BlockPos(-1, 0, -1),  new BlockPos(-1, 0, 0),  new BlockPos(-1, 0, 1),
			new BlockPos(-1, 1, -1),  new BlockPos(-1, 1, 0),  new BlockPos(-1, 1, 1),

			new BlockPos(0, -1, -1),  new BlockPos(0, -1, 0),  new BlockPos(0, -1, 1),
			new BlockPos(0, 0, -1),   						   new BlockPos(0, 0, 1),
			new BlockPos(0, 1, -1),   new BlockPos(0, 1, 0),   new BlockPos(0, 1, 1),

			new BlockPos(1, -1, -1),  new BlockPos(1, -1, 0),  new BlockPos(1, -1, 1),
			new BlockPos(1, 0, -1),   new BlockPos(1, 0, 0),   new BlockPos(1, 0, 1),
			new BlockPos(1, 1, -1),   new BlockPos(1, 1, 0),   new BlockPos(1, 1, 1)
	};

	private static final Map<Block, Float> TOUGHNESS_MAP = Map.of(
			Blocks.STRIPPED_SPRUCE_LOG, 7.0f,
			Blocks.STRIPPED_SPRUCE_WOOD, 7.0f,
			Blocks.MANGROVE_LOG, 50.0f,
			Blocks.MANGROVE_WOOD, 50.0f
	);

	@Init
	public static void init() {
		configCallback(SkyblockerConfigManager.get().foraging.sweepOverlay.sweepOverlayColor);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(SweepOverlay::render);
	}

	private static boolean isValidLocation() {
		return Utils.isInGalatea() || Utils.isInPark() || Utils.isInHub() || Utils.isInPrivateIsland();
	}

	/**
	 * Entry point from the world renderer. Displays colored boxes around
	 * logs that will be destroyed when sweeping.
	 * <p>
	 * If a throwable axe is equipped and the "Thrown Ability Overlay" option
	 * is enabled, a ray trace up to 50 blocks is performed to highlight logs
	 * at the target point. This ray-cast overlay is skipped while the axe's
	 * ability is on cooldown.
	 *
	 * @param wrc the world render context
	 */
	private static void render(WorldRenderContext wrc) {
		var config = SkyblockerConfigManager.get().foraging.sweepOverlay;
		if (!isValidLocation() || !config.enableSweepOverlay || CLIENT.player == null || CLIENT.world == null) {
			return;
		}

		ItemStack heldItem = CLIENT.player.getMainHandStack();
		String itemId = ItemUtils.getItemId(heldItem);
		boolean isValidAxe = VALID_AXES.contains(itemId);
		boolean isThrowableAxe = THROWABLE_AXES.contains(itemId);
		if (!isValidAxe && !isThrowableAxe) {
			return;
		}

		BlockHitResult blockHitResult = null;
		boolean isThrown = false;

		if (isValidAxe && CLIENT.crosshairTarget != null && CLIENT.crosshairTarget.getType() == HitResult.Type.BLOCK
				&& CLIENT.crosshairTarget instanceof BlockHitResult hitResult) {
			blockHitResult = hitResult;
		} else if (isThrowableAxe && config.enableThrownAbilityOverlay && !ItemCooldowns.isOnCooldown(heldItem)) {
			// Cast a ray up to 50 blocks for throwable axes
			// #todo gravity prediction
			Vec3d start = CLIENT.player.getCameraPosVec(1.0f);
			Vec3d look = CLIENT.player.getRotationVec(1.0f);
			Vec3d end = start.add(look.multiply(50.0));
			RaycastContext context = new RaycastContext(
					start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, CLIENT.player
			);
			HitResult hitResult = CLIENT.world.raycast(context);
			if (hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult rayHitResult) {
				blockHitResult = rayHitResult;
				isThrown = true;
			}
		}

		if (blockHitResult != null) {
			BlockState state = CLIENT.world.getBlockState(blockHitResult.getBlockPos());
			if (isLog(state)) {
				renderConnectedLogs(wrc, blockHitResult, state, isThrown);
			}
		}
	}

	/**
	 * Checks if a block is a log or wood type that can be chopped.
	 *
	 * @param state the block to check
	 * @return true if the block is a valid log or wood type
	 */
	private static boolean isLog(BlockState state) {
		if (Utils.isInGalatea()) {
			return state.isOf(Blocks.STRIPPED_SPRUCE_LOG)
					|| state.isOf(Blocks.STRIPPED_SPRUCE_WOOD)
					|| state.isOf(Blocks.MANGROVE_LOG)
					|| state.isOf(Blocks.MANGROVE_WOOD);
		} else if (Utils.isInHub()) {
			return state.isOf(Blocks.OAK_LOG) || state.isOf(Blocks.OAK_WOOD);
		}

		return state.isIn(BlockTags.LOGS);
	}

	/**
	 * Retrieves the player's Sweep stat.
	 * <p>
	 * The value is parsed from the tab list when available. If it cannot be
	 * found, an informational chat message is sent once asking the player to
	 * update their tab list using <code>/tablist</code>.
	 *
	 * @return the Sweep stat as a float
	 */
	private static float getSweepStat() {
		if (CLIENT.player == null) {
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
		if (!sweepStatNoticeShown && Utils.isInPark() && CLIENT.player != null) {
			CLIENT.player.sendMessage(Constants.PREFIX.get().append(
							Text.translatable("skyblocker.config.foraging.sweepOverlay.sweepStatMissingMessage")
									.formatted(Formatting.RED)),
					false);
			sweepStatNoticeShown = true;
		}

		return 0.0f;
	}

	/**
	 * Calculates the maximum number of logs that can be chopped based on Sweep stat and toughness.
	 * A hard cap of {@value #MAX_WOOD_CAP} logs is enforced.
	 *
	 * @param sweepStat the player's Sweep stat
	 * @param toughness the toughness of the log
	 * @return the maximum number of logs that can be broken
	 */
	private static int calculateMaxWood(float sweepStat, float toughness) {
		if (toughness <= 0) {
			return Math.min(MAX_WOOD_CAP, (int) Math.floor(sweepStat));
		}

		int logs = (int) Math.floor(sweepStat / toughness);
		return Math.min(MAX_WOOD_CAP, logs);
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
	 * Highlights all logs connected to the targeted block. A breadth-first
	 * search continues until the calculated maximum wood count is reached.
	 * <p>
	 * When triggered via a thrown axe, the overlay is drawn with a dimmer
	 * color and the blocks broken is halved.
	 *
	 * @param wrc           the world render context
	 * @param blockHitResult the block hit result from the crosshair or ray cast
	 * @param state         the block state of the targeted block
	 * @param isThrown      true if the hit comes from a ray cast (throwable axe)
	 */
	private static void renderConnectedLogs(WorldRenderContext wrc, BlockHitResult blockHitResult, BlockState state, boolean isThrown) {
		BlockPos startPos = blockHitResult.getBlockPos();
		World world = CLIENT.world;
		float sweepStat = getSweepStat();
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
		if (isThrown) {
			maxWood *= 0.5f;
		}

		queue.add(startPos);
		visited.add(startPos);

		while (woodCount < maxWood && !queue.isEmpty()) {
			BlockPos pos = queue.poll();
			BlockState currentState = world.getBlockState(pos);
			if (!isLog(currentState)) continue;

			woodCount++;
			RenderHelper.renderFilled(wrc, pos, renderColor, renderColor[3], false);

			for (BlockPos offset : NEIGHBOR_OFFSETS) {
				BlockPos neighbor = pos.add(offset);
				if (visited.contains(neighbor) || queue.contains(neighbor)) continue;

				if (isLog(world.getBlockState(neighbor))) {
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
