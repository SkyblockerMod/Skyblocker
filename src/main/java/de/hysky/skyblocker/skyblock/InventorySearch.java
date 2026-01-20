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
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public class InventorySearch {
	private static @Nullable AbstractContainerScreen<?> openedHandledScreen = null;
	private static final Int2BooleanMap slotToMatch = new Int2BooleanArrayMap(64);
	private static String search = "";

	@Init
	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			UIAndVisualsConfig.InventorySearchConfig inventorySearchConfig = SkyblockerConfigManager.get().uiAndVisuals.inventorySearch;
			if (!inventorySearchConfig.enabled.isEnabled() || !(screen instanceof AbstractContainerScreen<?> handledScreen)) return;
			openedHandledScreen = null;

			if (inventorySearchConfig.clickableText) Screens.getButtons(handledScreen).add(new SearchTextWidget(handledScreen));

			ScreenKeyboardEvents.allowKeyPress(handledScreen).register((screen1, input) -> {
				if (input.key() == (inventorySearchConfig.ctrlK ? GLFW.GLFW_KEY_K : GLFW.GLFW_KEY_F) && input.hasControlDownWithQuirk()) {
					InventorySearch.showSearchBar(handledScreen);
					return false;
				}
				return true;
			});
		});
	}

	public static void showSearchBar(AbstractContainerScreen<?> handledScreen) {
		if (handledScreen == openedHandledScreen) return;
		openedHandledScreen = handledScreen;
		EditBox textFieldWidget = getTextFieldWidget(handledScreen);
		Screens.getButtons(handledScreen).addFirst(textFieldWidget);
		Screens.getButtons(handledScreen).removeIf(button -> button instanceof SearchTextWidget); // remove search text
		handledScreen.setFocused(textFieldWidget);

		ScreenEvents.remove(handledScreen).register(InventorySearch::onScreenClosed);
	}

	private static EditBox getTextFieldWidget(AbstractContainerScreen<?> handledScreen) {
		// Slightly modified text field widget
		EditBox textFieldWidget = new SearchTextFieldWidget(handledScreen.getFont(), Component.translatable("skyblocker.inventorySearch.searchInventory"));
		textFieldWidget.setPosition((handledScreen.width - textFieldWidget.getWidth()) / 2, 15);
		textFieldWidget.setHint(Component.translatable("gui.socialInteractions.search_hint"));
		textFieldWidget.setValue(search); // Restore previous search
		textFieldWidget.setResponder(InventorySearch::onSearchTyped);
		return textFieldWidget;
	}

	public static boolean isSearching() {
		return openedHandledScreen != null;
	}

	public static boolean slotMatches(Slot slot) {
		return slotToMatch.computeIfAbsent(slot.index, i -> slot.hasItem() &&
				(slot.getItem().getHoverName().getString().toLowerCase(Locale.ENGLISH).contains(search) || ItemUtils.getLoreLineIf(slot.getItem(), s -> s.toLowerCase(Locale.ENGLISH).contains(search)) != null));
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
	private static class SearchTextWidget extends StringWidget {
		private final Component underlinedText;
		private final Component normalText;
		private final AbstractContainerScreen<?> screen;
		private boolean hoveredState = false;

		private SearchTextWidget(AbstractContainerScreen<?> handledScreen) {
			super(Component.translatable("skyblocker.inventorySearch.clickHereToSearch"), handledScreen.getFont());
			setPosition((handledScreen.width - this.getWidth()) / 2, 15);
			underlinedText = getMessage().copy().withStyle(ChatFormatting.UNDERLINE);
			normalText = getMessage().copy().withStyle(ChatFormatting.GRAY);
			screen = handledScreen;
			setMessage(normalText);
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			InventorySearch.showSearchBar(screen);
		}

		@Override
		public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
			if (isHovered() != hoveredState) {
				hoveredState = active = isHovered();
				if (hoveredState) setMessage(underlinedText);
				else setMessage(normalText);
			}

			super.renderWidget(context, mouseX, mouseY, delta);
		}
	}

	public static class SearchTextFieldWidget extends EditBox {
		Font textRenderer;
		Component message;

		public SearchTextFieldWidget(Font textRenderer, Component message) {
			super(textRenderer, 120, 20, message);
			this.textRenderer = textRenderer;
			this.message = message;
		}

		@Override
		public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
			super.renderWidget(context, mouseX, mouseY, deltaTicks);
			context.drawCenteredString(textRenderer, message, getX() + width / 2, getY() - 1 - textRenderer.lineHeight, CommonColors.WHITE);
		}

		@Override
		public boolean keyPressed(KeyEvent input) {
			// Makes the widget catch all key presses (except escape) to fix closing the inventory when pressing E
			// also check that the widget is focused and active
			return super.keyPressed(input) || (input.key() != GLFW.GLFW_KEY_ESCAPE && this.isFocused());
		}

		// Unfocus when clicking outside
		@Override
		public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
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
