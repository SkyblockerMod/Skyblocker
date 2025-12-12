package de.hysky.skyblocker.skyblock.tabhud.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.math.ColorHelper;

class StyledButtonWidget extends ButtonWidget {
	StyledButtonWidget(int width, int height, net.minecraft.text.Text message, PressAction onPress) {
		super(0, 0, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
	}

	@Override
	protected void drawIcon(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		int y = getY() - 1;
		int y2 = y + getHeight();
		TopBarWidget.drawButtonBorder(context, getX(), y, y2);
		TopBarWidget.drawButtonBorder(context, getRight(), y, y2);

		if (isHovered()) {
			context.fill(getX(), y, getRight() + 1, y2, ColorHelper.withAlpha(100, 0));
		} else {
			context.fill(getX(), y, getRight() + 1, y2, ColorHelper.withAlpha(50, 0));
		}
		this.drawLabel(context.getHoverListener(this, DrawContext.HoverType.NONE));
		if (this.isHovered()) {
			context.setCursor(this.isInteractable() ? StandardCursors.POINTING_HAND : StandardCursors.NOT_ALLOWED);
		}
	}
}
