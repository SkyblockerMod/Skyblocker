package de.hysky.skyblocker.skyblock.tabhud.config;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.util.ARGB;

class StyledButtonWidget extends Button {
	StyledButtonWidget(int width, int height, net.minecraft.network.chat.Component message, OnPress onPress) {
		super(0, 0, width, height, message, onPress, DEFAULT_NARRATION);
	}

	@Override
	protected void renderContents(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
		int y = getY() - 1;
		int y2 = y + getHeight();
		TopBarWidget.drawButtonBorder(context, getX(), y, y2);
		TopBarWidget.drawButtonBorder(context, getRight(), y, y2);

		if (isHovered()) {
			context.fill(getX(), y, getRight() + 1, y2, ARGB.color(100, 0));
		} else {
			context.fill(getX(), y, getRight() + 1, y2, ARGB.color(50, 0));
		}
		this.renderDefaultLabel(context.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE));
		if (this.isHovered()) {
			context.requestCursor(this.isActive() ? CursorTypes.POINTING_HAND : CursorTypes.NOT_ALLOWED);
		}
	}
}
