package de.hysky.skyblocker.skyblock.tabhud.config;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

class ToggleButtonWidget extends PressableWidget {

	private final BooleanConsumer onPress;
	private boolean state;

	ToggleButtonWidget(int width, int height, Text message, BooleanConsumer onPress) {
		super(0, 0, width, height, message);
		this.onPress = onPress;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		MinecraftClient minecraftClient = MinecraftClient.getInstance();
		int y = getY() - 1;
		int y2 = y + getHeight();
		context.drawVerticalLine(getX() - 1, y, y2, ColorHelper.withAlpha(15, -1));
		context.drawVerticalLine(getX(), y, y2, ColorHelper.withAlpha(100, 0));
		context.drawVerticalLine(getX() + 1, y, y2, ColorHelper.withAlpha(15, -1));


		context.drawVerticalLine(getRight() - 1, y, y2, ColorHelper.withAlpha(15, 0));
		context.drawVerticalLine(getRight(), y, y2, ColorHelper.withAlpha(100, 0));
		context.drawVerticalLine(getRight() + 1, y, y2, ColorHelper.withAlpha(15, 0));

		if (isHovered()) {
			context.fill(getX(), y, getRight() + 1, y2, ColorHelper.withAlpha(100, 0));
		} else {
			context.fill(getX(), y, getRight() + 1, y2, ColorHelper.withAlpha(50, 0));
		}
		int i = this.active ? 16777215 : 10526880;
		this.drawMessage(context, minecraftClient.textRenderer, i | MathHelper.ceil(this.alpha * 255.0F) << 24);
	}

	@Override
	public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
		int textWidth = textRenderer.getWidth(getMessage());
		int startX, endX;
		int squareX;
		int i = textWidth + 9 + 2;
		if (i > getWidth() - 6) {
			squareX = getRight() - 3 - 9; // margin + square size
			endX = squareX - 2;
			startX = getX() + 3;
		} else {
			int centerX = getX() + getWidth() / 2;
			squareX = centerX + i / 2 - 9;
			startX = centerX - i / 2;
			endX = squareX - 2;
		}
		drawScrollableText(context, textRenderer, getMessage(), startX, getY(), endX, getBottom(), color);
		int squareY = getY() + (getHeight() - 9) / 2;
		context.drawBorder(squareX, squareY, 9, 9, color);
		if (state) context.fill(squareX + 2, squareY + 2, squareX + 7, squareY + 7, color);
	}

	public void setState(boolean state) {
		this.state = state;
	}

	public boolean getState() {
		return state;
	}

	@Override
	public void onPress() {
		state = !state;
		onPress.accept(state);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
