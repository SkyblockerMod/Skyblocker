package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Predicate;

public class WardrobeKeybinds extends SimpleSlotTextAdder {
	public static final WardrobeKeybinds INSTANCE = new WardrobeKeybinds();

	private WardrobeKeybinds() {
		super("^(?:\\(\\d+/\\d+\\) )?(Armor Sets|Equipment Sets)$");
	}

	@Init
	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, _, _) -> {
			if (!(screen instanceof AbstractContainerScreen<?> containerScreen) || !INSTANCE.test(containerScreen) || !INSTANCE.isEnabled() || client.gameMode == null) return;
			ScreenKeyboardEvents.allowKeyPress(containerScreen).register((_, keyInput) ->
					allowInput(client, containerScreen, keybinding -> keybinding.matches(keyInput))
			);
			ScreenMouseEvents.allowMouseClick(containerScreen).register((_, click) ->
					allowInput(client, containerScreen, keybinding -> keybinding.matchesMouse(click))
			);
		});
	}

	private static boolean allowInput(Minecraft minecraft, AbstractContainerScreen<?> containerScreen, Predicate<KeyMapping> predicate) {
		// just in case
		if (minecraft.gameMode == null || minecraft.player == null) {
			return true;
		}

		boolean found = false;
		int i;
		for (i = 0; i < LoadoutKeybinds.LOADOUT_KEY_MAPPINGS.length; i++) {
			if (predicate.test(LoadoutKeybinds.LOADOUT_KEY_MAPPINGS[i])) {
				found = true;
				break;
			}
		}

		if (!found) {
			return true;
		}

		// The items start from the 5th row in the inventory. The i number we have is the column in the first row, so we have to offset it by 4 rows to get the 5th row, which is where the items start.
		i += 9 * 4;
		ItemStack itemStack = containerScreen.getMenu().getSlot(i).getItem();

		// Check if the item in the slot is a swap/unequip item before going further.
		// This prevents usage when the inventory hasn't loaded fully or when the slot pressed is locked or when the slot has no armor (which would be meaningless to click)
		if (!itemStack.is(Items.GRAY_DYE) && !itemStack.is(Items.LIME_DYE)) {
			return true;
		}

		minecraft.gameMode.handleContainerInput(containerScreen.getMenu().containerId, i, GLFW.GLFW_MOUSE_BUTTON_LEFT, ContainerInput.PICKUP, minecraft.player);
		return false;
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		if (!stack.is(Items.GRAY_DYE) && !stack.is(Items.LIME_DYE)) {
			return List.of();
		}

		if (!(slotId >= 36 && slotId <= 44)) {
			return List.of();
		}

		int loadoutIndex = slotId - 35 - 1;
		return SlotText.bottomLeftList(LoadoutKeybinds.LOADOUT_KEY_MAPPINGS[loadoutIndex].getTranslatedKeyMessage().plainCopy().withColor(SlotText.MID_BLUE));
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.enableWardrobeHelper;
	}
}
