package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.PlaySoundEvents;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.container.SlotTextAdder;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NineFTMSolver extends SimpleContainerSolver implements SlotTextAdder {
	public static final NineFTMSolver INSTANCE = new NineFTMSolver();
	private static final Logger LOGGER = LoggerFactory.getLogger(NineFTMSolver.class);
	private static final Item UNKNOWN_SLOT = Items.YELLOW_STAINED_GLASS_PANE;
	private static final Item CORRECT_SLOT = Items.GREEN_STAINED_GLASS_PANE;
	private static final Item SOLVED_SLOT = Items.LIME_STAINED_GLASS_PANE;
	private static final String BOMB_NAME = "BOMB";
	private static final Identifier BOMB_SOUND = SoundEvents.LAVA_POP.location();
	private static final int CHOICES = 4;

	private boolean isInMenu = false;
	private Map<Identifier, Set<Float>> sounds = new HashMap<>(CHOICES, 1f);
	private Set<Integer> clicks = new LinkedHashSet<>(CHOICES, 1f);
	private List<Integer> solution = new ArrayList<>(CHOICES);

	private NineFTMSolver() {
		super("^9f™ Network Relay$");
		PlaySoundEvents.FROM_SERVER.register(this::onSound);
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.enable9fNetworkHelper;
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		// Highlight bombs to make them stand out more.
		return slots.int2ObjectEntrySet().stream()
			.filter(entry -> entry.getValue().getCustomName() != null && entry.getValue().getCustomName().getString().equals(BOMB_NAME))
			.map(entry -> ColorHighlight.red(entry.getIntKey()))
			.toList();
	}

	@Override
	public void start(ContainerScreen screen) {
		isInMenu = true;
		ScreenEvents.afterTick(screen).register(_screen -> trackChangedSlots(screen));
	}

	@Override
	public void reset() {
		isInMenu = false;
		resetState();
	}

	@Override
	public List<SlotText> getText(@Nullable Slot _slot, ItemStack stack, int slotId) {
		// Avoid overlapping with stack size.
		if (stack.getCount() != 1) return List.of();
		// Add 1s to solved columns for aesthetic purposes.
		else if (stack.is(SOLVED_SLOT)) return SlotText.bottomRightList(Component.literal("1"));

		int counter = solution.indexOf(slotId);

		if (counter == -1) return List.of();

		// Display solution order for each known choice.
		return SlotText.bottomRightList(Component.literal(String.valueOf(counter + 1)));
	}

	@Override
	public boolean onClickSlot(int slotId, ItemStack stack, int _screenId, int button) {
		// Only track right-clicks on unknown slots, those are what play the sounds.
		if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && stack.is(UNKNOWN_SLOT)) {
			clicks.add(slotId);

			// Too many clicked slots, so clear progress & start over. (should never happen)
			if (clicks.size() > CHOICES) {
				LOGGER.error("[Skyblocker] Too many different clicked slots (expected {}, actual {}), starting over.", CHOICES, clicks.size());
				clearState();
			}
		}

		return false;
	}

	/**
	 * Adds a choice to the current solution unless it's a duplicate.
	 */
	private void addChoice(int slotId) {
		if (!solution.contains(slotId)) solution.add(slotId);
	}

	/**
	 * Clean up all partial data used to determine the next solution.
	 */
	private void clearState() {
		for (Set<Float> pitches : sounds.values()) pitches.clear();
		clicks.clear();
	}

	/**
	 * Fully reset the solver to its starting state.
	 */
	private void resetState() {
		clearState();
		sounds.clear();
		solution.clear();
	}

	/**
	 * Processes incoming sounds to look for the solution.
	 */
	private void onSound(ClientboundSoundPacket packet) {
		// Skip if not in menu or if entire solution is already found.
		if (!isInMenu || solution.size() == CHOICES) return;

		Identifier sound = packet.getSound().value().location();

		// Ignore all sounds from bombs.
		if (sound.equals(BOMB_SOUND)) return;

		Set<Float> pitches = sounds.putIfAbsent(sound, new LinkedHashSet<>());

		if (pitches == null) pitches = sounds.get(sound);

		pitches.add(packet.getPitch());

		// Too many pitches, so clear progress & start over. (should never happen)
		if (pitches.size() > CHOICES) {
			LOGGER.error("[Skyblocker] Too many pitches (expected {}, actual {}) found for sound {}, starting over.", CHOICES, pitches.size(), sound);
			clearState();

			return;
		// Can't solve yet, need 4 different pitches of the correct sound.
		} else if (pitches.size() != CHOICES) {
			return;
		// Not enough clicked slots, so clear progress & start over. (should never happen)
		} else if (clicks.size() < CHOICES) {
			LOGGER.error("[Skyblocker] Not enough clicked slots (expected {}, actual {}) for every pitch, starting over.", CHOICES, clicks.size());
			clearState();

			return;
		}

		// Sort slots from lowest to highest pitch & update solution.
		List<Choice> choices = new ArrayList<>(CHOICES);
		Iterator<Integer> clicksIter = clicks.iterator();
		Iterator<Float> pitchesIter = pitches.iterator();

		while (clicksIter.hasNext())
			choices.add(new Choice(clicksIter.next(), pitchesIter.next()));

		choices.stream()
			.sorted(Comparator.comparingDouble(Choice::pitch))
			.forEachOrdered(choice -> addChoice(choice.slotId));
	}

	private void trackChangedSlots(ContainerScreen screen) {
		ChestMenu menu = screen.getMenu();

		for (Slot slot : menu.slots) {
			ItemStack stack = slot.getItem();

			// If only one choice is unknown then add it in based on process of elimination.
			if (solution.size() == CHOICES - 1 && stack.is(UNKNOWN_SLOT) && !solution.contains(slot.index)) {
				addChoice(slot.index);
			// Add brute-forced choices to the solution.
			} else if (stack.is(CORRECT_SLOT) && !solution.contains(slot.index)) {
				addChoice(slot.index);
			// Solution has been applied, reset for the next (if any) solution.
			} else if (stack.is(SOLVED_SLOT) && solution.contains(slot.index)) {
				resetState();

				break;
			}
		}
	}

	private record Choice(int slotId, float pitch) {}
}
