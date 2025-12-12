package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.ints.Int2BooleanArrayMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public class InventorySearch {
	@Nullable
	private static HandledScreen<?> openedHandledScreen = null;
	private static final Int2BooleanMap slotToMatch = new Int2BooleanArrayMap(64);
	private static String search = "";

	@Init
	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			UIAndVisualsConfig.InventorySearchConfig inventorySearchConfig = SkyblockerConfigManager.get().uiAndVisuals.inventorySearch;
			if (!inventorySearchConfig.enabled.isEnabled() || !(screen instanceof HandledScreen<?> handledScreen)) return;
			openedHandledScreen = null;

			if (inventorySearchConfig.clickableText) Screens.getButtons(handledScreen).add(new SearchTextWidget(handledScreen));

			ScreenKeyboardEvents.allowKeyPress(handledScreen).register((screen1, input) -> {
				if (input.key() == (inventorySearchConfig.ctrlK ? GLFW.GLFW_KEY_K : GLFW.GLFW_KEY_F) && input.hasCtrl()) {
					InventorySearch.showSearchBar(handledScreen);
					return false;
				}
				return true;
			});
		});
	}

	public static void showSearchBar(HandledScreen<?> handledScreen) {
		if (handledScreen == openedHandledScreen) return;
		openedHandledScreen = handledScreen;
		TextFieldWidget textFieldWidget = getTextFieldWidget(handledScreen);
		Screens.getButtons(handledScreen).addFirst(textFieldWidget);
		Screens.getButtons(handledScreen).removeIf(button -> button instanceof SearchTextWidget); // remove search text
		handledScreen.setFocused(textFieldWidget);

		ScreenEvents.remove(handledScreen).register(InventorySearch::onScreenClosed);
	}

	private static TextFieldWidget getTextFieldWidget(HandledScreen<?> handledScreen) {
		// Slightly modified text field widget
		TextFieldWidget textFieldWidget = new SearchTextFieldWidget(handledScreen.getTextRenderer(), Text.translatable("skyblocker.inventorySearch.searchInventory"));
		textFieldWidget.setPosition((handledScreen.width - textFieldWidget.getWidth()) / 2, 15);
		textFieldWidget.setPlaceholder(Text.translatable("gui.socialInteractions.search_hint"));
		textFieldWidget.setText(search); // Restore previous search
		textFieldWidget.setChangedListener(InventorySearch::onSearchTyped);
		return textFieldWidget;
	}

	public static boolean isSearching() {
		return openedHandledScreen != null;
	}

	public static boolean slotMatches(Slot slot) {
		return slotToMatch.computeIfAbsent(slot.id, i -> slot.hasStack() &&
				(slot.getStack().getName().getString().toLowerCase(Locale.ENGLISH).contains(search) || ItemUtils.getLoreLineIf(slot.getStack(), s -> s.toLowerCase(Locale.ENGLISH).contains(search)) != null));
	}

	private static void onSearchTyped(String text) {
		slotToMatch.clear();
		search = text.toLowerCase(Locale.ENGLISH);
	}

	private static void onScreenClosed(Screen screen) {
		openedHandledScreen = null;
		slotToMatch.clear();

	}

	public static void refreshSlot(int slotId) {
		slotToMatch.remove(slotId);
	}

	/**
	 * Button to open the search bar, for accessibility reasons (pojav and general preferences)
	 */
	private static class SearchTextWidget extends TextWidget {
		private final Text underlinedText;
		private final Text normalText;
		private final HandledScreen<?> screen;
		private boolean hoveredState = false;

		private SearchTextWidget(HandledScreen<?> handledScreen) {
			super(Text.translatable("skyblocker.inventorySearch.clickHereToSearch"), handledScreen.getTextRenderer());
			setPosition((handledScreen.width - this.getWidth()) / 2, 15);
			underlinedText = getMessage().copy().formatted(Formatting.UNDERLINE);
			normalText = getMessage().copy().formatted(Formatting.GRAY);
			screen = handledScreen;
			setMessage(normalText);
		}

		@Override
		public void onClick(Click click, boolean doubled) {
			InventorySearch.showSearchBar(screen);
		}

		@Override
		public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			if (isHovered() != hoveredState) {
				hoveredState = active = isHovered();
				if (hoveredState) setMessage(underlinedText);
				else setMessage(normalText);
			}

			super.renderWidget(context, mouseX, mouseY, delta);
		}
	}

	public static class SearchTextFieldWidget extends TextFieldWidget {
		TextRenderer textRenderer;
		Text message;

		public SearchTextFieldWidget(TextRenderer textRenderer, Text message) {
			super(textRenderer, 120, 20, message);
			this.textRenderer = textRenderer;
			this.message = message;
		}

		@Override
		public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			super.renderWidget(context, mouseX, mouseY, deltaTicks);
			context.drawCenteredTextWithShadow(textRenderer, message, getX() + width / 2, getY() - 1 - textRenderer.fontHeight, Colors.WHITE);
		}

		@Override
		public boolean keyPressed(KeyInput input) {
			// Makes the widget catch all key presses (except escape) to fix closing the inventory when pressing E
			// also check that the widget is focused and active
			return super.keyPressed(input) || (input.key() != GLFW.GLFW_KEY_ESCAPE && this.isFocused());
		}

		// Unfocus when clicking outside
		@Override
		public boolean mouseClicked(Click click, boolean doubled) {
			if (isFocused() && !isMouseOver(click.x(), click.y())) {
				setFocused(false);
				return false;
			}

			if (super.mouseClicked(click, doubled)) {
				setFocused(true);
				return true;
			}
			return false;
		}
	}
}
