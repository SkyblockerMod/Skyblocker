package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Predicate;

public class WardrobeKeybinds extends SimpleSlotTextAdder {
	public static final WardrobeKeybinds INSTANCE = new WardrobeKeybinds();

	public WardrobeKeybinds() {
		super("Wardrobe \\([123]/3\\)");
	}

	@Init
	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof HandledScreen<?> handledScreen) || !INSTANCE.test(handledScreen) || !INSTANCE.isEnabled() || client.interactionManager == null) return;
			ScreenKeyboardEvents.allowKeyPress(handledScreen).register((ignored, keyInput) ->
					allowInput(client, handledScreen, keybinding -> keybinding.matchesKey(keyInput))
			);
			ScreenMouseEvents.allowMouseClick(handledScreen).register((ignored, click) ->
					allowInput(client, handledScreen, keybinding -> keybinding.matchesMouse(click))
			);
		});
	}

	private static boolean allowInput(MinecraftClient client, HandledScreen<?> handledScreen, Predicate<KeyBinding> predicate) {
		boolean found = false;
		int i;
		for (i = 0; i < client.options.hotbarKeys.length; i++) {
			if (predicate.test(client.options.hotbarKeys[i])) {
				found = true;
				break;
			}
		}
		if (!found) return true;
		// The items start from the 5th row in the inventory. The i number we have is the column in the first row, so we have to offset it by 4 rows to get the 5th row, which is where the items start.
		i += 9 * 4;
		ItemStack itemStack = handledScreen.getScreenHandler().getSlot(i).getStack();
		// Check if the item in the slot is a swap/unequip item before going further.
		// This prevents usage when the inventory hasn't loaded fully or when the slot pressed is locked or when the slot has no armor (which would be meaningless to click)
		if (!itemStack.isOf(Items.PINK_DYE) && !itemStack.isOf(Items.LIME_DYE)) return true;
		assert client.interactionManager != null;
		client.interactionManager.clickSlot(handledScreen.getScreenHandler().syncId, i, GLFW.GLFW_MOUSE_BUTTON_1, SlotActionType.PICKUP, client.player);
		return false;
	}

	@Override
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		if (!stack.isOf(Items.PINK_DYE) && !stack.isOf(Items.LIME_DYE)) return List.of();
		if (!(slotId >= 36 && slotId <= 44)) return List.of();
		return SlotText.bottomLeftList(Text.literal(String.valueOf(slotId - 35)).withColor(SlotText.MID_BLUE));
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.enableWardrobeHelper;
	}
}
