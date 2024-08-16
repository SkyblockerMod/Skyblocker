package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class InventorySearch {

	private static HandledScreen<?> openedHandledScreen = null;
	private static final Int2BooleanMap slotToMatch = new Int2BooleanOpenHashMap();
	private static String search = "";

	public static void search(HandledScreen<?> handledScreen) {
		if (handledScreen == openedHandledScreen) return;
		openedHandledScreen = handledScreen;
		TextFieldWidget textFieldWidget = getTextFieldWidget(handledScreen);
		Screens.getButtons(handledScreen).add(new TextWidget(0, 5, handledScreen.width, 10, Text.literal("Search Inventory"), Screens.getTextRenderer(handledScreen)));
		Screens.getButtons(handledScreen).addFirst(textFieldWidget);
		handledScreen.setFocused(textFieldWidget);

		ScreenEvents.remove(handledScreen).register(InventorySearch::reset);
	}

	private static @NotNull TextFieldWidget getTextFieldWidget(HandledScreen<?> handledScreen) {
		TextFieldWidget textFieldWidget = new TextFieldWidget(Screens.getTextRenderer(handledScreen), 120, 20, Text.literal("Search Inventory")){
			@Override
			public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
				return super.keyPressed(keyCode, scanCode, modifiers) || (keyCode != 256 && this.isNarratable() && this.isFocused());
			}

			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				if (isFocused() && !clicked(mouseX, mouseY)) {
					setFocused(false);
					return false;
				}
				return super.mouseClicked(mouseX, mouseY, button);
			}
		};
		textFieldWidget.setPosition((handledScreen.width - textFieldWidget.getWidth()) / 2, 15);
		textFieldWidget.setText(search);
		textFieldWidget.setChangedListener(InventorySearch::onSearchTyped);
		return textFieldWidget;
	}

	public static boolean isSearching() {
		return openedHandledScreen != null;
	}

	public static boolean slotMatches(Slot slot) {
		return slotToMatch.computeIfAbsent(slot.id, i -> slot.hasStack() &&
				(slot.getStack().getName().getString().toLowerCase().contains(search) || ItemUtils.getLoreLineIf(slot.getStack(), s -> s.toLowerCase().contains(search)) != null));
	}

	private static void onSearchTyped(String text) {
		slotToMatch.clear();
		search = text.toLowerCase();
	}

	private static void reset(Screen screen) {
		openedHandledScreen = null;
		slotToMatch.clear();

	}
}
