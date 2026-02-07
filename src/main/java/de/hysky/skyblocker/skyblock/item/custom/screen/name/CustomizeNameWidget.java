package de.hysky.skyblocker.skyblock.item.custom.screen.name;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.item.custom.screen.name.visitor.GetClickedPositionVisitor;
import de.hysky.skyblocker.skyblock.item.custom.screen.name.visitor.GetRenderWidthVisitor;
import de.hysky.skyblocker.skyblock.item.custom.screen.name.visitor.GetStyleVisitor;
import de.hysky.skyblocker.skyblock.item.custom.screen.name.visitor.InsertTextVisitor;
import de.hysky.skyblocker.skyblock.item.custom.screen.name.visitor.SetStyleVisitor;
import de.hysky.skyblocker.utils.OkLabColor;
import de.hysky.skyblocker.utils.render.HudHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Predicate;

public class CustomizeNameWidget extends AbstractContainerWidget {
	private static final Identifier INNER_SPACE_TEXTURE = SkyblockerMod.id("menu_inner_space");
	private static final int PADDING = 3;

	private final Minecraft client = Minecraft.getInstance();
	private final Font textRenderer = client.font;
	private String uuid = "";

	private Component text = Component.empty();
	private String textString = "";

	private final TextField textField;
	private final StringWidget previewWidget;
	private final FormattingButton[] formattingButtons;
	private final List<AbstractWidget> widgets;

	private final GridLayout grid = new GridLayout();

	private int selectionStart;
	private int selectionEnd;

	private @Nullable Style insertAs;

	public CustomizeNameWidget(Screen parent) {
		super(0, 0, 0, 0, Component.literal("Customize Item Name"));
		ImmutableList.Builder<AbstractWidget> builder = ImmutableList.builder();
		// the gui is a grid of 20 columns, should be 16 px each
		textField = grid.addChild(new TextField(), 1, 0, 1, 20);
		builder.add(textField);
		formattingButtons = new FormattingButton[]{
				new FormattingButton("B", ChatFormatting.BOLD, Style::isBold),
				new FormattingButton("I", ChatFormatting.ITALIC, Style::isItalic),
				new FormattingButton("U", ChatFormatting.UNDERLINE, Style::isUnderlined),
				new FormattingButton("S", ChatFormatting.STRIKETHROUGH, Style::isStrikethrough),
				new FormattingButton("|||", ChatFormatting.OBFUSCATED, Style::isObfuscated),
		};

		addFormattingButtons(builder);

		builder.add(grid.addChild(Button.builder(Component.translatable("skyblocker.customItemNames.screen.customColor"), b ->
				client.setScreen(ColorPopup.create(parent, color -> setStyle(Style.EMPTY.withColor(color))))
		).size(48, 16).build(), 2, 17, 1, 3));
		builder.add(grid.addChild(Button.builder(Component.translatable("skyblocker.customItemNames.screen.gradientColor"), b ->
				client.setScreen(ColorPopup.createGradient(parent, this::createGradient))
		).size(48, 16).build(), 3, 17, 1, 3));
		builder.add(grid.addChild(new StringWidget(20 * 16, textRenderer.lineHeight, Component.translatable("skyblocker.customItemNames.screen.howToRemove").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY), textRenderer)/*.alignLeft()*/, 4, 0, 1, 20, LayoutSettings.defaults().paddingTop(2)));
		builder.add(previewWidget = grid.addChild(new StringWidget(20 * 16, textRenderer.lineHeight, Component.empty(), textRenderer).setMaxWidth(20 * 16, StringWidget.TextOverflow.SCROLLING), 5, 0, 1, 20, LayoutSettings.defaults().paddingVertical(2).alignHorizontallyCenter()));
		widgets = builder.build();
		grid.arrangeElements();
		grid.setPosition(getX() + PADDING, getY() + PADDING);
		setSize(grid.getWidth() + PADDING * 2, grid.getHeight() + PADDING * 2);
		selectionStart = selectionEnd = textString.length();
	}

	// Makes it easier for Aaron Mod to add a chroma colour button
	private void addFormattingButtons(ImmutableList.Builder<AbstractWidget> builder) {
		for (int i = 0; i < formattingButtons.length; i++) {
			FormattingButton button = formattingButtons[i];
			builder.add(grid.addChild(button, 0, i));
		}

		int colorButtonIndex = 0;
		for (ChatFormatting formatting : ChatFormatting.values()) {
			if (formatting.isColor()) {
				builder.add(grid.addChild(new ColorButton(formatting), 2, colorButtonIndex++));
			}
		}
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		grid.setX(getX() + PADDING);
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		grid.setY(getY() + PADDING);
	}

	public void setItem(ItemStack stack) {
		uuid = stack.getUuid();
		setText(stack.getHoverName().copy(), false);
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
		context.blitSprite(
				RenderPipelines.GUI_TEXTURED,
				INNER_SPACE_TEXTURE,
				getX(),
				getY(),
				getWidth(),
				getHeight());
		if (Debug.debugEnabled()) {
			context.drawString(textRenderer, Component.literal("Selection Start: " + selectionStart + ", Selection End: " + selectionEnd), getX(), getBottom(), -1);
			context.drawString(textRenderer, Component.literal("Insert Style: " + (insertAs == null ? "null" : insertAs.toString())), getX(), getBottom() + 10, -1);
		}
		for (AbstractWidget widget : widgets) {
			widget.render(context, mouseX, mouseY, deltaTicks);
		}
	}

	/**
	 * Creates a gradient that goes from {@link CustomizeNameWidget#selectionStart} to {@link CustomizeNameWidget#selectionEnd}
	 *
	 * @param startColor the color at the start of the gradient
	 * @param endColor   the color at the end of the gradient
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
	 *
	 * @param style the style to set
	 */
	private void setStyle(Style style) {
		if (selectionStart == selectionEnd) {
			insertAs = style.applyTo(insertAs == null ? Style.EMPTY : insertAs);
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
	 *
	 * @param text the text to set
	 */
	public void setText(Component text, boolean updateConfig) {
		this.text = text;
		textString = text.getString();
		if (updateConfig && !uuid.isEmpty()) {
			SkyblockerConfigManager.updateOnly(config -> {
				if (textString.isBlank()) config.general.customItemNames.remove(uuid);
				else config.general.customItemNames.put(uuid, text.copy().setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.WHITE)));
			});
		}
		previewWidget.setMessage(text);
		grid.arrangeElements();

		// called before init
		if (textField != null) textField.updateMePrettyPlease = true;
	}

	public void setText(Component text) {
		setText(text, true);
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return widgets;
	}

	@Override
	public boolean charTyped(CharacterEvent input) {
		if (super.charTyped(input) || textField.isFocused()) return true;
		setFocused(textField);
		return textField.charTyped(input);
	}

	/**
	 * Inserts the given text at the current cursor position or replaces the selected text.
	 * If the text field is empty, it sets the text to the given string.
	 *
	 * @param str the text to insert
	 */
	public void insertText(String str) {
		str = StringUtil.filterText(str);
		if (textString.isEmpty()) {
			setText(Component.literal(str).setStyle(insertAs != null ? insertAs : Style.EMPTY));
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
	 * @param left      whether to move left or right
	 * @param shiftHeld whether shift is held
	 * @param ctrlHeld  whether ctrl is held
	 */
	private void moveCursor(boolean left, boolean shiftHeld, boolean ctrlHeld) {
		if (left && selectionStart == 0 || (!left && selectionStart == textString.length())) return;
		if (ctrlHeld) {
			selectionStart = getWordSkipPosition(left);
		} else {
			selectionStart = Util.offsetByCodepoints(textString, selectionStart, left ? -1 : 1);
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
	 * @param left     whether to erase left or right
	 * @param ctrlHeld whether ctrl is held
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

	@Override
	public void setFocused(boolean focused) {
		if (!focused) setFocused(null);
	}

	private class FormattingButton extends AbstractButton {
		private boolean enabled;
		private final ChatFormatting format;
		private final Predicate<Style> isEnabled;

		protected FormattingButton(Component message, ChatFormatting format, Predicate<Style> isEnabled) {
			super(0, 0, 16, 16, message);
			this.format = format;
			this.isEnabled = isEnabled;
			setTooltip(Tooltip.create(ConfigUtils.FORMATTING_FORMATTER.apply(format))); // Yoink from config utils hehhehehehehe
		}

		protected FormattingButton(String str, ChatFormatting format, Predicate<Style> isEnabled) {
			this(Component.literal(str).withStyle(format), format, isEnabled);
		}

		private void update(Style style) {
			setEnabled(isEnabled.test(style));
		}

		@Override
		public void onPress(InputWithModifiers input) {
			setEnabled(!enabled);
			switch (format) {
				case BOLD -> setStyle(Style.EMPTY.withBold(enabled));
				case ITALIC -> setStyle(Style.EMPTY.withItalic(enabled));
				case UNDERLINE -> setStyle(Style.EMPTY.withUnderlined(enabled));
				case STRIKETHROUGH -> setStyle(Style.EMPTY.withStrikethrough(enabled));
				case OBFUSCATED -> setStyle(Style.EMPTY.withObfuscated(enabled));
				default -> throw new IllegalStateException("Unexpected value: " + format);
			}
		}

		private void setEnabled(boolean enabled) {
			this.enabled = enabled;
			setMessage(getMessage().copy().withColor(enabled ? CommonColors.YELLOW : CommonColors.WHITE));
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {}

		@Override
		protected void renderContents(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
			this.renderDefaultSprite(context);
			this.renderDefaultLabel(context.textRenderer());
		}
	}

	private class ColorButton extends AbstractButton {
		private final ChatFormatting color;
		private final int intColor;

		private ColorButton(ChatFormatting format) {
			super(0, 0, 16, 16, ConfigUtils.FORMATTING_FORMATTER.apply(format));
			setTooltip(Tooltip.create(getMessage()));
			this.color = format;
			this.intColor = ARGB.opaque(color.getColor());
		}

		@Override
		public void onPress(InputWithModifiers input) {
			setStyle(Style.EMPTY.withColor(color));
		}

		@Override
		public void renderContents(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
			this.renderDefaultSprite(context);
			context.fill(getX() + 2, getY() + 2, getRight() - 2, getBottom() - 2, intColor);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {}

	}

	/**
	 * Used to capture inputs and render the text. Most logic is done in the screen itself
	 */
	private class TextField extends AbstractWidget {
		private int renderedSelectionStart;

		private int renderedSelectionEnd;
		private boolean updateMePrettyPlease = false;
		private int renderStart;
		private int renderEnd;

		private TextField() {
			super(0, 0, 320, 20, Component.literal("TextField"));
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
			if (renderedSelectionStart != selectionStart || renderedSelectionEnd != selectionEnd || updateMePrettyPlease) {
				renderedSelectionStart = selectionStart;
				renderedSelectionEnd = selectionEnd;
				updateMePrettyPlease = false;
				GetRenderWidthVisitor getRenderWidthVisitor = new GetRenderWidthVisitor(selectionStart, selectionEnd);
				text.visit(getRenderWidthVisitor, Style.EMPTY);
				renderStart = getRenderWidthVisitor.getWidths().firstInt();
				renderEnd = getRenderWidthVisitor.getWidths().secondInt();
			}

			context.fill(getX(), getY(), getRight(), getBottom(), CommonColors.BLACK);
			HudHelper.drawBorder(context, getX(), getY(), getWidth(), getHeight(), isFocused() ? CommonColors.WHITE : CommonColors.GRAY);
			int textX = getTextX();
			int textY = getY() + (getHeight() - textRenderer.lineHeight) / 2;

			if (renderStart != renderEnd) {
				context.fill(textX + renderStart, textY, textX + renderEnd, textY + textRenderer.lineHeight, CommonColors.BLUE);
			}
			if (this.isFocused()) {
				context.vLine(textX + (selectionStart < selectionEnd ? renderStart : renderEnd) - 1, textY - 1, textY + textRenderer.lineHeight, CommonColors.WHITE);
			}

			context.drawString(textRenderer, text, textX, textY, -1, false);

			this.handleCursor(context);
		}

		@Override
		protected void handleCursor(GuiGraphics context) {
			if (this.isHovered()) {
				context.requestCursor(CursorTypes.IBEAM);
			}
		}

		@Override
		public boolean keyPressed(KeyEvent input) {
			boolean captured = true;
			switch (input.key()) {
				case GLFW.GLFW_KEY_LEFT -> moveCursor(true, input.hasShiftDown(), input.hasControlDownWithQuirk());
				case GLFW.GLFW_KEY_RIGHT -> moveCursor(false, input.hasShiftDown(), input.hasControlDownWithQuirk());
				case GLFW.GLFW_KEY_BACKSPACE -> erase(true, input.hasControlDownWithQuirk());
				case GLFW.GLFW_KEY_DELETE -> erase(false, input.hasControlDownWithQuirk());
				default -> captured = false;
			}
			if (captured) return true;
			assert client != null;
			if (input.isSelectAll()) {
				selectionStart = 0;
				selectionEnd = textString.length();
				updateStyleButtons();
				captured = true;
			} else if (input.isCopy()) {
				client.keyboardHandler.setClipboard(text.getString().substring(selectionStart, selectionEnd));
				captured = true;
			} else if (input.isPaste()) {
				String clipboard = client.keyboardHandler.getClipboard();
				if (!clipboard.isEmpty()) {
					insertText(clipboard);
				}
				captured = true;
			} else if (input.isCut()) {
				client.keyboardHandler.setClipboard(text.getString().substring(selectionStart, selectionEnd));
				insertText("");
				captured = true;
			}
			return captured;
		}

		@Override
		public boolean charTyped(CharacterEvent input) {
			if (!active) {
				return false;
			} else if (input.isAllowedChatCharacter()) {
				insertText(input.codepointAsString());
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			GetClickedPositionVisitor getClickedPositionVisitor = new GetClickedPositionVisitor((int) click.x() - getTextX());
			text.visit(getClickedPositionVisitor, Style.EMPTY);
			selectionStart = selectionEnd = getClickedPositionVisitor.getPosition() < 0 ? textString.length() : getClickedPositionVisitor.getPosition();
			updateStyleButtons();
		}

		@Override
		protected void onDrag(MouseButtonEvent click, double offsetX, double offsetY) {
			GetClickedPositionVisitor getClickedPositionVisitor = new GetClickedPositionVisitor((int) click.x() - getTextX());
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
		protected void updateWidgetNarration(NarrationElementOutput builder) {}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}

	@Override
	protected int contentHeight() {
		return 0;
	}

	@Override
	protected double scrollRate() {
		return 0;
	}
}
