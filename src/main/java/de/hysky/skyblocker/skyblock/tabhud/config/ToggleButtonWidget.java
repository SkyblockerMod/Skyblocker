package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.utils.render.HudHelper;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.input.AbstractInput;
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
	protected void drawIcon(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		MinecraftClient minecraftClient = MinecraftClient.getInstance();
		int y = getY() - 1;
		int y2 = y + getHeight();
		TopBarWidget.drawButtonBorder(context, getX(), y, y2);
		TopBarWidget.drawButtonBorder(context, getRight(), y, y2);

		if (isHovered()) {
			context.fill(getX(), y, getRight() + 1, y2, ColorHelper.withAlpha(100, 0));
		} else {
			context.fill(getX(), y, getRight() + 1, y2, ColorHelper.withAlpha(50, 0));
		}
		int color = (this.active ? 16777215 : 10526880) | MathHelper.ceil(this.alpha * 255.0F) << 24;
		int textWidth = minecraftClient.textRenderer.getWidth(getMessage());
		int startX, endX;
		int squareX;
		int paddedTextWidth = textWidth + 9 + 2;
		if (paddedTextWidth > getWidth() - 6) {
			squareX = getRight() - 3 - 9; // margin + square size
			endX = squareX - 2;
			startX = getX() + 3;
		} else {
			int centerX = getX() + getWidth() / 2;
			squareX = centerX + paddedTextWidth / 2 - 9;
			startX = centerX - paddedTextWidth / 2;
			endX = squareX - 2;
		}
		context.getHoverListener(this, DrawContext.HoverType.NONE).text(
				getMessage(),
				startX,
				endX,
				getY(),
				getBottom()
		);
		int squareY = getY() + (getHeight() - 9) / 2;
		HudHelper.drawBorder(context, squareX, squareY, 9, 9, color);
		if (state) context.fill(squareX + 2, squareY + 2, squareX + 7, squareY + 7, color);
	}

	public void setState(boolean state) {
		this.state = state;
	}

	public boolean getState() {
		return state;
	}

	@Override
	public void onPress(AbstractInput input) {
		state = !state;
		onPress.accept(state);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
