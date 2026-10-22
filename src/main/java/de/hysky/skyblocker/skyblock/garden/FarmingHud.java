package de.hysky.skyblocker.skyblock.garden;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.floats.FloatLongPair;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.WorldEvents;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;


public class FarmingHud {
	private static final long LAUNCH_TIME = System.currentTimeMillis();
	private static final Logger LOGGER = LoggerFactory.getLogger(FarmingHud.class);
	public static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);
	private static final Pattern FARMING_XP = Pattern.compile("\\+(?<xp>\\d+(?:\\.\\d+)?) Farming \\((?:(?<percent>[\\d,]+(?:\\.\\d+)?%)|(?<current>[\\d,]+)/(?<max>[\\d,]+k?))\\)");
	private static final Minecraft client = Minecraft.getInstance();
	private static final int STATS_WINDOW = 5_000;
	private static final int COUNTER_WINDOW = 30_000;

	private static CounterType counterType = CounterType.NONE;
	private static final Deque<LongLongPair> counter = new ArrayDeque<>();
	private static final SimpleRegression counterRegression = new SimpleRegression();
	private static long estimatedCounter = 0;
	private static final LongPriorityQueue blockBreaks = new LongArrayFIFOQueue();
	private static final Queue<FloatLongPair> farmingXp = new ArrayDeque<>();

	private static float farmingXpPercentProgress;

	@Init
	public static void init() {
		ClientTickEvents.END_CLIENT_TICK.register(_ -> {
			if (shouldRender()) {
				long now = System.currentTimeMillis();
				if (!counter.isEmpty() && counter.peek().rightLong() + COUNTER_WINDOW < now) {
					LongLongPair poll = Objects.requireNonNull(counter.poll());
					if (counter.isEmpty()) counterRegression.clear();
					else counterRegression.removeData(poll.rightLong() - LAUNCH_TIME, poll.leftLong());
				}
				if (!blockBreaks.isEmpty() && blockBreaks.firstLong() + STATS_WINDOW < now) {
					blockBreaks.dequeueLong();
				}
				// don't clear if there's only 1 element, as it will just get added back by tryGetCounter anyway
				if (counter.size() > 1 && blockBreaks.isEmpty()) {
					counter.clear();
					counterRegression.clear();
				}
				if (!farmingXp.isEmpty() && farmingXp.peek().rightLong() + STATS_WINDOW < now) {
					farmingXp.poll();
				}

				assert client.player != null;
				ItemStack stack = client.player.getMainHandItem();
				if (tryGetCounter(stack, CounterType.CULTIVATING)) {
					counterType = CounterType.NONE;
				}
				if (counter.size() >= 2 && !blockBreaks.isEmpty() && blockBreaks.lastLong() + 500 > now) {
					Iterator<LongLongPair> iterator = counter.descendingIterator();
					LongLongPair last = iterator.next();
					LongLongPair secondLast = iterator.next();
					long sinceLast = now - last.rightLong();
					double v = (last.leftLong() - secondLast.leftLong()) / (double) (last.rightLong() - secondLast.rightLong());
					estimatedCounter = last.leftLong() + (long) (sinceLast * v);
				}
			}
		});
		ClientPlayerBlockBreakEvents.AFTER.register((_, _, _, _) -> {
			if (shouldRender()) {
				blockBreaks.enqueue(System.currentTimeMillis());
			}
		});
		// Cactus blocks broken with the Cactus Knife do not register above
		// The server replaces the blocks with air.
		WorldEvents.BLOCK_STATE_UPDATE.register((pos, oldState, newState) -> {
			if (client.player == null || client.level == null || !shouldRender()) return;
			if (oldState == null) return; // oldState is null if it gets broken on the client.
			if (!newState.isAir() || !oldState.is(Blocks.CACTUS)) return; // Cactus was replaced with air
			if (!client.level.getBlockState(pos.below()).is(Blocks.CACTUS)) return; // Don't count any blocks above one that was broken.
			if (client.player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > 64) return; // check if within 8 blocks of the player
			if (!client.player.getMainHandItem().getNeuName().startsWith("CACTUS_KNIFE")) return;
			blockBreaks.enqueue(System.currentTimeMillis());
		});

		ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
			if (shouldRender() && overlay) {
				Matcher matcher = FARMING_XP.matcher(ChatFormatting.stripFormatting(message.getString()));
				if (matcher.find()) {
					try {
						farmingXp.offer(FloatLongPair.of(NUMBER_FORMAT.parse(matcher.group("xp")).floatValue(), System.currentTimeMillis()));
						if (matcher.group("percent") != null) {
							farmingXpPercentProgress = NUMBER_FORMAT.parse(matcher.group("percent")).floatValue();
						} else if (matcher.group("current") != null) {
							float max = NUMBER_FORMAT.parse(matcher.group("max")).floatValue();
							if (matcher.group("max").endsWith("k")) max *= 1000;
							farmingXpPercentProgress = NUMBER_FORMAT.parse(matcher.group("current")).floatValue() / max * 100;
						}
					} catch (ParseException e) {
						LOGGER.error("[Skyblocker Farming HUD] Failed to parse farming xp", e);
					}
				}
			}

			return true;
		});
	}

	@SuppressWarnings("SameParameterValue")
	private static boolean tryGetCounter(ItemStack stack, CounterType counterType) {
		CompoundTag customData = ItemUtils.getCustomData(stack);
		if (customData.isEmpty() || !(customData.get(counterType.nbtKey) instanceof NumericTag)) return true;
		long count = customData.getLongOr(counterType.nbtKey, 0);
		if (FarmingHud.counterType != counterType) {
			counter.clear();
			counterRegression.clear();
			FarmingHud.counterType = counterType;
		}
		if (counter.isEmpty() || counter.peekLast().leftLong() != count) {
			long millis = System.currentTimeMillis();
			counter.offer(LongLongPair.of(count, millis));
			estimatedCounter = count;
			counterRegression.addData(millis - LAUNCH_TIME, count);
		}
		return false;
	}

	private static boolean shouldRender() {
		return WidgetManager.isWidgetInCurrentScreen(FarmingHudWidget.getInstance()) && client.player != null && Utils.getLocation() == Location.GARDEN;
	}

	public static CounterType counterType() {
		return counterType;
	}

	public static long counter(boolean useEstimate) {
		if (counter.isEmpty()) return 0;
		return useEstimate ? estimatedCounter : counter.peekLast().leftLong();
	}

	public static float cropsPerMinute() {
		if (counterRegression.getN() < 2) {
			return 0;
		}
		return (float) (counterRegression.getSlope() * 60_000);
	}

	public static double blockBreaks() {
		if (blockBreaks.isEmpty()) {
			return 0;
		}
		long firstTimestamp = blockBreaks.firstLong();
		long lastTimestamp = blockBreaks.lastLong();
		return Math.round((blockBreaks.size() - 1) / (double) (lastTimestamp - firstTimestamp) * 10000) / 10d;
	}

	public static float farmingXpPercentProgress() {
		return Math.clamp(farmingXpPercentProgress, 0, 100);
	}

	public static double farmingXpPerHour() {
		if (farmingXp.isEmpty()) {
			return 0;
		}
		return Math.round(farmingXp.peek().leftFloat() * blockBreaks() * 3600 * 10) / 10d;
	}

	public enum CounterType {
		NONE("", "Cultivating Needed"),
		CULTIVATING("farmed_cultivating", "Cultivating: ");

		private final String nbtKey;
		public final String text;

		CounterType(String nbtKey, String text) {
			this.nbtKey = nbtKey;
			this.text = text;
		}
	}
}
