package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import de.hysky.skyblocker.skyblock.profileviewer2.AbstractProfileViewerScreen;
import de.hysky.skyblocker.utils.render.HudHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

/**
 * This widget's purpose is for debugging and positing widgets. It currently outlines the bounds of the main content area.
 */
// TODO maybe add some cool functionality to draw vertical/horizontal lines that move with scroll wheel when holding down option
// TODO add ruler ticks every few px?
public final class RulerWidget extends ProfileViewerWidget {

	public RulerWidget() {
		super(0, 0, 0, 0, Component.empty());
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float a) {
		int borderWidth = AbstractProfileViewerScreen.BACKGROUND_WIDTH - (CONTENT_PADDING * 2);
		int borderHeight = AbstractProfileViewerScreen.BACKGROUND_HEIGHT - (CONTENT_PADDING * 2);

		// Content Border
		HudHelper.drawBorder(graphics, this.getX(), this.getY(), borderWidth, borderHeight, CommonColors.HIGH_CONTRAST_DIAMOND);
		// Content Origin Point
		graphics.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + 1, CommonColors.RED);
	}
}
