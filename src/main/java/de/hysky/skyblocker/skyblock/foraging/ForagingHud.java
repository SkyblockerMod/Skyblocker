package de.hysky.skyblocker.skyblock.foraging;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.floats.FloatLongPair;
import it.unimi.dsi.fastutil.ints.IntLongPair;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ForagingHud {
	private static final Logger LOGGER = LoggerFactory.getLogger(ForagingHud.class);
	private static final Identifier FORAGING_HUD = Identifier.of(SkyblockerMod.NAMESPACE, "foraging_hud");
	public static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);

	private static final Pattern FORAGING_XP_PATTERN =
			Pattern.compile("\\+(?<xp>\\d+(?:\\.\\d+)?) Foraging \\((?<percent>[\\d,]+(?:\\.\\d+)?%|[\\d,]+/[\\d,]+)\\)");

	private static final MinecraftClient client = MinecraftClient.getInstance();

	private static CounterType counterType = CounterType.NONE;
	private static final Deque<IntLongPair> counterHistory = new ArrayDeque<>();
	private static int currentCounter = 0;
	private static final LongPriorityQueue blockTimestamps = new LongArrayFIFOQueue();
	private static final Queue<FloatLongPair> xpQueue = new ArrayDeque<>();
	private static float xpPercentProgress = 0f;

	// ←── ADD: Remember last broken log’s Skyblock “Enchanted” ID:
	private static String lastLogSkyblockId = null;

	@Init
	public static void init() {
		HudLayerRegistrationCallback.EVENT.register(d ->
				d.attachLayerAfter(IdentifiedLayer.STATUS_EFFECTS, FORAGING_HUD, (context, tickCounter) -> {
					if (!shouldRender()) return;

					long now = System.currentTimeMillis();
					while (!counterHistory.isEmpty() && counterHistory.peek().rightLong() + 5000 < now) {
						counterHistory.poll();
					}
					while (!blockTimestamps.isEmpty() && blockTimestamps.firstLong() + 1000 < now) {
						blockTimestamps.dequeueLong();
					}
					while (!xpQueue.isEmpty() && xpQueue.peek().rightLong() + 1000 < now) {
						xpQueue.poll();
					}

					ItemStack stack = client.player.getMainHandStack();
					if (stack == null
							|| (tryGetCounter(stack, CounterType.FORESTER) &&
							tryGetCounter(stack, CounterType.COUNTER))) {
						counterType = CounterType.NONE;
					}
				})
		);

		// ←── MODIFY block‐break listener:
		ClientPlayerBlockBreakEvents.AFTER.register((world, player, pos, state) -> {
			if (!shouldRender()) return;

			// Identify broken block
			Block broken = state.getBlock();
			// Is it one of our log types? If so, record and increment:
			String skyId = getEnchantedIdForBlock(broken);
			if (skyId != null) {
				// Update HUD’s internal counter + remember this ID:
				incrementLogs(skyId);
			}
			// Always track timestamp for blocks/sec
			blockTimestamps.enqueue(System.currentTimeMillis());
		});

		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if (!shouldRender() || !overlay) return;
			String stripped = Formatting.strip(message.getString());
			Matcher m = FORAGING_XP_PATTERN.matcher(stripped);
			if (m.matches()) {
				try {
					float xp = NUMBER_FORMAT.parse(m.group("xp")).floatValue();
					xpQueue.offer(FloatLongPair.of(xp, System.currentTimeMillis()));

					String pct = m.group("percent");
					if (pct.endsWith("%")) {
						xpPercentProgress = NUMBER_FORMAT.parse(pct.replace("%", "")).floatValue();
					} else if (pct.contains("/")) {
						String[] parts = pct.split("/");
						float num = NUMBER_FORMAT.parse(parts[0]).floatValue();
						float den = NUMBER_FORMAT.parse(parts[1]).floatValue();
						xpPercentProgress = (num / den) * 100f;
					}
				} catch (ParseException e) {
					LOGGER.error("[Skyblocker Foraging HUD] failed to parse XP", e);
				}
			}
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(
						literal(SkyblockerMod.NAMESPACE)
								.then(literal("hud")
										.then(literal("foraging")
												.executes(Scheduler.queueOpenScreenCommand(
														() -> new WidgetsConfigurationScreen(
																Location.THE_PARK,
																"hud_foraging",
																null
														)
												))
										)
								)
				)
		);
	}

	//───────────────────────────────────────────────────────────────────────────────

	// ←── UPDATED: incrementLogs now takes a skyblock‐style item ID (e.g. "ENCHANTED_OAK_LOG"):
	public static void incrementLogs(String enchantedLogSkyblockId) {
		lastLogSkyblockId = enchantedLogSkyblockId;
		currentCounter++;
		long now = System.currentTimeMillis();
		if (counterHistory.isEmpty() ||
				counterHistory.peekLast().leftInt() != currentCounter) {
			counterHistory.offer(IntLongPair.of(currentCounter, now));
		}
	}

	/** Map a broken Block to the corresponding “ENCHANTED_…_LOG” Skyblock ID, or null if not a log. */
	public static String getEnchantedIdForBlock(Block block) {
		// Hypixel names are typically ENCHANTED_OAK_LOG, etc.
		if (block == net.minecraft.block.Blocks.OAK_LOG) {
			return "ENCHANTED_OAK_LOG";
		} else if (block == net.minecraft.block.Blocks.BIRCH_LOG) {
			return "ENCHANTED_BIRCH_LOG";
		} else if (block == net.minecraft.block.Blocks.SPRUCE_LOG) {
			return "ENCHANTED_SPRUCE_LOG";
		} else if (block == net.minecraft.block.Blocks.JUNGLE_LOG) {
			return "ENCHANTED_JUNGLE_LOG";
		} else if (block == net.minecraft.block.Blocks.ACACIA_LOG) {
			return "ENCHANTED_ACACIA_LOG";
		} else if (block == net.minecraft.block.Blocks.DARK_OAK_LOG) {
			return "ENCHANTED_DARK_OAK_LOG";
		}
		// (Add Nether/End wood types if you want those too.)
		return null;
	}

	private static boolean tryGetCounter(ItemStack stack, CounterType type) {
		NbtCompound data = ItemUtils.getCustomData(stack);
		if (data.isEmpty() || !(data.get(type.nbtKey) instanceof AbstractNbtNumber)) {
			return true;
		}
		int count = data.getInt(type.nbtKey, 0);
		if (counterType != type) {
			counterHistory.clear();
			counterType = type;
		}
		if (counterHistory.isEmpty() || counterHistory.peekLast().leftInt() != count) {
			counterHistory.offer(IntLongPair.of(count, System.currentTimeMillis()));
		}
		return false;
	}

	private static boolean shouldRender() {
		return SkyblockerConfigManager.get().foraging.park.foragingHud.enableHud
				&& client.player != null
				&& Utils.getLocation() == Location.THE_PARK;
	}

	public static String counterText() {
		return counterType.text;
	}

	public static int counter() {
		if (counterType == CounterType.NONE) {
			return currentCounter;
		} else if (!counterHistory.isEmpty()) {
			return counterHistory.peekLast().leftInt();
		} else {
			return 0;
		}
	}

	public static float logsPerMinute() {
		if (counterHistory.size() < 2) return 0f;
		IntLongPair first = counterHistory.peek();
		IntLongPair last = counterHistory.peekLast();
		long dt = last.rightLong() - first.rightLong();
		if (dt <= 0) return 0f;
		return ((float)(last.leftInt() - first.leftInt()) / dt) * 60_000f;
	}

	public static double blockBreaks() {
		if (blockTimestamps.isEmpty()) return 0d;
		long earliest = blockTimestamps.firstLong();
		long latest = blockTimestamps.lastLong();
		long count = blockTimestamps.size();
		double dtSec = (latest - earliest) / 1000.0;
		if (dtSec <= 0) return 0d;
		return Math.round(((count - 1) / dtSec) * 10) / 10d;
	}

	public static float foragingXpPercentProgress() {
		return Math.clamp(xpPercentProgress, 0f, 100f);
	}

	public static double foragingXpPerHour() {
		if (xpQueue.isEmpty()) return 0d;
		float lastXpGain = xpQueue.peek().leftFloat();
		double bps = blockBreaks();
		return Math.round(lastXpGain * bps * 3600 * 10) / 10d;
	}

	// ←── NEW: expose the last broken log’s skyblock ID for pricing:
	public static String getLastLogSkyblockId() {
		return lastLogSkyblockId;
	}

	public enum CounterType {
		NONE("", "Logs: "),
		COUNTER("mined_logs", "Log Counter: "),
		FORESTER("foraged_cultivating", "Forester Counter: ");

		private final String nbtKey, text;
		CounterType(String nbtKey, String text) {
			this.nbtKey = nbtKey;
			this.text = text;
		}
	}
}
