package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.container.SlotTextAdder;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TunerSolver extends SimpleContainerSolver implements SlotTextAdder {
	private static final Logger LOGGER = LoggerFactory.getLogger(TunerSolver.class);

	public static final TunerSolver INSTANCE = new TunerSolver();

	private TunerSolver() {
		super("^Tune Frequency$");
	}

	private static final Item[] COLOR_CYCLE = {
			Items.MAGENTA_DYE, Items.LIGHT_BLUE_DYE, Items.YELLOW_DYE, Items.LIME_DYE,
			Items.PINK_DYE, Items.CYAN_DYE, Items.PURPLE_DYE, Items.LAPIS_LAZULI,
			Items.COCOA_BEANS, Items.GREEN_DYE, Items.RED_DYE, Items.BONE_MEAL,
			Items.ORANGE_DYE
	};

	private static final Item[] GLASS_CYCLE = {
			Items.MAGENTA_STAINED_GLASS_PANE, Items.LIGHT_BLUE_STAINED_GLASS_PANE,
			Items.YELLOW_STAINED_GLASS_PANE, Items.LIME_STAINED_GLASS_PANE,
			Items.PINK_STAINED_GLASS_PANE, Items.CYAN_STAINED_GLASS_PANE,
			Items.PURPLE_STAINED_GLASS_PANE, Items.BLUE_STAINED_GLASS_PANE,
			Items.BROWN_STAINED_GLASS_PANE, Items.GREEN_STAINED_GLASS_PANE,
			Items.RED_STAINED_GLASS_PANE, Items.WHITE_STAINED_GLASS_PANE,
			Items.ORANGE_STAINED_GLASS_PANE
	};

	private static final String[] PITCH_CYCLE = {"Low", "Normal", "High"};
	private static final float[] PITCH_VALUES = {0.0952381f, 0.7936508f, 1.4920635f};

	private static final int[] SPEED_CYCLE = {1, 2, 3, 4, 5};
	private static final int[][] SPEED_RANGES = {
			{50, 64}, // Speed 1
			{40, 49}, // Speed 2
			{30, 39}, // Speed 3
			{20, 29}, // Speed 4
			{10, 19}  // Speed 5
	};

	// Solver results
	private int colorClicks = 0;
	private int speedClicks = 0;
	private int pitchClicks = 0;

	private boolean colorSolved = false;
	private boolean speedSolved = false;
	private boolean pitchSolved = false;

	// Flag to ensure getRequiredClicks runs only once per screen
	private boolean hasProcessed = false;
	private boolean isInMenu = false;

	// Pitch tracking
	private String currentPitch = null;
	private final List<Float> recentPitches = new ArrayList<>();
	private static final int MAX_PITCH_SAMPLES = 5;

	// Target pane movement tracking
	private int lastTargetSlot = -1;
	private int ticksSinceLastMove = 0;
	private int targetSpeed = -1; // Latest target speed from tick interval
	private int lastSpeedTicks = 0;

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().foraging.galatea.enableTunerSolver;
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		if (!hasProcessed) {
			ItemStack dyeStack = slots.get(46);
			if (dyeStack != null && !dyeStack.isEmpty() && isDye(dyeStack.getItem())) {
				if (!colorSolved) {
					colorClicks = computeColorClicks(slots);
					colorSolved = true;
				}
				if (!speedSolved) {
					maybeSolveSpeed(slots);
				}
				if (!pitchSolved) {
					currentPitch = readCurrentPitch(slots);
				}
				hasProcessed = true;
			}
		}
		return List.of();
	}

	@Override
	public void start(GenericContainerScreen screen) {
		resetState();
		isInMenu = true;
		ScreenEvents.afterTick(screen).register(s -> {
			Int2ObjectMap<ItemStack> slots = getSlots(screen);
			trackTargetPaneMovement(slots);
		});
		ScreenEvents.remove(screen).register(s -> resetState());
	}

	@Override
	public void reset() {
		resetState();
	}

	@Override
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		if (!isEnabled()) {
			return List.of();
		}
		if (slotId == 46 && colorSolved) {
			return SlotText.bottomRightList(Text.literal(String.valueOf(colorClicks)).withColor(SlotText.LIGHT_GREEN));
		}
		if (slotId == 48 && speedSolved) {
			return SlotText.bottomRightList(Text.literal(String.valueOf(speedClicks)).withColor(SlotText.LIGHT_GREEN));
		}
		if (slotId == 50 && pitchSolved) {
			return SlotText.bottomRightList(Text.literal(String.valueOf(pitchClicks)).withColor(SlotText.LIGHT_GREEN));
		}
		return List.of();
	}

	/**
	 * Updates the remaining click counters when the corresponding tuner slot
	 * is clicked. The counters are adjusted based on the cycle length of the
	 * element to correctly handle wrapping when clicking through the cycle
	 * multiple times.
	 */
	@Override
	public boolean onClickSlot(int slotId, ItemStack stack, int screenId, int button) {
		if (!SkyblockerConfigManager.get().foraging.galatea.enableTunerSolver) return false;
		if (!isInMenu) return false;

		if (button != 0 && button != 1) return false;

		int delta = button == 0 ? -1 : 1;

		if (colorSolved && slotId == 46) {
			colorClicks = updateClicks(colorClicks, COLOR_CYCLE.length, delta);
		} else if (speedSolved && slotId == 48) {
			speedClicks = updateClicks(speedClicks, SPEED_CYCLE.length, delta);
		} else if (pitchSolved && slotId == 50) {
			pitchClicks = updateClicks(pitchClicks, PITCH_CYCLE.length, delta);
		}
		return false;
	}

	/**
	 * Adjusts the remaining clicks taking into account the cycle length so that
	 * looping through the values keeps the counter accurate.
	 *
	 * @param clicks      current remaining clicks
	 * @param cycleLength length of the cycle (number of options)
	 * @param delta       change in clicks; {@code -1} for decrement, {@code 1} for increment
	 * @return the updated click count
	 */
	private static int updateClicks(int clicks, int cycleLength, int delta) {
		int forward = clicks >= 0 ? clicks : cycleLength + clicks; // distance when moving forward
		forward = (forward + delta + cycleLength) % cycleLength;
		int backward = cycleLength - forward;
		return forward <= backward ? forward : -backward;
	}


	private void resetState() {
		hasProcessed = false;
		isInMenu = false;
		colorSolved = false;
		speedSolved = false;
		pitchSolved = false;
		colorClicks = 0;
		speedClicks = 0;
		pitchClicks = 0;
		currentPitch = null;
		recentPitches.clear();
		lastTargetSlot = -1;
		ticksSinceLastMove = 0;
		targetSpeed = -1;
		lastSpeedTicks = 0;
	}

	private void trackTargetPaneMovement(Int2ObjectMap<ItemStack> slots) {
		int currentTargetSlot = -1;

		// Find the current target pane in slots 10–16
		for (int slot = 10; slot <= 16; slot++) {
			ItemStack stack = slots.get(slot);
			if (stack != null && isStainedGlassPane(stack.getItem())) {
				currentTargetSlot = slot;
				break;
			}
		}

		// Check if the target pane slot has changed
		if (currentTargetSlot != lastTargetSlot && lastTargetSlot != -1) {

			// Calculate target speed from tick interval
			int ticks = ticksSinceLastMove;
			targetSpeed = -1;
			for (int i = 0; i < SPEED_RANGES.length; i++) {
				if (ticks >= SPEED_RANGES[i][0] && ticks <= SPEED_RANGES[i][1]) {
					targetSpeed = SPEED_CYCLE[i];
					break;
				}
			}
			if (targetSpeed == -1) {
				LOGGER.warn("Tick interval {} does not match any speed range", ticks);
			}
			lastSpeedTicks = ticks;

			ticksSinceLastMove = 0;

			if (!speedSolved) {
				maybeSolveSpeed(slots);
			}
		} else if (currentTargetSlot != -1) {
			ticksSinceLastMove++;
		}

		lastTargetSlot = currentTargetSlot;
	}

	/**
	 * Determines the number of clicks needed to match the dye color in slot 46
	 * to the target glass pane color in slots 10–16.
	 *
	 * @param slots map of slot indices to their {@link ItemStack}
	 * @return number of clicks for color (+ for forward, - for backward, 0 if invalid)
	 */
	private static int computeColorClicks(Int2ObjectMap<ItemStack> slots) {

		// Read dye in slot 46
		ItemStack dyeStack = slots.get(46);
		if (dyeStack == null || dyeStack.isEmpty()) {
			LOGGER.warn("No dye found in slot 46");
			return 0;
		}
		Item dyeItem = dyeStack.getItem();
		int dyeIndex = getColorIndex(dyeItem, COLOR_CYCLE);
		if (dyeIndex == -1) {
			LOGGER.warn("Invalid dye item in slot 46: {}", dyeItem);
			return 0;
		}

		// Find the moving glass pane in slots 28–34
		ItemStack movingPane = null;
		int movingSlot = -1;
		for (int slot = 28; slot <= 34; slot++) {
			ItemStack stack = slots.get(slot);
			if (stack != null && isStainedGlassPane(stack.getItem())) {
				movingPane = stack;
				movingSlot = slot;
				break;
			}
		}
		if (movingPane == null) {
			LOGGER.warn("No stained glass pane found in slots 28–34");
			return 0;
		}
		Item movingItem = movingPane.getItem();
		int movingIndex = getColorIndex(movingItem, GLASS_CYCLE);
		if (movingIndex == -1) {
			LOGGER.warn("Invalid glass pane item in slot {}: {}", movingSlot, movingItem);
			return 0;
		}

		// Find the target glass pane in slots 10–16
		ItemStack targetPane = null;
		int targetSlot = -1;
		for (int slot = 10; slot <= 16; slot++) {
			ItemStack stack = slots.get(slot);
			if (stack != null && isStainedGlassPane(stack.getItem())) {
				targetPane = stack;
				targetSlot = slot;
				break;
			}
		}
		if (targetPane == null) {
			LOGGER.warn("No stained glass pane found in slots 10–16");
			return 0;
		}
		Item targetItem = targetPane.getItem();
		int targetIndex = getColorIndex(targetItem, GLASS_CYCLE);
		if (targetIndex == -1) {
			LOGGER.warn("Invalid glass pane item in slot {}: {}", targetSlot, targetItem);
			return 0;
		}

		// Calculate clicks to match dye to target pane
		int clicks = calculateClicks(dyeIndex, targetIndex);
		LOGGER.info("Color solved: Dye={}, Target={}, Required clicks={}",
				dyeStack.getName().getString(),
				targetPane.getName().getString(),
				clicks >= 0 ? "+" + clicks : clicks);
		return clicks;
	}

	public void onSound(PlaySoundS2CPacket packet) {
		if (!SkyblockerConfigManager.get().foraging.galatea.enableTunerSolver
				|| pitchSolved || !Utils.isInGalatea() || !isInMenu
				|| !packet.getSound().value().id().equals(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value().id())) {
			return;
		}

		float packetPitch = packet.getPitch();
		recentPitches.add(packetPitch);
		int sampleCount = recentPitches.size();
		String name = getPitchName(packetPitch);

		if (currentPitch == null) {
			LOGGER.warn("Current pitch not set, cannot compare");
			recentPitches.clear();
			return;
		}

		float expectedPitch = getPitchValue(currentPitch);
		if (Math.abs(packetPitch - expectedPitch) > 0.0001f) {
			String targetPitch = name;
			if (targetPitch == null) {
				LOGGER.warn("Invalid pitch value received: {}", packetPitch);
				recentPitches.clear();
				return;
			}

			int currentIndex = getPitchIndex(currentPitch);
			int targetIndex = getPitchIndex(targetPitch);
			if (currentIndex == -1 || targetIndex == -1) {
				LOGGER.warn("Invalid pitch indices: current={}, target={}", currentPitch, targetPitch);
				recentPitches.clear();
				return;
			}

			int clicks = calculatePitchClicks(currentIndex, targetIndex);
			LOGGER.info("Pitch solved: Current={}, Target={}, Required clicks={}",
					currentPitch, targetPitch, clicks >= 0 ? "+" + clicks : clicks);
			pitchClicks = clicks;
			pitchSolved = true;
			recentPitches.clear();
			return;
		}

		if (sampleCount >= MAX_PITCH_SAMPLES) {
			pitchClicks = 0;
			pitchSolved = true;
			LOGGER.info("Pitch solved: Current={}, Target={}, Required clicks=+0 (all samples match)",
					currentPitch,
					currentPitch);
			recentPitches.clear();
		}
	}

	private static int readCurrentSpeed(Int2ObjectMap<ItemStack> slots) {
		ItemStack speedStack = slots.get(48);
		if (speedStack != null && !speedStack.isEmpty()) {
			List<Text> lore = ItemUtils.getLore(speedStack);
			if (lore.size() >= 4) {
				try {
					String speedText = lore.get(3).getString();
					String[] parts = speedText.split(": ");
					int currentSpeed = Integer.parseInt(parts[1].trim());
					if (currentSpeed >= 1 && currentSpeed <= 5) {
						return currentSpeed;
					}
				} catch (NumberFormatException ignored) {
				}
			}
		}
		return 0;
	}

	private static String readCurrentPitch(Int2ObjectMap<ItemStack> slots) {
		ItemStack pitchStack = slots.get(50);
		if (pitchStack != null && !pitchStack.isEmpty()) {
			List<Text> lore = ItemUtils.getLore(pitchStack);
			if (lore.size() >= 3) {
				String pitchText = lore.get(2).getString();
				if (pitchText.contains("Low")) return "Low";
				if (pitchText.contains("Normal")) return "Normal";
				if (pitchText.contains("High")) return "High";
			}
		}
		return null;
	}

	private void maybeSolveSpeed(Int2ObjectMap<ItemStack> slots) {
		int currentSpeed = readCurrentSpeed(slots);
		if (currentSpeed > 0 && targetSpeed != -1) {
			int currentIndex = getSpeedIndex(currentSpeed);
			int targetIndex = getSpeedIndex(targetSpeed);
			if (currentIndex != -1 && targetIndex != -1) {
				speedClicks = calculateSpeedClicks(currentIndex, targetIndex);
				speedSolved = true;
				LOGGER.info(
						"Speed solved: Current={}, Target={}, Ticks={}, Required clicks={}",
						currentSpeed,
						targetSpeed,
						lastSpeedTicks,
						speedClicks >= 0 ? "+" + speedClicks : speedClicks);
			} else {
				LOGGER.warn("Invalid speed indices: current={}, target={}", currentSpeed, targetSpeed);
			}
		}
	}

	private static int getSpeedIndex(int speed) {
		for (int i = 0; i < SPEED_CYCLE.length; i++) {
			if (SPEED_CYCLE[i] == speed) {
				return i;
			}
		}
		return -1;
	}

	private static int calculateSpeedClicks(int fromIndex, int toIndex) {
		int forward = (toIndex - fromIndex + SPEED_CYCLE.length) % SPEED_CYCLE.length;
		int backward = (fromIndex - toIndex + SPEED_CYCLE.length) % SPEED_CYCLE.length;
		return forward <= backward ? forward : -backward;
	}

	private static float getPitchValue(String pitch) {
		for (int i = 0; i < PITCH_CYCLE.length; i++) {
			if (PITCH_CYCLE[i].equals(pitch)) {
				return PITCH_VALUES[i];
			}
		}
		return 0f;
	}

	private static String getPitchName(float pitch) {
		for (int i = 0; i < PITCH_VALUES.length; i++) {
			if (Math.abs(pitch - PITCH_VALUES[i]) < 0.0001f) {
				return PITCH_CYCLE[i];
			}
		}
		return null;
	}

	private static int getPitchIndex(String pitch) {
		for (int i = 0; i < PITCH_CYCLE.length; i++) {
			if (PITCH_CYCLE[i].equals(pitch)) {
				return i;
			}
		}
		return -1;
	}

	private static int calculatePitchClicks(int fromIndex, int toIndex) {
		int forward = (toIndex - fromIndex + PITCH_CYCLE.length) % PITCH_CYCLE.length;
		int backward = (fromIndex - toIndex + PITCH_CYCLE.length) % PITCH_CYCLE.length;
		return forward <= backward ? forward : -backward;
	}

	private static boolean isDye(Item item) {
		for (Item dye : COLOR_CYCLE) {
			if (item == dye) {
				return true;
			}
		}
		return false;
	}

	private static boolean isStainedGlassPane(Item item) {
		for (Item glass : GLASS_CYCLE) {
			if (item == glass) {
				return true;
			}
		}
		return false;
	}

	private static int getColorIndex(Item item, Item[] cycle) {
		for (int i = 0; i < cycle.length; i++) {
			if (cycle[i] == item) {
				return i;
			}
		}
		return -1;
	}

	private static int calculateClicks(int fromIndex, int toIndex) {
		int forward = (toIndex - fromIndex + COLOR_CYCLE.length) % COLOR_CYCLE.length;
		int backward = (fromIndex - toIndex + COLOR_CYCLE.length) % COLOR_CYCLE.length;
		return forward <= backward ? forward : -backward;
	}

	private static Int2ObjectMap<ItemStack> getSlots(GenericContainerScreen screen) {
		Int2ObjectMap<ItemStack> slots = new Int2ObjectOpenHashMap<>();
		GenericContainerScreenHandler handler = screen.getScreenHandler();
		int containerSize = handler.getRows() * 9;

		for (Slot slot : handler.slots.subList(0, containerSize)) {
			slots.put(slot.id, slot.getStack());
		}
		return slots;
	}
}
