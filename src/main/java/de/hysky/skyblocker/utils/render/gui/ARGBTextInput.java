package de.hysky.skyblocker.utils.render.gui;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.OptionalInt;
import java.util.function.IntConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.CommonColors;

public class ARGBTextInput extends AbstractWidget {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final ChatFormatting[] FORMATTINGS = new ChatFormatting[] {ChatFormatting.WHITE, ChatFormatting.RED, ChatFormatting.GREEN, ChatFormatting.BLUE};
	private static final String HEXADECIMAL_CHARS = "0123456789aAbBcCdDeEfF";

	/**
	 * Length in characters of the input
	 */
	private final int length;
	private final boolean drawBackground;
	private final Font textRenderer;
	/**
	 * Whether the alpha channel can be changed
	 */
	private final boolean hasAlpha;
	/**
	 * Mask to have full alpha if {@link ARGBTextInput#hasAlpha} is {@code false}
	 */
	private final int alphaMask;

	private String input;
	int index = 0;

	private @Nullable IntConsumer onChange = null;

	/**
	 * Height and width are automatically computed to be the size of the hex number + some padding if {@code drawBackground} is true.
	 * If the size needs to be changed, use {@link ARGBTextInput#setWidth(int)} and {@link ARGBTextInput#setHeight(int)}.
	 *
	 * @param x x position
	 * @param y y position
	 * @param textRenderer text renderer to render the text (duh!)
	 * @param drawBackground draws a black background and a white border if true
	 * @param hasAlpha if the controller allows to change the alpha color. If false alpha is FF.
	 *
	 * @see ARGBTextInput#setOnChange(IntConsumer)
	 */
	public ARGBTextInput(int x, int y, Font textRenderer, boolean drawBackground, boolean hasAlpha) {
		super(x, y, textRenderer.width(hasAlpha ? "AAAAAAAA" : "AAAAAA") + (drawBackground ? 6 : 0), 10 + (drawBackground ? 4 : 0), Component.nullToEmpty("ARGBTextInput"));
		this.drawBackground = drawBackground;
		this.textRenderer = textRenderer;
		this.length = hasAlpha ? 8 : 6;
		this.hasAlpha = hasAlpha;
		this.alphaMask = hasAlpha ? 0 : 0xFF000000;
		this.input = hasAlpha ? "FFFFFFFF" : "FFFFFF";
	}

	/**
	 * Constructor without alpha channel control.
	 * <br/>
	 * Height and width are automatically computed to be the size of the hex number + some padding if {@code drawBackground} is true.
	 * If the size needs to be changed, use {@link ARGBTextInput#setWidth(int)} and {@link ARGBTextInput#setHeight(int)}.
	 *
	 * @param x x position
	 * @param y y position
	 * @param textRenderer text renderer to render the text (duh!)
	 * @param drawBackground draws a black background and a white border if true
	 *
	 * @see ARGBTextInput#setOnChange(IntConsumer)
	 */
	public ARGBTextInput(int x, int y, Font textRenderer, boolean drawBackground) {
		this(x, y, textRenderer, drawBackground, false);
	}

	protected OptionalInt getOptionalARGBColor(String input) {
		try {
			int i = Integer.parseUnsignedInt(input, 16);
			return OptionalInt.of(alphaMask | i);
		} catch (NumberFormatException e) {
			LOGGER.error("Could not parse rgb color", e);
		}
		return OptionalInt.empty();
	}

	/**
	 * @return the color, or white if something somehow went wrong
	 */
	public int getARGBColor() {
		return getOptionalARGBColor(input).orElse(CommonColors.WHITE);
	}

	public void setARGBColor(int argb) {
		input = String.format(hasAlpha ? "%08X" : "%06X", argb & (~alphaMask));
	}

	/**
	 * Sets a consumer that will be called whenever the color is changed by the user (and not when {@link ARGBTextInput#setARGBColor(int)} is called) with the new color.
	 * The alpha channel will be at 255 (or FF)
	 * @param onChange the consumer
	 */
	public void setOnChange(@Nullable IntConsumer onChange) {
		this.onChange = onChange;
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		int selectionStart = textRenderer.width(input.substring(0, index));
		int selectionEnd = textRenderer.width(input.substring(0, index + 1));
		int textX = getX() + (drawBackground ? 3 : 0);
		int textY = getY() + (getHeight() - textRenderer.lineHeight) / 2;
		if (drawBackground) {
			context.fill(getX(), getY(), getRight(), getBottom(), isFocused() ? CommonColors.WHITE : CommonColors.GRAY);
			context.fill(getX() + 1, getY() + 1, getRight() - 1, getBottom() - 1, CommonColors.BLACK);
		}

		if (isFocused()) {
			context.fill(
					textX + selectionStart,
					textY,
					textX + selectionEnd,
					textY + textRenderer.lineHeight,
					0xFF_00_BB_FF
			);
			context.fill(
					textX + selectionStart,
					textY + textRenderer.lineHeight - 1,
					textX + selectionEnd,
					textY + textRenderer.lineHeight,
					CommonColors.WHITE
			);
		}
		context.drawString(
				textRenderer,
				visitor -> {
					int start = hasAlpha ? 0 : 1;
					for (int i = 0; i < input.length(); i++) {
						if (!visitor.accept(i, isHoveredOrFocused() ? Style.EMPTY.applyFormat(FORMATTINGS[i / 2 + start]) : Style.EMPTY, input.charAt(i))) return false;
					}
					return true;
				},
				textX,
				textY,
				CommonColors.WHITE,
				true
		);

		if (this.isHovered()) {
			context.requestCursor(CursorTypes.IBEAM);
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}

	@Override
	public boolean keyPressed(KeyEvent keyInput) {
		if (!isFocused()) return false;
		boolean bl = switch (keyInput.key()) {
			case GLFW.GLFW_KEY_DELETE -> {
				StringBuilder builder = new StringBuilder(input);
				builder.setCharAt(index, '0');
				input = builder.toString();
				yield true;
			}
			case GLFW.GLFW_KEY_BACKSPACE -> {
				StringBuilder builder = new StringBuilder(input);
				builder.setCharAt(index, '0');
				input = builder.toString();
				index = Math.max(0, index - 1);
				yield true;
			}
			case GLFW.GLFW_KEY_LEFT -> {
				index = Math.max(0, index - 1);
				yield true;
			}
			case GLFW.GLFW_KEY_RIGHT -> {
				index = Math.min(length - 1, index + 1);
				yield true;
			}
			default -> false;
		};
		if (bl) {
			callOnChange();
			return true;
		} else {
			if (keyInput.isCopy()) {
				Minecraft.getInstance().keyboardHandler.setClipboard(input);
				return true;
			} else if (keyInput.isPaste()) {
				String clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
				if (clipboard.startsWith("#")) clipboard = clipboard.substring(1);
				String s = clipboard.substring(0, Math.min(hasAlpha ? 8 : 6, clipboard.length()));
				getOptionalARGBColor(s.toUpperCase(Locale.ENGLISH)).ifPresent(color -> {
					setARGBColor(color);
					callOnChange();
				});
				return true;

			}
		}
		return super.keyPressed(keyInput);
	}

	@Override
	public boolean charTyped(CharacterEvent input) {
		if (!isFocused()) return false;
		if (HEXADECIMAL_CHARS.contains(input.codepointAsString())) {
			this.input = new StringBuilder(this.input).replace(index, index+1, input.codepointAsString().toUpperCase(Locale.ENGLISH)).toString();
			index = Math.min(length - 1, index + 1);
			callOnChange();
			return true;
		}

		return super.charTyped(input);
	}

	protected void callOnChange() {
		if (onChange != null) {
			onChange.accept(getARGBColor());
		}
	}

	@Override
	public void onClick(MouseButtonEvent click, boolean doubled) {
		super.onClick(click, doubled);
		index = findClickedChar((int) click.x());
	}

	@Override
	protected void onDrag(MouseButtonEvent click, double deltaX, double deltaY) {
		super.onDrag(click, deltaX, deltaY);
		index = findClickedChar((int) click.x());
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!isMouseOver(mouseX, mouseY)) return false;
		int i = findClickedChar((int) mouseX) / 2;
		int beginIndex = i * 2;
		OptionalInt anInt;
		try {
			int parsedInt = Integer.parseUnsignedInt(input.substring(beginIndex, beginIndex + 2), 16);
			anInt = OptionalInt.of(parsedInt);
		} catch (NumberFormatException e) {
			anInt = OptionalInt.empty();
			LOGGER.error("[Skyblocker] Failed to parse integer", e);
		}
		if (anInt.isPresent()) {
			int prev = anInt.getAsInt();
			int newInt = prev + (int) ((int) Math.signum(verticalAmount) * Math.ceil(Math.abs(verticalAmount)));
			newInt = Math.clamp(newInt, 0, 255);
			if (newInt != prev) {
				String format = String.format("%02X", newInt);
				input = new StringBuilder(input).replace(beginIndex, beginIndex + 2, format).toString();
				callOnChange();
			}
		}
		return true;
	}

	private int findClickedChar(int mouseX) {
		return Math.clamp(textRenderer.plainSubstrByWidth(input, mouseX - getX() - (drawBackground ? 3 : 0)).length(), 0, length - 1);
	}


}
