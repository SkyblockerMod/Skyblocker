package de.hysky.skyblocker.skyblock;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.PlaySoundEvents;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.container.SlotTextAdder;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NineFTMSolver extends SimpleContainerSolver implements ContainerListener, SlotTextAdder {
	public static final NineFTMSolver INSTANCE = new NineFTMSolver();
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Item UNKNOWN_SLOT = Items.YELLOW_STAINED_GLASS_PANE;
	private static final Item CORRECT_SLOT = Items.GREEN_STAINED_GLASS_PANE;
	private static final Item SOLVED_SLOT = Items.LIME_STAINED_GLASS_PANE;
	private static final String BOMB_CUSTOM_NAME = "BOMB";
	private static final Identifier BOMB_SOUND = SoundEvents.LAVA_POP.location();
	private static final int CHOICES = 4;

	private boolean isInMenu = false;
	private final Map<Identifier, Set<Float>> sounds = new HashMap<>(CHOICES, 1f);
	private final Set<Integer> clicks = new LinkedHashSet<>(CHOICES, 1f);
	private final List<Integer> solution = new ArrayList<>(CHOICES);
	private int bombSlotId = -1;

	private NineFTMSolver() {
		super("^9f™ Network Relay$");
		PlaySoundEvents.FROM_SERVER.register(this::onSound);
	}

	private static boolean isStackBomb(ItemStack stack) {
		return stack.getCustomName() != null && stack.getCustomName().getString().equals(BOMB_CUSTOM_NAME);
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.enable9fNetworkHelper;
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		if (bombSlotId == -1) return List.of();

		// Highlight bomb to make it stand out more.
		return List.of(ColorHighlight.red(bombSlotId));
	}

	@Override
	public void start(ContainerScreen screen) {
		isInMenu = true;
		screen.getMenu().addSlotListener(this);
	}

	@Override
	public void reset() {
		isInMenu = false;
		resetState();
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

	@Override
	public void slotChanged(AbstractContainerMenu _menu, int slotId, ItemStack stack) {
		// If only one choice is unknown then add it. (based on process of elimination)
		if (solution.size() == CHOICES - 1 && stack.is(UNKNOWN_SLOT) && !solution.contains(slotId)) {
			addSolvedSlot(slotId);
		// Track successful left clicks, aka brute forced choices.
		} else if (stack.is(CORRECT_SLOT) && !solution.contains(slotId)) {
			clicks.add(slotId);
			addSolvedSlot(slotId);
		// Solution has been applied, reset for the next (if any) solution.
		} else if (stack.is(SOLVED_SLOT) && solution.contains(slotId)) {
			resetState();
		// Track if bomb exists & where it is.
		} else if (bombSlotId == -1 && isStackBomb(stack)) {
			bombSlotId = slotId;
		} else if (bombSlotId == slotId && !isStackBomb(stack)) {
			bombSlotId = -1;
		}
	}

	@Override
	public void dataChanged(AbstractContainerMenu _menu, int _property, int _value) {}

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

	/**
	 * Adds a slot to the solution unless it's a duplicate.
	 */
	private void addSolvedSlot(int slotId) {
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

		// Ignore bomb sounds if a bomb exists, otherwise process them since some relays use the same sound.
		if (bombSlotId != -1 && sound.equals(BOMB_SOUND)) return;

		Set<Float> pitches = sounds.putIfAbsent(sound, new LinkedHashSet<>());

		if (pitches == null) pitches = sounds.get(sound);

		pitches.add(packet.getPitch());

		// Sort choices from lowest to highest pitch & complete solution.
		if (pitches.size() == CHOICES) {
			// Not enough clicked slots for every pitch, so clear progress & start over. (should never happen)
			if (clicks.size() != CHOICES) {
				LOGGER.error("[Skyblocker] Wrong amount of clicked slots (expected {}, actual {}), starting over.", CHOICES, clicks.size());
				clearState();
			}

			Iterator<Integer> slotIds = clicks.iterator();

			pitches.stream()
					.map(pitch -> new Choice(slotIds.next(), pitch))
					.sorted(Comparator.comparingDouble(Choice::pitch))
					.forEachOrdered(choice -> addSolvedSlot(choice.slotId));
		// Too many pitches, so clear progress & start over. (should never happen)
		} else if (pitches.size() > CHOICES) {
			LOGGER.error("[Skyblocker] Too many pitches (expected {}, actual {}) found for sound {}, starting over.", CHOICES, pitches.size(), sound);
			clearState();
		}
	}

	/**
	 * Represents a choice slot & its pitch.
	 */
	private record Choice(int slotId, float pitch) {}
}
