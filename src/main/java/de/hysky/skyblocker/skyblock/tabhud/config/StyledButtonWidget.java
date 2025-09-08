package de.hysky.skyblocker.skyblock.tabhud.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

class StyledButtonWidget extends ButtonWidget {
	StyledButtonWidget(int width, int height, Text message, PressAction onPress) {
		super(0, 0, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		MinecraftClient client = MinecraftClient.getInstance();
		int y = getY() - 1;
		int y2 = y + getHeight();
		TopBarWidget.drawButtonBorder(context, getX(), y, y2);
		TopBarWidget.drawButtonBorder(context, getRight(), y, y2);

		if (isHovered()) {
			context.fill(getX(), y, getRight() + 1, y2, ColorHelper.withAlpha(100, 0));
		} else {
			context.fill(getX(), y, getRight() + 1, y2, ColorHelper.withAlpha(50, 0));
		}
		int i = ColorHelper.withAlpha(this.alpha, this.active ? -1 : -6250336);
		this.drawMessage(context, client.textRenderer, i);
	}
}
