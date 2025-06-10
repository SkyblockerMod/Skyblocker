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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
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
	private static final int DEFAULT_MAX_WOOD = 0; // Default if Sweep stat is unavailable
	private static final Pattern SWEEP_VALUE_PATTERN = Pattern.compile("Sweep:\\s*(?:∮|§[0-9a-fk-or])*(\\d+)");
	private static final HashMap<Block, Float> TOUGHNESS_MAP = new HashMap<>();

	static {
		TOUGHNESS_MAP.put(Blocks.STRIPPED_SPRUCE_LOG, 7.0f); // FIG
		TOUGHNESS_MAP.put(Blocks.STRIPPED_SPRUCE_WOOD, 7.0f);
		TOUGHNESS_MAP.put(Blocks.MANGROVE_LOG, 50.0f); // MANGROVE
		TOUGHNESS_MAP.put(Blocks.MANGROVE_WOOD, 50.0f);
		// Add other wood types and their toughness values as needed
	}

	@Init
	public static void init() {
		configCallback(SkyblockerConfigManager.get().foraging.sweepOverlay.sweepOverlayColor);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(SweepOverlay::render);
	}

	private static void render(WorldRenderContext wrc) {
		if ((Utils.isInGalatea() || Utils.isInPark() || Utils.isInHub() || Utils.isInPrivateIsland())
				&& SkyblockerConfigManager.get().foraging.sweepOverlay.enableSweepOverlay
				&& client.player != null && client.world != null && client.crosshairTarget != null
				&& client.crosshairTarget.getType() == HitResult.Type.BLOCK) {

			ItemStack heldItem = client.player.getMainHandStack();
			String heldItemId = ItemUtils.getItemId(heldItem);
			if ((heldItemId.equals("JUNGLE_AXE") || heldItemId.equals("TREECAPITATOR_AXE") ||
				heldItemId.equals("FIG_AXE") || heldItemId.equals("FIGSTONE_AXE") ||
				heldItemId.equals("ROOKIE_AXE") || heldItemId.equals("PROMISING_AXE") ||
				heldItemId.equals("SWEET_AXE") || heldItemId.equals("EFFICIENT_AXE")
			)
							&& client.crosshairTarget instanceof BlockHitResult blockHitResult) {
				BlockState state = client.world.getBlockState(blockHitResult.getBlockPos());
				if (isLog(state.getBlock())) {
					renderConnectedLogs(wrc, blockHitResult, state);
				}
			}
		}
	}

	private static boolean isLog(Block block) {
		return block == Blocks.OAK_LOG ||
				block == Blocks.SPRUCE_LOG ||
				block == Blocks.BIRCH_LOG ||
				block == Blocks.JUNGLE_LOG ||
				block == Blocks.ACACIA_LOG ||
				block == Blocks.CHERRY_LOG ||
				block == Blocks.DARK_OAK_LOG ||
				block == Blocks.PALE_OAK_LOG ||
				block == Blocks.MANGROVE_LOG ||
				block == Blocks.MANGROVE_ROOTS ||
				block == Blocks.MUDDY_MANGROVE_ROOTS ||
				block == Blocks.STRIPPED_SPRUCE_LOG ||
				block == Blocks.STRIPPED_BIRCH_LOG ||
				block == Blocks.STRIPPED_JUNGLE_LOG ||
				block == Blocks.STRIPPED_ACACIA_LOG ||
				block == Blocks.STRIPPED_CHERRY_LOG ||
				block == Blocks.STRIPPED_DARK_OAK_LOG ||
				block == Blocks.STRIPPED_PALE_OAK_LOG ||
				block == Blocks.STRIPPED_OAK_LOG ||
				block == Blocks.STRIPPED_MANGROVE_LOG ||
				block == Blocks.STRIPPED_BAMBOO_BLOCK ||
				block == Blocks.OAK_WOOD ||
				block == Blocks.SPRUCE_WOOD ||
				block == Blocks.BIRCH_WOOD ||
				block == Blocks.JUNGLE_WOOD ||
				block == Blocks.ACACIA_WOOD ||
				block == Blocks.CHERRY_WOOD ||
				block == Blocks.DARK_OAK_WOOD ||
				block == Blocks.MANGROVE_WOOD ||
				block == Blocks.STRIPPED_OAK_WOOD ||
				block == Blocks.STRIPPED_SPRUCE_WOOD ||
				block == Blocks.STRIPPED_BIRCH_WOOD ||
				block == Blocks.STRIPPED_JUNGLE_WOOD ||
				block == Blocks.STRIPPED_ACACIA_WOOD ||
				block == Blocks.STRIPPED_CHERRY_WOOD ||
				block == Blocks.STRIPPED_DARK_OAK_WOOD ||
				block == Blocks.STRIPPED_PALE_OAK_WOOD ||
				block == Blocks.STRIPPED_MANGROVE_WOOD;
	}

	/**
	 * Attempts to retrieve the Sweep stat from the tab list using PlayerListManager.
	 * Returns the raw Sweep stat value as a float.
	 */
	private static float getSweepStat() {
		if (client.player == null) {
			return 0.0f;
		}

		List<String> playerList = PlayerListManager.getPlayerStringList();
		if (playerList == null || playerList.isEmpty()) {
			return 0.0f;
		}

		// Find the entry containing "Sweep:" and parse the value
		for (String entry : playerList) {
			Matcher matcher = SWEEP_VALUE_PATTERN.matcher(entry);
			if (matcher.find()) {
				try {
					return Float.parseFloat(matcher.group(1));
				} catch (NumberFormatException e) {
					LOGGER.warn("Failed to parse Sweep stat from tab list: {}", entry);
				}
			}
		}

		// Support for pre foraging update
		ItemStack heldItem = client.player.getMainHandStack();
		String itemId = heldItem.getSkyblockId();
		if (itemId.equals("JUNGLE_AXE")) {
			return 10.0f;
		} else if (itemId.equals("TREECAPITATOR_AXE")) {
			return 35.0f;
		}

		return 0.0f;
	}

	/**
	 * Calculates the maximum number of logs that can be chopped based on Sweep stat and toughness.
	 */
	private static int calculateMaxWood(float sweepStat, float toughness) {
		if (toughness <= 0) return DEFAULT_MAX_WOOD;
		float logs = (sweepStat / toughness); // Change when someone smarter than me figures out the formula
		return (int) Math.floor(logs);
	}

	/**
	 * Gets the toughness value for a given block state.
	 */
	private static float getToughness(BlockState state) {
		Block block = state.getBlock();
		return TOUGHNESS_MAP.getOrDefault(block, 1.0f); // Default toughness of 1 if not specified
	}

	private static void renderConnectedLogs(WorldRenderContext wrc, BlockHitResult blockHitResult, BlockState state) {
		BlockPos startPos = blockHitResult.getBlockPos();
		World world = client.world;
		HashSet<BlockPos> visited = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		int woodCount = 0;
		float sweepStat = getSweepStat();
		float toughness = getToughness(state);
		int maxWood = (sweepStat > 0) ? calculateMaxWood(sweepStat, toughness) : DEFAULT_MAX_WOOD;

		queue.add(startPos);

		while (woodCount < maxWood && !queue.isEmpty()) {
			BlockPos pos = queue.poll();
			if (!visited.add(pos)) continue;

			BlockState currentState = world.getBlockState(pos);
			if (!isLog(currentState.getBlock())) continue;

			woodCount++;
			RenderHelper.renderFilled(wrc, pos, colorComponents, colorComponents[3], false);

			for (int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 1; y++) {
					for (int z = -1; z <= 1; z++) {
						if (x == 0 && y == 0 && z == 0) continue;
						BlockPos neighbor = pos.add(x, y, z);
						if (visited.contains(neighbor) || queue.contains(neighbor)) continue;

						BlockState neighborState = world.getBlockState(neighbor);
						if (isLog(neighborState.getBlock())) {
							queue.add(neighbor);
						}
					}
				}
			}
		}
	}

	public static void configCallback(Color color) {
		colorComponents = color.getRGBComponents(null);
	}
}
