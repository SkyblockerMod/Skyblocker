package de.hysky.skyblocker.utils.render.gui;

import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.OptionalInt;
import java.util.function.IntConsumer;

public class ARGBTextInput extends ClickableWidget {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final Formatting[] FORMATTINGS = new Formatting[] {Formatting.WHITE, Formatting.RED, Formatting.GREEN, Formatting.BLUE};
	private static final String HEXADECIMAL_CHARS = "0123456789aAbBcCdDeEfF";

	/**
	 * Length in characters of the input
	 */
	private final int length;
	private final boolean drawBackground;
	private final TextRenderer textRenderer;
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
	 * @see ARGBTextInput#setOnChange(IntConsumer)
	 *
	 * @param x x position
	 * @param y y position
	 * @param textRenderer text renderer to render the text (duh!)
	 * @param drawBackground draws a black background and a white border if true
	 * @param hasAlpha if the controller allows to change the alpha color. If false alpha is FF.
	 *
	 */
	public ARGBTextInput(int x, int y, TextRenderer textRenderer, boolean drawBackground, boolean hasAlpha) {
		super(x, y, textRenderer.getWidth(hasAlpha ? "AAAAAAAA" : "AAAAAA") + (drawBackground ? 6 : 0), 10 + (drawBackground ? 4 : 0), Text.of("ARGBTextInput"));
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
	 * @see ARGBTextInput#setOnChange(IntConsumer)
	 *
	 * @param x x position
	 * @param y y position
	 * @param textRenderer text renderer to render the text (duh!)
	 * @param drawBackground draws a black background and a white border if true
	 *
	 */
	public ARGBTextInput(int x, int y, TextRenderer textRenderer, boolean drawBackground) {
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
		return getOptionalARGBColor(input).orElse(Colors.WHITE);
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
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		int selectionStart = textRenderer.getWidth(input.substring(0, index));
		int selectionEnd = textRenderer.getWidth(input.substring(0, index + 1));
		int textX = getX() + (drawBackground ? 3 : 0);
		int textY = getY() + (getHeight() - textRenderer.fontHeight) / 2;
		if (drawBackground) {
			context.fill(getX(), getY(), getRight(), getBottom(), isFocused() ? Colors.WHITE: Colors.GRAY);
			context.fill(getX() + 1, getY() + 1, getRight() - 1, getBottom() - 1, Colors.BLACK);
		}

		if (isFocused()) {
			context.fill(
					textX + selectionStart,
					textY,
					textX + selectionEnd,
					textY + textRenderer.fontHeight,
					0xFF_00_BB_FF
			);
			context.fill(
					textX + selectionStart,
					textY + textRenderer.fontHeight - 1,
					textX + selectionEnd,
					textY + textRenderer.fontHeight,
					Colors.WHITE
			);
		}
		context.drawText(
				textRenderer,
				visitor -> {
					int start = hasAlpha ? 0 : 1;
					for (int i = 0; i < input.length(); i++) {
						if (!visitor.accept(i, isSelected() ? Style.EMPTY.withFormatting(FORMATTINGS[i / 2 + start]) : Style.EMPTY, input.charAt(i))) return false;
					}
					return true;
				},
				textX,
				textY,
				-1,
				true
		);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!isFocused()) return false;
		boolean bl = switch (keyCode) {
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
			if (Screen.isCopy(keyCode)) {
				MinecraftClient.getInstance().keyboard.setClipboard(input);
				return true;
			} else if (Screen.isPaste(keyCode)) {
				String clipboard = MinecraftClient.getInstance().keyboard.getClipboard();
				String s = clipboard.substring(0, 6);
				getOptionalARGBColor(s.toUpperCase(Locale.ENGLISH)).ifPresent(color -> {
					input = s;
					callOnChange();
				});
				return true;

			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (!isFocused()) return false;
		if (HEXADECIMAL_CHARS.indexOf(chr) >= 0) {
			input = new	StringBuilder(input).replace(index, index+1, String.valueOf(chr).toUpperCase(Locale.ENGLISH)).toString();
			index = Math.min(length - 1, index + 1);
			callOnChange();
			return true;
		}

		return super.charTyped(chr, modifiers);
	}

	protected void callOnChange() {
		if (onChange != null) {
			onChange.accept(getARGBColor());
		}
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		super.onClick(mouseX, mouseY);
		findClickedChar((int) mouseX);
	}

	@Override
	protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
		super.onDrag(mouseX, mouseY, deltaX, deltaY);
		findClickedChar((int) mouseX);
	}

	private void findClickedChar(int mouseX) {
		index = Math.clamp(textRenderer.trimToWidth(input, mouseX - getX() - (drawBackground ? 3 : 0)).length(), 0, length - 1);
	}


}
