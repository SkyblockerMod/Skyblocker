package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
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

public class TunerSolver {
	private static final Logger LOGGER = LoggerFactory.getLogger(TunerSolver.class);

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
        private static int colorClicks = 0;
        private static int speedClicks = 0;
        private static int pitchClicks = 0;

        private static boolean colorSolved = false;
        private static boolean speedSolved = false;
        private static boolean pitchSolved = false;

        public static boolean isColorSolved() {
                return colorSolved;
        }

        public static boolean isSpeedSolved() {
                return speedSolved;
        }

        public static boolean isPitchSolved() {
                return pitchSolved;
        }

        public static int getColorClicks() {
                return colorClicks;
        }

        public static int getSpeedClicks() {
                return speedClicks;
        }

        public static int getPitchClicks() {
                return pitchClicks;
        }

	// Flag to ensure getRequiredClicks runs only once per screen
	private static boolean hasProcessed = false;
	private static boolean isInMenu = false;
	private static int tickCounter = 0;
	private static final int TIMEOUT_TICKS = 100; // ~5 seconds

	// Pitch tracking
	private static String currentPitch = null;
	private static final List<Float> recentPitches = new ArrayList<>();
	private static final int MAX_PITCH_SAMPLES = 5;

	// Target pane movement tracking
        private static int lastTargetSlot = -1;
        private static int ticksSinceLastMove = 0;
        private static int targetSpeed = -1; // Latest target speed from tick interval

	@Init
	public static void init() {
		ScreenEvents.BEFORE_INIT.register((_client, screen, _scaledWidth, _scaledHeight) -> {
			if (Utils.isInGalatea() && screen instanceof GenericContainerScreen genericContainerScreen) {
				if (genericContainerScreen.getTitle().getString().equals("Tune Frequency")) {
					hasProcessed = false;
					tickCounter = 0;
					isInMenu = true;
					currentPitch = null;
					recentPitches.clear();
					lastTargetSlot = -1;
					ticksSinceLastMove = 0;
					targetSpeed = -1;
					ScreenEvents.afterTick(screen).register(_screen -> {
						// Track target pane movement each tick
						trackTargetPaneMovement(genericContainerScreen);

						// Process container when dye is detected
						if (!hasProcessed) {
							Int2ObjectMap<ItemStack> slots = getSlots(genericContainerScreen);
							ItemStack dyeStack = slots.get(46);
							if (dyeStack != null && !dyeStack.isEmpty() && isDye(dyeStack.getItem())) {
								processContainer(genericContainerScreen);
								hasProcessed = true;
							} else {
								tickCounter++;
								if (tickCounter >= TIMEOUT_TICKS) {
									LOGGER.warn("No dye detected in slot 46 after {} ticks", TIMEOUT_TICKS);
									tickCounter = 0;
								}
							}
						}
					});
					ScreenEvents.remove(screen).register(_screen -> {
						hasProcessed = false;
						tickCounter = 0;
						isInMenu = false;
						currentPitch = null;
						recentPitches.clear();
						lastTargetSlot = -1;
						ticksSinceLastMove = 0;
						targetSpeed = -1;
					});
				}
			}
		});
	}

	private static void trackTargetPaneMovement(GenericContainerScreen screen) {
		Int2ObjectMap<ItemStack> slots = getSlots(screen);
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
			LOGGER.info("Target pane moved from slot {} to slot {} after {} ticks",
					lastTargetSlot, currentTargetSlot, ticksSinceLastMove);

			// Calculate target speed from tick interval
			int ticks = ticksSinceLastMove;
			targetSpeed = -1;
			for (int i = 0; i < SPEED_RANGES.length; i++) {
				if (ticks >= SPEED_RANGES[i][0] && ticks <= SPEED_RANGES[i][1]) {
					targetSpeed = SPEED_CYCLE[i];
					LOGGER.info("Target speed calculated: {} (ticks={})", targetSpeed, ticks);
					break;
				}
			}
			if (targetSpeed == -1) {
				LOGGER.warn("Tick interval {} does not match any speed range", ticks);
			}

                        ticksSinceLastMove = 0;

                        if (!speedSolved) {
                                maybeSolveSpeed(slots);
                        }
                } else if (currentTargetSlot != -1) {
                        ticksSinceLastMove++;
                }

		lastTargetSlot = currentTargetSlot;
	}

       private static void processContainer(GenericContainerScreen screen) {
               Int2ObjectMap<ItemStack> slots = getSlots(screen);
               if (slots.isEmpty()) {
                       LOGGER.warn("No slots available in container");
                       return;
               }

               if (!colorSolved) {
                       colorClicks = computeColorClicks(slots);
                       colorSolved = true;
                       LOGGER.info("Required clicks to match dye to target pane: {}", colorClicks >= 0 ? "+" + colorClicks : colorClicks);
               }

               if (!speedSolved) {
                       maybeSolveSpeed(slots);
               }

               if (!pitchSolved) {
                       currentPitch = readCurrentPitch(slots);
               }
       }

	/**
	 * Determines the number of clicks needed to match the dye color in slot 46
	 * to the target glass pane color in slots 10–16, and speed in slot 48 to target speed.
	 * Extracts speed and pitch from lore. Logs all slots, dye, speed, pitch, and panes.
	 * Simulates clicks on slot 48 for speed.
	 * @param slots Map of slot indices to their ItemStacks
	 * @param screen Current container screen for clicking
	 * @return Number of clicks for color (+ for forward, - for backward, 0 if invalid)
	 */
       private static int computeColorClicks(Int2ObjectMap<ItemStack> slots) {
               LOGGER.info("Listing all container slots:");
               for (var entry : slots.int2ObjectEntrySet()) {
                       int slot = entry.getIntKey();
                       ItemStack stack = entry.getValue();
                       String itemName = stack == null || stack.isEmpty() ? "Empty" : stack.getItem().getTranslationKey();
                       String itemType = stack == null || stack.isEmpty() ? "None" : stack.getItem().toString();
                       LOGGER.info("Slot {}: Type={}, Name={}", slot, itemType, itemName);
               }

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
		LOGGER.info("Found dye in slot 46: Type={}, Name={}", dyeItem.toString(), dyeItem.getTranslationKey());


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
		LOGGER.info("Found moving pane in slot {}: Type={}, Name={}",
				movingSlot, movingItem.toString(), movingItem.getTranslationKey());

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
		LOGGER.info("Found target pane in slot {}: Type={}, Name={}",
				targetSlot, targetItem.toString(), targetItem.getTranslationKey());

		// Calculate clicks to match dye to target pane
		return calculateClicks(dyeIndex, targetIndex);
	}

        public static void onSound(PlaySoundS2CPacket packet) {
                if (pitchSolved || !Utils.isInGalatea() || !isInMenu || !packet.getSound().value().id().equals(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value().id())) {
                        return;
                }

		float packetPitch = packet.getPitch();
		recentPitches.add(packetPitch);
		LOGGER.debug("Received note block bass sound with pitch: {}", packetPitch);

		if (recentPitches.size() < MAX_PITCH_SAMPLES) {
			return; // Wait for 5 samples
		}

		// Check if all pitches match the current pitch
		if (currentPitch == null) {
			LOGGER.warn("Current pitch not set, cannot compare");
			recentPitches.clear();
			return;
		}

		float expectedPitch = getPitchValue(currentPitch);
                boolean allMatch = recentPitches.stream().allMatch(p -> Math.abs(p - expectedPitch) < 0.0001f);
                if (allMatch) {
                        LOGGER.info("All {} pitches match current pitch: {}", MAX_PITCH_SAMPLES, currentPitch);
                        pitchClicks = 0;
                        pitchSolved = true;
                        recentPitches.clear();
                        return;
                }

		// Get the latest pitch and find the target pitch
		float latestPitch = recentPitches.get(recentPitches.size() - 1);
		String targetPitch = getPitchName(latestPitch);
		if (targetPitch == null) {
			LOGGER.warn("Invalid pitch value received: {}", latestPitch);
			recentPitches.clear();
			return;
		}

		// Calculate clicks to match target pitch
		int currentIndex = getPitchIndex(currentPitch);
		int targetIndex = getPitchIndex(targetPitch);
		if (currentIndex == -1 || targetIndex == -1) {
			LOGGER.warn("Invalid pitch indices: current={}, target={}", currentPitch, targetPitch);
			recentPitches.clear();
			return;
		}

                int clicks = calculatePitchClicks(currentIndex, targetIndex);
                LOGGER.info("Pitch mismatch: Current={}, Target={}, Required clicks={}",
                                currentPitch, targetPitch, clicks >= 0 ? "+" + clicks : clicks);
                pitchClicks = clicks;
                pitchSolved = true;

                recentPitches.clear();
        }

        private static int readCurrentSpeed(Int2ObjectMap<ItemStack> slots) {
                ItemStack speedStack = slots.get(48);
                if (speedStack != null && !speedStack.isEmpty()) {
                        List<Text> lore = ItemUtils.getLore(speedStack);
                        if (!lore.isEmpty() && lore.size() >= 2) {
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
                        if (!lore.isEmpty() && lore.size() >= 2) {
                                String pitchText = lore.get(2).getString();
                                if (pitchText.contains("Low")) return "Low";
                                if (pitchText.contains("Normal")) return "Normal";
                                if (pitchText.contains("High")) return "High";
                        }
                }
                return null;
        }

        private static void maybeSolveSpeed(Int2ObjectMap<ItemStack> slots) {
                int currentSpeed = readCurrentSpeed(slots);
                if (currentSpeed > 0 && targetSpeed != -1) {
                        int currentIndex = getSpeedIndex(currentSpeed);
                        int targetIndex = getSpeedIndex(targetSpeed);
                        if (currentIndex != -1 && targetIndex != -1) {
                                speedClicks = calculateSpeedClicks(currentIndex, targetIndex);
                                speedSolved = true;
                                LOGGER.info("Speed mismatch: Current={}, Target={}, Required clicks={}",
                                                currentSpeed, targetSpeed, speedClicks >= 0 ? "+" + speedClicks : speedClicks);
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
