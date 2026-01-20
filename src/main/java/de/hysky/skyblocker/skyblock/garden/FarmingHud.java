package de.hysky.skyblocker.skyblock.garden;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.WorldEvents;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.floats.FloatLongPair;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
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

public class FarmingHud {
	private static final Logger LOGGER = LoggerFactory.getLogger(FarmingHud.class);
	private static final Identifier FARMING_HUD = SkyblockerMod.id("farming_hud");
	public static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);
	private static final Pattern FARMING_XP = Pattern.compile("\\+(?<xp>\\d+(?:\\.\\d+)?) Farming \\((?<percent>[\\d,]+(?:\\.\\d+)?%|[\\d,]+/[\\d,]+)\\)");
	private static final Minecraft client = Minecraft.getInstance();
	private static CounterType counterType = CounterType.NONE;
	private static final Deque<LongLongPair> counter = new ArrayDeque<>();
	private static final LongPriorityQueue blockBreaks = new LongArrayFIFOQueue();
	private static final Queue<FloatLongPair> farmingXp = new ArrayDeque<>();
	private static float farmingXpPercentProgress;

	@Init
	public static void init() {
		HudElementRegistry.attachElementAfter(VanillaHudElements.STATUS_EFFECTS, FARMING_HUD, (context, tickCounter) -> {
			if (shouldRender()) {
				if (!counter.isEmpty() && counter.peek().rightLong() + 5000 < System.currentTimeMillis()) {
					counter.poll();
				}
				if (!blockBreaks.isEmpty() && blockBreaks.firstLong() + 1000 < System.currentTimeMillis()) {
					blockBreaks.dequeueLong();
				}
				if (!farmingXp.isEmpty() && farmingXp.peek().rightLong() + 1000 < System.currentTimeMillis()) {
					farmingXp.poll();
				}

				assert client.player != null;
				ItemStack stack = client.player.getMainHandItem();
				if (tryGetCounter(stack, CounterType.CULTIVATING)) {
					counterType = CounterType.NONE;
				}
			}
		});
		ClientPlayerBlockBreakEvents.AFTER.register((world, player, pos, state) -> {
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
			if (!client.player.getMainHandItem().getNeuName().equals("CACTUS_KNIFE")) return;
			blockBreaks.enqueue(System.currentTimeMillis());
		});

		ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
			if (shouldRender() && overlay) {
				Matcher matcher = FARMING_XP.matcher(ChatFormatting.stripFormatting(message.getString()));
				if (matcher.find()) {
					try {
						farmingXp.offer(FloatLongPair.of(NUMBER_FORMAT.parse(matcher.group("xp")).floatValue(), System.currentTimeMillis()));
						farmingXpPercentProgress = NUMBER_FORMAT.parse(matcher.group("percent")).floatValue();
					} catch (ParseException e) {
						LOGGER.error("[Skyblocker Farming HUD] Failed to parse farming xp", e);
					}
				}
			}

			return true;
		});
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("hud").then(literal("farming")
				.executes(Scheduler.queueOpenScreenCommand(() -> new WidgetsConfigurationScreen(Location.GARDEN, "hud_garden", null)))))));
	}

	private static boolean tryGetCounter(ItemStack stack, CounterType counterType) {
		CompoundTag customData = ItemUtils.getCustomData(stack);
		if (customData.isEmpty() || !(customData.get(counterType.nbtKey) instanceof NumericTag)) return true;
		long count = customData.getLongOr(counterType.nbtKey, 0);
		if (FarmingHud.counterType != counterType) {
			counter.clear();
			FarmingHud.counterType = counterType;
		}
		if (counter.isEmpty() || counter.peekLast().leftLong() != count) {
			counter.offer(LongLongPair.of(count, System.currentTimeMillis()));
		}
		return false;
	}

	private static boolean shouldRender() {
		return SkyblockerConfigManager.get().farming.garden.farmingHud.enableHud && client.player != null && Utils.getLocation() == Location.GARDEN;
	}

	public static String counterText() {
		return counterType.text;
	}

	public static long counter() {
		return counter.isEmpty() ? 0 : counter.peekLast().leftLong();
	}

	public static float cropsPerMinute() {
		if (counter.isEmpty()) {
			return 0;
		}
		LongLongPair first = counter.peek();
		LongLongPair last = counter.peekLast();
		return (float) (last.leftLong() - first.leftLong()) / (last.rightLong() - first.rightLong()) * 60_000f;
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
		NONE("", "No Cultivating Counter"),
		CULTIVATING("farmed_cultivating", "Cultivating Counter: ");

		private final String nbtKey;
		private final String text;

		CounterType(String nbtKey, String text) {
			this.nbtKey = nbtKey;
			this.text = text;
		}

		public boolean matchesText(String textToMatch) {
			return this.text.equals(textToMatch);
		}
	}
}
