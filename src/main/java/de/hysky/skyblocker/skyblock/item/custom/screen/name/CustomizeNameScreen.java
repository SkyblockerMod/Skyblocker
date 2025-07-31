package de.hysky.skyblocker.skyblock.item.custom.screen.name;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.item.custom.screen.name.visitor.*;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.OkLabColor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.function.Predicate;

public class CustomizeNameScreen extends Screen {
	private static final int BORDER_SIZE = 12;
	private static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("popup/background");

	private final String uuid;

	private Text text = Text.empty();
	private String textString = "";

	private TextField textField;
	private FormattingButton[] formattingButtons;

	private final GridWidget grid = new GridWidget();

	private int selectionStart;
	private int selectionEnd;

	private @Nullable Style insertAs;

	public CustomizeNameScreen(@NotNull ItemStack stack) {
		super(Text.literal("Customize Item Name"));
		uuid = ItemUtils.getItemUuid(stack);
		setText(stack.getName().copy());
	}

	@Override
	protected void init() {
		if (uuid.isEmpty()) {
			close();
			return;
		}
		// the gui is a grid of 20 columns, should be 16 px each
		textField = grid.add(new TextField(), 1, 0, 1, 20);
		addDrawableChild(textField);
		formattingButtons = new FormattingButton[]{
				new FormattingButton("B", Formatting.BOLD, Style::isBold),
				new FormattingButton("I", Formatting.ITALIC, Style::isItalic),
				new FormattingButton("U", Formatting.UNDERLINE, Style::isUnderlined),
				new FormattingButton("S", Formatting.STRIKETHROUGH, Style::isStrikethrough),
				new FormattingButton("|||", Formatting.OBFUSCATED, Style::isObfuscated),
		};
		for (int i = 0; i < formattingButtons.length; i++) {
			FormattingButton button = formattingButtons[i];
			addDrawableChild(grid.add(button, 0, i));
		}

		int i = 0;
		for (Formatting formatting : Formatting.values()) {
			if (formatting.isColor()) {
				addDrawableChild(grid.add(new ColorButton(formatting), 2, i++));
			}
		}

		assert client != null;
		addDrawableChild(grid.add(ButtonWidget.builder(Text.translatable("skyblocker.customItemNames.screen.customColor"), b ->
				client.setScreen(ColorPopup.create(this, color -> setStyle(Style.EMPTY.withColor(color))))
		).size(48, 16).build(), 2, 17, 1, 3));
		addDrawableChild(grid.add(ButtonWidget.builder(Text.translatable("skyblocker.customItemNames.screen.gradientColor"), b ->
				client.setScreen(ColorPopup.createGradient(this, this::createGradient))
		).size(48, 16).build(), 3, 17, 1, 3));
		addDrawableChild(grid.add(ButtonWidget.builder(Text.translatable("gui.cancel"), b -> close()).width(80).build(), 4, 0, 1, 10, Positioner.create().alignRight()));
		addDrawableChild(grid.add(ButtonWidget.builder(Text.translatable("gui.done"), b -> {
			SkyblockerConfigManager.update(config -> {
				if (textString.isBlank()) config.general.customItemNames.remove(uuid);
				else config.general.customItemNames.put(uuid, text.copy().setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.WHITE)));
			});
			close();
		}).width(80).build(), 4, 10, 1, 10, Positioner.create().alignLeft()));
		addDrawableChild(grid.add(new TextWidget(20 * 16, textRenderer.fontHeight, Text.translatable("skyblocker.customItemNames.screen.howToRemove").formatted(Formatting.ITALIC, Formatting.GRAY), textRenderer).alignLeft(), 5, 0, 1, 20, Positioner.create().marginTop(2)));
		refreshWidgetPositions();
		setFocused(textField);
		selectionStart = selectionEnd = textString.length();
	}

	@Override
	protected void refreshWidgetPositions() {
		grid.refreshPositions();
		grid.setPosition((width - grid.getWidth()) / 2, (height - grid.getHeight()) / 2);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		context.drawGuiTexture(
				RenderPipelines.GUI_TEXTURED,
				BACKGROUND_TEXTURE,
				grid.getX() - BORDER_SIZE,
				grid.getY() - BORDER_SIZE,
				grid.getWidth() + BORDER_SIZE * 2,
				grid.getHeight() + BORDER_SIZE * 2);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		// little preview
		context.drawCenteredTextWithShadow(textRenderer, text, width / 2, grid.getY() + grid.getHeight() + BORDER_SIZE + 6, -1);
		if (Debug.debugEnabled()) {
			context.drawTextWithShadow(textRenderer, Text.literal("Selection Start: " + selectionStart + ", Selection End: " + selectionEnd), 10, 10, -1);
			context.drawTextWithShadow(textRenderer, Text.literal("Insert Style: " + (insertAs == null ? "null" : insertAs.toString())), 10, 20, -1);
		}
	}

	/**
	 * Creates a gradient that goes from {@link CustomizeNameScreen#selectionStart} to {@link CustomizeNameScreen#selectionEnd}
	 * @param startColor the color at the start of the gradient
	 * @param endColor the color at the end of the gradient
	 */
	private void createGradient(int startColor, int endColor) {
		int previousSelectionStart = selectionStart;
		int previousSelectionEnd = selectionEnd;

		int selStart = Math.min(selectionStart, selectionEnd);
		int selSize = Math.abs(selectionEnd - selectionStart);
		if (selSize == 0) return;
		if (selSize == 1) {
			setStyle(Style.EMPTY.withColor(startColor));
		} else {
			for (int i = 0; i < selSize; i++) {
				selectionStart = selStart + i;
				selectionEnd = selStart + i + 1;
				int color = OkLabColor.interpolate(startColor, endColor, (float) i / (selSize - 1));
				setStyle(Style.EMPTY.withColor(color));
			}
		}
		selectionStart = previousSelectionStart;
		selectionEnd = previousSelectionEnd;
	}

	/**
	 * Sets the style of the selected text or the insert position if no text is selected.
	 * @param style the style to set
	 */
	private void setStyle(Style style) {
		if (selectionStart == selectionEnd) {
			insertAs = style.withParent(insertAs == null ? Style.EMPTY : insertAs);
			return;
		}
		SetStyleVisitor setStyleVisitor = new SetStyleVisitor(style, selectionStart, selectionEnd);
		text.visit(setStyleVisitor, Style.EMPTY);
		setText(setStyleVisitor.getNewText());
	}

	private void updateStyleButtons() {
		GetStyleVisitor styleVisitor = new GetStyleVisitor(selectionStart, selectionEnd);
		text.visit(styleVisitor, Style.EMPTY);
		Style style = styleVisitor.getStyle();
		for (FormattingButton button : formattingButtons) {
			button.update(style);
		}
	}

	/**
	 * Sets the text to be displayed in the text field and updates the textString to avoid calling getString() every time.
	 * @param text the text to set
	 */
	public void setText(Text text) {
		this.text = text;
		textString = text.getString();
		// called before init
		if (textField != null) textField.updateMePrettyPlease = true;
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (super.charTyped(chr, modifiers) || textField.isFocused()) return true;
		setFocused(textField);
		return textField.charTyped(chr, modifiers);
	}

	/**
	 * Inserts the given text at the current cursor position or replaces the selected text.
	 * If the text field is empty, it sets the text to the given string.
	 * @param str the text to insert
	 */
	public void insertText(String str) {
		str = StringHelper.stripInvalidChars(str);
		if (textString.isEmpty()) {
			setText(Text.literal(str).setStyle(insertAs != null ? insertAs : Style.EMPTY));
		} else {
			InsertTextVisitor visitor = new InsertTextVisitor(str, insertAs, selectionStart, selectionEnd);
			text.visit(visitor, Style.EMPTY);
			setText(visitor.getNewText());
			insertAs = null;
		}

		selectionStart = Math.min(selectionStart, selectionEnd) + str.length();
		selectionEnd = selectionStart;
		updateStyleButtons();
	}

	/**
	 * Moves the cursor left or right, depending on the direction.
	 * If shift is held, it will extend the selection.
	 * If ctrl is held, it will skip to the next word.
	 *
	 * @param left       whether to move left or right
	 * @param shiftHeld  whether shift is held
	 * @param ctrlHeld   whether ctrl is held
	 */
	private void moveCursor(boolean left, boolean shiftHeld, boolean ctrlHeld) {
		if (left && selectionStart == 0 || (!left && selectionStart == textString.length())) return;
		if (ctrlHeld) {
			selectionStart = getWordSkipPosition(left);
		} else {
			selectionStart = Util.moveCursor(textString, selectionStart, left ? -1 : 1);
		}
		if (!shiftHeld) selectionEnd = selectionStart;
		insertAs = null;
		updateStyleButtons();
	}

	/**
	 * Erases the text at the current cursor position or the selected text.
	 * If the selection is not empty, it will remove the selected text.
	 * If the selection is empty, it will erase one character or word in the requested direction.
	 *
	 * @param left      whether to erase left or right
	 * @param ctrlHeld  whether ctrl is held
	 */
	private void erase(boolean left, boolean ctrlHeld) {
		if (selectionStart != selectionEnd) {
			insertText("");
			return;
		}
		moveCursor(left, true, ctrlHeld);
		insertText("");
	}

	/**
	 * Skips one word in the requested direction from selectionStart
	 *
	 * @param left the direction
	 * @return the new position
	 */
	private int getWordSkipPosition(boolean left) {
		int i = selectionStart;

		if (!left) {
			int l = this.textString.length();
			i = this.textString.indexOf(32, i);
			if (i == -1) {
				i = l;
			} else {
				while (i < l && this.textString.charAt(i) == ' ') {
					i++;
				}
			}
		} else {
			while (i > 0 && this.textString.charAt(i - 1) == ' ') {
				i--;
			}

			while (i > 0 && this.textString.charAt(i - 1) != ' ') {
				i--;
			}
		}

		return i;
	}

	private class FormattingButton extends PressableWidget {
		private boolean enabled;
		private final Formatting format;
		private final Predicate<Style> isEnabled;

		protected FormattingButton(Text message, Formatting format, Predicate<Style> isEnabled) {
			super(0, 0, 16, 16, message);
			this.format = format;
			this.isEnabled = isEnabled;
			setTooltip(Tooltip.of(ConfigUtils.FORMATTING_FORMATTER.apply(format))); // Yoink from config utils hehhehehehehe
		}

		protected FormattingButton(String str, Formatting format, Predicate<Style> isEnabled) {
			this(Text.literal(str).formatted(format), format, isEnabled);
		}

		private void update(Style style) {
			setEnabled(isEnabled.test(style));
		}

		@Override
		public void onPress() {
			setEnabled(!enabled);
			switch (format) {
				case BOLD -> setStyle(Style.EMPTY.withBold(enabled));
				case ITALIC -> setStyle(Style.EMPTY.withItalic(enabled));
				case UNDERLINE -> setStyle(Style.EMPTY.withUnderline(enabled));
				case STRIKETHROUGH -> setStyle(Style.EMPTY.withStrikethrough(enabled));
				case OBFUSCATED -> setStyle(Style.EMPTY.withObfuscated(enabled));
				default -> throw new IllegalStateException("Unexpected value: " + format);
			}
		}

		private void setEnabled(boolean enabled) {
			this.enabled = enabled;
			setMessage(getMessage().copy().withColor(enabled ? Colors.YELLOW : Colors.WHITE));
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}

	private class ColorButton extends PressableWidget {
		private final Formatting color;
		private final int intColor;

		private ColorButton(Formatting format) {
			super(0, 0, 16, 16, ConfigUtils.FORMATTING_FORMATTER.apply(format));
			setTooltip(Tooltip.of(getMessage()));
			this.color = format;
			this.intColor = ColorHelper.fullAlpha(color.getColorValue());
		}

		@Override
		public void onPress() {
			setStyle(Style.EMPTY.withColor(color));
		}

		@Override
		public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
			context.fill(getX() + 2, getY() + 2, getRight() - 2, getBottom() - 2, intColor);
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}

	/**
	 * Used to capture inputs and render the text. Most logic is done in the screen itself
	 */
	private class TextField extends ClickableWidget {
		private int renderedSelectionStart;
		private int renderedSelectionEnd;
		private boolean updateMePrettyPlease = false;

		private int renderStart;
		private int renderEnd;

		private TextField() {
			super(0, 0, 320, 20, Text.literal("TextField"));
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			if (renderedSelectionStart != selectionStart || renderedSelectionEnd != selectionEnd || updateMePrettyPlease) {
				renderedSelectionStart = selectionStart;
				renderedSelectionEnd = selectionEnd;
				updateMePrettyPlease = false;
				GetRenderWidthVisitor getRenderWidthVisitor = new GetRenderWidthVisitor(selectionStart, selectionEnd);
				text.visit(getRenderWidthVisitor, Style.EMPTY);
				renderStart = getRenderWidthVisitor.getWidths().firstInt();
				renderEnd = getRenderWidthVisitor.getWidths().secondInt();
			}

			context.fill(getX(), getY(), getRight(), getBottom(), Colors.BLACK);
			context.drawBorder(getX(), getY(), getWidth(), getHeight(), isFocused() ? Colors.WHITE : Colors.GRAY);
			int textX = getTextX();
			int textY = getY() + (getHeight() - textRenderer.fontHeight) / 2;

			if (renderStart != renderEnd) {
				context.fill(textX + renderStart, textY, textX + renderEnd, textY + textRenderer.fontHeight, Colors.BLUE);
			}
			if (isFocused()) context.drawVerticalLine(textX + (selectionStart < selectionEnd ? renderStart : renderEnd) - 1, textY - 1, textY + textRenderer.fontHeight, Colors.WHITE);

			context.drawText(textRenderer, text, textX, textY, -1, false);
		}

		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			boolean captured = true;
			switch (keyCode) {
				case GLFW.GLFW_KEY_LEFT -> moveCursor(true, Screen.hasShiftDown(), Screen.hasControlDown());
				case GLFW.GLFW_KEY_RIGHT -> moveCursor(false, Screen.hasShiftDown(), Screen.hasControlDown());
				case GLFW.GLFW_KEY_BACKSPACE -> erase(true, Screen.hasControlDown());
				case GLFW.GLFW_KEY_DELETE -> erase(false, Screen.hasControlDown());
				default -> captured = false;
			}
			if (captured) return true;
			assert client != null;
			if (Screen.isSelectAll(keyCode)) {
				selectionStart = 0;
				selectionEnd = textString.length();
				updateStyleButtons();
				captured = true;
			} else if (Screen.isCopy(keyCode)) {
				client.keyboard.setClipboard(text.getString().substring(selectionStart, selectionEnd));
				captured = true;
			} else if (Screen.isPaste(keyCode)) {
				String clipboard = client.keyboard.getClipboard();
				if (!clipboard.isEmpty()) {
					insertText(clipboard);
				}
				captured = true;
			} else if (Screen.isCut(keyCode)) {
				client.keyboard.setClipboard(text.getString().substring(selectionStart, selectionEnd));
				insertText("");
				captured = true;
			}
			return captured;
		}

		@Override
		public boolean charTyped(char chr, int modifiers) {
			if (!active) {
				return false;
			} else if (StringHelper.isValidChar(chr)) {
				insertText(Character.toString(chr));
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			GetClickedPositionVisitor getClickedPositionVisitor = new GetClickedPositionVisitor((int) mouseX - getTextX());
			text.visit(getClickedPositionVisitor, Style.EMPTY);
			selectionStart = selectionEnd = getClickedPositionVisitor.getPosition() < 0 ? textString.length() : getClickedPositionVisitor.getPosition();
			updateStyleButtons();
		}

		@Override
		protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
			GetClickedPositionVisitor getClickedPositionVisitor = new GetClickedPositionVisitor((int) mouseX - getTextX());
			text.visit(getClickedPositionVisitor, Style.EMPTY);
			selectionStart = getClickedPositionVisitor.getPosition() < 0 ? textString.length() : getClickedPositionVisitor.getPosition();
			updateStyleButtons();
		}

		private int getTextX() {
			return getX() + 2;
		}

		@Override
		public void playDownSound(SoundManager soundManager) {}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}
}
