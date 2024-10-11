package de.hysky.skyblocker.skyblock.garden;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.HudRenderEvents;
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
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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
    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);
    private static final Pattern FARMING_XP = Pattern.compile("§3\\+(?<xp>\\d+.?\\d*) Farming \\((?<percent>[\\d,]+.?\\d*)%\\)");
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static CounterType counterType = CounterType.NONE;
    private static final Deque<IntLongPair> counter = new ArrayDeque<>();
    private static final LongPriorityQueue blockBreaks = new LongArrayFIFOQueue();
    private static final Queue<FloatLongPair> farmingXp = new ArrayDeque<>();
    private static float farmingXpPercentProgress;

    @Init
    public static void init() {
        HudRenderEvents.AFTER_MAIN_HUD.register((context, tickCounter) -> {
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

                ItemStack stack = client.player.getMainHandStack();
                if (stack == null || !tryGetCounter(stack, CounterType.CULTIVATING) && !tryGetCounter(stack, CounterType.COUNTER)) {
                    counterType = CounterType.NONE;
                }

                FarmingHudWidget.INSTANCE.update();
                FarmingHudWidget.INSTANCE.render(context, SkyblockerConfigManager.get().uiAndVisuals.tabHud.enableHudBackground);
            }
        });
        ClientPlayerBlockBreakEvents.AFTER.register((world, player, pos, state) -> {
            if (shouldRender()) {
                blockBreaks.enqueue(System.currentTimeMillis());
            }
        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (shouldRender() && overlay) {
                Matcher matcher = FARMING_XP.matcher(message.getString());
                if (matcher.matches()) {
                    try {
                        farmingXp.offer(FloatLongPair.of(NUMBER_FORMAT.parse(matcher.group("xp")).floatValue(), System.currentTimeMillis()));
                        farmingXpPercentProgress = NUMBER_FORMAT.parse(matcher.group("percent")).floatValue();
                    } catch (ParseException e) {
                        LOGGER.error("[Skyblocker Farming HUD] Failed to parse farming xp", e);
                    }
                }
            }
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("hud").then(literal("farming")
                .executes(Scheduler.queueOpenScreenCommand(() -> new FarmingHudConfigScreen(null)))))));
    }

    private static boolean tryGetCounter(ItemStack stack, CounterType counterType) {
        NbtCompound customData = ItemUtils.getCustomData(stack);
        if (customData == null || !customData.contains(counterType.nbtKey, NbtElement.NUMBER_TYPE)) return false;
        int count = customData.getInt(counterType.nbtKey);
        if (FarmingHud.counterType != counterType) {
            counter.clear();
            FarmingHud.counterType = counterType;
        }
        if (counter.isEmpty() || counter.peekLast().leftInt() != count) {
            counter.offer(IntLongPair.of(count, System.currentTimeMillis()));
        }
        return true;
    }

    private static boolean shouldRender() {
        return SkyblockerConfigManager.get().farming.garden.farmingHud.enableHud && client.player != null && Utils.getLocation() == Location.GARDEN;
    }

    public static String counterText() {
        return counterType.text;
    }

    public static int counter() {
        return counter.isEmpty() ? 0 : counter.peekLast().leftInt();
    }

    public static float cropsPerMinute() {
        if (counter.isEmpty()) {
            return 0;
        }
        IntLongPair first = counter.peek();
        IntLongPair last = counter.peekLast();
        return (float) (last.leftInt() - first.leftInt()) / (last.rightLong() - first.rightLong()) * 60_000f;
    }

    public static int blockBreaks() {
        return blockBreaks.size();
    }

    public static float farmingXpPercentProgress() {
        return farmingXpPercentProgress;
    }

    public static double farmingXpPerHour() {
        return farmingXp.stream().mapToDouble(FloatLongPair::leftFloat).sum() * blockBreaks() * 1800; // Hypixel only sends xp updates around every half a second
    }

    public enum CounterType {
        NONE("", "No Counter"),
        COUNTER("mined_crops", "Counter: "),
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
