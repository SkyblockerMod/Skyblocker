package de.hysky.skyblocker.skyblock;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.RegexUtils;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class LoadoutKeybinds extends SimpleSlotTextAdder {
	public static final LoadoutKeybinds INSTANCE = new LoadoutKeybinds();
	public static final KeyMapping LOADOUT_1 = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.skyblocker.loadout.1", GLFW.GLFW_KEY_1, SkyblockerMod.KEYBINDING_CATEGORY));
	public static final KeyMapping LOADOUT_2 = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.skyblocker.loadout.2", GLFW.GLFW_KEY_2, SkyblockerMod.KEYBINDING_CATEGORY));
	public static final KeyMapping LOADOUT_3 = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.skyblocker.loadout.3", GLFW.GLFW_KEY_3, SkyblockerMod.KEYBINDING_CATEGORY));
	public static final KeyMapping LOADOUT_4 = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.skyblocker.loadout.4", GLFW.GLFW_KEY_4, SkyblockerMod.KEYBINDING_CATEGORY));
	public static final KeyMapping LOADOUT_5 = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.skyblocker.loadout.5", GLFW.GLFW_KEY_5, SkyblockerMod.KEYBINDING_CATEGORY));
	public static final KeyMapping LOADOUT_6 = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.skyblocker.loadout.6", GLFW.GLFW_KEY_6, SkyblockerMod.KEYBINDING_CATEGORY));
	public static final KeyMapping LOADOUT_7 = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.skyblocker.loadout.7", GLFW.GLFW_KEY_7, SkyblockerMod.KEYBINDING_CATEGORY));
	public static final KeyMapping LOADOUT_8 = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.skyblocker.loadout.8", GLFW.GLFW_KEY_8, SkyblockerMod.KEYBINDING_CATEGORY));
	public static final KeyMapping LOADOUT_9 = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.skyblocker.loadout.9", GLFW.GLFW_KEY_9, SkyblockerMod.KEYBINDING_CATEGORY));
	public static final KeyMapping LOADOUT_10 = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.skyblocker.loadout.10", GLFW.GLFW_KEY_0, SkyblockerMod.KEYBINDING_CATEGORY));
	public static final KeyMapping LOADOUT_11 = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.skyblocker.loadout.11", GLFW.GLFW_KEY_MINUS, SkyblockerMod.KEYBINDING_CATEGORY));
	public static final KeyMapping LOADOUT_12 = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.skyblocker.loadout.12", GLFW.GLFW_KEY_EQUAL, SkyblockerMod.KEYBINDING_CATEGORY));
	protected static final KeyMapping[] LOADOUT_KEY_MAPPINGS = { LOADOUT_1, LOADOUT_2, LOADOUT_3, LOADOUT_4, LOADOUT_5, LOADOUT_6, LOADOUT_7, LOADOUT_8, LOADOUT_9, LOADOUT_10, LOADOUT_11, LOADOUT_12 };
	private static final Pattern LOADOUT_PATTERN = Pattern.compile("Loadout (?<index>\\d+)");

	private LoadoutKeybinds() {
		// ^(?:\(\d+/\d+\) )?(Loadouts)$
		super("^(?:\\(\\d+/\\d+\\) )?(Loadouts)$");
	}

	@Init
	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, _, _) -> {
			if (!(screen instanceof AbstractContainerScreen<?> handledScreen) || !INSTANCE.test(handledScreen) || !INSTANCE.isEnabled() || client.gameMode == null) return;
			ScreenKeyboardEvents.allowKeyPress(handledScreen).register((_, keyInput) ->
					allowInput(client, handledScreen, keybinding -> keybinding.matches(keyInput))
			);
			ScreenMouseEvents.allowMouseClick(handledScreen).register((_, click) ->
					allowInput(client, handledScreen, keybinding -> keybinding.matchesMouse(click))
			);
		});
	}

	private static boolean allowInput(Minecraft minecraft, AbstractContainerScreen<?> containerScreen, Predicate<KeyMapping> predicate) {
		if (minecraft.gameMode == null || minecraft.player == null) {
			return true;
		}

		boolean found = false;
		int loadoutIndex;
		for (loadoutIndex = 0; loadoutIndex < LOADOUT_KEY_MAPPINGS.length; loadoutIndex++) {
			if (predicate.test(LOADOUT_KEY_MAPPINGS[loadoutIndex])) {
				found = true;
				break;
			}
		}

		if (!found) {
			return true;
		}

		int slotIndex = loadoutIndexToSlotId(loadoutIndex);
		ItemStack stack = containerScreen.getMenu().getSlot(slotIndex).getItem();

		if (isItemInvalid(stack)) {
			return true;
		}

		minecraft.gameMode.handleContainerInput(containerScreen.getMenu().containerId, slotIndex, GLFW.GLFW_MOUSE_BUTTON_LEFT, ContainerInput.PICKUP, minecraft.player);
		return false;
	}

	private static int loadoutIndexToSlotId(int loadoutIndex) {
		// The most efficient way of doing this! I totally wasn't too lazy to do the math or anything...
		return switch (loadoutIndex) {
			case 0 -> 14;
			case 1 -> 15;
			case 2 -> 16;
			case 3 -> 23;
			case 4 -> 24;
			case 5 -> 25;
			case 6 -> 32;
			case 7 -> 33;
			case 8 -> 34;
			case 9 -> 41;
			case 10 -> 42;
			case 11 -> 43;
			default -> throw new IllegalArgumentException("Illegal loadoutIndex " + loadoutIndex);
		};
	}

	private static boolean isItemInvalid(ItemStack stack) {
		// Skip loadouts that aren't customized, unlocked, or non-existent
		return stack.is(Items.GRAY_DYE) || stack.is(Items.RED_DYE) || stack.is(Items.BLACK_STAINED_GLASS_PANE) || stack.isEmpty();
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		// We use the custom name because ItemStack#getHoverName can return the customized item name which causes
		// this to stop working
		Component customName = stack.getCustomName();

		if (slotId > 43 || isItemInvalid(stack) || customName == null) {
			return List.of();
		}

		// Note: The pattern implictly excludes locked loadouts
		Matcher matcher = LOADOUT_PATTERN.matcher(customName.getString());

		if (matcher.matches()) {
			int loadoutIndex = RegexUtils.parseIntFromMatcher(matcher, "index");
			int keyMappingIndex = (loadoutIndex - 1) % LOADOUT_KEY_MAPPINGS.length;

			// Ensure its between [0, 12]
			if (keyMappingIndex >= 0 && keyMappingIndex <= (LOADOUT_KEY_MAPPINGS.length - 1)) {
				return SlotText.bottomLeftList(LOADOUT_KEY_MAPPINGS[keyMappingIndex].getTranslatedKeyMessage().plainCopy().withColor(SlotText.MID_BLUE));
			}
		}

		return List.of();
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.enableWardrobeHelper;
	}
}
