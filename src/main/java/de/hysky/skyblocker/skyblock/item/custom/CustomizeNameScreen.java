package de.hysky.skyblocker.skyblock.item.custom;

import de.hysky.skyblocker.config.ConfigUtils;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.*;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class CustomizeNameScreen extends Screen {
	private Text text = Text.empty();
	private String textString = "";

	private TextField textField;
	private FormattingButton[] formattingButtons;

	private int selectionStart;
	private int selectionEnd;

	private @Nullable Style insertAs;

	protected CustomizeNameScreen() {
		super(Text.literal("Customize Item Name"));
	}

	@Override
	protected void init() {
		textField = new TextField();
		formattingButtons = new FormattingButton[]{
				new FormattingButton("B", Formatting.BOLD, Style::isBold),
				new FormattingButton("I", Formatting.ITALIC, Style::isItalic),
				new FormattingButton("U", Formatting.UNDERLINE, Style::isUnderlined),
				new FormattingButton("S", Formatting.STRIKETHROUGH, Style::isStrikethrough),
				new FormattingButton("|||", Formatting.OBFUSCATED, Style::isObfuscated),
		};
		addDrawableChild(textField);
		for (FormattingButton button : formattingButtons) {
			addDrawableChild(button);
		}
		refreshWidgetPositions();
	}

	@Override
	protected void refreshWidgetPositions() {
		int textBoxWidth = 300;
		int x = (width - textBoxWidth) / 2;
		textField.setDimensionsAndPosition(textBoxWidth, 20, x, height / 2);
		for (int i = 0; i < formattingButtons.length; i++) {
			formattingButtons[i].setPosition(x + i * 16, height / 2 - 16);
		}
	}

	private void setStyle(Style style) {
		if (selectionStart == selectionEnd) {
			insertAs = style.withParent(insertAs == null ? Style.EMPTY : insertAs);
			return;
		}
		SetStyleVisitor setStyleVisitor = new SetStyleVisitor(style);
		text.visit(setStyleVisitor, Style.EMPTY);
		setText(setStyleVisitor.newText);
	}

	private void updateStyleButtons() {
		GetStyleVisitor styleVisitor = new GetStyleVisitor();
		text.visit(styleVisitor, Style.EMPTY);
		Style style = styleVisitor.style;
		for (FormattingButton button : formattingButtons) {
			button.update(style);
		}
	}

	public void setText(Text text) {
		this.text = text;
		textString = text.getString();
		textField.updateMePrettyPlease = true;
	}

	public void insertText(String str) {
		if (text.getContent() == PlainTextContent.EMPTY) {
			setText(Text.literal(str).setStyle(insertAs != null ? insertAs : Style.EMPTY));
		} else {
			InsertTextVisitor visitor = new InsertTextVisitor(str);
			text.visit(visitor, Style.EMPTY);
			setText(visitor.getNewText());
		}

		selectionStart = Math.min(selectionStart, selectionEnd) + str.length();
		selectionEnd = selectionStart;
		updateStyleButtons();
	}

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
			super(0, 0, 0, 20, Text.literal("TextField"));
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			if (renderedSelectionStart != selectionStart || renderedSelectionEnd != selectionEnd || updateMePrettyPlease) {
				renderedSelectionStart = selectionStart;
				renderedSelectionEnd = selectionEnd;
				updateMePrettyPlease = false;
				GetRenderWidthVisitor getRenderWidthVisitor = new GetRenderWidthVisitor();
				text.visit(getRenderWidthVisitor, Style.EMPTY);
				renderStart = getRenderWidthVisitor.getWidths().firstInt();
				renderEnd = getRenderWidthVisitor.getWidths().secondInt();
			}

			context.drawText(textRenderer, "start: " + selectionStart + " end: " + selectionEnd, getX(), getBottom(), -1, true);

			context.fill(getX(), getY(), getRight(), getBottom(), Colors.BLACK);
			context.drawBorder(getX(), getY(), getWidth(), getHeight(), isFocused() ? Colors.WHITE : Colors.GRAY);
			int textX = getX() + 2;
			int textY = getY() + (getHeight() - textRenderer.fontHeight) / 2;

			if (renderStart != renderEnd) {
				context.fill(textX + renderStart, textY, textX + renderEnd, textY + textRenderer.fontHeight, Colors.BLUE);
			}
			if (isFocused()) context.drawVerticalLine(textX + (selectionStart < selectionEnd ? renderStart : renderEnd) - 1, textY, textY + textRenderer.fontHeight, Colors.WHITE);

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
			if (!isFocused()) return;
			GetClickedPositionVisitor getClickedPositionVisitor = new GetClickedPositionVisitor((int) mouseX - getX() - 2);
			text.visit(getClickedPositionVisitor, Style.EMPTY);
			selectionStart = selectionEnd = getClickedPositionVisitor.position < 0 ? textString.length() : getClickedPositionVisitor.position;
			updateStyleButtons();
		}

		@Override
		protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
			if (!isFocused()) return;
			GetClickedPositionVisitor getClickedPositionVisitor = new GetClickedPositionVisitor((int) mouseX - getX() - 2);
			text.visit(getClickedPositionVisitor, Style.EMPTY);
			selectionStart = getClickedPositionVisitor.position < 0 ? textString.length() : getClickedPositionVisitor.position;
			updateStyleButtons();
		}

		@Override
		public void playDownSound(SoundManager soundManager) {}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}


	private abstract class BaseVisitor implements StringVisitable.StyledVisitor<Void> {
		protected int selStart;
		protected int selSize;

		private BaseVisitor() {
			this.selStart = Math.min(selectionStart, selectionEnd);
			this.selSize = Math.abs(selectionStart - selectionEnd);
		}

		@Override
		public final Optional<Void> accept(Style style, String asString) {
			visit(style, asString);
			return Optional.empty();
		}

		protected abstract void visit(Style style, String asString);
	}

	private class GetRenderWidthVisitor extends BaseVisitor {
		private final IntIntMutablePair widths = new IntIntMutablePair(0, 0);
		private final MutableText text = Text.empty();

		@Override
		protected void visit(Style style, String asString) {
			if (asString.length() < selStart) { // not yet where we want to insert
				text.append(Text.literal(asString).setStyle(style));
				selStart -= asString.length();
				return;
			}

			if (selStart >= 0) {
				text.append(Text.literal(asString.substring(0, selStart)).setStyle(style));
				widths.first(textRenderer.getWidth(text));
				asString = asString.substring(selStart);
				selStart = -1;
				if (selSize == 0) widths.second(textRenderer.getWidth(text));
			}
			if (selSize == 0) return;
			if (asString.length() <= selSize) {
				text.append(Text.literal(asString).setStyle(style));
				selSize -= asString.length();
				if (selSize == 0) widths.second(textRenderer.getWidth(text));
			} else {
				text.append(Text.literal(asString.substring(0, selSize)).setStyle(style));
				widths.second(textRenderer.getWidth(text));
				selSize = 0;
			}
		}

		public IntIntPair getWidths() {
			return widths;
		}
	}

	private class GetClickedPositionVisitor extends BaseVisitor {
		private final MutableText text = Text.empty();
		private int position = -1;
		private final int x;

		private GetClickedPositionVisitor(int x) {
			this.x = x;
		}

		@Override
		protected void visit(Style style, String asString) {
			if (position >= 0) return;
			MutableText text1 = Text.literal(asString).setStyle(style);
			int originalWidth = textRenderer.getWidth(text);
			if (originalWidth + textRenderer.getWidth(text1) < x) {
				text.append(text1);
				return;
			}
			AtomicInteger atomicInteger = new AtomicInteger(0);
			TextContent content = new PlainTextContent() {
				@Override
				public String string() {
					return asString.substring(0, atomicInteger.get());
				}
				@Override
				public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
					return visitor.accept(string());
				}

				@Override
				public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
					return visitor.accept(style, string());
				}
			};
			MutableText text2 = MutableText.of(content).setStyle(style);
			while (originalWidth + textRenderer.getWidth(text2) < x && atomicInteger.get() < asString.length()) atomicInteger.incrementAndGet();
			position = Math.max(text.getString().length() + atomicInteger.get() - 1, 0);
		}
	}

	private class GetStyleVisitor extends BaseVisitor {
		private Style style = Style.EMPTY;
		private final boolean noSize = selSize == 0;
		private boolean done = false;

		@Override
		protected void visit(Style st, String asString) {
			if (done) return;
			if (asString.length() < selStart) {
				selStart -= asString.length();
				return;
			}
			if (selStart != 0) asString = asString.substring(selStart);
			selStart = 0;
			if (noSize) {
				this.style = st;
				this.done = true;
				return;
			}
			selSize -= asString.length();
			done = selSize <= 0;
			this.style = this.style
					.withBold(this.style.isBold() || st.isBold())
					.withItalic(this.style.isItalic() || st.isItalic())
					.withUnderline(this.style.isUnderlined() || st.isUnderlined())
					.withStrikethrough(this.style.isStrikethrough() || st.isStrikethrough())
					.withObfuscated(this.style.isObfuscated() || st.isObfuscated());
		}

		public Style getStyle() {
			return style;
		}
	}

	private class SetStyleVisitor extends BaseVisitor {
		private final MutableText newText = MutableText.of(new PlainTextContent.Literal(""));
		private final Style newStyle;

		private SetStyleVisitor(Style style) {
			super();
			this.newStyle = style;
		}

		private void addNewText(MutableText newText) {
			this.newText.append(newText);
		}

		@Override
		protected void visit(Style style, String asString) {
			if (asString.length() < selStart) { // not yet where we want to insert
				if (asString.isEmpty()) return;
				addNewText(Text.literal(asString).setStyle(style));
				selStart -= asString.length();
				return;
			}

			if (selStart >= 0) {
				String substring = asString.substring(0, selStart);
				addNewText(Text.literal(substring).setStyle(style));

				asString = asString.substring(selStart);
				insertAs = null;
				selStart = -1;
			}
			if (selSize <= 0) {
				if (!asString.isEmpty()) addNewText(Text.literal(asString).setStyle(style));
				return;
			}
			if (asString.length() <= selSize) {
				addNewText(Text.literal(asString).setStyle(newStyle.withParent(style)));
			}
			else {
				addNewText(Text.literal(asString.substring(0, selSize)).setStyle(newStyle.withParent(style)));
				addNewText(Text.literal(asString.substring(selSize)).setStyle(style));
			}
			selSize -= asString.length();
		}
	}

	private class InsertTextVisitor extends BaseVisitor {
		private final MutableText newText = MutableText.of(new PlainTextContent.Literal(""));
		private final String text;

		private InsertTextVisitor(String text) {
			super();
			this.text = text;
		}

		private void addNewText(MutableText newText) {
			this.newText.append(newText);
		}

		@Override
		public void visit(Style style, String asString) {
			if (asString.length() < selStart) { // not yet where we want to insert
				if (asString.isEmpty()) return;
				addNewText(Text.literal(asString).setStyle(style));
				selStart -= asString.length();
				return;
			}

			if (selStart >= 0) {
				String substring = asString.substring(0, selStart);
				if (insertAs != null && selSize == 0) {
					addNewText(Text.literal(substring).setStyle(style));
					if (!text.isEmpty()) addNewText(Text.literal(text).setStyle(insertAs.withParent(style)));
					insertAs = null;
				} else {
					addNewText(Text.literal(substring + text).setStyle(style));
				}
				asString = asString.substring(selStart);
				selStart = -1;
			}
			if (selSize <= 0) {
				if (!asString.isEmpty()) addNewText(Text.literal(asString).setStyle(style));
				return;
			}
			if (asString.length() > selSize) {
				addNewText(Text.literal(asString.substring(selSize)).setStyle(style));
			}
			selSize -= asString.length();
		}

		public MutableText getNewText() {
			return newText;
		}
	}
}
