package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

public final class TestTextWidget extends ProfileViewerWidget {

	public TestTextWidget(Component message) {
		// Statements before super cannot come fast enough ;(
		super(18, 18, getFont().width(message), getFont().lineHeight, message);
	}

	@Override
	protected void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		graphics.drawString(getFont(), this.getMessage(), this.getX(), this.getY(), CommonColors.WHITE);
	}
}
